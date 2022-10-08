package com.sonnh.coreuibe.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnh.coreuibe.services.CsvService;
import com.sonnh.coreuibe.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Map<String, Object> uploadCsv(@RequestParam("file") MultipartFile multipartFile,
                                         @RequestParam("keys") String keysStr) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            throw new Exception("File is empty");
        }

        List<String> keys = (List<String>) CommonUtils.parseJson(keysStr);
        var tableName = ((String) multipartFile.getOriginalFilename().replace(".csv", ""));
        csvService.uploadCsvFile(multipartFile, tableName, keys);
        return CommonUtils.responseObject("00", null, "Success");
    }

    @GetMapping("/bing")
    public String test() throws Exception {
        return "pong";

    }
}
