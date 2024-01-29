package io.metersphere.project.service;


import io.metersphere.project.dto.environment.ssl.KeyStoreEntry;
import io.metersphere.sdk.constants.LocalRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CommandService {

    public static String createFile(MultipartFile bodyFile) {
        MsFileUtils.validateFileName(bodyFile.getOriginalFilename());
        String dir = LocalRepositoryDir.getSystemTempDir();
        File fileDir = new File(dir);
        if (!fileDir.exists() && !fileDir.mkdir()) {
            throw new MSException(Translator.get("upload_fail"));
        }
        File file = new File(dir + IDGenerator.nextStr() + "_" + bodyFile.getOriginalFilename());
        try (InputStream in = bodyFile.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("upload_fail"));
        }
        return file.getPath();
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
                        assert dto != null;
                        dto.setType(line.split(":")[1]);
                    }
                }
                if (dto != null) {
                    dtoList.add(dto);
                }
            }
            File localFiles = new File(path);
            if (!localFiles.exists() && !localFiles.delete()) {
                LogUtils.info("delete file fail");
            }
            return dtoList;
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }
}
