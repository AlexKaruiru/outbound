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
public class Payee {

    private String identifierType;
    private String identifier;
    private String fspId;
    private String fullName;
    private String accountCategory;
    private String accountType;
    private Identity identity;

    @Getter
    @Setter
    @ToString
    public static class Identity{
        private String type;
        private String value;
    }
}