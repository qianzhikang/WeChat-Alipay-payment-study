package com.example.paymentstudy.service;

import com.wechat.pay.contrib.apache.httpclient.notification.Notification;

import java.io.IOException;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2022-07-18-20-27
 * @Author qianzhikang
 */
public interface WxpayService {

    /**
     *下单
     * @param productId 商品id
     * @return map
     */
    Map<String,Object> nativePay(Long productId) throws IOException;

    /**
     * 支付完成后订单的处理
     * @param notification 微信支付结果通知
     */
    void processOrder(Notification notification);

    /**
     * 取消订单
     * @param orderNo 订单号
     */
    void cancelOrder(String orderNo) throws IOException;
}
