package com.example.paymentstudy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePageMergePayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.example.paymentstudy.config.AlipayClientConfig;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.service.AlipayService;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.PaymentInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description 支付宝支付相关业务
 * @Date 2023-06-01-11-16
 * @Author qianzhikang
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipayClientConfig alipayClientConfig;

    @Resource
    private PaymentInfoService paymentInfoService;

    /**
     * 添加数据锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String tradeCreate(Long productId) {
        try {
            // 生成订单
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayClientConfig.getNotifyUrl());
            request.setReturnUrl(alipayClientConfig.getReturnUrl());
            // 组装请求参数
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderInfo.getOrderNo());
            BigDecimal total = new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100"));
            bizContent.put("total_amount", total);
            bizContent.put("subject", orderInfo.getTitle());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toJSONString());
            // 请求支付宝下单接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                System.out.println("success");
            } else {
                System.out.println("error");
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("创建支付交易失败！");
        }
    }

    @Override
    public void processOrder(Map<String, String> params) {
        // 获取订单号
        String orderNo = params.get("out_trade_no");
        if (lock.tryLock()) {
            try {
                // 处理重复通知的情况
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!orderStatus.equals(OrderStatus.NOTPAY.getType())) {
                    return;
                }
                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                // 记录支付宝支付日志
                paymentInfoService.createPaymentInfoAlipay(params);
            } finally {
                lock.unlock();
            }
        }
    }
}
