/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TipsErrorResponse {

    ErrorInformation errorInformation;

    public TipsErrorResponse() {
    }

    public TipsErrorResponse(ErrorInformation errorInformation) {
        this.errorInformation = errorInformation;
    }

    public TipsErrorResponse(String errorCode, String errorDescription) {
        this.errorInformation = new ErrorInformation(errorCode, errorDescription);
    }

    @Getter
    @Setter
    @ToString
    public static class ErrorInformation{
        private String errorCode;
        private String errorDescription;

        public ErrorInformation() {
        }

        public ErrorInformation(String errorCode, String errorDescription) {
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }
    }
}
