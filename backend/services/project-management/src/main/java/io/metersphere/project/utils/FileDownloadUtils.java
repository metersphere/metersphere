package io.metersphere.project.utils;

import io.metersphere.sdk.util.LogUtils;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileDownloadUtils {

    public static void zipFilesWithResponse(Map<String, File> fileMap, HttpServletResponse response) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (Map.Entry<String, File> fileEntry : fileMap.entrySet()) {
                String fileName = fileEntry.getKey();
                File file = fileEntry.getValue();
                if (file.exists()) {
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {

                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOutputStream.putNextEntry(zipEntry);
                        byte[] buffer = new byte[512];
                        int num;
                        while ((num = bis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, num);
                        }
                        zipOutputStream.closeEntry();
                    } catch (Exception ignore) {
                    }
                }
            }

            response.setContentType("application/zip");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename=files.zip");
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    public static void zipFilesWithResponse(String fileName, InputStream fileInputStream, HttpServletResponse response) {

        try (OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[512];
            int num;
            while ((num = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, num);
            }
            outputStream.close();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        } catch (Exception e) {
            LogUtils.error(e);
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
