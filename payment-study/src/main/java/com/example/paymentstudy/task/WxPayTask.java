package com.example.paymentstudy.task;

import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.WxpayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @Description 微信定时任务
 * @Date 2022-07-28-19-53
 * @Author qianzhikang
 */
@Slf4j
@Component
public class WxPayTask {


    @Resource
    private OrderInfoService orderInfoService;


    @Resource
    private WxpayService wxpayService;


    /**
     * 轮询查询订单状态
     * @throws IOException 异常
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws IOException {
        List<OrderInfo> noPayOrderByDuration = orderInfoService.getNoPayOrderByDuration(5);
        for (OrderInfo orderInfo : noPayOrderByDuration) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===》 {}",orderNo);
            //核实订单状态，调用微信支付查单接口
            wxpayService.checkOrderStatus(orderNo);
        }
    }
}
