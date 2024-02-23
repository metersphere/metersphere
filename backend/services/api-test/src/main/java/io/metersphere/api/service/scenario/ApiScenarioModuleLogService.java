package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioModule;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioModuleLogService {
    private static final String API_DEBUG_MODULE = "/api/scenario/module";
    private static final String ADD = API_DEBUG_MODULE + "/add";
    private static final String UPDATE = API_DEBUG_MODULE + "/update";
    private static final String DELETE = API_DEBUG_MODULE + "/delete";
    private static final String MOVE = API_DEBUG_MODULE + "/move";
    private static final String MOVE_TO = "file.log.move_to";

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveAddLog(ApiScenarioModule module, String operator) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE)
                .method(HttpMethodConstants.POST.name())
                .path(ADD)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(ApiScenarioModule module, String operator) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE)
                .method(HttpMethodConstants.POST.name())
                .path(UPDATE)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();

        operationLogService.add(dto);
    }

    public void saveDeleteModuleLog(List<BaseTreeNode> deleteModule, String operator, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        deleteModule.forEach(item -> {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(project.getId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.DELETE.name())
                    .module(OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE)
                    .method(HttpMethodConstants.GET.name())
                    .path(DELETE + "/%s")
                    .sourceId(item.getId())
                    .content(item.getName() + " " + Translator.get("log.delete_module"))
                    .createUser(operator)
                    .build().getLogDTO();
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }

    public void saveDeleteDataLog(List<ApiScenario> deleteData, String operator, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        deleteData.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.DELETE.name())
                            .module(OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE)
                            .method(HttpMethodConstants.GET.name())
                            .path(DELETE + "/%s")
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void saveMoveLog(@Validated NodeSortDTO request, String operator) {
        BaseModule moveNode = request.getNode();
        BaseModule previousNode = request.getPreviousNode();
        BaseModule nextNode = request.getNextNode();
        BaseModule parentModule = request.getParent();

        Project project = projectMapper.selectByPrimaryKey(moveNode.getProjectId());
        String logContent;
        if (nextNode == null && previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get(MOVE_TO) + parentModule.getName();
        } else if (nextNode == null) {
            logContent = moveNode.getName() + " " + Translator.get(MOVE_TO) + parentModule.getName() + " " + previousNode.getName() + Translator.get("file.log.next");
        } else if (previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get(MOVE_TO) + parentModule.getName() + " " + nextNode.getName() + Translator.get("file.log.previous");
        } else {
            logContent = moveNode.getName() + " " + Translator.get(MOVE_TO) + parentModule.getName() + " " +
                    previousNode.getName() + Translator.get("file.log.next") + " " + nextNode.getName() + Translator.get("file.log.previous");
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(moveNode.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE)
                .method(HttpMethodConstants.POST.name())
                .path(MOVE)
                .sourceId(moveNode.getId())
                .content(logContent)
                .originalValue(JSON.toJSONBytes(moveNode))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
