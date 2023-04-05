/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.services;


import com.outboundapi.models.TipsErrorResponse;
import com.outboundapi.models.TipsTransferResponse;
import com.outboundapi.utils.AppConfigs;
import com.outboundapi.utils.AppUtils;
import okhttp3.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FiService {

    private final AppConfigs appConfigs;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType XML = MediaType.get("text/xml");

    private static final Logger logger = LoggerFactory.getLogger(FiService.class);

    public FiService(AppConfigs appConfigs) {
        this.appConfigs = appConfigs;
    }

    public ResponseEntity<?> post(Map<String, String> headers, String body){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} -New FI Post Request", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Request Parameters -- {}", traceId, body);

        Map<String, Object> response = this.sendPostRequest(body, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            return ResponseEntity.ok(message);
        }else {
            return ResponseEntity.status(500).body(response);
        }
    }

    public Map<String, Object> sendRequest(String requestStr, String traceId){

        String fiRequest = this.getRequestHeader(traceId).replace("[Body]", requestStr);
        return this.sendPostRequest(fiRequest, traceId);
    }

    private Map<String, Object> sendPostRequest(String fiRequest, String traceId){
        Map<String, Object> returnVal = new HashMap<>();
        RequestBody body = RequestBody.create(fiRequest, XML);

        String fiEndpoint = appConfigs.getFiEndpoint();
        logger.info("{} - FI Endpoint - {}", traceId, fiEndpoint);

        Request request = new Request.Builder()
                .url(fiEndpoint)
                .post(body)
                .build();
        try {
            OkHttpClient.Builder newBuilder = new OkHttpClient.Builder().hostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }
            );
            //check if to bypass ssl
            if(appConfigs.isBypassFiHttps())
                AppUtils.bypassSSL(newBuilder, traceId);

            OkHttpClient newClient = newBuilder
                    .readTimeout(appConfigs.getTimeout(), TimeUnit.SECONDS)
                    .connectTimeout(appConfigs.getTimeout(), TimeUnit.SECONDS)
                    .build();
            logger.info("{} - FI Request - {}", traceId, fiRequest);

            int responseCode;
            String responseString;
            try (Response response = newClient.newCall(request).execute()) {
                responseCode = response.code();
                responseString = Objects.requireNonNull(response.body()).string();
                logger.info("{} - Request Successful - {}", traceId, true);
                logger.info("{} - Response Code - {}", traceId, responseCode);
                logger.info("{} - Response Message - {}", traceId, responseString);

                if (response.isSuccessful()) {
                    returnVal.put("status", "00");
                    returnVal.put("message", "Successful");
                } else {
                    returnVal.put("status", "01");
                    returnVal.put("message", "Failed");
                }
            }
            returnVal.put("responseCode", responseCode);
            returnVal.put("responseMsg", responseString);

        } catch (Exception ex) {
            returnVal.put("status", "01");
            returnVal.put("message", "Failed");
            returnVal.put("responseMsg", ex.getMessage());
            logger.info("{} - Request Failed? - {}", traceId, true);
            logger.info("{} - An error has occurred - {}", traceId, ex.getMessage());
        }
        return returnVal;
    }

    public Map<String, Object> sendApacheRequest(String requestStr, String traceId) {
        String fiRequest = this.getRequestHeader(traceId).replace("[Body]", requestStr);
        String fiEndpoint = appConfigs.getFiEndpoint();
        logger.info("{} - FI Endpoint - {}", traceId, fiEndpoint);

        Map<String, Object> returnVal = new HashMap<>();


        try{

            CloseableHttpClient client;
            HttpPost httpPost = new HttpPost(fiEndpoint);

            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);

            client = HttpClients.custom().setSSLSocketFactory(scsf).build();

            StringEntity entity = new StringEntity(fiRequest);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "text/xml");
            httpPost.setHeader("Content-type", "text/xml");

            CloseableHttpResponse response = client.execute(httpPost);

            logger.info("{} -  Response Code - {}", traceId, response.getStatusLine().getStatusCode());
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            logger.info("{} -  Response Message - {}", traceId, responseBody);

            client.close();
        }catch (Exception ex){
            logger.error("{} - An error has occurred - {}", traceId, ex.getMessage());
        }
        return returnVal;
    }

    private String getRequestHeader(String traceId){
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><FIXML xsi:schemaLocation=\"http://www.finacle.com/fixml executeFinacleScript.xsd\" xmlns=\"http://www.finacle.com/fixml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Header><RequestHeader><MessageKey><RequestUUID>[RequestUUID]</RequestUUID><ServiceRequestId>executeFinacleScript</ServiceRequestId><ServiceRequestVersion>10.2</ServiceRequestVersion><ChannelId>COR</ChannelId></MessageKey><RequestMessageInfo><BankId>TZ</BankId><TimeZone>GMT+03:00</TimeZone><EntityId/><EntityType/><ArmCorrelationId/><MessageDateTime>[MessageDateTime]</MessageDateTime></RequestMessageInfo><Security><Token><PasswordToken><UserId/><Password/></PasswordToken></Token><FICertToken/><RealUserLoginSessionId/><RealUser/><RealUserPwd/><SSOTransferToken/></Security></RequestHeader></Header><Body>[Body]</Body></FIXML>";
        header = header.replace("[RequestUUID]", "TIPS_"+traceId);
        header = header.replace("[MessageDateTime]", AppUtils.getDate("YYYY-MM-dd'T'HH:m:ss.SSS"));
        return header;
    }

    public String getErrorDescription(String message){
        Map<String, String> errors = AppUtils.readXML(message.toUpperCase(), "ERRORDETAIL", new String[]{"ERRORDESC"});
        return errors.get("ERRORDESC");
    }

}
