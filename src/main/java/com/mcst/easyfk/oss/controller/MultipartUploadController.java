package com.mcst.easyfk.oss.controller;

import com.mcst.easyfk.core.builders.RRBuilder;
import com.mcst.easyfk.core.dto.response.ResponseResult;
import com.mcst.easyfk.oss.dto.MultipartUploadRequest;
import com.mcst.easyfk.oss.dto.MultipartUploadResponse;
import com.mcst.easyfk.oss.service.MultipartUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传控制器（支持断点续传）
 *
 * @author liu yijun
 */
@Slf4j
@RestController
@RequestMapping("/oss/file/multipart")
@Tag(name = "分片上传接口", description = "支持大文件分片上传、断点续传、秒传")
public class MultipartUploadController {

    @Resource
    private MultipartUploadService multipartUploadService;

    /**
     * 初始化分片上传
     */
    @Operation(summary = "初始化分片上传", description = "初始化上传任务，返回uploadId和分片信息。支持秒传和断点续传。")
    @PostMapping("/init")
    public ResponseResult<MultipartUploadResponse> initUpload(@RequestBody MultipartUploadRequest request) {
        try {
            MultipartUploadResponse response = multipartUploadService.initUpload(request);
            return RRBuilder.buildSuccessBody(response);
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return RRBuilder.buildFailedBody(e.getMessage());
        }
    }

    /**
     * 查询上传状态
     */
    @Operation(summary = "查询上传状态", description = "查询当前上传任务的状态，返回已上传的分片列表")
    @GetMapping("/status/{uploadId}")
    public ResponseResult<MultipartUploadResponse> getUploadStatus(
            @Parameter(description = "上传ID", required = true)
            @PathVariable String uploadId) {
        try {
            MultipartUploadResponse response = multipartUploadService.getUploadStatus(uploadId);
            return RRBuilder.buildSuccessBody(response);
        } catch (Exception e) {
            log.error("查询上传状态失败，uploadId: {}", uploadId, e);
            return RRBuilder.buildFailedBody(e.getMessage());
        }
    }

    /**
     * 上传分片
     */
    @Operation(summary = "上传分片", description = "上传单个分片，分片索引从1开始")
    @PostMapping("/chunk")
    public ResponseResult<Boolean> uploadChunk(
            @Parameter(description = "上传ID", required = true)
            @RequestParam String uploadId,
            @Parameter(description = "分片索引（从1开始）", required = true)
            @RequestParam Integer chunkIndex,
            @Parameter(description = "分片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            boolean success = multipartUploadService.uploadChunk(uploadId, chunkIndex, file);
            return RRBuilder.buildSuccessBody(success);
        } catch (Exception e) {
            log.error("上传分片失败，uploadId: {}, chunkIndex: {}", uploadId, chunkIndex, e);
            return RRBuilder.buildFailedBody(e.getMessage());
        }
    }

    /**
     * 完成上传（合并分片）
     */
    @Operation(summary = "完成上传", description = "所有分片上传完成后调用，合并分片并返回文件URL")
    @PostMapping("/complete/{uploadId}")
    public ResponseResult<String> completeUpload(
            @Parameter(description = "上传ID", required = true)
            @PathVariable String uploadId) {
        try {
            String fileUrl = multipartUploadService.completeUpload(uploadId);
            return RRBuilder.buildSuccessBody(fileUrl);
        } catch (Exception e) {
            log.error("完成上传失败，uploadId: {}", uploadId, e);
            return RRBuilder.buildFailedBody(e.getMessage());
        }
    }

    /**
     * 取消上传
     */
    @Operation(summary = "取消上传", description = "取消上传任务，清理已上传的分片")
    @DeleteMapping("/abort/{uploadId}")
    public ResponseResult<Void> abortUpload(
            @Parameter(description = "上传ID", required = true)
            @PathVariable String uploadId) {
        try {
            multipartUploadService.abortUpload(uploadId);
            return RRBuilder.buildSuccessBody();
        } catch (Exception e) {
            log.error("取消上传失败，uploadId: {}", uploadId, e);
            return RRBuilder.buildFailedBody(e.getMessage());
        }
    }
}
