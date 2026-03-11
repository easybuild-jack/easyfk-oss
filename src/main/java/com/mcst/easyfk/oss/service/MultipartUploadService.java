package com.mcst.easyfk.oss.service;

import com.mcst.easyfk.oss.dto.MultipartUploadRequest;
import com.mcst.easyfk.oss.dto.MultipartUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传服务接口
 *
 * @author liu yijun
 */
public interface MultipartUploadService {

    /**
     * 初始化分片上传
     * <p>
     * 1. 检查文件MD5是否已存在（秒传）
     * 2. 如果存在相同uploadId的任务，返回已上传的分片信息（断点续传）
     * 3. 创建新的上传任务
     *
     * @param request 上传请求
     * @return 上传响应（包含uploadId或秒传URL）
     */
    MultipartUploadResponse initUpload(MultipartUploadRequest request);

    /**
     * 查询上传状态
     *
     * @param uploadId 上传ID
     * @return 上传状态信息
     */
    MultipartUploadResponse getUploadStatus(String uploadId);

    /**
     * 上传分片
     *
     * @param uploadId   上传ID
     * @param chunkIndex 分片索引（从1开始）
     * @param file       分片文件
     * @return 是否成功
     */
    boolean uploadChunk(String uploadId, int chunkIndex, MultipartFile file);

    /**
     * 完成上传（合并分片）
     *
     * @param uploadId 上传ID
     * @return 文件URL
     */
    String completeUpload(String uploadId);

    /**
     * 取消上传
     *
     * @param uploadId 上传ID
     */
    void abortUpload(String uploadId);
}
