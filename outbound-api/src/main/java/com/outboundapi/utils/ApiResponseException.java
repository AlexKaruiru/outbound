/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import com.outboundapi.models.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiResponseException extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiError> handleExceptions(Exception e) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value(); // 500
        return new ResponseEntity<>(
                new ApiError(
                        status,
                        "An error has occurred",
                        e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpClientErrorException.UnsupportedMediaType.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaTypeError(Exception e) {
        int status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        return new ResponseEntity<>(
                new ApiError(
                        status,
                        "Unsupported Media Type",
                        e.getMessage()
                ),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(value = HttpClientErrorException.NotFound.class)
    public ResponseEntity<ApiError> handleNotFoundError(Exception e) {
        int status = HttpStatus.NOT_FOUND.value();
        return new ResponseEntity<>(
                new ApiError(
                        status,
                        "Endpoint Not Found",
                        e.getMessage()
                ),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = HttpClientErrorException.MethodNotAllowed.class)
    public ResponseEntity<ApiError> handleMethodNotAllowedError(Exception e) {
        int status = HttpStatus.METHOD_NOT_ALLOWED.value();
        return new ResponseEntity<>(
                new ApiError(
                        status,
                        "Method Not Allowed",
                        e.getMessage()
                ),
                HttpStatus.METHOD_NOT_ALLOWED);
    }
}
