package com.sonnh.coreuibe.services;

import com.opencsv.CSVReader;
import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.repositories.CsvRepository;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional
public class CsvService {

    final CsvRepository csvRepository;


    @Autowired
    public CsvService(CsvRepository csvRepository) {

        this.csvRepository = csvRepository;
    }

    public void saveCsvToTemp(MultipartFile multipartFile) throws Exception {
        if (multipartFile == null) {
            throw new Exception("File is empty");
        }

        var file = new File(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        var fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
    }

    public void uploadCsvFile(MultipartFile multipartFile, String tableName) {
        if (multipartFile == null || StringUtils.isEmpty(tableName)) {
            return;
        }
        var filePath = Constant.PATH_TEMP + multipartFile.getOriginalFilename();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath))) {
            saveCsvToTemp(multipartFile);
            var headers = getHeadersCsvFile(bufferedReader);
            var rows = getRowsCsvFile(bufferedReader);
            csvRepository.createTableDynamic(tableName, headers);
            csvRepository.saveColumnsMeta(tableName, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getHeadersCsvFile(BufferedReader bufferedReader) throws Exception {
        var firstRow = bufferedReader.readLine();
        var headers = firstRow.split(",");

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            var columnName = headers[i].toLowerCase().replace(" ", "_");
            var columnDisplayName = headers[i];
            result.put(columnName, columnDisplayName);
        }
        return result;
    }

    public List<String[]> getRowsCsvFile(BufferedReader bufferedReader) throws Exception {
        List<String[]> results = new ArrayList<>();
        while ((bufferedReader.read()) != -1) {
            var row = bufferedReader.readLine().split(",");
            results.add(row);
        }

        return results;
    }
}
