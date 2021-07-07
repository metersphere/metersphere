package io.metersphere.commons.utils;

import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.JarConfigService;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FileUtils {
    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body";
    public static final String MD_IMAGE_DIR = "/opt/metersphere/data/image/markdown";

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

    public static void createBodyFiles(String requestId, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyFiles) && StringUtils.isNotBlank(requestId)) {
            String path = BODY_FILE_DIR + "/" + requestId;
            File testDir = new File(path);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            bodyFiles.forEach(item -> {
                File file = new File(path + "/" + item.getOriginalFilename());
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

    public static File getFileByName(String name) {
        String path = BODY_FILE_DIR + "/" + name;
        return new File(path);
    }

    public static void copyBdyFile(String originId, String toId) {
        try {
            FileUtil.copyDir(new File(FileUtils.BODY_FILE_DIR + "/" + originId),
                    new File(FileUtils.BODY_FILE_DIR + "/" + toId));
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
        File file = new File(BODY_FILE_DIR + "/" + requestId);
        FileUtil.deleteContents(file);
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
                        files.add(file);
                    }
                }
            } else if (key instanceof CSVDataSet) {
                CSVDataSet source = (CSVDataSet) key;
                if (source != null && source.getFilename() != null) {
                    BodyFile file = new BodyFile();
                    file.setId(source.getFilename());
                    file.setName(source.getFilename());
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
             ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
        }
        return buffer;
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
            if (file.isDirectory() && !path.endsWith("/")) {
                file = new File(path + "/");
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
            e.printStackTrace();
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
                if (file.isDirectory() && !path.endsWith("/")) {
                    file = new File(path + "/");
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
}
