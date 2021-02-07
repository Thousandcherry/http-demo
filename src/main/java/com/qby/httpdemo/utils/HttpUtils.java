package com.qby.httpdemo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.qby.httpdemo.dto.HttpResponseEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http 请求封装
 * 在实际项目中，经常会有一些对接第三方接口的需求，一般是获取或传输数据，有时候需要在后台发起http
 * 请求数据或传输数据。这个工具类就是对http请求方法进行二次封装，方便调用
 * @author thousandcherry
 * @date 2021/2/7 14:19
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    /**
     * 客户端和服务器建立连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 5000;
    /**
     * 连接池获取连接超时时间
     */
    private static final int CONNECT_REQUEST_TIMEOUT = 5000;
    /**
     * 客户端和服务器建立连接后，客户端从服务器读取数据超时时间
     */
    private static final int SOCKET_TIMEOUT = 10000;

    /**
     * 发送POST请求
     *
     * @param: url 请求地址
     * @param: params 请求参数
     * @param: header 请求头
     * @author thousandcherry
     * @date 2021/2/7 16:04
     */
    public static HttpResponseEntity post(String url, Map<String, String> params, Map<String, String> header) {

        CloseableHttpClient httpCilent = null;
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = null;

        try {

            if (!StringUtils.hasText(url)) {
                logger.info("【post参数校验】url不能为空");
                throw new Exception("【post参数校验】url不能为空");
            }

            httpCilent = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setRedirectsEnabled(true)
                    .build();

            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);

            //设置post请求头
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");

            //自定义的请求头
            if (header != null && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }

            //设置post请求参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");//解决中文乱码问题
                entity.setContentEncoding("UTF-8");
                httpPost.setEntity(entity);
            }

            //发送请求
            httpResponse = httpCilent.execute(httpPost);

            //请求状态码
            int code = httpResponse.getStatusLine().getStatusCode();
            String data = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            HttpResponseEntity resEntity = new HttpResponseEntity();
            resEntity.setCode(code);
            resEntity.setData(JSON.parseObject(data));

            logger.info("返回结果：{}", resEntity.toString());

            return resEntity;
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("解析请求返回结果异常");
        }  catch (Exception e) {
            e.printStackTrace();
            logger.error("http post 请求异常");
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (httpCilent != null) {
                    httpCilent.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 发送GET请求
     *
     * @param: url 请求地址，不要带?号
     * @param: params 请求参数，get请求会将参数拼接到url后面
     * @param: header 请求头
     * @author thousandcherry
     * @date 2021/2/7 16:03
     */
    public static HttpResponseEntity get(String url, Map<String, String> params, Map<String, String> header) {

        CloseableHttpClient httpCilent = null;
        CloseableHttpResponse httpResponse = null;
        HttpGet httpGet = null;

        try {

            if (!StringUtils.hasText(url)) {
                logger.info("【get参数校验】url不能为空");
                throw new Exception("【get参数校验】url不能为空");
            }

            httpCilent = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setRedirectsEnabled(true)
                    .build();

            httpGet = new HttpGet();
            httpGet.setConfig(requestConfig);

            if (params != null && !params.isEmpty()) {
                //设置get参数
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                String paramsStr = URLEncodedUtils.format(formParams, "UTF-8");
                httpGet.setURI(URI.create(url + "?" + paramsStr));
            } else {
                httpGet.setURI(URI.create(url));
            }

            //设置get请求头
            if (header != null && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }

            //发送请求
            httpResponse = httpCilent.execute(httpGet);

            //请求状态码
            int code = httpResponse.getStatusLine().getStatusCode();
            String data = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            HttpResponseEntity resEntity = new HttpResponseEntity();
            resEntity.setCode(code);
            resEntity.setData(JSON.parseObject(data));

            logger.info("返回结果：{}", resEntity.toString());

            return resEntity;
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("解析请求返回结果异常");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("http get 请求异常");
        } finally {

            if (httpGet != null) {
                httpGet.releaseConnection();
            }

            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (httpCilent != null) {
                    httpCilent.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}