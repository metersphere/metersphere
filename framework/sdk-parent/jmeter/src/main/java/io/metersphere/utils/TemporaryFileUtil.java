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

    public String generateLocalFileDir() {
        return fileFolder + DEFAULT_FILE_FOLDER + File.separator;
    }

    //生成执行文件的相对路径
    public String generateRelativeDir(String folder, String fileMetadataId, long updateTime) {
        if (StringUtils.isBlank(folder)) {
            folder = DEFAULT_FILE_FOLDER;
        }
        if (StringUtils.isBlank(fileMetadataId)) {
            return folder + File.separator;
        } else {
            String metadataIdFolder = folder + File.separator + fileMetadataId + File.separator;
            return updateTime == 0 ? metadataIdFolder : metadataIdFolder + updateTime + File.separator;
        }
    }

    //生成执行文件的绝对路径
    public String generateFileDir(String folder, String fileMetadataId, long updateTime) {
        return fileFolder + generateRelativeDir(folder, fileMetadataId, updateTime);
    }

    private String getFileName(String fileName, String fileSuffix) {
        if (StringUtils.endsWithIgnoreCase(fileName, "." + fileSuffix)) {
            return fileName;
        } else {
            if (StringUtils.isNotBlank(fileSuffix)) {
                return StringUtils.join(fileName, ".", fileSuffix);
            } else {
                return fileName;
            }
        }
    }

    public String generateFilePath(String folder, String fileMetadataId, long updateTime, String fileName, String fileSuffix) {
        return generateFileDir(folder, fileMetadataId, updateTime) + getFileName(fileName, fileSuffix);
    }

    public String generateLocalFilePath(String filePath) {
        return generateLocalFileDir() + filePath;
    }

    //node使用 判断local文件
    public File getLocalFile(String filePath) {
        File file = new File(this.generateLocalFilePath(filePath));
        return file.exists() ? file : null;
    }

    public File getFile(String folder, String fileMetadataId, long updateTime, String fileName, String fileSuffix) {
        File file = new File(generateFilePath(folder, fileMetadataId, updateTime, fileName, fileSuffix));
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    public void saveFile(String folder, String fileMetadataId, long updateTime, String fileName, String fileSuffix, byte[] fileBytes) {
        //删除过期文件
        deleteOldFile(folder, fileMetadataId, updateTime, fileName);
        this.createFile(generateFilePath(folder, fileMetadataId, updateTime, fileName, fileSuffix), fileBytes);
    }

    public void saveFileByParamCheck(String folder, String fileMetadataId, long updateTime, String fileName, String fileSuffix, byte[] fileBytes) {
        if (fileBytes != null && StringUtils.isNotBlank(folder) && updateTime > 0
                && StringUtils.isNotBlank(fileName) && fileBytes.length > 0) {
            //删除过期文件
            deleteOldFile(folder, fileMetadataId, updateTime, fileName);
            this.createFile(generateFilePath(folder, fileMetadataId, updateTime, fileName, fileSuffix), fileBytes);
        }
    }

    //node也调用了该方法
    public void deleteOldFile(String folder, String fileMetadataId, long lastUpdateTime, String deleteFileName) {
        if (StringUtils.isEmpty(fileMetadataId)) {
            //本地文件不涉及到判断。  原因：在ms上不会将本地文件放在执行文件中；在node上本地文件再更新会存到Minio里。
            return;
        }
        String newFileFolderName = String.valueOf(lastUpdateTime);
        List<File> deleteFileList = new ArrayList<>();
        File file = new File(generateFileDir(folder, fileMetadataId, 0));
        //当前目录下存放的是以时间戳命名的文件夹，文件夹里存放着具体的文件。所以要删除这个
        if (file.isDirectory()) {
            File[] checkFileFolders = file.listFiles();
            if (checkFileFolders != null) {
                for (File checkFileFolder : checkFileFolders) {
                    if (checkFileFolder.isDirectory()) {
                        File[] checkFiles = checkFileFolder.listFiles();
                        if (checkFiles != null) {
                            for (File checkFile : checkFiles) {
                                if (!StringUtils.equals(checkFileFolder.getName(), newFileFolderName)) {
                                    //MinIO文件名称可能会修改。所以这里只判断文件所处的时间戳文件夹是否对应
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
