/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import com.outboundapi.services.FiService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AccessTokenService {

    private final AppConfigs appConfigs;
    private static final Logger logger = LoggerFactory.getLogger(FiService.class);

    public AccessTokenService(AppConfigs appConfigs) {
        this.appConfigs = appConfigs;
    }

    public String getAccessToken() {

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder().hostnameVerifier(
                new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }
        );

        OkHttpClient client =
                httpBuilder
                .connectTimeout(appConfigs.getTimeout(), TimeUnit.MILLISECONDS)
                .build();

        String credentials = appConfigs.getConsumerKey()+":"+appConfigs.getConsumerSecret();
        // Create the proper headers
        Headers headers = new Headers.Builder().add("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes())).set("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8").build();
        // Create POST data
        FormBody formBody = new FormBody.Builder().add("grant_type", "client_credentials").build();
        // Create the Request
        String url = appConfigs.getTipsEndpoint()+"/token";
        Request req = new Request.Builder()
                .url(url)
                .post(formBody)
                .headers(headers).build();
        // Get the response
        try {
            Response res = client.newCall(req).execute();
            Map<String, Object> obj = AppUtils.convertJsonToObject(res.body().string());
            return obj.get("access_token").toString();
        } catch (IOException | IllegalStateException e) {
            logger.error("An error occurred while fetching the access token - {}", e.getMessage());
        }
        return "";
    }
}
