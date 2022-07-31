package com.example.paymentstudy.service.impl;

import com.example.paymentstudy.config.WxPayConfig;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.enums.wxpay.WxApiType;
import com.example.paymentstudy.enums.wxpay.WxNotifyType;
import com.example.paymentstudy.enums.wxpay.WxTradeState;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.PaymentInfoService;
import com.example.paymentstudy.service.WxpayService;
import com.example.paymentstudy.util.OrderNoUtils;
import com.example.paymentstudy.vo.Response;
import com.google.gson.Gson;
import com.mysql.cj.util.StringUtils;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description 微信支付控制器
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
    private OrderInfoService orderInfoService;


    @Resource
    private PaymentInfoService paymentInfoService;


    /**
     * 添加数据锁
     */
    private final ReentrantLock lock = new ReentrantLock();


    /**
     * 下单
     *
     * @param productId 商品id
     * @return map
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws IOException {

        log.info("生成订单");
        //生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (orderInfo != null && !StringUtils.isEmptyOrWhitespaceOnly(codeUrl)) {
            log.info("订单已存在");
            // 返回二维码
            HashMap<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }

        log.info("调用统一下单API");
        //调用统一下单api
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        HashMap amountMap = new HashMap();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        String jsonParams = gson.toJson(paramsMap);

        log.info("支付参数" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
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
                System.out.println("failed,resp code = " + statusCode + ",return body = " + EntityUtils.toString(response.getEntity()));
                throw new IOException("request failed");
            }
            HashMap<String, String> resultMap = gson.fromJson(EntityUtils.toString(response.getEntity()), HashMap.class);
            // 解析二维码
            codeUrl = resultMap.get("code_url");

            // 保存二维码
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            // 返回二维码
            HashMap<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        } finally {
            response.close();
        }
    }

    /**
     * 支付完成后订单的处理
     *
     * @param notification 微信支付结果通知
     */
    @Override
    public void processOrder(Notification notification) {
        //从notification中获取解密报文
        String decryptData = notification.getDecryptData();

        //解析json数据
        Gson gson = new Gson();
        HashMap hashMap = gson.fromJson(decryptData, HashMap.class);
        String orderNo = (String) hashMap.get("out_trade_no");

        // 数据锁，处理由于网络原因，通知访问接口多次请求造成数据混乱
        // 使用数据锁对数据进行并发控制
        if (lock.tryLock()) {
            try {
                //处理微信重复通知
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }

                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                //记录支付日志
                paymentInfoService.createPaymentInfo(decryptData);
            } finally {
                // 需要主动释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     */
    @Override
    public void cancelOrder(String orderNo) throws IOException {
        //调用微信支付关单接口
        this.closeOrder(orderNo);
        //更新订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }

    /**
     * 查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Override
    public String queryOrder(String orderNo) throws IOException {
        log.info("查单接口调用 ====》 {}", orderNo);
        //拼接url
        String format = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        String url = wxPayConfig.getDomain().concat(format);

        //创建http post请求
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功，返回结果：" + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                log.info("失败，响应码" + statusCode + "，返回结果：" + bodyAsString);
                throw new IOException("请求失败");
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 根据订单号查询微信支付接口，核实订单状态
     * 若支付完成，则更新商户端订单状态
     * 若支付未完成，则调用关单接口，并更新状态
     *
     * @param orderNo 订单号
     */
    @Override
    public void checkOrderStatus(String orderNo) throws IOException {
        log.warn("更具订单号核实订单状态 ====》 {}",orderNo);

        //查询订单状态
        String result = this.queryOrder(orderNo);
        Gson gson = new Gson();
        HashMap resultMap = gson.fromJson(result, HashMap.class);
        //获取微信端订单状态
        Object tradeState = resultMap.get("trade_state");

        //判断订单状态
        if (WxTradeState.SUCCESS.getType().equals(tradeState)) {
            log.warn("核实订单已支付 ===》 {}",orderNo );
            //确认已支付，修改订单状态
            orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(result);
        }
    }

    /**
     * 微信关单接口的调用
     *
     * @param orderNo 订单号
     */
    private void closeOrder(String orderNo) throws IOException {
        log.info("关单接口调用，订单号=====》{}", orderNo);

        //创建远程请求对象
        //替换枚举中使用 %s 的占位符，format为替换后
        String format = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        //拼接url
        String url = wxPayConfig.getDomain().concat(format);
        HttpPost httpPost = new HttpPost(url);
        log.info("请求url === > {}", url);


        //组装json请求体
        Gson gson = new Gson();
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===》 {}", jsonParams);

        //设置post请求对象
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        //解析请求返回值
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功");
            } else if (statusCode == 204) {
                log.info("成功，没有返回body");
            } else {
                log.info("关单失败，响应码：" + statusCode);
                throw new IOException("request failed");
            }
        } finally {
            response.close();
        }
    }

}
