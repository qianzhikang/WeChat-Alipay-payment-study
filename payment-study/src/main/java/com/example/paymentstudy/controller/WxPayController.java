package com.example.paymentstudy.controller;

import com.example.paymentstudy.service.WxpayService;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2022-07-18-20-25
 * @Author qianzhikang
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付API")
@Slf4j
public class WxPayController {

    @Resource
    WxpayService wxpayService;

    @ApiOperation("调用统一下单API生产支付二维码")
    @PostMapping("/native/{productId}")
    public Response nativePay(@PathVariable Long productId) throws IOException {
        log.info("发起支付请求");
        Map<String, Object> map = wxpayService.nativePay(productId);
        return Response.success().setData(map);
    }

}
