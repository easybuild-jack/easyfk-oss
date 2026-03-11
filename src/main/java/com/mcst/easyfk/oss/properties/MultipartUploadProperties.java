package com.mcst.easyfk.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分片上传配置属性
 *
 * @author liu yijun
 */
@ConfigurationProperties(prefix = "easyfk.config.oss.multipart")
@Data
public class MultipartUploadProperties {

    /**
     * 是否启用分片上传
     */
    private Boolean enabled = Boolean.FALSE;

    /**
     * 默认分片大小（字节），默认5MB
     */
    private Integer chunkSize = 5 * 1024 * 1024;

    /**
     * 最大文件大小（字节），默认10GB
     */
    private Long maxFileSize = 10L * 1024 * 1024 * 1024;

    /**
     * 上传任务过期时间（小时），默认24小时
     */
    private Integer taskExpireHours = 24;

    /**
     * 是否启用秒传功能
     */
    private Boolean instantUploadEnabled = Boolean.TRUE;

    /**
     * 秒传记录缓存过期时间（天），默认30天，0表示永不过期
     */
    private Integer instantUploadCacheDays = 30;
}
