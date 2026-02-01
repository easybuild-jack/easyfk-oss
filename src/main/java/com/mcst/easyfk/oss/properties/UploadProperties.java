package com.mcst.easyfk.oss.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liuyijun
 */
@ConfigurationProperties(prefix = "easyfk.config.oss.upload")
@Data
public class UploadProperties {

    /**
     * 是否开启图片压缩
     */
    private Boolean compress = Boolean.FALSE;

    /**
     * 图片压缩的后缀名，多个用英文逗号隔开，* 表示全部图片格式
     */
    private String compressImageExtName = "*";

    /**
     * 图片的压缩比例 值在0-1之间，1f就是原图，0.5就是原图的一半大小
     */
    private Float scale = 1f;

    /**
     * 图片压缩的质量 值在0-1 之间，越接近1质量越好，越接近0 质量越差
     */
    private Float quality = 0.5f;

    /**
     * 是否重写文件名
     */
    private Boolean overwriteFileName = Boolean.FALSE;


    /**
     * 是否开启图片缩略图
     */
    private Boolean thumbnail = Boolean.FALSE;

    /**
     * 缩略图的宽度
     */
    private int thumbnailWidth = 200;
    /**
     * 缩略图的高度
     */
    private int thumbnailHeight = 200;


}
