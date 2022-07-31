package com.example.paymentstudy.service;

import com.wechat.pay.contrib.apache.httpclient.notification.Notification;

/**
 * @author qianzhikang
 */
public interface PaymentInfoService {

    /**
     * 记录支付日志
     * @param result 微信支付通知
     */
    void createPaymentInfo(String result);
}
