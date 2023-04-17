package io.metersphere.service;

import com.google.common.base.CaseFormat;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ProjectApplicationExample;
import io.metersphere.base.mapper.ProjectApplicationMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectMapper projectMapper;

    public String getLogDetails(ProjectApplication projectApplication) {
        if (projectApplication != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(projectApplication, SystemReference.projectApplicationColumns);
            Project project = projectMapper.selectByPrimaryKey(projectApplication.getProjectId());
            if (project == null) {
                return null;
            }
            DetailColumn column = new DetailColumn("项目名称", "projectName", project.getName(), null);
            columns.add(column);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(project.getId()), project.getId(), "修改链接报告时间", Objects.requireNonNull(SessionUtils.getUser()).getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public ProjectApplication getProjectApplication(String projectId, String type) {
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(projectApplicationExample);
        if (projectApplications == null || projectApplications.size() == 0) {
            return new ProjectApplication();
        }
        return projectApplications.get(0);
    }

    public HashMap<String, String> getProjectConfigMap(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectApplication> applications = projectApplicationMapper.selectByExample(example);
        return applications.stream().collect(HashMap::new, (k, v) -> k.put(v.getType(), v.getTypeValue()), HashMap::putAll);
    }

    /**
     * 返回指定项目下某配置的值
     *
     * @param projectId 指定项目ID
     * @param type      ProjectApplicationType中某项目配置
     * @return 如果有该配置返回字符串类型，否则返回NULL
     */
    public String getTypeValue(String projectId, String type) {
        ProjectApplication application = this.getProjectApplication(projectId, type);
        if (application != null && StringUtils.isEmpty(application.getTypeValue())) {
            return application.getTypeValue();
        }
        return null;
    }

    public List<ProjectApplication> selectByExample(ProjectApplicationExample example) {
        return projectApplicationMapper.selectByExample(example);
    }

    /**
     * 获取指定项目下的所有应用配置选项组成的对象
     *
     * @param projectId 项目ID
     * @return 项目配置对象ProjectConfig
     */
    public ProjectConfig getProjectConfig(String projectId) {
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectApplication> applications = projectApplicationMapper.selectByExample(projectApplicationExample);
        return assignConfigValue(applications);
    }

    private ProjectConfig assignConfigValue(List<ProjectApplication> applications) {
        ProjectConfig config = new ProjectConfig();
        Class<? extends ProjectConfig> clazz = config.getClass();
        Field[] fields = clazz.getDeclaredFields();
        HashMap<String, String> map = applications.stream()
                .collect(HashMap::new, (k, v) -> k.put(v.getType(), v.getTypeValue()), HashMap::putAll);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                String str = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
                Method method = config.getClass().getMethod("set" + StringUtils.capitalize(name), field.getType());
                if (StringUtils.isNotBlank(map.get(str))) {
                    method.invoke(config, valueOf(field.getType(), map.get(str)));
                }
            } catch (Exception e) {
                LogUtil.error("get project config error.");
                LogUtil.error(e.getMessage(), e);
            }
        }
        return config;
    }

    /**
     * 返回指定项目下某配置的值
     *
     * @param projectId 项目ID
     * @param type      ProjectApplicationType中某项目配置
     * @return ProjectConfig
     */
    public ProjectConfig getSpecificTypeValue(String projectId, String type) {
        ProjectApplication application = this.getProjectApplication(projectId, type);
        ProjectConfig config = new ProjectConfig();
        Class<? extends ProjectConfig> clazz = config.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, type));
            try {
                field.setAccessible(true);
                String name = field.getName();
                Method method = config.getClass().getMethod("set" + StringUtils.capitalize(name), field.getType());
                if (StringUtils.isNotBlank(application.getTypeValue())) {
                    method.invoke(config, valueOf(field.getType(), application.getTypeValue()));
                }
            } catch (Exception e) {
                LogUtil.error("get project config error.");
                LogUtil.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return config;
        }
        return config;
    }

    private Object valueOf(Class<?> type, String value) {
        //todo 其他类型
        if (type == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == Integer.class) {
            try {
                return Integer.valueOf(value);
            } catch (Exception e) {
                return 0;
            }
        }
        return value;
    }


    public Map<String, String> getCustomNumMapByProjectIds(List<String> projectIds) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria()
                .andProjectIdIn(projectIds)
                .andTypeEqualTo(ProjectApplicationType.CASE_CUSTOM_NUM.name())
                .andTypeValueEqualTo("true");

        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        return projectApplications.stream()
                .collect(Collectors.toMap(ProjectApplication::getProjectId, ProjectApplication::getType));
    }

    public Boolean checkCustomNumByProjectId(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andTypeEqualTo(ProjectApplicationType.CASE_CUSTOM_NUM.name())
                .andTypeValueEqualTo("true");
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        return projectApplications.size() > 0;
    }


    public List<String> getProjectIds(List<String> projectIds) {
        if(CollectionUtils.isEmpty(projectIds)){
            return new ArrayList<>();
        }
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria()
                .andProjectIdIn(projectIds)
                .andTypeEqualTo(ProjectApplicationType.POOL_ENABLE.name());
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        return  projectApplications.stream().map(ProjectApplication::getProjectId).collect(Collectors.toList());
    }
}
