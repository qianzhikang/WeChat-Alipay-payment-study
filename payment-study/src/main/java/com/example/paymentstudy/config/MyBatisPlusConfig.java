package com.example.paymentstudy.config;

import org.mapstruct.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Description TODO
 * @Date 2022-07-18-11-42
 * @Author qianzhikang
 */
@Configuration
@EnableTransactionManagement  //启用事物管理
@MapperScan("com.example.paymentstudy.mapper")
public class MyBatisPlusConfig {

}
