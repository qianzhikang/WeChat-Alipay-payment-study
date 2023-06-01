package com.example.paymentstudy.service;

import com.wechat.pay.contrib.apache.httpclient.notification.Notification;

import java.util.Map;

/**
 * @author qianzhikang
 */
public interface PaymentInfoService {

    /**
     * 记录支付日志
     * @param result 微信支付通知
     */
    void createPaymentInfo(String result);

    /**
     * 记录支付日志（支付宝）
     * @param params
     */
    void createPaymentInfoAlipay(Map<String, String> params);
}
