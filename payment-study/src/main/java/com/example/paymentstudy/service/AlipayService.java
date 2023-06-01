package com.example.paymentstudy.service;

import java.util.Map;

/**
 * @Description TODO
 * @Date 2023-06-01-11-16
 * @Author qianzhikang
 */
public interface AlipayService {
    String tradeCreate(Long productId);


    void processOrder(Map<String, String> params);
}
