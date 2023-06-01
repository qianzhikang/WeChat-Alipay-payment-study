package com.example.paymentstudy.config;

import com.alipay.api.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Description 支付宝支付配置
 * @Date 2023-06-01-10-27
 * @Author qianzhikang
 */
@Configuration
@PropertySource("classpath:alipay-sandbox.properties")
@ConfigurationProperties(prefix = "alipay") //读取alipay节点
@Data
@Slf4j
public class AlipayClientConfig {
    private String appId;
    private String sellerId;
    private String gatewayUrl;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String contentKey;
    private String returnUrl;
    private String notifyUrl;


    /**
     * alipay客户端
     * @return
     * @throws AlipayApiException
     */
    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setAppId(this.appId);
        alipayConfig.setServerUrl(this.gatewayUrl);
        alipayConfig.setAlipayPublicKey(this.alipayPublicKey);
        alipayConfig.setPrivateKey(this.merchantPrivateKey);
        alipayConfig.setAppCertContent(this.contentKey);
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        return new DefaultAlipayClient(alipayConfig);
    }
}
