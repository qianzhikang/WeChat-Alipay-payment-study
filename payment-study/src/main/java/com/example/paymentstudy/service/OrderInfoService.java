package com.example.paymentstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.paymentstudy.entity.OrderInfo;

import java.util.List;

/**
 * @author qianzhikang
 */
public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 根据商品id 创建订单信息
     * @param productId 商品id
     * @return 订单信息对象
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 存储支付二维码链接（二维码两小时内有效）
     * @param orderNo 订单号
     * @param codeUrl 二维码url
     */
    void saveCodeUrl(String orderNo,String codeUrl);

    /**
     * 查询订单列表（按创建时间倒序）
     * @return 订单信息列表
     */
    List<OrderInfo> listOrderByCreateTimeDesc();
}
