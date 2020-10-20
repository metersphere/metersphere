package io.metersphere.api.service;

import io.metersphere.base.domain.ApiTestJarConfig;
import io.metersphere.base.domain.ApiTestJarConfigExample;
import io.metersphere.base.mapper.ApiTestJarConfigMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestJarConfigService {

    private static final String JAR_FILE_DIR = "/opt/metersphere/data/jar";

    @Resource
    private ApiTestJarConfigMapper apiTestJarConfigMapper;

    public List<ApiTestJarConfig> list(String projectId) {
        ApiTestJarConfigExample example = new ApiTestJarConfigExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return apiTestJarConfigMapper.selectByExample(example);
    }

    public ApiTestJarConfig get(String id) {
        return apiTestJarConfigMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        ApiTestJarConfig apiTestJarConfig = apiTestJarConfigMapper.selectByPrimaryKey(id);
        deleteJarFile(apiTestJarConfig.getPath());
        apiTestJarConfigMapper.deleteByPrimaryKey(id);
    }

    public void update(ApiTestJarConfig apiTestJarConfig, MultipartFile file) {
        checkExist(apiTestJarConfig);
        apiTestJarConfig.setOwner(SessionUtils.getUser().getId());
        apiTestJarConfig.setUpdateTime(System.currentTimeMillis());
        String deletePath = apiTestJarConfig.getPath();
        if (file != null) {
            apiTestJarConfig.setFileName(file.getOriginalFilename());
            apiTestJarConfig.setPath(getJarPath(apiTestJarConfig, file));
        }
        apiTestJarConfigMapper.updateByPrimaryKey(apiTestJarConfig);
        if (file != null) {
            deleteJarFile(deletePath);
            createJarFiles(apiTestJarConfig.getProjectId(), file);
        }
    }

    public String add(ApiTestJarConfig apiTestJarConfig, MultipartFile file) {
        apiTestJarConfig.setId(UUID.randomUUID().toString());
        apiTestJarConfig.setOwner(SessionUtils.getUser().getId());
        checkExist(apiTestJarConfig);
        apiTestJarConfig.setCreateTime(System.currentTimeMillis());
        apiTestJarConfig.setUpdateTime(System.currentTimeMillis());
        apiTestJarConfig.setPath(getJarPath(apiTestJarConfig, file));
        apiTestJarConfig.setFileName(file.getOriginalFilename());
        apiTestJarConfigMapper.insert(apiTestJarConfig);
        createJarFiles(apiTestJarConfig.getProjectId(), file);
        return apiTestJarConfig.getId();
    }

    public void deleteJarFiles(String testId) {
        File file = new File(JAR_FILE_DIR + "/" + testId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteJarFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getJarPath(ApiTestJarConfig apiTestJarConfig, MultipartFile file) {
        return JAR_FILE_DIR + "/" + apiTestJarConfig.getProjectId() + "/" + file.getOriginalFilename();
    }

    private String createJarFiles(String projectId, MultipartFile jar) {
        if (jar == null) {
            return null;
        }
        String dir = JAR_FILE_DIR + "/" + projectId;
        File testDir = new File(dir);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        String filePath = testDir + "/" + jar.getOriginalFilename();
        File file = new File(filePath);
        try (InputStream in = jar.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtil.error(e);
            MSException.throwException(Translator.get("upload_fail"));
        }
        return filePath;
    }


    private void checkExist(ApiTestJarConfig jarConfig) {
        if (jarConfig.getName() != null) {
            ApiTestJarConfigExample example = new ApiTestJarConfigExample();
            ApiTestJarConfigExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(jarConfig.getName())
                    .andProjectIdEqualTo(jarConfig.getProjectId());
            if (StringUtils.isNotBlank(jarConfig.getId())) {
                criteria.andIdNotEqualTo(jarConfig.getId());
            }
            if (apiTestJarConfigMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("api_test_jarConfig_already_exists"));
            }
        }
    }
}
