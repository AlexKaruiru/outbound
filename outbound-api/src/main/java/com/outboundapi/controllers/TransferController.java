/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.controllers;

import com.outboundapi.models.TipsErrorResponse;
import com.outboundapi.models.TransferConfirmationRequest;
import com.outboundapi.models.TransferRequest;
import com.outboundapi.services.TransferService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping(path = "/transfers/{payerRef}")
    public ResponseEntity<?> inboundInquiry(@RequestHeader Map<String, String> headers, @PathVariable String payerRef){
        return transferService.inboundInquiry(headers, payerRef);
    }

    @PutMapping(path = "/transfers/{payerRef}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundTransferConfirmation(@RequestHeader Map<String, String> headers, @RequestBody String body, @PathVariable String payerRef){
        return transferService.outboundTransferConfirmation(headers, body, payerRef);
    }

    @PostMapping(path = "/transfers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> inboundTransfer(@RequestHeader Map<String, String> headers, @RequestBody String body){
        return transferService.inboundTransfer(headers, body);
    }

    @PutMapping(path = "/transfers/{payerRef}/error", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> outboundTransferCallbackError(@RequestHeader Map<String, String> headers, @RequestBody TipsErrorResponse request,  @PathVariable String payerRef){
        return transferService.outboundTransferCallbackError(headers, request, payerRef);
    }

}
