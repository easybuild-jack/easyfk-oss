package com.mcst.easyfk.oss.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分片上传初始化请求
 *
 * @author liu yijun
 */
@Data
@Schema(description = "分片上传初始化请求")
public class MultipartUploadRequest {

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "video.mp4")
    private String fileName;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED, example = "104857600")
    private Long fileSize;

    @Schema(description = "文件MD5值（用于秒传和校验）", requiredMode = Schema.RequiredMode.REQUIRED, example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileMd5;

    @Schema(description = "分片大小（字节），默认5MB", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "5242880")
    private Integer chunkSize;

    @Schema(description = "业务名称（用于存储路径分类）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "video")
    private String bizName;
}
