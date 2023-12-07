package io.metersphere.project.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileDownloadUtils {

    public static byte[] listBytesToZip(Map<String, File> fileMap) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (Map.Entry<String, File> fileEntry : fileMap.entrySet()) {
                String fileName = fileEntry.getKey();
                File file = fileEntry.getValue();
                if (file.exists()) {
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOutputStream.putNextEntry(zipEntry);
                        byte[] buffer = new byte[1024];
                        int num;
                        while ((num = bis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, num);
                        }
                        zipOutputStream.closeEntry();
                    } catch (Exception ignore) {
                    }
                }
            }
            zipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
