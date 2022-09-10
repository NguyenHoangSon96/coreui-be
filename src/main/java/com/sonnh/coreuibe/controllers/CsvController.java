package com.sonnh.coreuibe.controllers;

import com.sonnh.coreuibe.services.CsvService;
import com.sonnh.coreuibe.utils.PricefxClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/csv")
@CrossOrigin(origins = "*")
@Slf4j
public class CsvController {

    final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping(value = "/upload-csv", consumes = "multipart/form-data")
    public String uploadCsv(@RequestPart("file") MultipartFile multipartFile, @RequestPart(value = "keys") List<String> keys) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename()) || CollectionUtils.isEmpty(keys)) {
            throw new Exception("File is empty");
        }
        var tableName = multipartFile.getOriginalFilename().replace(".csv", "");
        csvService.uploadCsvFile(multipartFile, tableName, keys);
        return "";
    }

    @GetMapping("/test")
    public void test() throws Exception {
        csvService.test();

    }
}
