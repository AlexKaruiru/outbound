/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.controllers;


import com.outboundapi.models.ReversalCallbackRequest;
import com.outboundapi.models.ReversalConfirmationRequest;
import com.outboundapi.models.ReversalRequest;
import com.outboundapi.models.TransferRequest;
import com.outboundapi.services.ReversalService;
import com.outboundapi.services.TransferService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ReversalController {

    private final ReversalService reversalService;

    public ReversalController(ReversalService reversalService) {
        this.reversalService = reversalService;
    }

    @PostMapping(path = "/reversal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> inboundReversal(@RequestHeader Map<String, String> headers, @RequestBody ReversalRequest request){
        return reversalService.inboundReversal(headers, request);
    }

    @PutMapping(path = "/reversal/{payerRef}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundReversalCallback(@RequestHeader Map<String, String> headers, @RequestBody ReversalCallbackRequest request, @PathVariable String payerRef){
        return reversalService.outboundReversalCallback(headers, request, payerRef);
    }

    @PutMapping(path = "/transfersReversal/{payerRef}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transferReversalConfirmation(@RequestHeader Map<String, String> headers, @RequestBody ReversalConfirmationRequest request, @PathVariable String payerRef){
        return reversalService.transferReversalConfirmation(headers, request, payerRef);
    }
}
