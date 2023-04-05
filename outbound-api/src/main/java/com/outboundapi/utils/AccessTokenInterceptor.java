/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;

public class AccessTokenInterceptor implements Interceptor {

    private final AccessTokenService accessTokenService;
    private final AppConfigs appConfigs;

    public AccessTokenInterceptor(AccessTokenService accessTokenService, AppConfigs appConfigs) {
        super();
        this.accessTokenService = accessTokenService;
        this.appConfigs = appConfigs;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String accessToken = accessTokenService.getAccessToken();
        Request request = appendCommonHeaders(chain.request(), accessToken);
        //can log the requests here
        return chain.proceed(request);
    }

    @NonNull
    private Request appendCommonHeaders(@NonNull Request request, @NonNull String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .addHeader("Request-Id", UUID.randomUUID().toString())
                .addHeader("content-type", "application/json")
                .addHeader("date", AppUtils.getDate("YYYY-MM-dd HH:m:ss"))
                .addHeader("fsp-source", appConfigs.getFspId())
                .build();
    }
}
