package com.hk.aiagent.cos.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangkun
 * @date 2024/12/15 20:13
 */
@Configuration
@ConfigurationProperties(prefix = "tenxun.cos.config")
@Data
public class CosConfig {
    /**
     * 域名
     */
    private String host;
    /**
     * secretId
     */
    private String secretId;
    /**
     * secretKey
     */

    private String secretKey;
    /**
     * 地域
     */

    private String region;
    /**
     * 桶名
     */
    private String bucket;

    /**
     * 超时时间
     */
    private Integer timeOut;

    private final static int INTERVAL = 1000;

    @Bean
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        if (timeOut != null) {
            clientConfig.setConnectionTimeout(timeOut * INTERVAL);
            clientConfig.setSocketTimeout(timeOut * INTERVAL);
            clientConfig.setRequestTimeout(timeOut * INTERVAL);
        }
        //添加代理
        //        clientConfig.setHttpProxyIp("127.0.0.1");
        //        clientConfig.setHttpProxyPort(1087);

        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;

    }


}
