package com.qby.httpdemo;

import com.qby.httpdemo.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HttpDemoApplicationTests {

    @Test
    void contextLoads() {

        //测试接口地址，改成实际接口地址
        String url = "http://127.0.0.1:8080/";

        HttpUtils.get(url, null, null);
    }

}
