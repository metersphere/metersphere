package io.metersphere.system.service;

import io.metersphere.constants.HttpMethodConstants;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemProjectLogService {

    @Resource
    private ProjectMapper projectMapper;

    private static final String PRE_URI = "/system/project";

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO addLog(AddProjectRequest project) {
        LogDTO dto = new LogDTO(
                "system",
                "",
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_PROJECT,
                project.getName());

        dto.setPath(PRE_URI + "/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(project));
        return dto;
    }

    /**
     * @param request
     * @return
     */
    public LogDTO updateLog(UpdateProjectRequest request) {
        Project project = projectMapper.selectByPrimaryKey(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    "",
                    project.getId(),
                    project.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PROJECT,
                    "编辑全局用户组对应的权限配置");
            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    public LogDTO updateLog(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    "",
                    project.getId(),
                    project.getCreateUser(),
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.SYSTEM_PROJECT,
                    "编辑全局用户组对应的权限配置");
            dto.setPath("/revoke");
            dto.setMethod(HttpMethodConstants.GET.name());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }


    /**
     * 删除接口日志
     *
     * @param id
     * @return
     */
    public LogDTO deleteLog(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    "",
                    id,
                    project.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SYSTEM_PROJECT,
                    project.getName());

            dto.setPath("/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }
}
