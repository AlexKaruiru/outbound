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
public class ReversalConfirmationRequest {

    private String switchRef;
    private String payeeRef;
    private String switchReversalRef;
    private String payerReversalRef;
    private String payeeReversalRef;
    private String reversalState;
    private String completedTimestamp;
    private String settlementWindowId;
}
