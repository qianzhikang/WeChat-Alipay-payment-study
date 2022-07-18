package com.example.paymentstudy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.OrderInfo;
import com.example.paymentstudy.mapper.OrderInfoMapper;
import com.example.paymentstudy.service.OrderInfoService;
import org.springframework.stereotype.Service;

/**
 * @author qianzhikang
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

}
