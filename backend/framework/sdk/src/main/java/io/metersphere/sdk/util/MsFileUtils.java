package io.metersphere.sdk.util;

import io.metersphere.sdk.exception.MSException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MsFileUtils {
    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotBlank(fileName) && StringUtils.contains(fileName, "." + File.separator)) {
                    throw new MSException(Translator.get("invalid_parameter"));
                }
            }
        }
    }

    public static void deleteDir(String path) throws Exception {
        File file = new File(path);
        FileUtils.deleteDirectory(file);
    }

    /**
     * 获取流文件
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try (OutputStream os = new FileOutputStream(file);) {
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * MultipartFile 转 File
     *
     * @param file
=     */
    public static File multipartFileToFile(MultipartFile file) {
        if (file != null && file.getSize() > 0) {
            try (InputStream ins = file.getInputStream()) {
                validateFileName(file.getOriginalFilename());
                File toFile = new File(FileUtils.getTempDirectoryPath()+File.separator+Objects.requireNonNull(file.getOriginalFilename()));
                inputStreamToFile(ins, toFile);
                return toFile;
            } catch (Exception e) {
                LogUtils.error(e);
            }
        }
        return null;
    }

    public static File zipFile(String rootPath, String zipFolder) {
        File folder = new File(rootPath + File.separator + zipFolder);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                return null;
            }
            File zipFile = new File(rootPath + File.separator + zipFolder + ".zip");

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (File file : files) {
                    String fileName = file.getName();
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                        zipOutputStream.putNextEntry(new ZipEntry(fileName));
                        byte[] buffer = new byte[512];
                        int num;
                        while ((num = bis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, num);
                        }
                        zipOutputStream.closeEntry();
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception e) {
                LogUtils.error(e);
            }
            return zipFile;
        }

        return null;
    }

    public static File[] unZipFile(File file, String targetPath) {
        InputStream input = null;
        OutputStream output = null;
        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
             ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = null;
            while ((entry = zipInput.getNextEntry()) != null) {
                File outFile = new File(targetPath + File.separator + entry.getName());
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdir();
                }
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                input = zipFile.getInputStream(entry);
                output = new FileOutputStream(outFile);
                int temp = 0;
                while ((temp = input.read()) != -1) {
                    output.write(temp);
                }
            }
            File folder = new File(targetPath);
            if (folder.isDirectory()) {
                return folder.listFiles();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }
}
