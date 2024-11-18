package io.metersphere.functional.service;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewModule;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewModuleLogService {
    private static final String CASE_REVIEW_MODULE = "/case/review/module";
    private static final String ADD = CASE_REVIEW_MODULE + "/add";
    private static final String UPDATE = CASE_REVIEW_MODULE + "/update";
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    /**
     * 评审新增模块日志
     *
     * @param caseReviewModule
     * @param userId
     */
    public void addModuleLog(CaseReviewModule caseReviewModule, String userId) {
        Project project = projectMapper.selectByPrimaryKey(caseReviewModule.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(caseReviewModule.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW_MODULE)
                .method(HttpMethodConstants.POST.name())
                .path(ADD)
                .sourceId(caseReviewModule.getId())
                .content(caseReviewModule.getName())
                .originalValue(JSON.toJSONBytes(caseReviewModule))
                .createUser(userId)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 评审更新模块日志
     *
     * @param updateModule
     * @param operator
     */
    public void updateModuleLog(CaseReviewModule updateModule, String operator) {
        Project project = projectMapper.selectByPrimaryKey(updateModule.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW_MODULE)
                .method(HttpMethodConstants.POST.name())
                .path(UPDATE)
                .sourceId(updateModule.getId())
                .content(updateModule.getName())
                .originalValue(JSON.toJSONBytes(updateModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }


    /**
     * 评审删除模块日志
     *
     * @param deleteModule
     * @param projectId
     * @param userId
     * @param path
     */
    public void batchDelLog(List<CaseReviewModule> deleteModule, String projectId, String userId, String path) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> dtoList = new ArrayList<>();
        deleteModule.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    project.getOrganizationId(),
                    item.getId(),
                    userId,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW_MODULE,
                    item.getName() + " " + Translator.get("log.delete_module"));
            dto.setPath(path);
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }
}
