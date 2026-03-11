package com.mcst.easyfk.oss.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.mcst.easyfk.cache.api.ICacheService;
import com.mcst.easyfk.core.constants.CharacterConstant;
import com.mcst.easyfk.core.constants.FileType;
import com.mcst.easyfk.core.utils.common.EmptyUtil;
import com.mcst.easyfk.oss.dto.MultipartUploadRequest;
import com.mcst.easyfk.oss.dto.MultipartUploadResponse;
import com.mcst.easyfk.oss.dto.UploadTask;
import com.mcst.easyfk.oss.properties.MultipartUploadProperties;
import com.mcst.easyfk.oss.service.MultipartUploadService;
import com.mcst.easyfk.oss.util.FileTypeUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 分片上传服务实现
 *
 * @author liu yijun
 */
@Slf4j
@Service
public class MultipartUploadServiceImpl implements MultipartUploadService {

    private static final String CACHE_NS = "multipart-upload";
    private static final String TASK_KEY_PREFIX = "task:";
    private static final String PART_KEY_PREFIX = "part:";
    private static final String MD5_KEY_PREFIX = "md5:";

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    @Lazy
    private ICacheService cacheService;

    @Resource
    private MultipartUploadProperties multipartProperties;

    @Override
    public MultipartUploadResponse initUpload(MultipartUploadRequest request) {
        // 检查功能是否启用
        checkEnabled();
        // 参数校验
        validateRequest(request);

        // 1. 检查秒传
        if (Boolean.TRUE.equals(multipartProperties.getInstantUploadEnabled()) && EmptyUtil.isNotEmpty(request.getFileMd5())) {
            String existingUrl = cacheService.queryByKey(MD5_KEY_PREFIX + request.getFileMd5(), CACHE_NS);
            if (EmptyUtil.isNotEmpty(existingUrl)) {
                log.debug("秒传成功，文件MD5: {}, URL: {}", request.getFileMd5(), existingUrl);
                return MultipartUploadResponse.instantSuccess(existingUrl);
            }
        }

        // 2. 计算分片信息
        int chunkSize = request.getChunkSize() != null ? request.getChunkSize() : multipartProperties.getChunkSize();
        int totalChunks = (int) Math.ceil((double) request.getFileSize() / chunkSize);

        // 3. 生成存储路径
        String extName = FileNameUtil.extName(request.getFileName());
        FileType fileType = FileTypeUtil.getFileType(extName);
        String path = buildStoragePath(request.getBizName(), fileType);
        String saveFileName = EmptyUtil.isNotEmpty(extName)
                ? IdUtil.fastSimpleUUID() + CharacterConstant.DOT + extName
                : IdUtil.fastSimpleUUID();

        // 4. 调用 x-file-storage 初始化分片上传
        FileInfo fileInfo = fileStorageService.initiateMultipartUpload().setPath(path).setSaveFilename(saveFileName).setOriginalFilename(request.getFileName()).setSize(request.getFileSize()).init();

        if (fileInfo == null || EmptyUtil.isEmpty(fileInfo.getUploadId())) {
            throw new RuntimeException("初始化分片上传失败");
        }

        String uploadId = fileInfo.getUploadId();

        // 5. 创建上传任务并缓存
        UploadTask task = new UploadTask();
        task.setUploadId(uploadId);
        task.setFileName(request.getFileName());
        task.setFileMd5(request.getFileMd5());
        task.setFileSize(request.getFileSize());
        task.setChunkSize(chunkSize);
        task.setTotalChunks(totalChunks);
        task.setPlatform(fileInfo.getPlatform());
        task.setPath(path);
        task.setSaveFileName(saveFileName);
        task.setBizName(request.getBizName());
        task.setCreateTime(System.currentTimeMillis());

        Duration taskTtl = Duration.ofHours(multipartProperties.getTaskExpireHours());
        cacheService.cacheObject(TASK_KEY_PREFIX + uploadId, task, taskTtl, CACHE_NS);

        log.debug("分片上传初始化成功，uploadId: {}, 文件名: {}, 总分片数: {}", uploadId, request.getFileName(), totalChunks);

        return MultipartUploadResponse.initSuccess(uploadId, chunkSize, totalChunks, new ArrayList<>());
    }

    @Override
    public MultipartUploadResponse getUploadStatus(String uploadId) {
        UploadTask task = getTask(uploadId);
        if (task == null) {
            throw new RuntimeException("上传任务不存在或已过期");
        }

        List<Integer> uploadedChunks = getUploadedChunkList(uploadId, task.getTotalChunks());

        return MultipartUploadResponse.initSuccess(uploadId, task.getChunkSize(), task.getTotalChunks(), uploadedChunks);
    }

    @Override
    public boolean uploadChunk(String uploadId, int chunkIndex, MultipartFile file) {
        // 1. 获取上传任务
        UploadTask task = getTask(uploadId);
        if (task == null) {
            throw new RuntimeException("上传任务不存在或已过期");
        }

        // 2. 校验分片索引
        if (chunkIndex < 1 || chunkIndex > task.getTotalChunks()) {
            throw new RuntimeException("分片索引无效，有效范围: 1-" + task.getTotalChunks());
        }

        // 3. 检查是否已上传（使用单独的 key 存储每个分片状态，保证并发安全）
        String partKey = buildPartKey(uploadId, chunkIndex);
        if (cacheService.existKey(partKey, CACHE_NS)) {
            log.debug("分片已上传，跳过，uploadId: {}, chunkIndex: {}", uploadId, chunkIndex);
            return true;
        }

        // 4. 构建 FileInfo 用于上传分片
        FileInfo fileInfo = buildFileInfoForUpload(task);

        try {
            // 5. 调用 x-file-storage 上传分片
            fileStorageService.uploadPart(fileInfo, chunkIndex, file.getInputStream(), file.getSize());

            // 6. 记录已上传分片（原子操作，每个分片一个 key）
            Duration taskTtl = Duration.ofHours(multipartProperties.getTaskExpireHours());
            cacheService.cacheObject(partKey, true, taskTtl, CACHE_NS);

            log.debug("分片上传成功，uploadId: {}, chunkIndex: {}/{}", uploadId, chunkIndex, task.getTotalChunks());
            return true;
        } catch (Exception e) {
            log.error("分片上传失败，uploadId: {}, chunkIndex: {}", uploadId, chunkIndex, e);
            throw new RuntimeException("分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String completeUpload(String uploadId) {
        // 1. 获取上传任务
        UploadTask task = getTask(uploadId);
        if (task == null) {
            throw new RuntimeException("上传任务不存在或已过期");
        }

        // 2. 检查所有分片是否已上传
        List<Integer> missingParts = getMissingChunks(uploadId, task.getTotalChunks());
        if (!missingParts.isEmpty()) {
            throw new RuntimeException("分片未完全上传，缺少分片: " + missingParts);
        }

        // 3. 构建 FileInfo 用于完成上传
        FileInfo fileInfo = buildFileInfoForUpload(task);

        try {
            // 4. 调用 x-file-storage 完成上传
            fileInfo = fileStorageService.completeMultipartUpload(fileInfo).complete();

            if (fileInfo == null || EmptyUtil.isEmpty(fileInfo.getUrl())) {
                throw new RuntimeException("合并分片失败");
            }

            String fileUrl = fileInfo.getUrl();

            // 5. 记录秒传缓存（ICacheService 需要显式指定过期时间，与 Spring Cache 无关）
            if (Boolean.TRUE.equals(multipartProperties.getInstantUploadEnabled()) && EmptyUtil.isNotEmpty(task.getFileMd5())) {
                int cacheDays = multipartProperties.getInstantUploadCacheDays();
                Duration md5CacheTtl = cacheDays > 0 ? Duration.ofDays(cacheDays) : Duration.ofDays(365);
                cacheService.cacheObject(MD5_KEY_PREFIX + task.getFileMd5(), fileUrl, md5CacheTtl, CACHE_NS);
            }

            // 6. 清理上传任务缓存
            cleanupTask(uploadId);

            log.debug("分片上传完成，uploadId: {}, 文件URL: {}", uploadId, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("完成上传失败，uploadId: {}", uploadId, e);
            throw new RuntimeException("完成上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void abortUpload(String uploadId) {
        // 1. 获取上传任务
        UploadTask task = getTask(uploadId);
        if (task == null) {
            log.warn("上传任务不存在，可能已过期，uploadId: {}", uploadId);
            return;
        }

        // 2. 构建 FileInfo 用于取消上传
        FileInfo fileInfo = buildFileInfoForUpload(task);

        try {
            // 3. 调用 x-file-storage 取消上传
            fileStorageService.abortMultipartUpload(fileInfo).abort();
        } catch (Exception e) {
            log.warn("调用存储平台取消上传失败，uploadId: {}", uploadId, e);
        }

        // 4. 清理缓存
        cleanupTask(uploadId);

        log.debug("上传任务已取消，uploadId: {}", uploadId);
    }

    /**
     * 检查分片上传功能是否启用
     */
    private void checkEnabled() {
        if (!Boolean.TRUE.equals(multipartProperties.getEnabled())) {
            throw new RuntimeException("分片上传功能未启用");
        }
    }

    /**
     * 参数校验
     */
    private void validateRequest(MultipartUploadRequest request) {
        if (EmptyUtil.isEmpty(request.getFileName())) {
            throw new RuntimeException("文件名不能为空");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new RuntimeException("文件大小无效");
        }
        if (request.getFileSize() > multipartProperties.getMaxFileSize()) {
            throw new RuntimeException("文件大小超过限制，最大允许: " + multipartProperties.getMaxFileSize() / 1024 / 1024 + "MB");
        }
    }

    /**
     * 构建存储路径
     */
    private String buildStoragePath(String bizName, FileType fileType) {
        if (EmptyUtil.isNotEmpty(bizName)) {
            return bizName + CharacterConstant.SLASH + fileType.name() + CharacterConstant.SLASH;
        }
        return fileType.name() + CharacterConstant.SLASH;
    }

    /**
     * 获取上传任务
     */
    private UploadTask getTask(String uploadId) {
        return cacheService.queryByKey(TASK_KEY_PREFIX + uploadId, CACHE_NS);
    }

    /**
     * 构建分片 key
     */
    private String buildPartKey(String uploadId, int chunkIndex) {
        return PART_KEY_PREFIX + uploadId + ":" + chunkIndex;
    }

    /**
     * 获取已上传的分片列表
     */
    private List<Integer> getUploadedChunkList(String uploadId, int totalChunks) {
        List<Integer> uploadedChunks = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            if (cacheService.existKey(buildPartKey(uploadId, i), CACHE_NS)) {
                uploadedChunks.add(i);
            }
        }
        return uploadedChunks;
    }

    /**
     * 获取缺失的分片列表
     */
    private List<Integer> getMissingChunks(String uploadId, int totalChunks) {
        List<Integer> missingChunks = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            if (!cacheService.existKey(buildPartKey(uploadId, i), CACHE_NS)) {
                missingChunks.add(i);
            }
        }
        return missingChunks;
    }

    /**
     * 构建用于上传操作的 FileInfo
     */
    private FileInfo buildFileInfoForUpload(UploadTask task) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUploadId(task.getUploadId());
        fileInfo.setPlatform(task.getPlatform());
        fileInfo.setPath(task.getPath());
        fileInfo.setFilename(task.getSaveFileName());
        fileInfo.setOriginalFilename(task.getFileName());
        fileInfo.setSize(task.getFileSize());
        return fileInfo;
    }

    /**
     * 清理上传任务缓存
     */
    private void cleanupTask(String uploadId) {
        UploadTask task = getTask(uploadId);
        // 清理任务信息
        cacheService.removeKey(TASK_KEY_PREFIX + uploadId, CACHE_NS);
        // 清理所有分片记录
        if (task != null) {
            for (int i = 1; i <= task.getTotalChunks(); i++) {
                cacheService.removeKey(buildPartKey(uploadId, i), CACHE_NS);
            }
        }
    }
}
