package com.hd.calculator.app.function.network;

import android.util.Log;

import com.hd.calculator.app.BuildConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * @author allever
 */
public class AuthorizationInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String authorization = "eyJhbGciOiJIUzI1NiJ9.eyJhcHBDb2RlIjoiZGluZy1iaWxsLWFwcCIsInRlbmFudElkIjowLCJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYm9zcyIsInNpZ25UaW1lIjoxNzUxNzgxNTM5MDUzLCJleHBpcmVUaW1lIjoxNzUxNzkyMzM5MDUzLCJleHRyYSI6IiJ9.GwEc-Ksqvn170Yq_xcoatbJFdEB5j1F1zTwaRsyvn4A";
        Request.Builder requestBuilder = originalRequest.newBuilder();
        requestBuilder.addHeader("Authorization", authorization);

        requestBuilder.method(originalRequest.method(), originalRequest.body());
        Request request = requestBuilder.build();
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        String responseBodyString = (responseBody == null ? "null" : responseBody.string());
        if (BuildConfig.DEBUG) {
            Log.i("LogInterceptor", "请求链接 = " + request.url());
            Log.i("LogInterceptor", "请求体 = " + getRequestInfo(request));
            Log.i("LogInterceptor", "请求结果 = " + responseBodyString);
        }

        ResponseBody body = ResponseBody.create(responseBodyString.getBytes(), responseBody == null ? MediaType.parse("application/json") : responseBody.contentType());
        return response.newBuilder().body(body).build();
    }

    /**
     * 打印请求消息
     *
     * @param request 请求的对象
     */
    private String getRequestInfo(Request request) {
        String str = "";
        if (request == null) {
            return str;
        }
        RequestBody requestBody = request.body();
        if (requestBody == null) {
            return str;
        }
        try {
            Buffer bufferedSink = new Buffer();
            requestBody.writeTo(bufferedSink);
            Charset charset = StandardCharsets.UTF_8;
            str = bufferedSink.readString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
