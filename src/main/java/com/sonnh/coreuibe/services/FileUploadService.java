package com.sonnh.coreuibe.services;

import com.sonnh.coreuibe.configs.Constant;
import com.sonnh.coreuibe.exceptions.BussinessException;
import com.sonnh.coreuibe.repositories.CsvRepository;
import com.sonnh.coreuibe.repositories.IPriceFx;
import com.sonnh.coreuibe.utils.CommonUtils;
import com.sonnh.coreuibe.utils.FileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import net.pricefx.restapiclient.calls.ApiManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.nio.charset.Charset;
import java.util.*;

@Service
@Transactional
@Slf4j
public class FileUploadService {

    final CsvRepository csvRepository;

    final IPriceFx iPriceFx;

    @Autowired
    public FileUploadService(CsvRepository csvRepository, IPriceFx iPriceFx) {
        this.csvRepository = csvRepository;
        this.iPriceFx = iPriceFx;
    }

    public void uploadCsvFile(MultipartFile multipartFile, List<String> keys) throws Exception {
        var tableName = ((String) Objects.requireNonNull(multipartFile.getOriginalFilename()).replace(".csv", ""));
        tableName = CommonUtils.camelToSnake(tableName);
        FileUploadUtils.saveCsvToTemp(multipartFile);

        var filePath = Constant.PATH_TEMP + multipartFile.getOriginalFilename();
        List<String> lines = FileUtils.readLines(FileUtils.getFile(filePath), Charset.defaultCharset());
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }

//        var headers = Arrays.stream(lines.get(0).replace("?", "").split(",")).toList();
        var rawHeaders = Arrays.stream(lines.get(0)
                                            .replace("(", "")
                                            .replace(")", "")
                                            .replace("?", "")
                                            .split(",")
                                      )
                               .toList();
        List<String> duplicateHeaders = findDuplicateHeaders(rawHeaders);
        List<String> headers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(duplicateHeaders)) {
            for (String dupHeader : duplicateHeaders) {
                for (int i = 0; i < rawHeaders.size(); i++) {
                    var rawHeader = rawHeaders.get(i);
                    if (Objects.equals(rawHeaders.get(i), dupHeader)) {
                        headers.add(rawHeader + i);
                    } else {
                        headers.add(rawHeader);
                    }
                }
            }
        } else {
            headers = rawHeaders;
        }

        for (String header : headers) {
            csvRepository.upsertColumnMeta(tableName, header, keys);
        }

        var rows = lines.subList(1, lines.size()).stream().map(row -> Arrays.stream(row.split(",", -1)).toList()).toList();
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

    public void uploadExcelFile(MultipartFile multipartFile, List<String> keys) throws Exception {
        var fileName = multipartFile.getOriginalFilename();
        if (StringUtils.isEmpty(fileName) || CollectionUtils.isEmpty(keys) || !fileName.endsWith(".xlsx")) {
            throw new BussinessException("Invalid params");
        }

        FileUploadUtils.saveExcelToTemp(multipartFile);
        Workbook workbook = new XSSFWorkbook(FileUtils.getFile(Constant.PATH_TEMP + fileName));
        Sheet sheet = workbook.getSheetAt(0);
        Row firstRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        Iterator<Cell> headerCellIterator = firstRow.cellIterator();
        while (headerCellIterator.hasNext()) {
            Cell cell = headerCellIterator.next();
            headers.add(cell.getStringCellValue());
            log.info("header cell " + cell.getStringCellValue());
        }


        Iterator<Row> rowIterator = sheet.rowIterator();
        ArrayList<Object> results = new ArrayList<>();
        var rowIndex = 0;
        while (rowIterator.hasNext()) {
            if (rowIndex > 0) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                LinkedHashMap<Object, Object> rowMap = new LinkedHashMap<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    var cellValue = switch (cell.getCellType()) {
                        case STRING -> {
                            yield cell.getStringCellValue();
                        }
                        case NUMERIC -> {
                            yield cell.getNumericCellValue();
                        }
                        case BOOLEAN -> {
                            yield cell.getBooleanCellValue();
                        }
                        case BLANK -> {
                            yield "";
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + cell.getCellType());
                    };
                    int columnIndex = cell.getColumnIndex();
                    rowMap.put(headers.get(columnIndex), cellValue);
                    log.info("Row " + cellValue);
                }
                results.add(rowMap);
//                System.out.println(rowMap);
            }
            rowIndex++;
        }

        //todo sua loi con add header
        System.out.println(results);
        workbook.close();

    }

    public Map<String, Object> convertCsvRowToMap(List<String> headers, List<String> rows) throws Exception {
        HashMap<String, Object> results = new LinkedHashMap<>();
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
