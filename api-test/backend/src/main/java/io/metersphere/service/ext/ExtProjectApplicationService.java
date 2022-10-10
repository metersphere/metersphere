package io.metersphere.service.ext;

import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ProjectApplicationExample;
import io.metersphere.base.mapper.ProjectApplicationMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.request.AddProjectRequest;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseProjectService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ExtProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private UserMapper userMapper;

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
        } else {
            projectApplicationMapper.insertSelective(conf);
        }

        if (StringUtils.equals(type, ProjectApplicationType.MOCK_TCP_PORT.name())) {
            //检查Mock环境是否需要同步更新
            baseEnvironmentService.getMockEnvironmentByProjectId(projectId);
        } else if (StringUtils.equals(type, ProjectApplicationType.MOCK_TCP_OPEN.name())) {
            this.doHandleMockTcpStatus(projectId, value);
        }
    }

    private void doHandleMockTcpStatus(String projectId, String value) {
        boolean isOpen = Boolean.parseBoolean(value);
        int port = 0;
        //获取旧端口
        ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(projectId, ProjectApplicationType.MOCK_TCP_PORT.name());
        Integer oldPort = config.getMockTcpPort();
        if (oldPort != null) {
            port = oldPort;
        }

        AddProjectRequest project = new AddProjectRequest();
        project.setMockTcpPort(port);
        project.setId(projectId);
        if (!isOpen) {
            this.closeMockTcp(project);
        } else {
            if (port == 0) {
                MSException.throwException("tcp port cannot be 0");
            }
            if (port > 0) {
                baseProjectService.checkMockTcpPort(port);
            }
            this.checkProjectTcpPort(project);
            if (BooleanUtils.isTrue(Boolean.parseBoolean(value))) {
                this.reloadMockTcp(project, port);
            } else {
                this.closeMockTcp(project);
            }
        }
    }

    public void reloadMockTcp(Project project, int oldPort) {
        this.closeMockTcp(oldPort);
        this.openMockTcp(project);
    }

    public void openMockTcp(Project project) {
        if (project != null) {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                TCPPool.createTcp(mockPort);
            }
        }
    }

    public void checkProjectTcpPort(AddProjectRequest project) {
        //判断端口是否重复
        if (project.getMockTcpPort() != null && project.getMockTcpPort().intValue() != 0) {
            String projectId = StringUtils.isEmpty(project.getId()) ? "" : project.getId();
            ProjectApplicationExample example = new ProjectApplicationExample();
            example.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name()).andTypeValueEqualTo(String.valueOf(project.getMockTcpPort())).andProjectIdNotEqualTo(projectId);
            if (projectApplicationMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("tcp_mock_not_unique"));
            }
        }
    }

    public void closeMockTcp(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        this.closeMockTcp(project);
    }

    public void closeMockTcp(Project project) {
        if (project == null) {
            MSException.throwException("Project not found!");
        } else {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                this.closeMockTcp(mockPort);
            }
        }
    }

    public void closeMockTcp(int tcpPort) {
        if (tcpPort != 0) {
            TCPPool.closeTcp(tcpPort);
        }
    }
    public void updateCurrentUserByResourceId(String resourceId) {
        Project project = baseProjectMapper.selectProjectByResourceId(resourceId);
        if (project == null) {
            return;
        }
        SessionUser user = SessionUtils.getUser();
        user.setLastProjectId(project.getId());
        user.setLastWorkspaceId(project.getWorkspaceId());
        userMapper.updateByPrimaryKeySelective(user);
    }
}
