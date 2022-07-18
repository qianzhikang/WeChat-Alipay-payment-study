package com.example.paymentstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.paymentstudy.entity.Product;
import com.example.paymentstudy.mapper.ProductMapper;
import com.example.paymentstudy.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * @author qianzhikang
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
