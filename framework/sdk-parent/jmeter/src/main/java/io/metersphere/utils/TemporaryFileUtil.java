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
                + File.separator + "api-folder"
                + File.separator + "file"
                + File.separator + folder
                + File.separator;
    }

    public String generateFileDir(String folder, long updateTime) {
        if (StringUtils.isBlank(folder)) {
            folder = DEFAULT_FILE_FOLDER;
        }
        if (updateTime == 0) {
            return fileFolder + folder + File.separator;
        } else {
            return fileFolder + folder + File.separator + updateTime + File.separator;
        }

    }

    public String generateFilePath(String folder, long updateTime, String fileName) {
        return generateFileDir(folder, updateTime) + fileName;
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
        deleteOldFile(folder, updateTime, fileName);
        this.createFile(generateFilePath(folder, updateTime, fileName), fileBytes);
    }

    public void saveFileByParamCheck(String folder, long updateTime, String fileName, byte[] fileBytes) {
        if (fileBytes != null && StringUtils.isNotBlank(folder) && updateTime > 0
                && StringUtils.isNotBlank(fileName) && fileBytes.length > 0) {
            //删除过期文件
            deleteOldFile(folder, updateTime, fileName);
            this.createFile(generateFilePath(folder, updateTime, fileName), fileBytes);
        }
    }

    //node也调用了该方法
    public void deleteOldFile(String folder, long lastUpdateTime, String deleteFileName) {
        String newFileFolderName = String.valueOf(lastUpdateTime);
        List<File> deleteFileList = new ArrayList<>();
        File file = new File(generateFileDir(folder, 0));
        //当前目录下存放的是以时间戳命名的文件夹，文件夹里存放着具体的文件。所以要删除这个
        if (file.isDirectory()) {
            File[] checkFileFolders = file.listFiles();
            if (checkFileFolders != null) {
                for (File checkFileFolder : checkFileFolders) {
                    if (checkFileFolder.isDirectory()) {
                        File[] checkFiles = checkFileFolder.listFiles();
                        if (checkFiles != null) {
                            for (File checkFile : checkFiles) {
                                if (StringUtils.equals(checkFile.getName(), deleteFileName)
                                        && !StringUtils.equals(checkFileFolder.getName(), newFileFolderName)) {
                                    //文件名称相同，但是所属的时间戳文件夹与本次不相同的文件，是过期文件。
                                    deleteFileList.add(checkFile);
                                }
                            }
                        }
                    }
                }
            }
        }

        deleteFileList.forEach(deleteFile -> {
            if (deleteFile.exists()) {
                deleteFile.delete();
            }
            File deleteFileFolder = deleteFile.getParentFile();
            if (deleteFileFolder.isDirectory() && deleteFileFolder.listFiles().length == 0) {
                deleteFileFolder.delete();
            }
        });
    }

    //勿删。 这段代码在node中会用到。
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
            file.delete();
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
}
