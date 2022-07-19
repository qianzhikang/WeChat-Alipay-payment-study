package com.example.paymentstudy.controller;

import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/list")
    public Response list(){
        List<OrderInfo> orderInfoList = orderInfoService.listOrderByCreateTimeDesc();
        return Response.success().data("list",orderInfoList);
    }
}
