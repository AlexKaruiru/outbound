/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SettlementRequest {

    private SettlementWindow settlementWindow;
    private List<TransactionsList> outgoingTransactions;
    private List<TransactionsList> incomingTransactions;
    private List<TransactionsList> netTransactions;
    private List<AccountsPosition> accountsPosition;

    @Setter
    @Getter
    @ToString
    public static class SettlementWindow{
        private String id;
        private String closingDate;
        private String openingDate;
        private String description;
    }

    @Setter
    @Getter
    @ToString
    public static class TransactionsList{
        private String currency;
        private String volume;
        private String value;
    }

    @Setter
    @Getter
    @ToString
    public static class AccountsPosition{
        private String accountType;
        private String currency;
        private String amount;
        private List<LedgerType> ledgerType;
    }

    @Setter
    @Getter
    @ToString
    public static class LedgerType{
        private String name;
        private String amount;
    }

}
