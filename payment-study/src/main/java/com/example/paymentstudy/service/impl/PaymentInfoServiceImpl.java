package com.example.paymentstudy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.PaymentInfo;
import com.example.paymentstudy.enums.PayType;
import com.example.paymentstudy.mapper.PaymentInfoMapper;
import com.example.paymentstudy.service.PaymentInfoService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
