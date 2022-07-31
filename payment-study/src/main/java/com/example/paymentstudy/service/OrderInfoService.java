package com.example.paymentstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.enums.OrderStatus;

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

    /**
     * 根据订单号修改支付状态
     * @param orderNo 订单号
     * @param orderStatus 订单状态枚举
     */
    void updateStatusByOrderNo(Object orderNo, OrderStatus orderStatus);

    /**
     * 获取订单状态
     * @param orderNo 订单号
     * @return 订单状态
     */
    String getOrderStatus(String orderNo);

    /**
     * 查询超时未支付的订单
     * @param minutes 时间/分钟
     * @return 符合条件的订单列表
     */
    List<OrderInfo> getNoPayOrderByDuration(int minutes);

    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    OrderInfo getOrderByOrdernNo(String orderNo);
}
