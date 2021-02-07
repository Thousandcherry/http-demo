package com.qby.httpdemo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * http 请求结果
 * @author thousandcherry
 * @date 2021/2/7 15:31
 */
@Getter
@Setter
public class HttpResponseEntity {
    /**状态码*/
    private int code;
    /**请求返回数据*/
    private Map<String, Object> data;
}
