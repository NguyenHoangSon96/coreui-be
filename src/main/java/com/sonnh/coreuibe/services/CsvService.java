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

    public void uploadCsvFile(MultipartFile multipartFile, String tableName, List<String> keys) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(tableName)) {
            return;
        }
        saveCsvToTemp(multipartFile);
        var filePath = Constant.PATH_TEMP + multipartFile.getOriginalFilename();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath))) {
            var headers = bufferedReader.readLine();
            var columnNames = getColumnCsvFile(headers);
            var columnDisplayNames = headers.split(",");

            if (!csvRepository.checkIsTableExist(tableName)) {
                csvRepository.createTableDynamic(tableName, columnNames);
            }
            csvRepository.saveColumnsMeta(tableName, columnNames, Arrays.stream(columnDisplayNames).toList());

            List<String[]> rows = new ArrayList<>();
            for (var row = bufferedReader.readLine(); StringUtils.isNotEmpty(row); row = bufferedReader.readLine()) {
                rows.add(row.split(","));
            }

            var rowMaps = convertRowToMap(columnNames.stream().toList(), rows);
            for (var row : rowMaps) {
                csvRepository.saveOrUpdateCsvRow(tableName, keys, row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getColumnCsvFile(String firstRow) throws Exception {
        Objects.requireNonNull(firstRow);

        var headers = firstRow.split(",");
        List<String> results = new ArrayList<>();
        for (String header : headers) {
            if (Objects.equals(header, "Pricing Logic") || Objects.equals(header, "User Group (Edit)") || Objects.equals(header, "User Group (View Details)")) {
                continue;
            }
            var columnName = header.toLowerCase().replace(" ", "_");
            results.add(columnName);
        }
        return results;
    }

    public List<Map<String, String>> convertRowToMap(List<String> columnNames, List<String[]> rows) {
        List<Map<String, String>> results = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            Map<String, String> map = new LinkedHashMap<>();
            for (int j = 0; j < columnNames.size(); j++) {
                map.put(columnNames.get(j), row[j]);
            }
            results.add(map);
        }
        return results;
    }

    public void test() throws Exception {
//        csvRepository.saveCsvRows(List.of(), );
    }

}
