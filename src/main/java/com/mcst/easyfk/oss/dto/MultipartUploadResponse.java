package com.mcst.easyfk.oss.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分片上传响应
 *
 * @author liu yijun
 */
@Data
@Schema(description = "分片上传响应")
public class MultipartUploadResponse {

    @Schema(description = "上传ID（后续分片上传需要携带）")
    private String uploadId;

    @Schema(description = "分片大小（字节）")
    private Integer chunkSize;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "已上传的分片编号列表（用于断点续传）")
    private List<Integer> uploadedChunks;

    @Schema(description = "是否秒传成功")
    private Boolean instantUpload;

    @Schema(description = "文件URL（秒传成功或上传完成时返回）")
    private String fileUrl;

    /**
     * 创建秒传成功响应
     */
    public static MultipartUploadResponse instantSuccess(String fileUrl) {
        MultipartUploadResponse response = new MultipartUploadResponse();
        response.setInstantUpload(true);
        response.setFileUrl(fileUrl);
        return response;
    }

    /**
     * 创建初始化成功响应
     */
    public static MultipartUploadResponse initSuccess(String uploadId, Integer chunkSize,
                                                       Integer totalChunks, List<Integer> uploadedChunks) {
        MultipartUploadResponse response = new MultipartUploadResponse();
        response.setUploadId(uploadId);
        response.setChunkSize(chunkSize);
        response.setTotalChunks(totalChunks);
        response.setUploadedChunks(uploadedChunks);
        response.setInstantUpload(false);
        return response;
    }

    /**
     * 创建上传完成响应
     */
    public static MultipartUploadResponse completeSuccess(String fileUrl) {
        MultipartUploadResponse response = new MultipartUploadResponse();
        response.setInstantUpload(false);
        response.setFileUrl(fileUrl);
        return response;
    }
}
