package com.hk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangkun
 * @date 2025/6/1 9:36
 */
@Data
@Configuration
@ConfigurationProperties("pexels")
public class PexelsConfig {

    private String url;

    private String apiKey;
}
