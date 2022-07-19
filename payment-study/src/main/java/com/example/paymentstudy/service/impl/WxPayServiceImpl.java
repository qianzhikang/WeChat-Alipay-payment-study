package com.example.paymentstudy.service.impl;

import com.example.paymentstudy.config.WxPayConfig;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.enums.wxpay.WxApiType;
import com.example.paymentstudy.enums.wxpay.WxNotifyType;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.WxpayService;
import com.example.paymentstudy.util.OrderNoUtils;
import com.google.gson.Gson;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2022-07-18-20-28
 * @Author qianzhikang
 */
@Service
@Slf4j
public class WxPayServiceImpl implements WxpayService {

    @Resource
    WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;


    @Resource
    OrderInfoService orderInfoService;
    /**
     * 下单
     * @param productId 商品id
     * @return map
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws IOException {

        log.info("生成订单");
        //生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (orderInfo != null && !StringUtils.isEmptyOrWhitespaceOnly(codeUrl)){
            log.info("订单已存在");
            // 返回二维码
            HashMap<String, Object> map = new HashMap<>();
            map.put("codeUrl",codeUrl);
            map.put("orderNo",orderInfo.getOrderNo());
            return map;
        }

        log.info("调用统一下单API");
        //调用统一下单api
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("appid",wxPayConfig.getAppid());
        paramsMap.put("mchid",wxPayConfig.getMchId());
        paramsMap.put("description",orderInfo.getTitle());
        paramsMap.put("out_trade_no",orderInfo.getOrderNo());
        paramsMap.put("notify_url",wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        HashMap amountMap = new HashMap();
        amountMap.put("total",orderInfo.getTotalFee());
        amountMap.put("currency","CNY");
        paramsMap.put("amount",amountMap);
        String jsonParams = gson.toJson(paramsMap);

        log.info("支付参数" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //处理成功
                log.info("success,return body = " + EntityUtils.toString(response.getEntity()));
            } else if (statusCode == 204) {
                //处理成功，无返回Body
                log.info("success");
            } else {
                System.out.println("failed,resp code = " + statusCode+ ",return body = " + EntityUtils.toString(response.getEntity()));
                throw new IOException("request failed");
            }
            HashMap<String,String> resultMap = gson.fromJson(EntityUtils.toString(response.getEntity()), HashMap.class);
            // 解析二维码
            codeUrl = resultMap.get("code_url");

            // 保存二维码
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo,codeUrl);

            // 返回二维码
            HashMap<String, Object> map = new HashMap<>();
            map.put("codeUrl",codeUrl);
            map.put("orderNo",orderInfo.getOrderNo());
            return map;
        } finally {
            response.close();
        }
    }
}
