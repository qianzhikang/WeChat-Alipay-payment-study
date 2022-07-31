package com.example.paymentstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//引入定时任务
@EnableScheduling
public class PaymentStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentStudyApplication.class, args);
    }

}
