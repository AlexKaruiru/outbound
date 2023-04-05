/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TransferRequest {

    private String payerRef;
    private Payee payer;
    private Payee payee;
    private Amount amount;
    private Amount endUserFee;
    private TransactionType transactionType;
    private String description;

    @Setter
    @Getter
    @ToString
    public static class Amount{
        private String amount;
        private String currency;
    }

    @Setter
    @Getter
    @ToString
    public static class TransactionType{
        private String scenario;
        private String initiator;
        private String initiatorType;
    }


}
