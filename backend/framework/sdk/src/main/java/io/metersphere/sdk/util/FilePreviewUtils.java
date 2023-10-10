package io.metersphere.sdk.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class FilePreviewUtils {
    private static final String BASE_FILE_FOLDER = File.separator + "opt" + File.separator + "metersphere" + File.separator + "data" + File.separator + "file" + File.separator + "preview" + File.separator;

    public static boolean isImage(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "jpg", "jpeg", "png", "gif", "bmp", "svg", "ico");
    }

    //生成执行文件的绝对路径
    public static String getFileAbsolutePath(String fileName) {
        return BASE_FILE_FOLDER + fileName;
    }

    public static File getFile(String fileName) {
        File file = new File(getFileAbsolutePath(fileName));
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    public static String catchFileIfNotExists(String fileName, byte[] fileBytes) {
        if (getFile(fileName) == null && fileBytes != null) {
            createFile(getFileAbsolutePath(fileName), fileBytes);
        }
        return getFileAbsolutePath(fileName);
    }

    public static void deleteFile(String deleteFileName) {
        File file = new File(getFileAbsolutePath(deleteFileName));
        if (file.exists()) {
            file.delete();
        }
    }

    private static void createFile(String filePath, byte[] fileBytes) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            LogUtils.error(e);
        }

        try (InputStream in = new ByteArrayInputStream(fileBytes); OutputStream out = new FileOutputStream(file)) {
            final int MAX = 4096;
            byte[] buf = new byte[MAX];
            for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
                out.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            LogUtils.error(e);
        }
    }
}
