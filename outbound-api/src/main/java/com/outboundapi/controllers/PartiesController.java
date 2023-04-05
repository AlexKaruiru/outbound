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
@RequestMapping("/api/v1/parties")
public class PartiesController {

    private final AccountLookupService accountLookupService;

    public PartiesController(AccountLookupService accountLookupService) {
        this.accountLookupService = accountLookupService;
    }

    @GetMapping(path = "/{identifierType}/{identifier}/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> inboundSync(@RequestHeader Map<String, String> headers, @PathVariable String identifierType, @PathVariable String identifier){
        return accountLookupService.inboundSync(headers, identifierType, identifier);
    }

    @GetMapping(path = "/{identifierType}/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> inboundAsync(@RequestHeader Map<String, String> headers, @PathVariable String identifierType, @PathVariable String identifier){
        return accountLookupService.inboundAsync(headers, identifierType, identifier);
    }

    @PutMapping(path = "/{identifierType}/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundCallback(@RequestHeader Map<String, String> headers, @RequestBody Payee request, @PathVariable String identifierType, @PathVariable String identifier){
        return accountLookupService.outboundCallback(headers, request, identifierType, identifier);
    }

    @PutMapping(path = "/{identifierType}/{identifier}/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundErrorCallback(@RequestHeader Map<String, String> headers, @RequestBody TipsErrorResponse request, @PathVariable String identifierType, @PathVariable String identifier){
        return accountLookupService.outboundErrorCallback(headers, request, identifierType, identifier);
    }
}
