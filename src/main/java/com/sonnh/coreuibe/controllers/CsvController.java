package com.sonnh.coreuibe.controllers;

import com.sonnh.coreuibe.services.CsvService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/csv")
//@CrossOrigin("*")
public class CsvController {

    final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload-csv")
    public String uploadCsv(@RequestParam("file") MultipartFile multipartFile) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            throw new Exception("File is empty");
        }

        var tableName = multipartFile.getOriginalFilename().replace(".csv", "");
        csvService.uploadCsvFile(multipartFile, tableName);
        return "";
    }

    @GetMapping("test")
    public void test() throws Exception{
        csvService.test();
    }
}
