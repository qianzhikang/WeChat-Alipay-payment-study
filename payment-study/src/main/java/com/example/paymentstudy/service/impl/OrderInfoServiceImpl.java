package com.example.paymentstudy.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.entity.Product;
import com.example.paymentstudy.enums.OrderStatus;
import com.example.paymentstudy.mapper.OrderInfoMapper;
import com.example.paymentstudy.mapper.ProductMapper;
import com.example.paymentstudy.service.OrderInfoService;
import com.example.paymentstudy.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author qianzhikang
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {


    @Resource
    private ProductMapper productMapper;

    /**
     * 根据商品id 创建订单信息
     *
     * @param productId 商品id
     * @return 订单信息对象
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId) {

        // 查询已存在但未支付但订单
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId);
        if (orderInfo != null) {
            return orderInfo;
        }

        //获取商品信息
        Product product = productMapper.selectById(productId);

        //生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        // 这里的baseMapper 相当于OrderInfoMapper，原因是 OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        // 内部的baseMapper 在此处对应范型中指定的OrderInfoMapper
        baseMapper.insert(orderInfo);

        return orderInfo;
    }

    /**
     * 存储支付二维码链接（二维码两小时内有效）
     *
     * @param orderNo 订单号
     * @param codeUrl 二维码url
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);

        baseMapper.update(orderInfo, queryWrapper);
    }

    /**
     * 查询订单列表（按创建时间倒序）
     *
     * @return 订单信息列表
     */
    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据订单号修改支付状态
     *
     * @param orderNo     订单号
     * @param orderStatus 订单状态枚举
     */
    @Override
    public void updateStatusByOrderNo(Object orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态");
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        // 创建订单信息并且更新
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        baseMapper.update(orderInfo, queryWrapper);
    }

    /**
     * 获取订单状态
     *
     * @param orderNo 订单号
     * @return 订单状态
     */
    @Override
    public String getOrderStatus(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        if (orderInfo == null) {
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    /**
     * 查询超时未支付的订单
     *
     * @param minutes 时间/分钟
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {

        //minutes 之前的时间
        Instant minus = Instant.now().minus(Duration.ofMinutes(minutes));
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status",OrderStatus.NOTPAY.getType());
        queryWrapper.le("create_time",minus);
        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);
        return orderInfoList;
    }

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Override
    public OrderInfo getOrderByOrdernNo(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据商品id查询未支付订单
     * 防止订单重复创建
     *
     * @param productId 商品id
     * @return 订单信息对象
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        // 在实际项目中还需要判断当前是否为当前用户下单
        //queryWrapper.eq("user_id",userId);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        return orderInfo;
    }
}
