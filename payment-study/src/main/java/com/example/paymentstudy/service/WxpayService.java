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

    /**
     * 查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    String queryOrder(String orderNo) throws IOException;

    /**
     * 根据订单号查询微信支付接口，核实订单状态
     * 若支付完成，则更新商户端订单状态
     * 若支付未完成，则调用关单接口，并更新状态
     * @param orderNo
     */
    void checkOrderStatus(String orderNo) throws IOException;
}
