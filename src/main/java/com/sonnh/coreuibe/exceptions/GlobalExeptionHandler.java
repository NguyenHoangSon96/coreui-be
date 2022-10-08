package com.sonnh.coreuibe.exceptions;


import com.sonnh.coreuibe.utils.CommonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ControllerAdvice
public class GlobalExeptionHandler {

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Map<String, Object>> generalExceptionHandler(Exception e, HttpServletRequest request) {
        var body = CommonUtils.responseObject("99", null, "Something when wrong");
        return ResponseEntity.ok(body);
    }
}
