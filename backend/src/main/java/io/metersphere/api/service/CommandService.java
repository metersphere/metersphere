package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ssl.KeyStoreConfig;
import io.metersphere.api.dto.ssl.KeyStoreEntry;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class CommandService {

    public List<KeyStoreEntry> get(String password, MultipartFile file) {
        try {
            String path = FileUtils.createFile(file);
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
                        MSException.throwException(Translator.get("ssl_password_error"));
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
            FileUtils.deleteFile(path);
            return dtoList;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            MSException.throwException(e.getMessage());
        }
        return null;
    }

    public void createKeyStore(String alias, String path) {
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
            MSException.throwException(e.getMessage());
        }
    }

    public void mergeKeyStore(String newKeyStore, KeyStoreConfig sslConfig) {
        try {
            // 创建零时keyStore
            this.createKeyStore("ms-run", newKeyStore);
            // 修改别名
            Map<String, List<KeyStoreEntry>> entryMap = new HashMap<>();
            if (sslConfig != null && CollectionUtils.isNotEmpty(sslConfig.getEntrys())) {
                sslConfig.getEntrys().forEach(item -> {
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
                            File srcFile = new File(FileUtils.BODY_FILE_DIR + "/ssl/" + item.getId() + "_" + item.getName());
                            try {
                                // 开始合并
                                File destFile = new File(newKeyStore);
                                List<String> arguments = new ArrayList();
                                arguments.add("keytool");
                                arguments.add("-genkeypair");
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

                                SystemCommand nativeCommand = new SystemCommand(destFile.getParentFile(), (Map) null);
                                int exitVal = nativeCommand.run(arguments);
                                if (exitVal > 0) {
                                    MSException.throwException("合并条目：【" + entry.getOriginalAsName() + " 】失败");
                                }
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            MSException.throwException(e.getMessage());
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
                        MSException.throwException(Translator.get("ssl_password_error"));
                    }
                    if (line.contains("Exception")) {
                        MSException.throwException(Translator.get("ssl_file_error"));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            MSException.throwException(e.getMessage());
            return false;
        }
    }
}
