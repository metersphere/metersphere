package io.metersphere.functional.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.FunctionalCaseHistoryLogDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileAssociationExample;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseLogService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseService functionalCaseService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;

    @Resource
    private BugMapper bugMapper;


    /**
     * 更新用例 日志
     *
     * @param request
     * @return
     */
    public LogDTO updateFunctionalCaseFileLog(FunctionalCaseAssociationFileRequest request) {
        FunctionalCaseHistoryLogDTO historyLogDTO = getOriginalValue(request.getCaseId());
        LogDTO dto = getUpdateLogDTO(request.getProjectId(), request.getCaseId(), historyLogDTO.getFunctionalCase().getName(), "/attachment/upload/file");
        dto.setModifiedValue(JSON.toJSONBytes(request));
        dto.setOriginalValue(JSON.toJSONBytes(historyLogDTO));
        return dto;
    }


    /**
     * 更新用例 日志
     *
     * @param request
     * @return
     */
    public LogDTO deleteFunctionalCaseFileLog(FunctionalCaseDeleteFileRequest request) {
        FunctionalCaseHistoryLogDTO historyLogDTO = getOriginalValue(request.getCaseId());
        LogDTO dto = getUpdateLogDTO(request.getProjectId(), request.getCaseId(), historyLogDTO.getFunctionalCase().getName(), "/attachment/delete/file");
        dto.setModifiedValue(JSON.toJSONBytes(request));
        dto.setOriginalValue(JSON.toJSONBytes(historyLogDTO));
        return dto;
    }


    /**
     * 更新用例 日志
     *
     * @param requests
     * @param files
     * @return
     */
    public LogDTO updateFunctionalCaseLog(FunctionalCaseEditRequest requests, List<MultipartFile> files) {
        FunctionalCaseHistoryLogDTO historyLogDTO = getOriginalValue(requests.getId());
        LogDTO dto = getUpdateLogDTO(requests.getProjectId(), requests.getId(), requests.getName(), "/functional/case/update");
        dto.setModifiedValue(JSON.toJSONBytes(requests));
        dto.setOriginalValue(JSON.toJSONBytes(historyLogDTO));
        return dto;
    }

    private FunctionalCaseHistoryLogDTO getOriginalValue(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey(id);

        //自定义字段
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdEqualTo(id);
        List<FunctionalCaseCustomField> customFields = functionalCaseCustomFieldMapper.selectByExample(fieldExample);

        //附件  本地 + 文件库
        FunctionalCaseAttachmentExample attachmentExample = new FunctionalCaseAttachmentExample();
        attachmentExample.createCriteria().andCaseIdEqualTo(id);
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(attachmentExample);

        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(id);
        List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(example);


        FunctionalCaseHistoryLogDTO historyLogDTO = new FunctionalCaseHistoryLogDTO(functionalCase, functionalCaseBlob, customFields, caseAttachments, fileAssociationList);
        return historyLogDTO;
    }


    /**
     * 删除用例 日志
     *
     * @param request
     * @return
     */
    public LogDTO deleteFunctionalCaseLog(FunctionalCaseDeleteRequest request) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getId());
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }

    public List<LogDTO> batchDeleteFunctionalCaseLog(FunctionalCaseBatchRequest request) {
        List<String> ids = functionalCaseService.doSelectIds(request, request.getProjectId());
        return batchDeleteFunctionalCaseLogByIds(ids, "/functional/case/batch/deleteToGc");
    }

    public List<LogDTO> batchDeleteFunctionalCaseLogByIds(List<String> ids, String path) {
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<FunctionalCase> functionalCases = extFunctionalCaseMapper.getLogInfo(ids, false);
            functionalCases.forEach(functionalCase -> {
                LogDTO dto = new LogDTO(
                        functionalCase.getProjectId(),
                        null,
                        functionalCase.getId(),
                        null,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                        functionalCase.getName());

                dto.setPath(path);
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    /**
     * 恢复项目
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    "",
                    id,
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_RECYCLE,
                    functionalCase.getName());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }

    /**
     * 恢复项目
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public List<LogDTO> batchRecoverLog(FunctionalCaseBatchRequest request) {
        List<String> ids = getSelectIdsByTrash(request, request.getProjectId());
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<FunctionalCase> functionalCases = extFunctionalCaseMapper.getLogInfo(ids, true);
            functionalCases.forEach(functionalCase -> {
                LogDTO dto = new LogDTO(
                        functionalCase.getProjectId(),
                        "",
                        functionalCase.getId(),
                        null,
                        OperationLogType.RECOVER.name(),
                        OperationLogModule.CASE_MANAGEMENT_CASE_RECYCLE,
                        functionalCase.getName());

                dto.setPath("/functional/case/batch/recover");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    /**
     * 彻底删除
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteTrashCaseLog(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_RECYCLE,
                    functionalCase.getName());

            dto.setPath("/functional/case/trash/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }


    /**
     * 取消关联
     *
     * @param id ID
     * @return 日志详情
     */
    public LogDTO disassociateLog(String id) {
        FunctionalCaseDemand functionalCaseDemand = functionalCaseDemandMapper.selectByPrimaryKey(id);
        if (functionalCaseDemand == null) {
            return null;
        }
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseDemand.getCaseId());
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DISASSOCIATE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/demand/cancel/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCaseDemand));
            return dto;
        }
        return null;
    }

    /**
     * 取消关联
     *
     * @param request request
     * @return 日志详情
     */
    public LogDTO disassociateCaseLog(DisassociateOtherCaseRequest request) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getCaseId());
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DISASSOCIATE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/test/disassociate/case");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }


    public List<LogDTO> batchEditFunctionalCaseLog(FunctionalCaseBatchEditRequest request) {
        List<String> ids = functionalCaseService.doSelectIds(request, request.getProjectId());
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<FunctionalCase> functionalCases = extFunctionalCaseMapper.getLogInfo(ids, false);
            functionalCases.forEach(functionalCase -> {
                FunctionalCaseHistoryLogDTO historyLogDTO = getOriginalValue(functionalCase.getId());
                LogDTO dto = getUpdateLogDTO(functionalCase.getProjectId(), functionalCase.getId(), functionalCase.getName(), "/functional/case/batch/edit");
                dto.setOriginalValue(JSON.toJSONBytes(historyLogDTO));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public List<LogDTO> batchDeleteTrashCaseLog(FunctionalCaseBatchRequest request) {
        List<String> ids = getSelectIdsByTrash(request, request.getProjectId());
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<FunctionalCase> functionalCases = extFunctionalCaseMapper.getLogInfo(ids, true);
            functionalCases.forEach(functionalCase -> {
                LogDTO dto = new LogDTO(
                        functionalCase.getProjectId(),
                        null,
                        functionalCase.getId(),
                        null,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.CASE_MANAGEMENT_CASE_RECYCLE,
                        functionalCase.getName());

                dto.setPath("/functional/case/batch/delete");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public <T> List<String> getSelectIdsByTrash(T dto, String projectId) {
        BaseFunctionalCaseBatchDTO request = (BaseFunctionalCaseBatchDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseMapper.getIds(request, projectId, true);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public LogDTO disassociateBugLog(String id) {
        BugRelationCase bugRelationCase = bugRelationCaseMapper.selectByPrimaryKey(id);
        if (bugRelationCase != null) {
            Bug bug = bugMapper.selectByPrimaryKey(bugRelationCase.getBugId());
            LogDTO dto = new LogDTO(
                    null,
                    null,
                    bugRelationCase.getBugId(),
                    null,
                    OperationLogType.DISASSOCIATE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                    bug.getTitle() + "缺陷");

            dto.setPath("/functional/case/test/disassociate/bug/" + id);
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bugRelationCase));
            return dto;
        }
        return null;
    }


    public LogDTO disassociateRelateLog(RelationshipDeleteRequest request) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getCaseId());
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DISASSOCIATE.name(),
                    OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/relationship/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }

    @NotNull
    private static LogDTO getUpdateLogDTO(String projectId, String sourceId, String content, String path) {
        LogDTO dto = new LogDTO(
                projectId,
                null,
                sourceId,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                content);
        dto.setHistory(true);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }


    public LogDTO exportExcelLog(FunctionalCaseExportRequest request, String url, String userId, String orgId) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                orgId,
                request.getFileId(),
                userId,
                OperationLogType.EXPORT.name(),
                OperationLogModule.CASE_MANAGEMENT_CASE_CASE,
                "");
        dto.setHistory(true);
        dto.setPath("/functional/case/export/" + url);
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }
}
