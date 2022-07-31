package com.example.paymentstudy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.paymentstudy.entity.RefundInfo;

/**
 * @author qianzhikang
 */
public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 根据订单号创建退款单
     * @param orderNo 订单号
     * @param reason 退款理由
     * @return 退款单信息
     */
    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    /**
     * 更新退款信息
     * @param content 退款返回
     */
    void updateRefund(String content);
}
