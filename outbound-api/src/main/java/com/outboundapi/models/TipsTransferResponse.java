/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.models;


import com.outboundapi.utils.AppUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TipsTransferResponse {

    private String payerRef;
    private String status;
    @Builder.Default
    private String datetime = AppUtils.getDate("YYYY-MM-dd HH:mm:ss");

    public TipsTransferResponse() {
    }

    public TipsTransferResponse(String payerRef, String status) {
        this.payerRef = payerRef;
        this.status = status;
        this.datetime = AppUtils.getDate("YYYY-MM-dd HH:m:ss");
    }
}
