package com.example.paymentstudy.controller;

import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 订单信息控制器
 * @Date 2022-07-19-14-09
 * @Author qianzhikang
 */
@Api(tags = "订单查询接口")
@CrossOrigin
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 查询订单列表
     * @return 统一返回结果
     */
    @GetMapping("/list")
    public Response list(){
        List<OrderInfo> orderInfoList = orderInfoService.listOrderByCreateTimeDesc();
        return Response.success().data("list",orderInfoList);
    }

    /**
     * 查询订单状态接口
     * @param orderNo 订单号
     * @return 统一返回结果
     */
    @GetMapping("/query-order-status/{orderNo}")
    public Response queryOrderStatus(@PathVariable String orderNo){
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            //支付成功
            return Response.success().setMessage("支付成功");
        }
        //支付中状态码101
        return Response.success().setCode(101).setMessage("支付中...");
    }
}
