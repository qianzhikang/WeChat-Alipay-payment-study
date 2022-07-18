package com.example.paymentstudy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.PaymentInfo;
import com.example.paymentstudy.mapper.PaymentInfoMapper;
import com.example.paymentstudy.service.PaymentInfoService;
import org.springframework.stereotype.Service;

/**
 * @author qianzhikang
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
