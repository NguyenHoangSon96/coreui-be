package com.sonnh.coreuibe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnh.coreuibe.configs.Constant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

@Slf4j
public final class PricefxClient {

    private static OkHttpClient okHttpClient;
    private static String baseUrl;

    private static ObjectMapper objectMapper;

    private PricefxClient() {
    }

    static {
        okHttpClient = new OkHttpClient();
        baseUrl = Constant.BASE_URL + String.format("/%s/", Constant.PARTITION_CE);
        objectMapper = new ObjectMapper();
    }

    static void get(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return;
        }

        String url = baseUrl + uri;
//        Request request = new Request.Builder().url(URL.)


    }

    public static List<Map<String, Object>> post(String uri, Map<String, Object> payload) throws Exception {
        String userName = String.format("%s/%s:%s", Constant.PARTITION_CE, Constant.USER_NAME, Constant.PASSWORD_CE);
        String authorize = String.format("Basic %s", Base64.getEncoder().encodeToString(userName.getBytes()));
        URL url = new URL(String.format("%s/%s%s", Constant.BASE_URL, Constant.PARTITION_CE, uri));
        Request request = new Request.Builder().url(url)
                                               .header("Content-Type", "application/json")
                                               .header("Authorization", authorize)
                                               .post(RequestBody.create("".getBytes()))
                                               .build();
        Response response = okHttpClient.newCall(request).execute();

        String responseStr = Objects.requireNonNull(response.body()).string();
        List<Map<String, Object>> datas = (List<Map<String, Object>>) ((Map) objectMapper.readValue(responseStr, Map.class).get("response")).get("data");
        //todo log with function name
        log.info("Url: " + url);
        log.info("Response: " + responseStr);
        log.info("Data: " + objectMapper.writeValueAsString(datas));

        if (CollectionUtils.isEmpty(datas)) {
            return List.of();
        }
        response.close();
        return datas;
    }
}
