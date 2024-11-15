package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewLogService {

    @Resource
    private CaseReviewMapper caseReviewMapper;

    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;

    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    /**
     * 新增用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO addCaseReviewLog(CaseReviewRequest requests) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                requests.getName());

        dto.setPath("/case/review/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }

    /**
     * 复制用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO copyCaseReviewLog(CaseReviewRequest requests) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.COPY.name(),
                OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                requests.getName());

        dto.setPath("/case/review/copy");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }

    /**
     * 更新用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO updateCaseReviewLog(CaseReviewRequest requests) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(requests.getId());
        if (caseReview == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                caseReview.getCreateUser(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                requests.getName());

        dto.setPath("/case/review/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(caseReview));
        return dto;
    }
    /**
     * 排序更新用例评审 日志
     *
     * @param request 页面参数
     * @return LogDTO
     */
    public LogDTO updateCaseReviewLogByPos(PosRequest request) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(request.getTargetId());
        if (caseReview == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                caseReview.getCreateUser(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                caseReview.getName());

        dto.setPath("/case/edit/pos");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(caseReview));
        return dto;
    }


    /**
     * 删除用例 日志
     *
     * @param reviewId reviewId
     * @return LogDTO
     */
    public LogDTO deleteFunctionalCaseLog(String reviewId) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        if (caseReview != null) {
            LogDTO dto = new LogDTO(
                    caseReview.getProjectId(),
                    null,
                    caseReview.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                    caseReview.getName());

            dto.setPath("/case/review/delete");
            dto.setMethod(HttpMethodConstants.DELETE.name());
            dto.setOriginalValue(JSON.toJSONBytes(caseReview));
            return dto;
        }
        return null;
    }

    public List<LogDTO> associateCaseLog(CaseReviewAssociateRequest request) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(request.getReviewId());
        if (caseReview == null) {
            return null;
        }
        List<LogDTO> dtoList = new ArrayList<>();
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        if (CollectionUtils.isEmpty(caseIds)) {
            return null;
        }
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdIn(caseIds);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(functionalCases)) {
            return null;
        }
        for (FunctionalCase functionalCase : functionalCases) {
            LogDTO dto = new LogDTO(
                    caseReview.getProjectId(),
                    null,
                    caseReview.getId(),
                    null,
                    OperationLogType.ASSOCIATE.name(),
                    OperationLogModule.CASE_REVIEW_DETAIL,
                    functionalCase.getName());

            dto.setPath("/case/review/associate");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            dtoList.add(dto);
        }

        return dtoList;
    }

    public LogDTO disAssociateCaseLog(String reviewId, String caseId) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        if (caseReview == null) {
            return null;
        }
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
        if (functionalCase == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                null,
                OperationLogType.DISASSOCIATE.name(),
                OperationLogModule.CASE_REVIEW_DETAIL,
                functionalCase.getName());

        dto.setPath("/case/review/disassociate");
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
        return dto;
    }


    public List<LogDTO> batchDisassociateCaseLog(BaseReviewCaseBatchRequest request) {
        List<String> ids = caseReviewFunctionalCaseService.doSelectIds(request);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(id -> {
                CaseReviewFunctionalCase caseReviewFunctionalCase = caseReviewFunctionalCaseMapper.selectByPrimaryKey(id);
                FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseReviewFunctionalCase.getCaseId());
                if (caseReviewFunctionalCase != null) {
                    LogDTO dto = new LogDTO(
                            null,
                            null,
                            caseReviewFunctionalCase.getId(),
                            null,
                            OperationLogType.DISASSOCIATE.name(),
                            OperationLogModule.CASE_REVIEW_DETAIL,
                            functionalCase.getName());
                    dto.setPath("/case/review/batch/disassociate");
                    dto.setMethod(HttpMethodConstants.POST.name());
                    dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                    dtoList.add(dto);
                }
            });
        }
        return dtoList;
    }

    /**
     * 移动更新用例评审
     * @param request
     * @return
     */
    public List<LogDTO> updateBatchCaseReviewLog(CaseReviewBatchRequest request) {
        if (StringUtils.isBlank(request.getMoveModuleId())) {
           return new ArrayList<>();
        }
        List<String> ids = doSelectReviewIds(request);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            CaseReviewExample caseReviewExample = new CaseReviewExample();
            caseReviewExample.createCriteria().andIdIn(ids);
            List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
            caseReviews.forEach(caseReview -> {
                LogDTO dto = new LogDTO(
                        caseReview.getProjectId(),
                        null,
                        caseReview.getId(),
                        caseReview.getCreateUser(),
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.CASE_MANAGEMENT_REVIEW_REVIEW,
                        caseReview.getName());

                dto.setPath("/case/batch/move");
                dto.setMethod(HttpMethodConstants.POST.name());
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public <T> List<String> doSelectIds(T dto, String projectId) {
        BaseFunctionalCaseBatchDTO request = (BaseFunctionalCaseBatchDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseMapper.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public List<String>doSelectReviewIds(CaseReviewBatchRequest request) {
        List<String> ids;
        if (request.isSelectAll()) {
            ids = extCaseReviewMapper.getIds(request, request.getProjectId());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
        } else {
            ids = request.getSelectIds();
        }
        return ids;
    }


    public void createCaseAndAssociateLog(CaseReview caseReview, FunctionalCase functionalCase, String userId) {
        Project project = projectMapper.selectByPrimaryKey(caseReview.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(caseReview.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ASSOCIATE.name())
                .module(OperationLogModule.CASE_REVIEW_DETAIL)
                .method(HttpMethodConstants.POST.name())
                .path("/functional/case/add")
                .sourceId(caseReview.getId())
                .content(functionalCase.getName())
                .createUser(userId)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
