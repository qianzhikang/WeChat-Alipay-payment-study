package com.example.paymentstudy.service.impl;

import com.example.paymentstudy.config.WxPayConfig;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.entity.RefundInfo;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.enums.wxpay.WxApiType;
import com.example.paymentstudy.enums.wxpay.WxNotifyType;
import com.example.paymentstudy.enums.wxpay.WxTradeState;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.service.PaymentInfoService;
import com.example.paymentstudy.service.RefundInfoService;
import com.example.paymentstudy.service.WxpayService;
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
import org.springframework.transaction.annotation.Transactional;

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
    private CloseableHttpClient wxPayNoSignClient;

    @Resource
    private OrderInfoService orderInfoService;


    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RefundInfoService refundInfoService;


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
        log.warn("更具订单号核实订单状态 ====》 {}", orderNo);

        //查询订单状态
        String result = this.queryOrder(orderNo);
        Gson gson = new Gson();
        HashMap resultMap = gson.fromJson(result, HashMap.class);
        //获取微信端订单状态
        Object tradeState = resultMap.get("trade_state");

        //判断订单状态
        if (WxTradeState.SUCCESS.getType().equals(tradeState)) {
            log.warn("核实订单已支付 ===》 {}", orderNo);
            //确认已支付，修改订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(result);
        }
    }

    /**
     * 退款
     *
     * @param orderNo 订单号
     * @param reason  退款理由
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refund(String orderNo, String reason) throws IOException {
        log.info("创建退款单记录");
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);

        log.info("调用退款API");
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);

        //封装请求参数
        Gson gson = new Gson();
        HashMap paramsMap = new HashMap();
        paramsMap.put("out_trade_no", orderNo);
        paramsMap.put("out_refund_no", refundInfo.getRefundNo());
        paramsMap.put("reason", reason);
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));

        Map amountMap = new HashMap();
        amountMap.put("refund", refundInfo.getRefund());
        amountMap.put("total", refundInfo.getTotalFee());
        amountMap.put("currency", "CNY");

        paramsMap.put("amount", amountMap);


        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ====> {}", jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("退款成功，返回结果=====> {}", bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("退款异常，状态码：" + statusCode + ",返回结果：" + bodyAsString);
            }

            //更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);

            refundInfoService.updateRefund(bodyAsString);
        } finally {
            response.close();
        }


    }

    /**
     * 退款信息查询
     *
     * @param refundNo 退款编号
     * @return 退款结果
     */
    @Override
    public String queryRefund(String refundNo) throws IOException {
        log.info("查询退款结果调用 ====》 {}", refundNo);
        String format = String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo);
        String url = wxPayConfig.getDomain().concat(format);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("请求成功，返回结果：{}", bodyAsString);
            } else if (statusCode == 204) {
                log.info("请求成功");
            } else {
                throw new RuntimeException("查询退款异常，状态码：" + statusCode + "，返回结果：" + bodyAsString);
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 处理退款
     *
     * @param notification 微信支付通知（加密的）返回结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processRefund(Notification notification) {
        log.info("退款通知处理");
        String decryptData = notification.getDecryptData();
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(decryptData, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");
        if (lock.tryLock()) {
            try {
                //已经执行退款，不需要执行后面流程
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }
                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
                //更新退款单
                refundInfoService.updateRefund(decryptData);
            } finally {
                //释放锁
                lock.unlock();
            }
        }


    }

    /**
     * 请求微信端，获取对应账单的下载url
     *
     * @param billDate 账单时间
     * @param type     账单类型
     * @return 账单下载url
     */
    @Override
    public String queryBill(String billDate, String type) throws IOException {
        log.warn("申请账单接口调用{}", billDate);
        String url = "";
        if ("tradebill".equals(type)) {
            url = WxApiType.TRADE_BILLS.getType();
        } else if ("fundflowbill".equals(type)) {
            url = WxApiType.FUND_FLOW_BILLS.getType();
        } else {
            throw new RuntimeException("不支持的账单类型");
        }

        url = wxPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);
        log.info("请求url= {}",url);

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept","application/json");

        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        try{
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("请求成功，返回结果：{}", bodyAsString);
            } else if (statusCode == 204) {
                log.info("请求成功");
            } else {
                throw new RuntimeException("查询退款异常，状态码：" + statusCode + "，返回结果：" + bodyAsString);
            }

            Gson gson = new Gson();
            HashMap<String,String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            return resultMap.get("download_url");
        }finally {
            response.close();
        }
    }

    /**
     * 下载账单
     *
     * @param billDate 账单日期
     * @param type     账单类型
     * @return 账单数据
     */
    @Override
    public String downloadBill(String billDate, String type) throws IOException {
        log.info("下载账单接口调用 {} {}",billDate,type);

        String downloadUrl = this.queryBill(billDate, type);

        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.addHeader("Accept","application/json");

        CloseableHttpResponse response = wxPayNoSignClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("请求成功，返回结果：{}", bodyAsString);
            } else if (statusCode == 204) {
                log.info("请求成功");
            } else {
                throw new RuntimeException("查询退款异常，状态码：" + statusCode + "，返回结果：" + bodyAsString);
            }
            return bodyAsString;
        }finally {
            response.close();
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
