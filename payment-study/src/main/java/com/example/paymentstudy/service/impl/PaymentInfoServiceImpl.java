package com.example.paymentstudy.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.PaymentInfo;
import com.example.paymentstudy.enums.PayType;
import com.example.paymentstudy.mapper.PaymentInfoMapper;
import com.example.paymentstudy.service.PaymentInfoService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qianzhikang
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    /**
     * 记录支付日志
     *
     * @param result 微信支付通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentInfo(String result) {
        log.info("记录支付日志");
        Gson gson = new Gson();
        HashMap hashMap = gson.fromJson(result, HashMap.class);
        //订单号
        String orderNo = (String) hashMap.get("out_trade_no");
        //业务编号 对账用
        String transactionId = (String) hashMap.get("transaction_id");
        //交易类型 （扫码等等）
        String tradeType = (String) hashMap.get("trade_type");
        //交易状态
        String tradeState = (String) hashMap.get("trade_state");
        //实际支付金额
        Map<String,Object> amount = (Map<String, Object>) hashMap.get("amount");
        Integer payerTotal = ((Double)amount.get("payer_total")).intValue();

        //创建支付日志对象并赋值
        PaymentInfo paymentInfo =  new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        //支付类型->枚举类型提供
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        //完整的支付结果通知
        paymentInfo.setContent(result);

        //入库
        baseMapper.insert(paymentInfo);

    }

    /**
     * 记录支付日志（支付宝）
     *
     * @param params
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentInfoAlipay(Map<String, String> params) {
        log.info("记录支付日志");
        // 订单号
        String orderNo = params.get("out_trade_no");
        // 业务编号
        String tradeNo = params.get("trade_no");
        // 订单状态
        String tradeStatus = params.get("trade_status");
        // 订单金额
        String total_amount = params.get("total_amount");
        int totalAmountInt = new BigDecimal(total_amount).multiply(new BigDecimal("100")).intValue();

        PaymentInfo paymentInfo =  new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setTransactionId(tradeNo);
        paymentInfo.setTradeType("电脑端支付");
        paymentInfo.setPaymentType(PayType.ALIPAY.getType());
        paymentInfo.setTradeState(tradeStatus);
        paymentInfo.setPayerTotal(totalAmountInt);

        String json = JSONObject.toJSONString(paymentInfo);
        paymentInfo.setContent(json);

        //入库
        baseMapper.insert(paymentInfo);

    }
}
