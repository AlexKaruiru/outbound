/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.services;


import com.outboundapi.models.*;
import com.outboundapi.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ReversalService {

    private final FiService fiService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ReversalService(FiService fiService) {
        this.fiService = fiService;
    }

    public ResponseEntity<?> inboundReversal(Map<String, String> headers, ReversalRequest request){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Inbound Reversal Request ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Request Parameters -- {}", traceId, AppUtils.convertToJson(request));

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundXferReversal.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payerRef>[payerRef]</payerRef><payerReversalRef>[payerReversalRef]</payerReversalRef><payeeRef>[payeeRef]</payeeRef><switchRef>[switchRef]</switchRef><amount>[amount]</amount><currency>[currency]</currency><reversalReason>[reversalReason]</reversalReason></executeFinacleScript_CustomData></executeFinacleScriptRequest>";

        requestStr = requestStr.replace("[payerRef]", request.getPayerRef());
        requestStr = requestStr.replace("[payerReversalRef]", request.getPayerReversalRef());
        requestStr = requestStr.replace("[payeeRef]", request.getPayeeRef());
        requestStr = requestStr.replace("[switchRef]", request.getSwitchRef());
        requestStr = requestStr.replace("[amount]", request.getAmount().getAmount());
        requestStr = requestStr.replace("[currency]", request.getAmount().getCurrency());
        requestStr = requestStr.replace("[reversalReason]", request.getReversalReason());

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // @todo -  Add more Validation
                TipsTransferResponse tipsResponse = new TipsTransferResponse(request.getPayerRef(), "RECEIVED");
                logger.info("{} - Successful Response ", traceId);
                return ResponseEntity.accepted().build();
            }else{
                String error = fiService.getErrorDescription(message);
                //@todo - do more error validations, check the other codes as well
                TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                logger.info("{} - Error Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tipsErrorResponse);
            }
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    public ResponseEntity<?> outboundReversalCallback(Map<String, String> headers, ReversalCallbackRequest request, String payerRef){
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - Outbound Reversal Confirmation (Messaging) Callback ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Payer Ref -- {}", traceId, payerRef);
        logger.info("{} - Request Parameters -- {}", traceId, AppUtils.convertToJson(request));

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundRevMessNotification.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payerRef>[payerRef]</payerRef><reversalReason>[reversalReason]</reversalReason><reversalState>[reversalState]</reversalState></executeFinacleScript_CustomData></executeFinacleScriptRequest>";

        requestStr = requestStr.replace("[payerRef]", payerRef);
        requestStr = requestStr.replace("[reversalReason]", request.getReversalReason());
        requestStr = requestStr.replace("[reversalState]", request.getReversalState());

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // @todo -  Add more Validation
                TipsTransferResponse tipsResponse = new TipsTransferResponse(payerRef, "RECEIVED");
                logger.info("{} - Successful Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                return ResponseEntity.ok().build();
            }else{
                String error = fiService.getErrorDescription(message);
                //@todo - do more error validations, check the other codes as well
                TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                logger.info("{} - Error Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tipsErrorResponse);
            }
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    public ResponseEntity<?> transferReversalConfirmation(Map<String, String> headers, ReversalConfirmationRequest request, String payerRef){
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - Transfer Reversal Confirmation ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Payer Ref -- {}", traceId, payerRef);
        logger.info("{} - Request Parameters -- {}", traceId, AppUtils.convertToJson(request));

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundRevConfirmation.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payeeRef>[payeeRef]</payeeRef><switchRef>[switchRef]</switchRef><completedTimestamp>[completedTimestamp]</completedTimestamp><payeeReversalRef>[payeeReversalRef]</payeeReversalRef><payerReversalRef>[payerReversalRef]</payerReversalRef><switchReversalRef>[switchReversalRef]</switchReversalRef><reversalState>[reversalState]</reversalState></executeFinacleScript_CustomData></executeFinacleScriptRequest>";

        requestStr = requestStr.replace("[payerRef]", payerRef);
        requestStr = requestStr.replace("[payeeRef]", request.getPayeeRef());
        requestStr = requestStr.replace("[switchRef]", request.getSwitchRef());
        requestStr = requestStr.replace("[completedTimestamp]", request.getCompletedTimestamp());
        requestStr = requestStr.replace("[payeeReversalRef]", request.getPayeeReversalRef());
        requestStr = requestStr.replace("[payerReversalRef]", request.getPayerReversalRef());
        requestStr = requestStr.replace("[switchReversalRef]", request.getSwitchReversalRef());
        requestStr = requestStr.replace("[reversalState]", request.getReversalState());

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // @todo -  Add more Validation
                TipsTransferResponse tipsResponse = new TipsTransferResponse(payerRef, "RECEIVED");
                logger.info("{} - Successful Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                return ResponseEntity.ok().build();
            }else{
                String error = fiService.getErrorDescription(message);
                //@todo - do more error validations, check the other codes as well
                TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                logger.info("{} - Error Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tipsErrorResponse);
            }
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }
}
