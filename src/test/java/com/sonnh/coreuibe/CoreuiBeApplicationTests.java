package com.sonnh.coreuibe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.services.CsvService;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@SpringBootTest
class CoreuiBeApplicationTests {

    @Autowired
    CsvService csvService;

    @Test
    void contextLoads() {

    }

    @Test
    void testGetColumnCsvFile() throws Exception {
        var columns = csvService.getColumnCsvFile(List.of("Product Id", "customer", "Invoice Date", ""));
        Assertions.assertArrayEquals(List.of("product_id", "customer", "invoice_date", "").toArray(), columns.toArray());
    }

    @Test
    void testGetColumnCsvFileFail() throws Exception {
        List<String> columns = List.of();
        Assertions.assertNull(csvService.getColumnCsvFile(columns));

        Assertions.assertNull(csvService.getColumnCsvFile(null));
    }

    @Test
    void testUrl() throws IOException {
        String authorize = String.format("Basic %s", Base64.getEncoder().encodeToString("ce-0262/admin:SXK6HYZN3Lc7".getBytes()));
        var uri = "/datamart.getfcs/DM";
        URL url = new URL(Constant.BASE_URL + "/ce-02623" + uri);
        Request request = new Request.Builder().url(url)
                                               .header("Content-Type", "application/json")
                                               .header("Authorization", authorize)
                                               .post(RequestBody.create("".getBytes()))
                                               .build();
        OkHttpClient okHttpClient = new OkHttpClient();

        Response response = okHttpClient.newCall(request).execute();
        Map responseMap = (Map) new ObjectMapper().readValue(Objects.requireNonNull(response.body()).string(), Map.class).get("response");
        var data = responseMap.get("data");
        if (ObjectUtils.isNotEmpty(data) && data instanceof Collection<?>) {
            System.out.println(data);
        }
        response.close();
    }

}
