package com.mcst.easyfk.oss.controller;

import cn.hutool.core.io.file.FileNameUtil;
import com.mcst.easyfk.core.builders.RRBuilder;
import com.mcst.easyfk.core.constants.CharacterConstant;
import com.mcst.easyfk.core.constants.FileType;
import com.mcst.easyfk.core.dto.response.ResponseResult;
import com.mcst.easyfk.core.utils.common.EmptyUtil;
import com.mcst.easyfk.oss.properties.UploadProperties;
import com.mcst.easyfk.oss.util.FileTypeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


/**
 * @author liu yijun
 * @version 1.0
 */
@RestController
@RequestMapping("/oss/file")
public class OssController {
    @Resource
    private UploadProperties uploadProperties;

    @Resource
    private FileStorageService fileStorageService;//注入实列

    /**
     * 统一上传文件，支持所有类型
     */
    @Operation(description = "统一上传接口")
    @PostMapping("/upload")
    @Parameters({@Parameter(description = "业务名称", name = "bizName"), @Parameter(description = "file", name = "文件信息", required = true)})
    public ResponseResult<String> upload(MultipartHttpServletRequest multipartRequest) {
        // 验证并提取文件
        FileValidationResult validationResult = validateAndExtractFile(multipartRequest, null);
        if (!validationResult.success()) {
            return validationResult.getErrorResponse();
        }
        // 准备上传预处理
        UploadPretreatment pretreatment = prepareUploadPretreatment(
                validationResult.file(),
                validationResult.bizName(),
                validationResult.fileType(),
                validationResult.extName()
        );
        // 应用图片处理（如果适用）
        applyImageProcessing(pretreatment, validationResult.fileType(), validationResult.extName());
        // 执行上传
        return executeUpload(pretreatment);
    }


    /**
     * 图片上传接口
     */
    @Operation(description = "图片上传接口")
    @PostMapping("/upload/image")
    @Parameters({@Parameter(description = "业务名称", name = "bizName"), @Parameter(description = "file", name = "文件信息", required = true)})
    public ResponseResult<String> imageUpload(MultipartHttpServletRequest multipartRequest) {
        // 验证并提取文件（要求必须是图片）
        FileValidationResult validationResult = validateAndExtractFile(multipartRequest, FileType.IMAGE);
        if (!validationResult.success()) {
            return validationResult.getErrorResponse();
        }
        // 准备上传预处理
        UploadPretreatment pretreatment = prepareUploadPretreatment(
                validationResult.file(),
                validationResult.bizName(),
                validationResult.fileType(),
                validationResult.extName()
        );
        // 设置图片内容类型
        pretreatment.setContentType("image/" + validationResult.extName());
        // 应用图片处理
        applyImageProcessing(pretreatment, validationResult.fileType(), validationResult.extName());
        // 执行上传
        return executeUpload(pretreatment);
    }

    /**
     * 视频上传接口
     */
    @Operation(description = "视频上传接口")
    @PostMapping("/upload/video")
    @Parameters({@Parameter(description = "业务名称", name = "bizName"), @Parameter(description = "file", name = "文件信息", required = true)})
    public ResponseResult<String> videoUpload(MultipartHttpServletRequest multipartRequest) {
        // 验证并提取文件（要求必须是视频）
        FileValidationResult validationResult = validateAndExtractFile(multipartRequest, FileType.VIDEO);
        if (!validationResult.success()) {
            return validationResult.getErrorResponse();
        }
        // 准备上传预处理
        UploadPretreatment pretreatment = prepareUploadPretreatment(
                validationResult.file(),
                validationResult.bizName(),
                validationResult.fileType(),
                validationResult.extName()
        );
        // 设置视频内容类型
        pretreatment.setContentType("video/" + validationResult.extName());
        // 执行上传
        return executeUpload(pretreatment);
    }

    /**
     * 验证并提取文件
     *
     * @param multipartRequest 多部分请求
     * @param requiredFileType 要求的文件类型，null表示不限制
     * @return 验证结果
     */
    private FileValidationResult validateAndExtractFile(MultipartHttpServletRequest multipartRequest, FileType requiredFileType) {
        MultipartFile file = getFile(multipartRequest);
        if (EmptyUtil.isEmpty(file)) {
            return FileValidationResult.failed("上传的文件不可为空");
        }
        String filename = file.getOriginalFilename();
        assert filename != null;
        String extName = FileNameUtil.extName(filename);
        FileType fileType = FileTypeUtil.getFileType(extName);
        // 检查文件类型是否符合要求
        if (requiredFileType != null && !fileType.equals(requiredFileType)) {
            return FileValidationResult.failed("上传的文件类型不支持");
        }
        String bizName = multipartRequest.getParameter("bizName");
        return FileValidationResult.success(file, bizName, fileType, extName);
    }

    /**
     * 准备上传预处理对象
     */
    private UploadPretreatment prepareUploadPretreatment(MultipartFile file, String bizName, FileType fileType, String extName) {
        UploadPretreatment pretreatment = fileStorageService.of(file);

        // 设置存储路径
        if (EmptyUtil.isNotEmpty(bizName)) {
            pretreatment.setPath(bizName + CharacterConstant.SLASH + fileType.name() + CharacterConstant.SLASH);
        } else {
            pretreatment.setPath(fileType.name() + CharacterConstant.SLASH);
        }

        return pretreatment;
    }

    /**
     * 应用图片处理（缩略图、压缩）
     */
    private void applyImageProcessing(UploadPretreatment pretreatment, FileType fileType, String extName) {
        if (!fileType.equals(FileType.IMAGE)) {
            return;
        }
        // 应用缩略图
        if (uploadProperties.getThumbnail()) {
            pretreatment.thumbnail(thumbnail ->
                    thumbnail.size(uploadProperties.getThumbnailWidth(), uploadProperties.getThumbnailHeight())
            );
        }
        // 应用图片压缩
        if (uploadProperties.getCompress()) {
            boolean compress = CharacterConstant.STAR.equals(uploadProperties.getCompressImageExtName()) ||
                    uploadProperties.getCompressImageExtName().contains(extName);
            if (compress) {
                pretreatment.image(img -> img.scale(uploadProperties.getScale()))
                        .image(img -> img.outputQuality(uploadProperties.getQuality()));
            }
        }
    }

    /**
     * 执行文件上传
     */
    private ResponseResult<String> executeUpload(UploadPretreatment pretreatment) {
        FileInfo fileInfo = pretreatment.upload();
        if (EmptyUtil.isEmpty(fileInfo)) {
            return RRBuilder.buildFailedBody("上传失败");
        }
        return RRBuilder.buildSuccessBody(fileInfo.getUrl());
    }

    /**
     * 从请求中获取文件
     */
    private MultipartFile getFile(MultipartHttpServletRequest multipartRequest) {
        MultipartFile file = multipartRequest.getFile("file");
        if (EmptyUtil.isEmpty(file)) {
            file = multipartRequest.getFile("files");
        }
        return file;
    }

    /**
     * 文件验证结果封装类
     */
    private record FileValidationResult(boolean success, MultipartFile file, String bizName, FileType fileType,
                                        String extName, String errorMessage) {
        public static FileValidationResult success(MultipartFile file, String bizName,
                                                   FileType fileType, String extName) {
            return new FileValidationResult(true, file, bizName, fileType, extName, null);
        }

        public static FileValidationResult failed(String errorMessage) {
            return new FileValidationResult(false, null, null, null, null, errorMessage);
        }

        public ResponseResult<String> getErrorResponse() {
            return RRBuilder.buildFailedBody(errorMessage);
        }
    }
}
