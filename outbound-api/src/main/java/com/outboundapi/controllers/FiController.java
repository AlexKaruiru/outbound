/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.controllers;


import com.outboundapi.services.FiService;
import com.outboundapi.services.TokenService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FiController {

    private final FiService fiService;

    public FiController(FiService fiService) {
        this.fiService = fiService;
    }

    @PostMapping(path = "/fi/post", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<?> post(@RequestHeader Map<String, String> headers, @RequestBody String body){
        return fiService.post(headers, body);
    }
}
