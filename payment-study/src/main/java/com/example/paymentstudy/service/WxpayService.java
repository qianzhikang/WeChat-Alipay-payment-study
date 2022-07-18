package com.example.paymentstudy.service;

import java.io.IOException;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2022-07-18-20-27
 * @Author qianzhikang
 */
public interface WxpayService {
    /**
     * 下单
     * @param productId 商品id
     * @return map
     */
    Map<String,Object> nativePay(Long productId) throws IOException;
}
