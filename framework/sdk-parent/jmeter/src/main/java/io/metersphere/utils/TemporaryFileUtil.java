package io.metersphere.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TemporaryFileUtil {
    public final String fileFolder;

    public static final String NODE_FILE_FOLDER = "node";
    public static final String MS_FILE_FOLDER = "ms";

    public static final String DEFAULT_FILE_FOLDER = "default";

    public TemporaryFileUtil(String folder) {
        if (StringUtils.isBlank(folder)) {
            folder = DEFAULT_FILE_FOLDER;
        }
        this.fileFolder = File.separator + "opt"
                + File.separator + "metersphere"
                + File.separator + "data"
                + File.separator + "body"
                + File.separator + "local-file"
                + File.separator + folder
                + File.separator;
    }

    public String generateFileDir(String folder) {
        if (StringUtils.isBlank(folder)) {
            folder = DEFAULT_FILE_FOLDER;
        }
        return fileFolder + folder + File.separator;
    }

    public String generateFilePath(String folder, long updateTime, String fileName) {
        String finalFileName = updateTime > 0 ? updateTime + "_" + fileName : fileName;
        return generateFileDir(folder) + finalFileName;
    }

    public File getFile(String folder, long updateTime, String fileName) {
        File file = new File(generateFilePath(folder, updateTime, fileName));
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    public void saveFile(String folder, long updateTime, String fileName, byte[] fileBytes) {
        //删除过期文件
        deleteOldFile(folder, fileName);
        this.createFile(generateFilePath(folder, updateTime, fileName), fileBytes);
    }

    public void saveFileByParamCheck(String folder, long updateTime, String fileName, byte[] fileBytes) {
        if (fileBytes != null && StringUtils.isNotBlank(folder) && updateTime > 0
                && StringUtils.isNotBlank(fileName) && fileBytes.length > 0) {
            //删除过期文件
            deleteOldFile(folder, fileName);
            this.createFile(generateFilePath(folder, updateTime, fileName), fileBytes);
        }
    }

    private void deleteOldFile(String folder, String deleteFileName) {
        List<String> deleteFileList = new ArrayList<>();
        File file = new File(generateFileDir(folder));
        if (file.exists() && file.isDirectory()) {
            String[] fileNameArr = file.list();
            if (fileNameArr != null) {
                for (String fileName : fileNameArr) {
                    if (fileName.endsWith("_" + deleteFileName)) {
                        deleteFileList.add(fileName);
                    }
                }
            }
        }
        deleteFileList.forEach(fileName -> this.deleteFile(generateFileDir(folder) + fileName));
    }

    public byte[] fileToByte(File tradeFile) {
        byte[] buffer = null;
        try (FileInputStream fis = new FileInputStream(tradeFile);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
        return buffer;
    }

    private void createFile(String filePath, byte[] fileBytes) {
        File file = new File(filePath);
        if (file.exists()) {
            this.deleteFile(filePath);
        }
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            LoggerUtil.error(e);
        }

        try (InputStream in = new ByteArrayInputStream(fileBytes); OutputStream out = new FileOutputStream(file)) {
            final int MAX = 4096;
            byte[] buf = new byte[MAX];
            for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
                out.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            LoggerUtil.error(e);
        }
    }

    public void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
