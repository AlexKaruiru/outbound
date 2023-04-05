/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfigs {

    @Value("${app.timeout}")
    private long timeout;
    @Value("${app.tips.endpoint}")
    private String tipsEndpoint;
    @Value("${app.tips.bypass-https}")
    private boolean bypassTipsHttps;
    @Value("${app.fi.endpoint}")
    private String fiEndpoint;
    @Value("${app.fi.bypass-https}")
    private boolean bypassFiHttps;
    @Value("${app.tips.auth.consumer-key}")
    private String consumerKey;
    @Value("${app.tips.auth.consumer-secret}")
    private String consumerSecret;
    @Value("${app.tips.fsp-id}")
    private String fspId;
    @Value("${app.signature-certificate.path}")
    private String certificatePath;
    @Value("${app.signature-certificate.password}")
    private String certificatePassword;
    @Value("${app.signature-certificate.alias}")
    private String certificateAlias;
}
