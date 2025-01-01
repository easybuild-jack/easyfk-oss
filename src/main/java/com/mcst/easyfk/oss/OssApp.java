package com.mcst.easyfk.oss;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liu yijun
 * @version 1.0
 */
@EnableFileStorage
@SpringBootApplication
public class OssApp {
    public static void main(String[] args) {
        SpringApplication.run(OssApp.class, args);
    }
}
