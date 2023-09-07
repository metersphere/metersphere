package io.metersphere.project.utils;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileDownloadUtils {
    public static byte[] listBytesToZip(Map<String, byte[]> mapReport) {
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
