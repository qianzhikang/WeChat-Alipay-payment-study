package com.example.paymentstudy.controller;

import com.example.paymentstudy.config.WxPayConfig;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description 测试控制器
 * @Date 2022-07-18-15-08
 * @Author qianzhikang
 */
@Api(tags = "测试控制器")
@RestController
@RequestMapping("/api/test")
public class TestController {
    @Resource
    private WxPayConfig wxPayConfig;

    @GetMapping("/id")
    public Response getWxPayConfig() {
        String mchId = wxPayConfig.getMchId();
        return Response.success().data("mchId", mchId);
    }
}
