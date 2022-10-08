package com.sonnh.coreuibe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.xml.transform.Result;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommonUtils {

    public static String camelToSnake(String str) {
        String result = "";
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result
                        = result
                        + Character.toLowerCase(ch);
            } else {
                result = result + ch;
            }
        }
        return result;
    }

    public static String convertToPostgresColumnName(String column) {
        if (StringUtils.isEmpty(column)) {
            return null;
        }

        return column.toLowerCase().replace(" ", "_");
    }

    public static Object parseJson(String json) throws Exception {
        Object result;
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        result = objectMapper.readValue(json, Object.class);
        return result;
    }

    public static Map<String, Object> responseObject(String statusCode, Object body, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("statusCode", statusCode);
        result.put("body", body);
        result.put("message", message);
        return result;
    }
}
