package io.metersphere.commons.utils;

import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class FileUtils {
    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    private static void create(List<String> bodyUploadIds, List<MultipartFile> bodyFiles, String path) {
        String filePath = BODY_FILE_DIR;
        if (StringUtils.isNotEmpty(path)) {
            filePath = path;
        }
        if (CollectionUtils.isNotEmpty(bodyUploadIds) && CollectionUtils.isNotEmpty(bodyFiles)) {
            File testDir = new File(filePath);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            for (int i = 0; i < bodyUploadIds.size(); i++) {
                MultipartFile item = bodyFiles.get(i);
                File file = new File(filePath + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
                try (InputStream in = item.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                    file.createNewFile();
                    final int MAX = 4096;
                    byte[] buf = new byte[MAX];
                    for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
                        out.write(buf, 0, bytesRead);
                    }
                } catch (IOException e) {
                    LogUtil.error(e);
                    MSException.throwException(Translator.get("upload_fail"));
                }
            }
        }
    }

    public static void createBodyFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        FileUtils.create(bodyUploadIds, bodyFiles, null);
    }

    public static void createFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles, String path) {
        FileUtils.create(bodyUploadIds, bodyFiles, path);
    }

    public static String createFile(MultipartFile bodyFile) {
        String dir = "/opt/metersphere/data/body/tmp/";
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(dir + UUID.randomUUID().toString() + "_" + bodyFile.getOriginalFilename());
        try (InputStream in = bodyFile.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtil.error(e);
            MSException.throwException(Translator.get("upload_fail"));
        }
        return file.getPath();
    }

    public static void delFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String uploadFile(MultipartFile uploadFile, String path, String name) {
        if (uploadFile == null) {
            return null;
        }
        File testDir = new File(path);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        String filePath = testDir + "/" + name;
        File file = new File(filePath);
        try (InputStream in = uploadFile.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("upload_fail"));
        }
        return filePath;
    }

    public static String uploadFile(MultipartFile uploadFile, String path) {
        return uploadFile(uploadFile, path, uploadFile.getOriginalFilename());
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

}
