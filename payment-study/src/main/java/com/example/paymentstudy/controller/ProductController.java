package com.example.paymentstudy.controller;

import com.example.paymentstudy.entity.Product;
import com.example.paymentstudy.service.ProductService;
import com.example.paymentstudy.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description 测试
 * @Date 2022-07-15-12-29
 * @Author qianzhikang
 */
@Api(tags = "商品管理")
@CrossOrigin //开放跨域访问
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @ApiOperation("测试接口")
    @GetMapping("/test")
    public Response test() {
        // 需要返回数据时链式操作即可
        return Response.success().data("msg","hello").data("user",new Date());
    }


    @GetMapping("/list")
    public Response list(){
        List<Product> list = productService.list();
        return Response.success().data("productList",list);
    }
}
