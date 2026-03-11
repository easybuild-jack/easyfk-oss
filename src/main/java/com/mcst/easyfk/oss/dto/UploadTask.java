package com.mcst.easyfk.oss.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分片上传任务实体
 *
 * @author liu yijun
 */
@Data
public class UploadTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上传ID（由存储平台生成）
     */
    private String uploadId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件MD5（用于秒传和校验）
     */
    private String fileMd5;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 分片大小（字节）
     */
    private Integer chunkSize;

    /**
     * 总分片数
     */
    private Integer totalChunks;

    /**
     * 存储平台标识
     */
    private String platform;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 保存的文件名
     */
    private String saveFileName;

    /**
     * 业务名称
     */
    private String bizName;

    /**
     * 创建时间戳
     */
    private Long createTime;
}
