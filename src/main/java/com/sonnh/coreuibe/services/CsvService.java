package com.sonnh.coreuibe.services;

import com.opencsv.CSVReader;
import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.repositories.CsvRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Service
@Transactional
public class CsvService {

    final CsvRepository csvRepositories;


    @Autowired
    public CsvService(CsvRepository csvRepositories) {

        this.csvRepositories = csvRepositories;
    }

    public void saveCsvTemp(MultipartFile multipartFile) throws Exception {
        if (multipartFile == null) {
            throw new Exception("File is empty");
        }

        var file = new File(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        var fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
    }

    public void createTableDynamic(String tableName, Map<String, String> columnMap) throws Exception {
        csvRepositories.createTableDynamic(tableName, columnMap);
    }

    public void uploadCsvFile(MultipartFile multipartFile, String tableName) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(tableName)) {
            throw new Exception("Null");
        }

        // Save csv to temp folder
        saveCsvTemp(multipartFile);

        File file = new File(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
        CSVReader csvReader = new CSVReader(bufferedReader);
        var a = csvReader.readNext();
        System.out.println(Arrays.toString(a));
//        List<String[]> rows = getRowsCsvFile(csvReader);


//        csvReader.readNext();
//        Map<String, String> columnMap = getColumnsCsvFile(csvReader);
//
//        createTableDynamic(tableName, columnMap);
//        saveColumnsMeta(tableName, columnMap);

//        List<Map<String, String>> rows = readCsvFile(headers, csvReader);
    }

    private void saveColumnsMeta(String tableName,  Map<String, String> columnMap) throws Exception {
        csvRepositories.saveColumnsMeta(tableName, columnMap);
    }

    public Map<String, String> getColumnsCsvFile(CSVReader csvReader) throws Exception {
        var firstRow = csvReader.readNext();

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < firstRow.length; i++) {
            var columnName = firstRow[i].toLowerCase().replace(" ", "_");
            var columnDisplayName = firstRow[i];
            result.put(columnName, columnDisplayName);
        }
        return result;
    }

    public List<String[]> getRowsCsvFile(CSVReader csvReader) throws Exception {
        List<String[]> results = new ArrayList<>();
        while (csvReader.iterator().hasNext()) {
            var row = csvReader.readNext();
            results.add(row);
        }
        return results;
    }
}
