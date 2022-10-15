package com.sonnh.coreuibe.exceptions;


import com.sonnh.coreuibe.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExeptionHandler {

    @ExceptionHandler(value = {BussinessException.class})
    protected ResponseEntity<Map<String, Object>> bussinessExceptionHandler(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        var body = CommonUtils.responseObject("10", null, e.getMessage());
        return ResponseEntity.ok(body);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Map<String, Object>> generalExceptionHandler(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        var body = CommonUtils.responseObject("99", null, "Something when wrongs");
        return ResponseEntity.ok(body);
    }
}

