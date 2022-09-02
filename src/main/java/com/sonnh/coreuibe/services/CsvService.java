package com.sonnh.coreuibe.services;

import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.repositories.CsvRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    public void uploadCsvFile(MultipartFile multipartFile, String tableName) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(tableName)) {
            return;
        }
//        saveCsvToTemp(multipartFile);
        var filePath = Constant.PATH_TEMP + multipartFile.getOriginalFilename();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath))) {
            var headers = getHeadersCsvFile(bufferedReader);
            if (!csvRepository.checkIfTableExist(tableName)) {
                csvRepository.createTableDynamic(tableName, headers);
            }
            csvRepository.saveColumnsMeta(tableName, headers);

            var rows = getRowsCsvFile(bufferedReader);
            csvRepository.saveCsvRows(tableName, rows, headers.keySet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getHeadersCsvFile(BufferedReader bufferedReader) throws Exception {
        var firstRow = bufferedReader.readLine();
        var headers = firstRow.split(",");

        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            if (Objects.equals(headers[i], "Pricing Logic") || Objects.equals(headers[i], "User Group (Edit)") || Objects.equals(headers[i], "User Group (View Details)")) {
                continue;
            }

            var columnName = headers[i].toLowerCase().replace(" ", "_");
            var columnDisplayName = headers[i];
            result.put(columnName, columnDisplayName);
        }
        return result;
    }

    public List<String[]> getRowsCsvFile(BufferedReader bufferedReader) throws Exception {
        List<String[]> results = new ArrayList<>();
        var row = bufferedReader.readLine();
        while (StringUtils.isNotEmpty(row)) {
            results.add(row.split(","));
            row = bufferedReader.readLine();
        }

        return results;
    }

    public void test() throws Exception {
//        csvRepository.saveCsvRows(List.of(), );
    }
}
