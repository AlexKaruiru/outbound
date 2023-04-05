/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.services;


import com.outboundapi.models.ApiError;
import com.outboundapi.models.Payee;
import com.outboundapi.models.ResponseModel;
import com.outboundapi.models.TipsErrorResponse;
import com.outboundapi.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AccountLookupService {

    private final FiService fiService;
    private final TipsService tipsService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AccountLookupService(FiService fiService, TipsService tipsService) {
        this.tipsService = tipsService;
        this.fiService = fiService;
    }

    public ResponseEntity<?> inboundSync(Map<String, String> headers, String identifierType, String identifier){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Account Lookup Sync ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Identifier Type -- {}", traceId, identifierType);
        logger.info("{} - Identifier -- {}", traceId, identifier);

        String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundAcctLookup.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><AccountNumber>[AccountNumber]</AccountNumber><IdentifierType>[IdentifierType]</IdentifierType></executeFinacleScript_CustomData></executeFinacleScriptRequest>";
        requestStr = requestStr.replace("[AccountNumber]", identifier);
        requestStr = requestStr.replace("[IdentifierType]", identifierType);

        Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
        String status = "FAILED";

        String message = response.get("responseMsg").toString();
        if(response.get("status").equals("00")){
            // check if the xml has errors
            if(!message.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                // the xml response from FI is successful
                String[] respTags = {"identifierType", "identifier", "fspId", "fullName", "accountCategory", "accountType"};
                Map<String, String> res = AppUtils.readXML(message, "AccountDetails", respTags);
                String[] identityRespTags = {"type", "value"};
                Map<String, String> identityRes = AppUtils.readXML(message, "identity", identityRespTags);

                Payee payee = new Payee();
                Payee.Identity identity = new Payee.Identity();
                String identityType = (identityRes.get("type")!=null)?identityRes.get("type"):"NIN";
                String identityValue = (identityRes.get("value")!=null)?identityRes.get("value"):"447324234232";
                identity.setType(identityType);
                identity.setValue(identityValue); // change this
                payee.setAccountType(res.get("accountType"));
                payee.setAccountCategory(res.get("accountCategory"));
                payee.setFspId(res.get("fspId"));
                payee.setFullName(res.get("fullName"));
                payee.setIdentifierType(res.get("identifierType"));
                payee.setIdentifier(res.get("identifier"));
                payee.setIdentity(identity);
                logger.info("{} - Successful Response ", traceId);
                logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(payee));
                return ResponseEntity.ok(payee);
            }else{
                status = fiService.getErrorDescription(message);
            }
        }
        TipsErrorResponse errorResponse = new TipsErrorResponse("404", status);
        logger.error("{} - Error Response ", traceId);
        logger.error("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(errorResponse));
        return ResponseEntity.badRequest().body(errorResponse);
    }

    public ResponseEntity<?> inboundAsync(Map<String, String> headers, String identifierType, String identifier){
        // save the request to the DB or Queue
        // return the response to tips
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Account Lookup Async ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Identifier Type -- {}", traceId, identifierType);
        logger.info("{} - Identifier -- {}", traceId, identifier);
        logger.info("{} - Response ", traceId);
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<?> outboundCallback(Map<String, String> headers, Payee request, String identifierType, String identifier){
        // save the response to the DB or Queue
        // return the response to tips
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - Outbound callback ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Identifier Type -- {}", traceId, identifierType);
        logger.info("{} - Identifier -- {}", traceId, identifier);
        logger.info("{} - Request Parameters -- {}", traceId, AppUtils.convertToJson(request));
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> outboundErrorCallback(Map<String, String> headers, TipsErrorResponse request, String identifierType, String identifier){
        // save the response to the DB or Queue
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - Account Lookup Async Error Response ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Identifier Type -- {}", traceId, identifierType);
        logger.info("{} - Identifier -- {}", traceId, identifier);
        logger.info("{} - Request Parameters -- {}", traceId, AppUtils.convertToJson(request));
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> outboundSync(Map<String, String> headers, String identifierType, String identifier){
        // this request comes from internal systems
        // sends a sync request to TIPS for account look up
        // respond to the channel
        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Channel Account Look Up", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        Map<String, String> isValid = AppUtils.validateInternalRequests(headers);
        if(isValid.get("status").equals("success")){
            logger.info("{} - Identifier Type -- {}", traceId, identifierType);
            logger.info("{} - Identifier -- {}", traceId, identifier);

            ResponseModel responseModel = tipsService.syncAccountLookup(identifierType, identifier, traceId);
            if(responseModel.getStatus().equals("00")){
                return ResponseEntity.ok().body(responseModel);
            }else{
                // return error 500 -  Exception
                ApiError apiError = new ApiError(500, "An Error has occurred", responseModel.getMessage());
                return ResponseEntity.internalServerError().body(apiError);
            }

        }else {
            // return error 400-  bad request
            ApiError apiError = new ApiError(400, "Bad Request", isValid.get("errors"));
            logger.error("{} - Error Response - {}", traceId, AppUtils.convertToJson(apiError));
            return ResponseEntity.badRequest().body(apiError);
        }

    }

    public void inboundCallback(){
        // query periodically if there are requests to send back to TIPS
        // this only applies for successful account lookup requests
    }

    public void inboundErrorCallback(){
        // query periodically if there are requests to send back to TIPS
        // this only applies for failed requests
    }

}
