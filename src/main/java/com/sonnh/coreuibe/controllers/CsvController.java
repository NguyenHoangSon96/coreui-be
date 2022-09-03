package com.sonnh.coreuibe.controllers;

import com.sonnh.coreuibe.services.CsvService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/csv")
@Slf4j
public class CsvController {

    final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload-csv")
    public String uploadCsv(@RequestPart("file") MultipartFile multipartFile, @RequestPart(value = "keys") List<String> keys) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            throw new Exception("File is empty");
        }
        var tableName = multipartFile.getOriginalFilename().replace(".csv", "");
        csvService.uploadCsvFile(multipartFile, tableName, keys);
        return "";
    }

    @GetMapping("test")
    public void test() throws Exception {
        csvService.test();
    }
}
