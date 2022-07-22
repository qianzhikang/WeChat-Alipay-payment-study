package com.example.paymentstudy.controller;

import com.example.paymentstudy.config.WxPayConfig;
import com.example.paymentstudy.service.WxpayService;
import com.example.paymentstudy.util.HttpUtils;
import com.example.paymentstudy.vo.Response;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.wechat.pay.contrib.apache.httpclient.notification.Request;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 微信支付接口
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
    private WxpayService wxpayService;

    @Resource
    private Verifier verifier;

    @Resource
    private WxPayConfig wxPayConfig;

    @ApiOperation("调用统一下单API生产支付二维码")
    @PostMapping("/native/{productId}")
    public Response nativePay(@PathVariable Long productId) throws IOException {
        log.info("发起支付请求");
        Map<String, Object> map = wxpayService.nativePay(productId);
        return Response.success().setData(map);
    }

    /**
     * 支付通知接口，用于微信支付结果的通知
     *
     * @param request  请求
     * @param response 响应
     * @return 结果
     */
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {

        // json工具
        Gson gson = new Gson();

        // 创建应答对象
        Map<String, String> resMap = new HashMap<>();

        try {
            // 使用自定义工具解析请求体
            String body = HttpUtils.readData(request);

            // 使用json反序列化
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("支付通知的id ===》 {}", bodyMap.get("id"));
            log.info("支付通知完整数据 ===》 {}", body);

            // 构造等待验证的请求
            // 获取请求头中微信平台证书序列号
            String wechatPaySerial = request.getHeader("Wechatpay-Serial");
            // 获取请求头中的随机串
            String nonce = request.getHeader("Wechatpay-Nonce");
            // 获取请求头中的时间戳
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            // 获取请求头中的签名串
            String signature = request.getHeader("Wechatpay-Signature");
            NotificationRequest notificationRequest = new NotificationRequest.Builder().withSerialNumber(wechatPaySerial)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();
            // 验证与解密
            NotificationHandler handler = new NotificationHandler(verifier,wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            Notification notification = handler.parse((notificationRequest));
            log.info(notification.getDecryptData());


            // 订单处理
            wxpayService.processOrder(notification);

            // 通知微信成功应答
            // 200 或 204 代表成功，204不携带信息
            response.setStatus(200);

            // 应答对象配置
            resMap.put("code", "SUCCESS");
            resMap.put("message", "成功");

            // 返回应答对象
            return gson.toJson(resMap);
        } catch (Exception e) {
            // 处理异常情况
            log.error(e.getMessage());
            response.setStatus(500);
            resMap.put("code", "ERROR");
            resMap.put("message", "失败");
            return gson.toJson(resMap);
        }
    }

}
