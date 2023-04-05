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

import java.util.Map;
import java.util.UUID;

@Service
public class TransferService {

    private final FiService fiService;
    private final SignatureUtil signatureUtil;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TransferService(FiService fiService, SignatureUtil signatureUtil) {
        this.fiService = fiService;
        this.signatureUtil = signatureUtil;
    }

    public ResponseEntity<?> outboundTransferConfirmation(Map<String, String> headers, String body, String payerRef){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Transfer Confirmation Callback ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Payer Ref -- {}", traceId, payerRef);
        logger.info("{} - Request Parameters -- {}", traceId, body);

        //check the signature
        String signature = headers.get("signature");
        boolean match = signatureUtil.verify( body, signature);
        logger.info("Signature ok? - {}", match);

        String message = "";
        TransferConfirmationRequest request = this.getTransferConfirmationRequest(body, traceId);
        if(null!=request){
            String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundXferConfirmation.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payeeRef>[payeeRef]</payeeRef><switchRef>[switchRef]</switchRef><completedTimestamp>[completedTimestamp]</completedTimestamp><transferState>[transferState]</transferState><payerRef>[payerRef]</payerRef><payerIdentifier>[payerIdentifier]</payerIdentifier></executeFinacleScript_CustomData></executeFinacleScriptRequest>";
            requestStr = requestStr.replace("[payeeRef]", request.getPayeeRef());
            requestStr = requestStr.replace("[switchRef]", request.getSwitchRef());
            requestStr = requestStr.replace("[completedTimestamp]", request.getCompletedTimestamp());
            requestStr = requestStr.replace("[transferState]", request.getTransferState());
            requestStr = requestStr.replace("[payerRef]", payerRef);
            requestStr = requestStr.replace("[payerIdentifier]", request.getPayer().getIdentifier());

            Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
            message = response.get("responseMsg").toString();
            if(response.get("status").equals("00")){
                // check if the xml has errors
                if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                    // @todo -  Add more Validation
                    TipsTransferResponse tipsResponse = new TipsTransferResponse(payerRef, "RECEIVED");
                    logger.info("{} - Successful Response ", traceId);
                    logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                    return ResponseEntity.accepted().body(tipsResponse);
                }else{
                    String error = fiService.getErrorDescription(message);
                    //@todo - do more error validations, check the other codes as well
                    TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                    logger.info("{} - Error Response ", traceId);
                    logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(tipsErrorResponse);
                }
            }
        }else {
            message = "An error has occurred while decoding the request";
        }

        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    public ResponseEntity<?> inboundTransfer(Map<String, String> headers, String body){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Transfer Request ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Request Parameters -- {}", traceId, body);

        //check the signature
        String signature = headers.get("signature");
        if(signature!=null && !signature.isEmpty()){
            boolean match = signatureUtil.verify( body, signature);
            logger.info("Signature ok? - {}", match);
        }
        String message = "";
        TransferRequest request = this.getTransferRequest(body, traceId);
        if(null!=request){
            String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundXfer.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payerRef>[payerRef]</payerRef><payeridentifierType>[payeeidentifierType]</payeridentifierType><payeridentifier>[payeeidentifier]</payeridentifier><payerfspId>[payerfspId]</payerfspId><payerfullName>[payerfullName]</payerfullName><payeraccountCategory>[payeraccountCategory]</payeraccountCategory><payeraccountType>[payeraccountType]</payeraccountType><payerIdentityType>[payerIdentityType]</payerIdentityType><payerIdentityValue>[payerIdentityValue]</payerIdentityValue><payeeidentifierType>[payeeidentifierType]</payeeidentifierType><payeeidentifier>[payeeidentifier]</payeeidentifier><payeefspId>[payeefspId]</payeefspId><payeefullName>[payeefullName]</payeefullName><payeeaccountCategory>[payeeaccountCategory]</payeeaccountCategory><payeeaccountType>[payeeaccountType]</payeeaccountType><payeeIdentityType>[payeeIdentityType]</payeeIdentityType><payeeIdentityValue>[payeeIdentityType]</payeeIdentityValue><TransactionAmount>[TransactionAmount]</TransactionAmount><TransactionCurrency>[TransactionCurrency]</TransactionCurrency><transactionTypescenario>[transactionTypescenario]</transactionTypescenario><transactionTypeinitiator>[transactionTypeinitiator]</transactionTypeinitiator><transactionTypeinitiatorType>[transactionTypeinitiatorType]</transactionTypeinitiatorType><description>[description]</description></executeFinacleScript_CustomData></executeFinacleScriptRequest>";

            requestStr = requestStr.replace("[payerRef]", request.getPayerRef());
            requestStr = requestStr.replace("[payeridentifierType]", request.getPayer().getIdentifierType());
            requestStr = requestStr.replace("[payeridentifier]", request.getPayer().getIdentifier());
            requestStr = requestStr.replace("[payerfspId]", request.getPayer().getFspId());
            requestStr = requestStr.replace("[payerfullName]", request.getPayer().getFullName());
            requestStr = requestStr.replace("[payeraccountCategory]", request.getPayer().getAccountCategory());
            requestStr = requestStr.replace("[payeraccountType]", request.getPayer().getAccountType());
            requestStr = requestStr.replace("[payerIdentityType]", request.getPayer().getIdentity().getType());
            requestStr = requestStr.replace("[payerIdentityValue]", request.getPayer().getIdentity().getValue());
            requestStr = requestStr.replace("[payeeidentifierType]", request.getPayee().getIdentifierType());
            requestStr = requestStr.replace("[payeeidentifier]", request.getPayee().getIdentifier());
            requestStr = requestStr.replace("[payeefspId]", request.getPayee().getFspId());
            requestStr = requestStr.replace("[payeefullName]", request.getPayee().getFullName());
            requestStr = requestStr.replace("[payeeaccountCategory]", request.getPayee().getAccountCategory());
            requestStr = requestStr.replace("[payeeaccountType]", request.getPayee().getAccountType());
            requestStr = requestStr.replace("[payeeIdentityType]", request.getPayee().getIdentity().getType());
            requestStr = requestStr.replace("[payeeIdentityValue]", request.getPayee().getIdentity().getValue());
            requestStr = requestStr.replace("[TransactionAmount]", request.getAmount().getAmount());
            requestStr = requestStr.replace("[TransactionCurrency]", request.getAmount().getCurrency());
            requestStr = requestStr.replace("[transactionTypescenario]", request.getTransactionType().getScenario());
            requestStr = requestStr.replace("[transactionTypeinitiator]", request.getTransactionType().getInitiator());
            requestStr = requestStr.replace("[transactionTypeinitiatorType]", request.getTransactionType().getInitiatorType());
            requestStr = requestStr.replace("[description]", request.getDescription());

            Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
            message = response.get("responseMsg").toString();
            if(response.get("status").equals("00")){
                // check if the xml has errors
                if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                    // @todo -  Add more Validation
                    TipsTransferResponse tipsResponse = new TipsTransferResponse(request.getPayerRef(), "RECEIVED");
                    logger.info("{} - Successful Response ", traceId);
                    logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                    return ResponseEntity.accepted().body(tipsResponse);
                }else{
                    String error = fiService.getErrorDescription(message);
                    //@todo - do more error validations, check the other codes as well
                    TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                    logger.info("{} - Error Response ", traceId);
                    logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(tipsErrorResponse);
                }
            }
        }else {
            message = "An error has occurred while decoding the request";
        }

        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    public ResponseEntity<?> inboundInquiry(Map<String, String> headers, String payerRef){
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Inbound inquiry Request ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Payer Ref -- {}", traceId, payerRef);

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundTranInq.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payerReference>[payerReference]</payerReference></executeFinacleScript_CustomData></executeFinacleScriptRequest>";
        requestStr = requestStr.replace("[payerReference]", payerRef);

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // @todo -  Add more Validation
                TipsTransferResponse tipsResponse = new TipsTransferResponse(payerRef, "RECEIVED");
                logger.info("{} - Successful Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                return ResponseEntity.ok().body(tipsResponse);
            }else{
                String error = fiService.getErrorDescription(message);
                //@todo - do more error validations, check the other codes as well
                TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                logger.info("{} - Error Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(tipsErrorResponse);
            }
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    public ResponseEntity<?> outboundTransferCallbackError(Map<String, String> headers, TipsErrorResponse request, String payerRef){
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - Outbound Error Callback Request ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Payer Ref -- {}", traceId, payerRef);

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundXferError.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><payerRef>[payerRef]</payerRef><errorDescription>[errorDescription]</errorDescription></executeFinacleScript_CustomData></executeFinacleScriptRequest>";
        requestStr = requestStr.replace("[payerRef]", payerRef);
        requestStr = requestStr.replace("[errorDescription]", request.getErrorInformation().getErrorDescription());

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // @todo -  Add more Validation
                TipsTransferResponse tipsResponse = new TipsTransferResponse(payerRef, "RECEIVED");
                logger.info("{} - Successful Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsResponse));
                return ResponseEntity.ok().body(tipsResponse);
            }else{
                String error = fiService.getErrorDescription(message);
                //@todo - do more error validations, check the other codes as well
                TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", error);
                logger.info("{} - Error Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(tipsErrorResponse);
            }
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    private TransferConfirmationRequest getTransferConfirmationRequest(String body, String traceId){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, TransferConfirmationRequest.class);
        }catch (Exception exception){
            logger.error("{} - An error has occurred while decoding the request - {}", traceId, exception.getMessage());
        }
        return null;
    }

    private TransferRequest getTransferRequest(String body, String traceId){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, TransferRequest.class);
        }catch (Exception exception){
            logger.error("{} - An error has occurred while decoding the request - {}", traceId, exception.getMessage());
        }
        return null;

    }


}
