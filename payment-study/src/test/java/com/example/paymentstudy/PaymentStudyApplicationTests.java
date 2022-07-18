package com.example.paymentstudy;

import com.example.paymentstudy.config.WxPayConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.security.PrivateKey;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class PaymentStudyApplicationTests {

    @Resource
    private WxPayConfig wxPayConfig;

    @Test
    void contextLoads() {
    }


    /**
     *商户私钥获取测试
     */
    //@Test
    //void getPrivateKey(){
    //    //在配置文件获取文件地址
    //    String privateKeyPath = wxPayConfig.getPrivateKeyPath();
    //    //获取文件中的私钥
    //    PrivateKey privateKey = wxPayConfig.getPrivateKey(privateKeyPath);
    //    System.out.println(privateKey);
    //}

}
