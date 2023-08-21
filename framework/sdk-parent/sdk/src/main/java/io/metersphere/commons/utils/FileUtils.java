package io.metersphere.commons.utils;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.BodyFile;
import io.metersphere.service.JarConfigService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static final String ROOT_DIR = "/opt/metersphere/";
    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body";
    public static final String MD_IMAGE_DIR = "/opt/metersphere/data/image/markdown";
    public static final String MD_IMAGE_TEMP_DIR = "/opt/metersphere/data/image/markdown/temp";
    public static final String UI_IMAGE_DIR = "/opt/metersphere/data/image/ui/screenshots";
    public static final String ATTACHMENT_DIR = "/opt/metersphere/data/attachment";
    public static final String ATTACHMENT_TMP_DIR = "/opt/metersphere/data/attachment/tmp";

    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotEmpty(fileName) && StringUtils.contains(fileName, "." + File.separator)) {
                    MSException.throwException(Translator.get("invalid_parameter"));
                }
            }
        }
    }

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

    public static void createFile(String filePath, byte[] fileBytes) {
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
            LogUtil.error(e);
        }

        try (InputStream in = new ByteArrayInputStream(fileBytes); OutputStream out = new FileOutputStream(file)) {
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
                validateFileName(item.getOriginalFilename());
                File file = new File(filePath + File.separator + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
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

    public static String create(String id, MultipartFile item) {
        String filePath = BODY_FILE_DIR + "/plugin";
        if (item != null) {
            validateFileName(item.getOriginalFilename());
            File testDir = new File(filePath);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            File file = new File(filePath + File.separator + id + "_" + item.getOriginalFilename());
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
            return file.getPath();
        }
        return null;
    }

    public static void createBodyFiles(String requestId, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyFiles) && StringUtils.isNotBlank(requestId)) {
            String path = BODY_FILE_DIR + File.separator + requestId;
            File testDir = new File(path);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            bodyFiles.forEach(item -> {
                validateFileName(item.getOriginalFilename());
                File file = new File(path + File.separator + item.getOriginalFilename());
                try (InputStream in = item.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                    file.createNewFile();
                    FileUtil.copyStream(in, out);
                } catch (IOException e) {
                    LogUtil.error(e);
                    MSException.throwException(Translator.get("upload_fail"));
                }
            });
        }
    }

    public static void copyBodyFiles(String sourceId, String targetId) {
        try {
            String sourcePath = BODY_FILE_DIR + File.separator + sourceId;
            String targetPath = BODY_FILE_DIR + File.separator + targetId;
            copyFolder(sourcePath, targetPath);
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
    }

    /**
     * 强制覆盖文件
     *
     * @param sourceId 源ID
     * @param targetId 目标ID
     */
    public static void forceOverrideBodyFiles(String sourceId, String targetId) {
        //删除源文件
        deleteBodyFiles(targetId);
        copyBodyFiles(sourceId, targetId);
    }

    /**
     * 复制文件夹(使用缓冲字节流)
     *
     * @param sourcePath 源文件夹路径
     * @param targetPath 目标文件夹路径
     */
    public static void copyFolder(String sourcePath, String targetPath) {
        //源文件夹路径
        File sourceFile = new File(sourcePath);
        //目标文件夹路径
        File targetFile = new File(targetPath);

        if (!sourceFile.exists() || !sourceFile.isDirectory()) {
            return;
        }
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }

        File[] files = sourceFile.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            copyFileToDir(file, targetFile);
        }
    }

    public static void copyFileToDir(String filePath, String targetPath) {
        //源文件路径
        File sourceFile = new File(filePath);
        //目标文件夹路径
        File targetDir = new File(targetPath);

        if (!sourceFile.exists()) {
            return;
        }
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        copyFileToDir(sourceFile, targetDir);
    }

    public static void moveFileToDir(String filePath, String targetPath) {
       copyFileToDir(filePath, targetPath);
       deleteFile(filePath);
    }

    private static void copyFileToDir(File file, File targetDir) {
        //文件要移动的路径
        String movePath = targetDir + File.separator + file.getName();
        if (file.isDirectory()) {
            //如果是目录则递归调用
            copyFolder(file.getAbsolutePath(), movePath);
        } else {
            //如果是文件则复制文件
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(movePath))) {
                byte[] b = new byte[1024];
                int temp;
                while ((temp = in.read(b)) != -1) {
                    out.write(b, 0, temp);
                }
            } catch (Exception e) {
                LoggerUtil.error(e);
            }
        }
    }


    public static File getFileByName(String name) {
        String path = BODY_FILE_DIR + File.separator + name;
        return new File(path);
    }

    public static File getBodyFileByName(String name, String requestId) {
        String path = BODY_FILE_DIR + File.separator + requestId + File.separator + name;
        return new File(path);
    }

    public static void copyBdyFile(String originId, String toId) {
        try {
            if (StringUtils.isNotEmpty(originId) && StringUtils.isNotEmpty(toId) && !StringUtils.equals(originId, toId)) {
                FileUtil.copyDir(new File(FileUtils.BODY_FILE_DIR + File.separator + originId),
                        new File(FileUtils.BODY_FILE_DIR + File.separator + toId));
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static void createBodyFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        FileUtils.create(bodyUploadIds, bodyFiles, null);
    }

    public static void createFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles, String path) {
        FileUtils.create(bodyUploadIds, bodyFiles, path);
    }

    public static String createFile(MultipartFile bodyFile) {
        validateFileName(bodyFile.getOriginalFilename());
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

    public static void deleteBodyFiles(String requestId) {
        File file = new File(BODY_FILE_DIR + File.separator + requestId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
        //删除临时目录中的文件
        file = new File(BODY_FILE_DIR + File.separator + "tmp" + File.separator + requestId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String uploadFile(MultipartFile uploadFile, String path, String name) {
        validateFileName(name);
        if (uploadFile == null) {
            return null;
        }
        File testDir = new File(path);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        String filePath = testDir + File.separator + name;
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

    public static void deleteDir(String path) {
        File file = new File(path);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 获取当前jmx 涉及到的文件
     *
     * @param tree
     */
    public static void getFiles(HashTree tree, List<BodyFile> files) {
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy source = (HTTPSamplerProxy) key;
                if (source != null && source.getHTTPFiles().length > 0) {
                    for (HTTPFileArg arg : source.getHTTPFiles()) {
                        BodyFile file = new BodyFile();
                        file.setId(arg.getParamName());
                        file.setName(arg.getPath());
                        if (arg.getPropertyAsBoolean("isRef")) {
                            file.setStorage(StorageConstants.FILE_REF.name());
                            file.setFileId(arg.getPropertyAsString("fileId"));
                        }
                        files.add(file);
                    }
                }
            } else if (key instanceof CSVDataSet) {
                CSVDataSet source = (CSVDataSet) key;
                if (source != null && StringUtils.isNotEmpty(source.getPropertyAsString("filename"))) {
                    BodyFile file = new BodyFile();
                    file.setId(source.getPropertyAsString("filename"));
                    file.setName(source.getPropertyAsString("filename"));
                    files.add(file);
                }
            }
            if (node != null) {
                getFiles(node, files);
            }
        }
    }

    public static byte[] fileToByte(File tradeFile) {
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
            LogUtil.error(e);
        }
        return buffer;
    }

    public static File byteToFile(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static String fileToStr(File tradeFile) {
        String buffer = null;
        try (FileInputStream fis = new FileInputStream(tradeFile);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toString();
        } catch (Exception e) {
        }
        return buffer;
    }

    public static List<FileMetadata> getRepositoryFileMetadata(HashTree tree) {
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        List<FileMetadata> list = new ArrayList<>();
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy source = (HTTPSamplerProxy) key;
                if (source != null && source.getHTTPFiles().length > 0) {
                    for (HTTPFileArg arg : source.getHTTPFiles()) {
                        if (arg.getPropertyAsBoolean("isRef") && fileMetadataService != null) {
                            FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(arg.getPropertyAsString("fileId"));
                            if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                                list.add(fileMetadata);
                                arg.setPath(fileMetadata.getName());
                                arg.setName(fileMetadata.getName());
                            }
                        }
                    }
                }
            } else if (key instanceof CSVDataSet) {
                CSVDataSet source = (CSVDataSet) key;
                if (source != null && StringUtils.isNotEmpty(source.getPropertyAsString("filename"))) {
                    if (source.getPropertyAsBoolean("isRef") && fileMetadataService != null) {
                        FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(source.getPropertyAsString("fileId"));
                        if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                            list.add(fileMetadata);
                            source.setFilename(fileMetadata.getName());
                        }
                    }
                }
            }
            if (node != null) {
                list.addAll(getRepositoryFileMetadata(node));
            }
        }
        return list;
    }

    public static boolean isFolderExists(String requestId) {
        File file = new File(BODY_FILE_DIR + File.separator + requestId);
        return file.isDirectory();
    }

    public static void deleteBodyTmpFiles(String reportId) {
        if (StringUtils.isNotEmpty(reportId)) {
            String executeTmpFolder = StringUtils.join(
                    BODY_FILE_DIR,
                    File.separator,
                    "tmp",
                    File.separator,
                    reportId
            );
            try {
                FileUtils.deleteDir(executeTmpFolder);
            } catch (Exception e) {
                LoggerUtil.error("删除[" + reportId + "]执行中产生的临时文件失败!", e);
            }

        }
    }

    public List<Object> getZipJar() {
        List<Object> jarFiles = new LinkedList<>();
        // jar 包
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        List<JarConfig> jars = jarConfigService.list();
        List<File> files = new ArrayList<>();

        jars.forEach(jarConfig -> {
            String path = jarConfig.getPath();
            File file = new File(path);
            if (file.isDirectory() && !path.endsWith(File.separator)) {
                file = new File(path + File.separator);
            }
            files.add(file);
        });

        try {
            File file = CompressUtils.zipFiles(UUID.randomUUID().toString() + ".zip", files);
            FileSystemResource resource = new FileSystemResource(file);
            byte[] fileByte = this.fileToByte(file);
            if (fileByte != null) {
                ByteArrayResource byteArrayResource = new ByteArrayResource(fileByte) {
                    @Override
                    public String getFilename() throws IllegalStateException {
                        return resource.getFilename();
                    }
                };
                jarFiles.add(byteArrayResource);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }

        return jarFiles;
    }

    public List<Object> getJar() {
        List<Object> jarFiles = new LinkedList<>();
        // jar 包
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        List<JarConfig> jars = jarConfigService.list();
        jars.forEach(jarConfig -> {
            try {
                String path = jarConfig.getPath();
                File file = new File(path);
                if (file.isDirectory() && !path.endsWith(File.separator)) {
                    file = new File(path + File.separator);
                }
                FileSystemResource resource = new FileSystemResource(file);
                byte[] fileByte = this.fileToByte(file);
                if (fileByte != null) {
                    ByteArrayResource byteArrayResource = new ByteArrayResource(fileByte) {
                        @Override
                        public String getFilename() throws IllegalStateException {
                            return resource.getFilename();
                        }
                    };
                    jarFiles.add(byteArrayResource);
                }

            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
        return jarFiles;
    }

    public List<Object> getMultipartFiles(HashTree hashTree) {
        List<Object> multipartFiles = new LinkedList<>();
        // 获取附件
        List<BodyFile> files = new LinkedList<>();
        getFiles(hashTree, files);
        if (CollectionUtils.isNotEmpty(files)) {
            for (BodyFile bodyFile : files) {
                File file = new File(bodyFile.getName());
                if (file != null && !file.exists()) {
                    FileSystemResource resource = new FileSystemResource(file);
                    byte[] fileByte = this.fileToByte(file);
                    if (fileByte != null) {
                        ByteArrayResource byteArrayResource = new ByteArrayResource(fileByte) {
                            @Override
                            public String getFilename() throws IllegalStateException {
                                return resource.getFilename();
                            }
                        };
                        multipartFiles.add(byteArrayResource);
                    }
                }
            }
        }
        return multipartFiles;
    }

    public static Boolean writeToFile(String filePath, byte[] content) {
        OutputStream oStream = null;
        try {
            oStream = new FileOutputStream(filePath);
            oStream.write(content);
            return Boolean.TRUE;
        } catch (Exception exception) {
            exception.printStackTrace();
            return Boolean.FALSE;
        } finally {
            try {
                oStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath(BodyFile file) {
        String type = StringUtils.isNotEmpty(file.getFileType()) ? file.getFileType().toLowerCase() : null;
        String name = file.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, file.getProjectId(), File.separator, name);
    }

    public static String getFilePath(FileMetadata fileMetadata) {
        String type = StringUtils.isNotEmpty(fileMetadata.getType()) ? fileMetadata.getType().toLowerCase() : null;
        String name = fileMetadata.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, fileMetadata.getProjectId(), File.separator, name);
    }
}
