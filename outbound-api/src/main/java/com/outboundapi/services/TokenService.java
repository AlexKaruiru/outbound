/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.outboundapi.models.*;
import com.outboundapi.utils.AppUtils;
import com.outboundapi.utils.SignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    private final TipsService tipsService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TokenService(TipsService tipsService) {
        this.tipsService = tipsService;
    }

    public ResponseEntity<?> getToken(Map<String, String> headers){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Get Token Request ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        // validate the channel-id

        String message = "Bad Request";
        int status = 400;
        String token = tipsService.getToken(traceId);
        if(null!=token && !token.isEmpty()){
            logger.info("{} - Successful Response ", traceId);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok().body(response);
        }else {
            status = 401;
            message = "Invalid Credentials";
        }
        return ResponseEntity.status(status).body(new ResponseModel(String.valueOf(status), message));
    }

}
