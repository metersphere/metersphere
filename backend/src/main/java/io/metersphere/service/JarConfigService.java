package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.base.domain.JarConfigExample;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.JarConfigMapper;
import io.metersphere.base.mapper.ext.ExtJarConfigMapper;
import io.metersphere.commons.constants.JarConfigResourceType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.JarConfigRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class JarConfigService {

    private static final String JAR_FILE_DIR = "/opt/metersphere/data/jar";

    @Resource
    ExtJarConfigMapper extJarConfigMapper;

    @Resource
    ProjectService projectService;

    @Resource
    private JarConfigMapper jarConfigMapper;

    public List<JarConfig> list() {
        JarConfigExample example = new JarConfigExample();
        return jarConfigMapper.selectByExample(example);
    }


    public List<JarConfig> listByProjectIds(List<String> projectIds) {
        if(CollectionUtils.isEmpty(projectIds)){
            return new ArrayList<>();
        }
        JarConfigExample example = new JarConfigExample();
        example.createCriteria().andResourceTypeEqualTo("PROJECT").andResourceIdIn(projectIds);
        return jarConfigMapper.selectByExample(example);
    }

    public Pager<List<JarConfig>> list(JarConfigRequest request, int pageNum, int pageSize) {
        buildQueryRequest(request);
        Page<Object> page = PageHelper.startPage(pageNum, pageSize, true);
        return PageUtils.setPageInfo(page, extJarConfigMapper.list(request));
    }

    public void buildQueryRequest(JarConfigRequest request) {
        if (request.getResourceId() == null || request.getResourceType() == null) {
            MSException.throwException("resourceId or resourceType could not be null!");
        }
        if (request.getResourceType().equals(JarConfigResourceType.PROJECT.name())) {
            Project project = projectService.getProjectById(request.getResourceId());
            request.setWorkspaceId(project.getWorkspaceId());
        }
    }

    public JarConfig get(String id) {
        return jarConfigMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        JarConfig jarConfig = jarConfigMapper.selectByPrimaryKey(id);
        FileUtils.deleteDir(getJarDir(jarConfig.getId()));
        jarConfigMapper.deleteByPrimaryKey(id);
    }

    public void update(JarConfig jarConfig, MultipartFile file) {
        checkExist(jarConfig);
        jarConfig.setEnable(true);// todo 审批机制时需修改
        jarConfig.setModifier(SessionUtils.getUser().getId());
        jarConfig.setUpdateTime(System.currentTimeMillis());
        String deletePath = jarConfig.getPath();
        if (file != null) {
            jarConfig.setFileName(file.getOriginalFilename());
            jarConfig.setPath(getJarPath(file, jarConfig.getId()));
        }
        jarConfigMapper.updateByPrimaryKey(jarConfig);
        if (file != null) {
            FileUtils.deleteFile(deletePath);
            FileUtils.uploadFile(file, getJarDir(jarConfig.getId()));
        }
    }

    public String add(JarConfig jarConfig, MultipartFile file) {
        if (file != null && !file.getOriginalFilename().endsWith(".jar")) {
            MSException.throwException("上传文件类型错误，请上传正确jar文件");
        }
        jarConfig.setId(UUID.randomUUID().toString());
        jarConfig.setCreator(SessionUtils.getUser().getId());
        jarConfig.setModifier(SessionUtils.getUser().getId());
        checkExist(jarConfig);
        jarConfig.setEnable(true);// todo 审批机制时需修改
        jarConfig.setCreateTime(System.currentTimeMillis());
        jarConfig.setUpdateTime(System.currentTimeMillis());
        jarConfig.setPath(getJarPath(file, jarConfig.getId()));
        jarConfig.setFileName(file.getOriginalFilename());
        jarConfigMapper.insert(jarConfig);
        FileUtils.uploadFile(file, getJarDir(jarConfig.getId()));
        return jarConfig.getId();
    }

    public String getJarPath(MultipartFile file, String id) {
        return JAR_FILE_DIR + "/" + id + "/" + file.getOriginalFilename();
    }

    public String getJarDir(String id) {
        return JAR_FILE_DIR + "/" + id;
    }

    private void checkExist(JarConfig jarConfig) {
        if (jarConfig.getName() != null) {
            JarConfigRequest request = new JarConfigRequest();
            BeanUtils.copyBean(request, jarConfig);
            buildQueryRequest(request);
            if (extJarConfigMapper.checkExist(request) > 0) {
                MSException.throwException(Translator.get("name_already_exists"));
            }
        }
    }

    public String getLogDetails(String id) {
        JarConfig jarConfig = jarConfigMapper.selectByPrimaryKey(id);
        if (jarConfig != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(jarConfig, SystemReference.jarColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(jarConfig.getId()), null, jarConfig.getName(), jarConfig.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    /**
     * 兼容性处理，将原来的jar包设置成工作空间级别，并拷贝到对应的目录
     */
    public void initJarPath() {
        List<JarConfig> list = jarConfigMapper.selectByExample(new JarConfigExample());
        Set<String> oldFiles = new HashSet<>();
        list.forEach(item -> {
            try {
                String path = item.getPath();
                String[] split = path.split("/");
                String fileName = split[split.length - 1];
                oldFiles.add(fileName);
                FileUtils.copyFileToDir(JAR_FILE_DIR + "/" + fileName, getJarDir(item.getId()));
                item.setPath(getJarDir(item.getId()) + "/" + fileName);
                jarConfigMapper.updateByPrimaryKey(item);
            } catch (Exception e) {
                LogUtil.error(JSONObject.toJSON(item));
                LogUtil.error(e);
            }
        });
        oldFiles.forEach(path -> FileUtils.deleteFile(JAR_FILE_DIR + "/" + path));
    }
}
