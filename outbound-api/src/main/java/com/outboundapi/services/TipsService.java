/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.services;

import com.outboundapi.models.ResponseModel;
import com.outboundapi.models.TipsErrorResponse;
import com.outboundapi.utils.AccessTokenInterceptor;
import com.outboundapi.utils.AccessTokenService;
import com.outboundapi.utils.AppConfigs;
import com.outboundapi.utils.AppUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class TipsService {

    private final AccessTokenService accessTokenService;
    private final AppConfigs appConfigs;

    private static final Logger logger = LoggerFactory.getLogger(TipsService.class);

    public TipsService(AccessTokenService accessTokenService, AppConfigs appConfigs) {
        this.accessTokenService = accessTokenService;
        this.appConfigs = appConfigs;
    }

    public ResponseModel syncAccountLookup(String identifierType, String identifier, String traceId) {
        String status = "01";
        String message = "An error has occurred";
        String url = appConfigs.getTipsEndpoint() + "/account-lookup/1.0/parties/"+identifierType+"/"+identifier+"/sync";
        Request request = new Request.Builder()
                .url(url)
                .build();
        logger.info("{} - Endpoint - {}", traceId, url);

        try {
            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder().hostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }
            );

            if (appConfigs.isBypassTipsHttps())
                AppUtils.bypassSSL(httpBuilder, traceId);

            OkHttpClient client = httpBuilder
                    .connectTimeout(appConfigs.getTimeout(), TimeUnit.SECONDS)
                    .readTimeout(appConfigs.getTimeout(), TimeUnit.SECONDS)
                    .addInterceptor(new AccessTokenInterceptor(this.accessTokenService, this.appConfigs))
                    .build();

            Response response = client.newCall(request).execute();

            System.out.println("Response Code - " + response.code());
            String responseMsg = AppUtils.convertToJson(Objects.requireNonNull(response.body()).string());
            System.out.println("Response Message - " + responseMsg);
            response.body().close();

            if(response.isSuccessful()) {
                status = "00";
                message = responseMsg;
            }else {
                TipsErrorResponse error = AppUtils.mapTipsErrorResponse(responseMsg);
                status = Integer.toString(response.code());
                message = (error!=null)?error.getErrorInformation().getErrorDescription():"An error has occurred. Please contact admin";
            }

        }catch(IOException ex) {
            message = ex.getMessage();
        }
        return new ResponseModel(status, message);
    }

    public String getToken(String traceId){
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
        logger.info("{} - Token URL - {}", traceId, url);
        Request req = new Request.Builder()
                .url(url)
                .post(formBody)
                .headers(headers).build();
        // Get the response
        try {
            Response res = client.newCall(req).execute();
            logger.info("{} - Token Response Code - {}", traceId, res.code());
            Map<String, Object> obj = AppUtils.convertJsonToObject(res.body().string());
            return obj.get("access_token").toString();
        } catch (IOException | IllegalStateException e) {
            logger.error("An error occurred while fetching the access token - {}", e.getMessage());
        }
        return "";
    }

}
