package com.sonnh.coreuibe.services;

import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.repositories.CsvRepository;
import com.sonnh.coreuibe.repositories.IPriceFx;
import com.sonnh.coreuibe.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

@Service
@Transactional
@Slf4j
public class CsvService {

    final CsvRepository csvRepository;

    final IPriceFx iPriceFx;


    @Autowired
    public CsvService(CsvRepository csvRepository, IPriceFx iPriceFx) {

        this.csvRepository = csvRepository;
        this.iPriceFx = iPriceFx;
    }

    public void saveCsvToTemp(MultipartFile multipartFile) throws Exception {
        if (multipartFile == null) {
            throw new Exception("File is empty");
        }
        var file = new File(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes(), false);
    }

    public void uploadCsvFile(MultipartFile multipartFile, String tableName, List<String> keys) throws Exception {
        if (multipartFile == null || StringUtils.isEmpty(tableName)) {
            return;
        }
        tableName = CommonUtils.camelToSnake(tableName);
        saveCsvToTemp(multipartFile);

        var filePath = Constant.PATH_TEMP + multipartFile.getOriginalFilename();
        List<String> lines = FileUtils.readLines(FileUtils.getFile(filePath), Charset.defaultCharset());
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }

        var headers = Arrays.stream(lines.get(0).split(",")).toList();
        List<String> duplicateHeaders = findDuplicateHeaders(headers);
        if (CollectionUtils.isNotEmpty(duplicateHeaders)) {
            throw new Exception("Duplicate header " + duplicateHeaders);
        }

        for (String header : headers) {
            csvRepository.upsertColumnMeta(tableName, header);
        }

        var rows = lines.subList(1, lines.size() - 1).stream().map(row -> Arrays.stream(row.split(",", -1)).toList()).toList();
        List<Map<String, Object>> rowMaps = new ArrayList<>();
        for (List<String> row : rows) {
            var rowMap = convertCsvRowToMap(headers, row);
            rowMaps.add(rowMap);
        }

        boolean isTableExist = csvRepository.isTableExist(tableName);
        if (!isTableExist) {
            csvRepository.createTableDynamic(tableName, headers);
        }

        for (Map<String, Object> row : rowMaps) {
            csvRepository.upsertRow(tableName, keys, row);
        }


    }

    public Map<String, Object> convertCsvRowToMap(List<String> headers, List<String> rows) throws Exception {
        HashMap<String, Object> results = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            results.put(headers.get(i), rows.get(i));
        }
        return results;
    }

    public List<String> getColumnCsvFile(List<String> headers) throws Exception {
        if (CollectionUtils.isEmpty(headers)) {
            return null;
        }

        List<String> results = new ArrayList<>();
        for (String header : headers) {
            if (Objects.equals(header, "Pricing Logic") || Objects.equals(header, "User Group (Edit)") || Objects.equals(header, "User Group (View Details)")) {
                continue;
            }
            var columnName = header.toLowerCase().replace(" ", "_").replace("?", "");
            results.add(columnName);
        }
        return results;
    }

    public void test() throws Exception {
//        var uri = "/fetch";
//        List<TypeCodeModel> typeCodes = (List<TypeCodeModel>) PricefxClient.post(uri, null);
//        TypeCodeModel typeCodeModel = new TypeCodeModel();
//
//        typeCodeModel.setCodeString("a");
//        typeCodeModel.setMassEditable(false);
//        typeCodeModel.setName("b");
//
//        iPriceFx.save(typeCodeModel);

//        iPriceFx.saveAll(typeCodes);
//        new PfxClient()


    }

    public List<String> findDuplicateHeaders(List<String> headers) throws Exception {
        List<String> results = new ArrayList<>();
        HashSet<Object> set = new HashSet<>();
        headers.forEach(header -> {
            if (!set.add(header)) {
                results.add(header);
            }
        });
        return results;
    }
}
