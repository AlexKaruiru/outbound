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
public class ReversalRequest {

    private String payerReversalRef;
    private String payerRef;
    private String payeeRef;
    private String switchRef;
    private Amount amount;
    private String reversalReason;

    @Setter
    @Getter
    @ToString
    public static class Amount{
        private String amount;
        private String currency;
    }
}
