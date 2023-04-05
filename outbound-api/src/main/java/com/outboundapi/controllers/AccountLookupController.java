/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.controllers;


import com.outboundapi.models.Payee;
import com.outboundapi.models.TipsErrorResponse;
import com.outboundapi.services.AccountLookupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account-lookup")
public class AccountLookupController {

    private final AccountLookupService accountLookupService;

    public AccountLookupController(AccountLookupService accountLookupService) {
        this.accountLookupService = accountLookupService;
    }

    @GetMapping(path = "/{identifierType}/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundSync(@RequestHeader Map<String, String> headers, @PathVariable String identifierType, @PathVariable String identifier){
        return accountLookupService.outboundSync(headers, identifierType, identifier);
    }
}
