package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ssl.KeyStoreDTO;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

@Service
public class CommandService {

    public List<KeyStoreDTO> get(String password, MultipartFile file) {
        try {
            String path = FileUtils.createFile(file);
            // 执行验证指令
            if(StringUtils.isNotEmpty(password)) {
                password = JSON.parseObject(password, String.class);
            }
            String keytoolArgs[] = {"keytool", "-rfc", "-list", "-keystore", path, "-storepass", password};
            Process p = new ProcessBuilder(keytoolArgs).start();
            List<KeyStoreDTO> dtoList = new LinkedList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = null;
                KeyStoreDTO dto = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("keystore password was incorrect")) {
                        MSException.throwException("认证密码错误，请重新输入密码");
                    }
                    if (line.startsWith("别名")) {
                        if (dto != null) {
                            dtoList.add(dto);
                        }
                        dto = new KeyStoreDTO();
                        dto.setOriginalAsName(line.split(":")[1]);
                    }
                    if (line.startsWith("条目类型")) {
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
}
