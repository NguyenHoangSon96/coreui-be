package com.sonnh.coreuibe.controllers;

import com.sonnh.coreuibe.exceptions.BussinessException;
import com.sonnh.coreuibe.services.FileUploadService;
import com.sonnh.coreuibe.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin(origins = "*", maxAge = 4800, allowCredentials = "false")
@RestController
@RequestMapping("/upload-file")
@Slf4j
public class CsvController {

    final FileUploadService fileUploadService;

    public CsvController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/csv")
    public Map<String, Object> uploadCsv(@RequestParam("file") MultipartFile multipartFile,
                                         @RequestParam("keys") String keysStr) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            throw new Exception("File or keys is empty");
        }

        List<String> keys = (List<String>) CommonUtils.parseJson(keysStr);
        fileUploadService.uploadCsvFile(multipartFile, keys);
        return CommonUtils.responseObject("00", null, "Success");
    }

    @PostMapping(value = "/excel")
    public Map<String, Object> uploadExcel(@RequestParam("file") MultipartFile multipartFile,
                                           @RequestParam("keys") List<String> keys) throws Exception {
        fileUploadService.uploadExcelFile(multipartFile, keys);
        return CommonUtils.responseObject("00", null, "Success");
    }


    @GetMapping("/bing")
    public String test() throws Exception {
        return "pong";

    }
}
