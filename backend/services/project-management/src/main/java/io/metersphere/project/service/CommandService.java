package io.metersphere.project.service;


import io.metersphere.project.dto.environment.ssl.KeyStoreEntry;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class CommandService {

    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body/";
    public static String createFile(MultipartFile bodyFile) {
        validateFileName(bodyFile.getOriginalFilename());
        String dir = BODY_FILE_DIR;
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(dir + UUID.randomUUID().toString() + "_" + bodyFile.getOriginalFilename());
        try (InputStream in = bodyFile.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("upload_fail"));
        }
        return file.getPath();
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotEmpty(fileName) && StringUtils.contains(fileName, "." + File.separator)) {
                    throw new MSException(Translator.get("invalid_parameter"));
                }
            }
        }
    }

    public List<KeyStoreEntry> getEntry(String password, MultipartFile file) {
        try {
            String path = createFile(file);
            // 执行验证指令
            if (StringUtils.isNotEmpty(password)) {
                password = JSON.parseObject(password, String.class);
            }
            String[] args = {"keytool", "-rfc", "-list", "-keystore", path, "-storepass", password};
            Process p = new ProcessBuilder(args).start();
            List<KeyStoreEntry> dtoList = new LinkedList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = null;
                KeyStoreEntry dto = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("keystore password was incorrect")) {
                        throw new MSException(Translator.get("ssl_password_error"));
                    }
                    if (line.startsWith("别名") || line.startsWith("Alias name")) {
                        if (dto != null) {
                            dtoList.add(dto);
                        }
                        dto = new KeyStoreEntry();
                        dto.setOriginalAsName(line.split(":")[1]);
                    }
                    if (line.startsWith("条目类型") || line.startsWith("Entry type")) {
                        dto.setType(line.split(":")[1]);
                    }
                }
                if (dto != null) {
                    dtoList.add(dto);
                }
            }
            deleteFile(path);
            return dtoList;
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }

   /* public void createKeyStore(String alias, String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            List<String> arguments = new ArrayList();
            arguments.add("keytool");
            arguments.add("-genkeypair");
            arguments.add("-alias");
            arguments.add(alias);
            arguments.add("-dname");
            arguments.add("CN=localhost,OU=cn,O=cn,L=cn,ST=cn,C=cn");
            arguments.add("-keyalg");
            arguments.add("RSA");
            arguments.add("-keystore");
            arguments.add(f.getName());
            arguments.add("-storepass");
            arguments.add("ms123...");
            arguments.add("-keypass");
            arguments.add("ms123...");
            arguments.add("-validity");
            arguments.add(Integer.toString(1024));
            SystemCommand nativeCommand = new SystemCommand(f.getParentFile(), (Map) null);
            nativeCommand.run(arguments);
        } catch (Exception e) {
            throw new MSException(e.getMessage());
        }
    }

    public void mergeKeyStore(String newKeyStore, KeyStoreConfig sslConfig) {
        try {
            // 创建零时keyStore
            this.createKeyStore("ms-run", newKeyStore);
            // 修改别名
            Map<String, List<KeyStoreEntry>> entryMap = new HashMap<>();
            if (sslConfig != null && CollectionUtils.isNotEmpty(sslConfig.getEntry())) {
                sslConfig.getEntry().forEach(item -> {
                    if (entryMap.containsKey(item.getSourceId())) {
                        entryMap.get(item.getSourceId()).add(item);
                    } else {
                        List<KeyStoreEntry> list = new ArrayList<>();
                        list.add(item);
                        entryMap.put(item.getSourceId(), list);
                    }
                });
            }
            if (sslConfig != null && CollectionUtils.isNotEmpty(sslConfig.getFiles())) {
                sslConfig.getFiles().forEach(item -> {
                    List<KeyStoreEntry> entries = entryMap.get(item.getId());
                    if (CollectionUtils.isNotEmpty(entries)) {
                        entries.forEach(entry -> {
                            File srcFile = new File(BODY_FILE_DIR + "/ssl/" + item.getId() + "_" + item.getName());
                            try {
                                // 开始合并
                                File destFile = new File(newKeyStore);
                                List<String> arguments = new ArrayList();
                                arguments.add("keytool");
                                arguments.add("-importkeystore");
                                arguments.add("-srckeystore");
                                arguments.add(srcFile.getName());
                                arguments.add("-srcstorepass");
                                arguments.add(item.getPassword());
                                arguments.add("-srcalias");
                                arguments.add(entry.getOriginalAsName().trim());
                                arguments.add("-srckeypass");
                                arguments.add(entry.getPassword());

                                arguments.add("-destkeystore");
                                arguments.add(destFile.getName());
                                arguments.add("-deststorepass");
                                arguments.add("ms123...");
                                arguments.add("-destalias");
                                arguments.add(StringUtils.isNotEmpty(entry.getNewAsName()) ? entry.getNewAsName().trim() : entry.getOriginalAsName().trim());
                                arguments.add("-destkeypass");
                                arguments.add("ms123...");

                                LogUtils.info("证书合并命令：", String.join(StringUtils.SPACE, arguments));
                                SystemCommand nativeCommand = new SystemCommand(destFile.getParentFile(), (Map) null);
                                int exitVal = nativeCommand.run(arguments);
                                if (exitVal > 0) {
                                    throw new MSException("合并条目：【" + entry.getOriginalAsName() + " 】失败");
                                }
                            } catch (Exception e) {
                                LogUtils.error(e);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }

    public boolean checkKeyStore(String password, String path) {
        try {
            String keytoolArgs[] = {"keytool", "-rfc", "-list", "-keystore", path, "-storepass", password};
            Process p = new ProcessBuilder(keytoolArgs).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("keystore password was incorrect")) {
                        throw new MSException(Translator.get("ssl_password_error"));
                    }
                    if (line.contains("Exception")) {
                        throw  new MSException(Translator.get("ssl_file_error"));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(e.getMessage());
        }
    }*/
}
