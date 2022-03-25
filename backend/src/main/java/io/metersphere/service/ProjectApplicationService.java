package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.CaseFormat;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectApplicationMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.AddProjectRequest;
import io.metersphere.controller.request.ProjectApplicationRequest;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestCaseService testCaseService;
    @Lazy
    @Resource
    private ProjectService projectService;

    public void updateProjectApplication(ProjectApplication projectApplication) {
        this.doBeforeUpdate(projectApplication);
        this.createOrUpdateConfig(projectApplication);
    }

    private void doBeforeUpdate(ProjectApplication projectApplication) {
        String type = projectApplication.getType();
        String value = projectApplication.getTypeValue();
        String projectId = projectApplication.getProjectId();
        if (StringUtils.equals(type, ProjectApplicationType.CASE_CUSTOM_NUM.name())
                && BooleanUtils.isTrue(Boolean.parseBoolean(value))) {
            testCaseService.updateTestCaseCustomNumByProjectId(projectId);
        } else if (StringUtils.equals(type, ProjectApplicationType.MOCK_TCP_PORT.name())) {
            this.doHandleMockTcpPort(projectId, value);
        } else if (StringUtils.equals(type, ProjectApplicationType.CLEAN_TRACK_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.CLEAN_API_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.CLEAN_LOAD_REPORT.name())) {
            this.doHandleCleanUp(projectId, type, value);
        }
    }

    private void doHandleCleanUp(String projectId, String type, String value) {
        AddProjectRequest request = new AddProjectRequest();
        request.setId(projectId);
        Boolean cleanUp = Boolean.valueOf(value);
        if (BooleanUtils.isTrue(cleanUp)) {
            request.setCleanTrackReport(cleanUp);
        } else {
            ProjectConfig config = getProjectConfig(projectId);
            if (StringUtils.equals(type, ProjectApplicationType.CLEAN_TRACK_REPORT.name())) {
                config.setCleanTrackReport(cleanUp);
            } else if (StringUtils.equals(type, ProjectApplicationType.CLEAN_API_REPORT.name())) {
                config.setCleanApiReport(cleanUp);
            } else if (StringUtils.equals(type, ProjectApplicationType.CLEAN_LOAD_REPORT.name())) {
                config.setCleanLoadReport(cleanUp);
            }
            // 根据这三个状态判断定时清理任务是否开启
            request.setCleanTrackReport(config.getCleanTrackReport());
            request.setCleanApiReport(config.getCleanApiReport());
            request.setCleanLoadReport(config.getCleanLoadReport());
        }
        projectService.addOrUpdateCleanUpSchedule(request);
    }

    private void doHandleMockTcpPort(String projectId, String value) {

        int lastTcpNum = 0;
        //获取旧端口
        ProjectConfig config = getSpecificTypeValue(projectId, ProjectApplicationType.MOCK_TCP_PORT.name());
        Integer oldPort = config.getMockTcpPort();
        if (oldPort != null) {
            lastTcpNum = oldPort;
        }

        int port;
        try {
            port = Integer.parseInt(value);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            LogUtil.error("update project config parse mock port error.");
            port = 0;
        }

        if (port == 0) {
            MSException.throwException("tcp port cannot be 0");
        }

        if (port > 0) {
            projectService.checkMockTcpPort(port);
        }

        AddProjectRequest project = new AddProjectRequest();
        project.setMockTcpPort(port);
        project.setId(projectId);
        projectService.checkProjectTcpPort(project);

        if (lastTcpNum != port) {
            TCPPool.closeTcp(lastTcpNum);
        }

    }

    private void doHandleMockTcpStatus(String projectId, String value) {

        boolean isOpen = Boolean.parseBoolean(value);
        int port = 0;
        //获取旧端口
        ProjectConfig config = getSpecificTypeValue(projectId, ProjectApplicationType.MOCK_TCP_PORT.name());
        Integer oldPort = config.getMockTcpPort();
        if (oldPort != null) {
            port = oldPort;
        }

        AddProjectRequest project = new AddProjectRequest();
        project.setMockTcpPort(port);
        project.setId(projectId);
        if(!isOpen){
            projectService.closeMockTcp(project);
        }else {
            if (port == 0) {
                MSException.throwException("tcp port cannot be 0");
            }
            if (port > 0) {
                projectService.checkMockTcpPort(port);
            }
            projectService.checkProjectTcpPort(project);
            if (BooleanUtils.isTrue(Boolean.parseBoolean(value))) {
                projectService.reloadMockTcp(project, port);
            } else {
                projectService.closeMockTcp(project);
            }
        }
    }

    public void updateProjectApplication(String projectId, String type, String value) {
        ProjectApplication application = new ProjectApplication();
        application.setProjectId(projectId);
        application.setType(type);
        application.setTypeValue(value);
        this.updateProjectApplication(application);
    }

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

    public void createOrUpdateConfig(String projectId, String type, String value) {
        ProjectApplication conf = new ProjectApplication();
        conf.setProjectId(projectId);
        conf.setType(type);
        conf.setTypeValue(value);
        this.createOrUpdateConfig(conf);
    }

    public void createOrUpdateConfig(ProjectApplication conf) {
        String projectId = conf.getProjectId();
        String type = conf.getType();
        String value = conf.getTypeValue();
        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(type) || StringUtils.isEmpty(value)) {
            LogUtil.error("create or update project config error. project id or conf type or value is blank.");
            return;
        }
        LogUtil.info("create or update project config: " + projectId + "-" + type + "-" + value);
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
            projectApplicationMapper.updateByExample(conf, example);
        }else {
            projectApplicationMapper.insertSelective(conf);
        }

        if(StringUtils.equals(type,ProjectApplicationType.MOCK_TCP_PORT.name())){
            //检查Mock环境是否需要同步更新
            ApiTestEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            if (apiTestEnvironmentService != null) {
                apiTestEnvironmentService.getMockEnvironmentByProjectId(projectId);
            }
        } else if (StringUtils.equals(type, ProjectApplicationType.MOCK_TCP_OPEN.name())) {
            this.doHandleMockTcpStatus(projectId, value);
        }

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

    public void updateProjectConfigBatch(ProjectApplicationRequest request) {
        List<ProjectApplication> applications = request.getConfigs();
        if (CollectionUtils.isNotEmpty(applications)) {
            List<ProjectApplication> applicationByTcp = new ArrayList<>();
            for (ProjectApplication application : applications) {
                if (StringUtils.equals(application.getType(), ProjectApplicationType.MOCK_TCP_PORT.name())) {
                    applicationByTcp.add(0, application);
                } else if (StringUtils.equals(application.getType(), ProjectApplicationType.MOCK_TCP_OPEN.name())) {
                    applicationByTcp.add(application);
                } else {
                    this.updateProjectApplication(application);
                }
            }

            if (CollectionUtils.isNotEmpty(applicationByTcp)) {
                for (ProjectApplication application : applicationByTcp) {
                    this.updateProjectApplication(application);
                }
            }
        }
    }
}
