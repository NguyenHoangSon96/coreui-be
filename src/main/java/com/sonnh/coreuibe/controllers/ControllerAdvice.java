package com.sonnh.coreuibe.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<String> generalExceptionHandler(RuntimeException re, WebRequest wr) {
        re.printStackTrace();
        return ResponseEntity.internalServerError().body("Something went wrong");
    }
}
