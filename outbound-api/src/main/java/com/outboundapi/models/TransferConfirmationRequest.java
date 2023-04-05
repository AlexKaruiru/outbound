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
public class TransferConfirmationRequest {

    private String payeeRef;
    private String switchRef;
    private String completedTimestamp;
    private String transferState;
    private String settlementWindowId;
    private Payee payer;
    private Payee payee;

}