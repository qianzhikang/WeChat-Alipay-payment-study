package com.example.paymentstudy.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.entity.RefundInfo;
import com.example.paymentstudy.mapper.RefundInfoMapper;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.RefundInfoService;
import com.example.paymentstudy.util.OrderNoUtils;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author qianzhikang
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 根据订单号创建退款单
     *
     * @param orderNo 订单号
     * @param reason  退款理由
     * @return 退款单信息
     */
    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {

        //查询订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrdernNo(orderNo);

        //创建退款单信息
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderNo(orderNo);
        //工具类生成退款单编号
        refundInfo.setRefundNo(OrderNoUtils.getRefundNo());
        //设置支付金额
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        //设置退款金额，支持部分退款
        refundInfo.setRefund(orderInfo.getTotalFee());
        //设置退款理由
        refundInfo.setReason(reason);
        //插入退款表
        baseMapper.insert(refundInfo);

        return refundInfo;
    }

    /**
     * 更新退款信息
     *
     * @param content 退款返回
     */
    @Override
    public void updateRefund(String content) {
        Gson gson = new Gson();
        HashMap<String, String> resultMap = gson.fromJson(content, HashMap.class);

        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refund_no", resultMap.get("out_refund_no"));

        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(resultMap.get("refund_id"));


        //查询退款和申请退款时，更新的状态
        if (resultMap.get("status") != null) {
            refundInfo.setRefundStatus(resultMap.get("status"));
            refundInfo.setContentReturn(content);
        }

        //微信端通知回掉中更新的退款通知
        if (resultMap.get("refund_status") != null) {
            refundInfo.setRefundStatus(resultMap.get("refund_status"));
            refundInfo.setContentNotify(content);
        }

        baseMapper.update(refundInfo,queryWrapper);

    }
}
