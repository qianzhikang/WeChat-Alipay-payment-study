package com.example.paymentstudy.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 统一返回封装
 * @Date 2022-07-15-12-45
 * @Author qianzhikang
 */

@Data
public class Response {
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public static Response success(){
        Response response = new Response();
        response.setCode(200);
        response.setMessage("请求成功");
        return response;
    }

    public static Response error(){
        Response response = new Response();
        response.setCode(-1);
        response.setMessage("请求失败");
        return response;
    }

    public Response data(String key,Object value){
        this.data.put(key,value);
        return this;
    }
}
