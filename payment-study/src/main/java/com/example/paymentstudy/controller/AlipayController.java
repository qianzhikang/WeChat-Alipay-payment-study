package com.example.paymentstudy.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.paymentstudy.config.AlipayClientConfig;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.service.AlipayService;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * @Description TODO
 * @Date 2023-06-01-11-12
 * @Author qianzhikang
 */
@CrossOrigin
@RestController
@RequestMapping("/api/ali-pay")
@Api(tags = "支付宝支付API")
@Slf4j
public class AlipayController {

    @Resource
    private AlipayService alipayService;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AlipayClientConfig alipayClientConfig;

    @ApiOperation("统一下单接口")
    @PostMapping("/trade/page/pay/{productId}")
    public Response tradePagePay(@PathVariable("productId") Long productId) {
        // 支付宝接受请求后，返回html形式的表单，包含自动提交的脚本
        String formStr = alipayService.tradeCreate(productId);
        return Response.success().data("formStr", formStr);
    }

    @ApiOperation("支付结果通知接口")
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> params) {
        String result = "failure";
        try {
            // 调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayClientConfig.getAlipayPublicKey(), AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
            if (!signVerified) {
                log.info("支付宝通知签名验证失败");
                return result;
            }

            // 根据sdk文档进行通知校验
            // 1.校验订单号
            String out_trade_no = params.get("out_trade_no");
            OrderInfo orderInfo = orderInfoService.getOrderByOrdernNo(out_trade_no);
            if (orderInfo == null) {
                log.error("订单不存在！");
                return result;
            }
            // 2.校验订单金额
            String total_amount = params.get("total_amount");
            int totalAmountInt = new BigDecimal(total_amount).multiply(new BigDecimal("100")).intValue();
            int totalFeeInt = orderInfo.getTotalFee().intValue();
            if (totalAmountInt != totalFeeInt) {
                log.error("金额校验失败！");
                return result;
            }
            // 3. 校验商户id
            String seller_id = params.get("seller_id");
            String sellerId = alipayClientConfig.getSellerId();
            if (!seller_id.equals(sellerId)) {
                log.error("商家pid校验失败！");
                return result;
            }
            // 4. 校验appid
            String app_id = params.get("app_id");
            String appId = alipayClientConfig.getAppId();
            if (!app_id.equals(appId)) {
                log.error("appId校验失败！");
                return result;
            }
            // 5. 校验交易状态
            String trade_status = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(trade_status)) {
                log.error("交易状态异常！");
                return result;
            }

            // 处理业务
            alipayService.processOrder(params);

        } catch (AlipayApiException e) {
            throw new RuntimeException("支付验证失败！");
        }
        return result;
    }
}
