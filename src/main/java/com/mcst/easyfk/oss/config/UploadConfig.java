package com.mcst.easyfk.oss.config;

import cn.hutool.core.io.FileUtil;
import com.mcst.easyfk.oss.properties.UploadProperties;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liu yijun
 */
@Configuration
@EnableConfigurationProperties({UploadProperties.class})
public class UploadConfig {

    /**
     * 文件上传临时路径
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String location = System.getProperty("user.dir") + "/data/tmp";
        FileUtil.mkdir(location);
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }
}
