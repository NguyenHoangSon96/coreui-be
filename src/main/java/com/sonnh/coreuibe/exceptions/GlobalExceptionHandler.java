package com.sonnh.coreuibe.exceptions;


import com.sonnh.coreuibe.utils.CommonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Map<String, Object>> generalExceptionHandler(Exception e, HttpServletRequest re) {
        Map<String, Object> result = CommonUtils.responseObject("99", null, "Something went wrong");
        return ResponseEntity.ok(result);
    }
}
