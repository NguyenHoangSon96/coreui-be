package com.sonnh.coreuibe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnh.coreuibe.services.CsvService;
import com.sonnh.coreuibe.utils.PricefxClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 4800, allowCredentials = "false")
@RestController
@RequestMapping("/csv")
@Slf4j
public class CsvController {

    final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping(value = "/upload-csv")
    public String uploadCsv(@RequestPart("file") MultipartFile multipartFile,
                            @RequestPart("data") String dataJson) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            throw new Exception("File is empty");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(dataJson, Map.class);
        List<String> keys = (List<String>) data.get("keys");

        var tableName = ((String) data.get("fileName")).replace(".csv", "");
        csvService.uploadCsvFile(multipartFile, tableName, keys);
        return "";

    }

    @GetMapping("/bing")
    public String test() throws Exception {
        return "pong";

    }
}
