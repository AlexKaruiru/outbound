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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SettlementService {

    private final FiService fiService;
    private final SignatureUtil signatureUtil;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SettlementService(FiService fiService, SignatureUtil signatureUtil) {
        this.fiService = fiService;
        this.signatureUtil = signatureUtil;
    }

    public ResponseEntity<?> inboundSettlement(Map<String, String> headers, String body){

        String traceId = UUID.randomUUID().toString();
        logger.info("{} - New Transaction - Inbound Settlement ", traceId);
        for (Map.Entry<String, String> entry: headers.entrySet()){
            logger.info("{} - Header - {} -  {}", traceId, entry.getKey(), entry.getValue());
        }
        logger.info("{} - Request Parameters -- {}", traceId, body);

        String message = "";
        SettlementRequest request = this.getRequest(body, traceId);
        if(null!=request){
            String requestStr = "<executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>TIPSInboundSettlement.scr</requestId></ExecuteFinacleScriptInputVO><executeFinacleScript_CustomData><settlementWindowid>[settlementWindowid]</settlementWindowid><settlementWindowdate>[settlementWindowdate]</settlementWindowdate><settlementWindowdescription>[settlementWindowdescription]</settlementWindowdescription><outgoingTransactionscurrency>[outgoingTransactionscurrency]</outgoingTransactionscurrency><outgoingTransactionsvolume>[outgoingTransactionsvolume]</outgoingTransactionsvolume><outgoingTransactionsvalue>[outgoingTransactionsvalue]</outgoingTransactionsvalue><incomingTransactionscurrency>[incomingTransactionscurrency]</incomingTransactionscurrency><incomingTransactionsvolume>[incomingTransactionsvolume]</incomingTransactionsvolume><incomingTransactionsvalue>[incomingTransactionsvalue]</incomingTransactionsvalue><netTransactionscurrency>[netTransactionscurrency]</netTransactionscurrency><netTransactionsvolume>[netTransactionsvolume]</netTransactionsvolume><netTransactionsvalue>[netTransactionsvalue]</netTransactionsvalue><positionAccountAmount>[positionAccountAmount]</positionAccountAmount><principalLedgerTypeAmount>[principalLedgerTypeAmount]</principalLedgerTypeAmount><positionAccountCurrency>[positionAccountCurrency]</positionAccountCurrency><feeAccountAmount>[feeAccountAmount]</feeAccountAmount><feeAccountCurrency>[feeAccountCurrency]</feeAccountCurrency><interchageFeeAmount>[interchageFeeAmount]</interchageFeeAmount><processingFeeAmount>[processingFeeAmount]</processingFeeAmount></executeFinacleScript_CustomData></executeFinacleScriptRequest>";

            SettlementRequest.SettlementWindow settlementWindow = request.getSettlementWindow();
            requestStr = requestStr.replace("[settlementWindowid]", settlementWindow.getId());
            requestStr = requestStr.replace("[settlementWindowdate]", settlementWindow.getClosingDate());
            requestStr = requestStr.replace("[settlementWindowdescription]", settlementWindow.getDescription());

            SettlementRequest.TransactionsList outgoingTrans = request.getOutgoingTransactions().get(0);
            requestStr = requestStr.replace("[outgoingTransactionscurrency]", outgoingTrans.getCurrency());
            requestStr = requestStr.replace("[outgoingTransactionsvolume]", outgoingTrans.getVolume());
            requestStr = requestStr.replace("[outgoingTransactionsvalue]", outgoingTrans.getValue());

            SettlementRequest.TransactionsList incomingTrans = request.getIncomingTransactions().get(0);
            requestStr = requestStr.replace("[incomingTransactionscurrency]", incomingTrans.getCurrency());
            requestStr = requestStr.replace("[incomingTransactionsvolume]", incomingTrans.getVolume());
            requestStr = requestStr.replace("[incomingTransactionsvalue]", incomingTrans.getValue());

            SettlementRequest.TransactionsList netTrans = request.getNetTransactions().get(0);
            requestStr = requestStr.replace("[netTransactionscurrency]", netTrans.getCurrency());
            requestStr = requestStr.replace("[netTransactionsvolume]", netTrans.getVolume());
            requestStr = requestStr.replace("[netTransactionsvalue]", netTrans.getValue());

            List<SettlementRequest.AccountsPosition> accountsPositionList = request.getAccountsPosition();
            for(SettlementRequest.AccountsPosition accountsPosition: accountsPositionList){

                if(accountsPosition.getAccountType().equalsIgnoreCase("POSITION")){
                    requestStr = requestStr.replace("[positionAccountAmount]", accountsPosition.getAmount());
                    requestStr = requestStr.replace("[positionAccountCurrency]", accountsPosition.getCurrency());


                    List<SettlementRequest.LedgerType> ledgerTypeList = accountsPosition.getLedgerType();
                    for(SettlementRequest.LedgerType ledgerType: ledgerTypeList){
                        if(ledgerType.getName().equalsIgnoreCase("PRINCIPAL_VALUE")){
                            requestStr = requestStr.replace("[principalLedgerTypeAmount]", ledgerType.getAmount());
                        }
                    }
                }

                if(accountsPosition.getAccountType().equalsIgnoreCase("FEES")){
                    requestStr = requestStr.replace("[feeAccountAmount]", accountsPosition.getAmount());
                    requestStr = requestStr.replace("[feeAccountCurrency]", accountsPosition.getCurrency());

                    String interchangeFeeAmt = "0";
                    String processingFeeAmount = "0";

                    List<SettlementRequest.LedgerType> ledgerTypeList = accountsPosition.getLedgerType();
                    for(SettlementRequest.LedgerType ledgerType: ledgerTypeList){

                        if(ledgerType.getName().equalsIgnoreCase("INTERCHANGE_FEES")){
                            interchangeFeeAmt = ledgerType.getAmount();
                        }
                        if(ledgerType.getName().equalsIgnoreCase("PROCESSING_FEE")){
                            processingFeeAmount = ledgerType.getAmount();
                        }
                    }
                    requestStr = requestStr.replace("[interchageFeeAmount]", interchangeFeeAmt);
                    requestStr = requestStr.replace("[processingFeeAmount]", processingFeeAmount);
                }
            }

            //Map<String, Object> response = fiService.sendRequest(requestStr, traceId);
            Map<String, Object> response = fiService.sendApacheRequest(requestStr, traceId);
            String responseMsg = response.get("responseMsg").toString();
            if(response.get("status").equals("00")){
                // check if the xml has errors
                if(!responseMsg.toUpperCase().contains("FIBUSINESSEXCEPTION")){
                    logger.info("{} - Successful Response ", traceId);
                    return ResponseEntity.ok().build();
                }else{
                    message = fiService.getErrorDescription(responseMsg);
                    //@todo - do more error validations, check the other codes as well
                    TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("3208", message);
                    logger.info("{} - Error Response ", traceId);
                    logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tipsErrorResponse);
                }
            }else {
                message = "An error has occurred while processing the request";
            }
        }else{
            message = "An error has occurred while decoding the request";
        }
        TipsErrorResponse tipsErrorResponse = new TipsErrorResponse("500", message);
        logger.info("{} - Error Response ", traceId);
        logger.info("{} - Response to TIPS - {}", traceId, AppUtils.convertToJson(tipsErrorResponse));
        return ResponseEntity.internalServerError().body(tipsErrorResponse);
    }

    private SettlementRequest getRequest(String body, String traceId){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, SettlementRequest.class);
        }catch (Exception exception){
            logger.error("{} - An error has occurred while decoding the request - {}", traceId, exception.getMessage());
        }
        return null;

    }

}
