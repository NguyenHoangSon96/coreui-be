package com.sonnh.coreuibe.utils;

import com.sonnh.coreuibe.configs.Constant;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class FileUploadUtils {

    public static void saveCsvToTemp(MultipartFile multipartFile) throws Exception {
        if (StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            return;
        }

        var file = new File(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes(), false);
    }

    public static void saveExcelToTemp(MultipartFile multipartFile) throws Exception {
        if (StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
            return;
        }

        File file = FileUtils.getFile(Constant.PATH_TEMP + multipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes(), false);
    }
}
