package io.metersphere.project.utils;

import io.metersphere.sdk.util.LogUtils;

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
            LogUtils.error(e);
            return new byte[0];
        }
    }

    public static byte[] listFileBytesToZip(Map<String, byte[]> mapReport) {
        try {
            if (!mapReport.isEmpty()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);
                for (Map.Entry<String, byte[]> report : mapReport.entrySet()) {
                    ZipEntry entry = new ZipEntry(report.getKey());
                    entry.setSize(report.getValue().length);
                    zos.putNextEntry(entry);
                    zos.write(report.getValue());
                }
                zos.closeEntry();
                zos.close();
                return baos.toByteArray();
            }
        } catch (Exception e) {
            return new byte[10];
        }
        return new byte[10];
    }
}
