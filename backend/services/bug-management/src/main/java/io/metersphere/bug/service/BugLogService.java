package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.domain.BugContentExample;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.mapper.BugContentMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugLogService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private BugService bugService;
    @Resource
    private BugContentMapper bugContentMapper;
    @Resource
    private OperationLogService operationLogService;


    /**
     * 新增缺陷日志
     *
     * @param request 请求参数
     * @param files 文件
     * @return 日志
     */
    @SuppressWarnings("unused")
    public LogDTO addLog(BugEditRequest request, List<MultipartFile> files) {
        LogDTO dto = new LogDTO(request.getProjectId(), null, null, null, OperationLogType.ADD.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, getPlatformTitle(request));
        dto.setHistory(true);
        dto.setPath("/bug/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 更新缺陷日志
     *
     * @param request 请求参数
     * @param files  文件
     * @return 日志
     */
    @SuppressWarnings("unused")
    public LogDTO updateLog(BugEditRequest request, List<MultipartFile> files) {
        BugDTO history = getOriginalValue(request.getId());
        LogDTO dto = new LogDTO(request.getProjectId(), null, request.getId(), null, OperationLogType.UPDATE.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, getPlatformTitle(request));
        dto.setHistory(true);
        dto.setPath("/bug/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        dto.setOriginalValue(JSON.toJSONBytes(history));
        return dto;
    }

    /**
     * 删除缺陷日志
     *
     * @param id 缺陷ID
     * @return 日志
     */
    @SuppressWarnings("unused")
    public LogDTO deleteLog(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null) {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.DELETE.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, bug.getTitle());
            dto.setHistory(true);
            dto.setPath("/bug/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            return dto;
        }
        return null;
    }

    /**
     * 恢复缺陷日志
     *
     * @param id 缺陷ID
     * @return 日志
     */
    @SuppressWarnings("unused")
    public LogDTO recoverLog(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null) {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.RECOVER.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, bug.getTitle());
            dto.setHistory(true);
            dto.setPath("/bug/trash/recover");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            return dto;
        }
        return null;
    }

    /**
     * 批量删除缺陷日志
     * @param request 请求参数
     * @return 日志
     */
    public List<LogDTO> batchDeleteLog(BugBatchRequest request) {
        List<String> batchIds = bugService.getBatchIdsByRequest(request);
        BugExample example = new BugExample();
        example.createCriteria().andIdIn(batchIds);
        List<Bug> bugs = bugMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bugs)) {
            return null;
        }
        List<LogDTO> logs = new ArrayList<>();
        bugs.forEach(bug -> {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.DELETE.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, bug.getTitle());
            dto.setHistory(true);
            dto.setPath("/bug/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            logs.add(dto);
        });
        return logs;
    }

    /**
     * 删除回收站缺陷日志
     *
     * @param id 缺陷ID
     * @return 日志
     */
    @SuppressWarnings("unused")
    public LogDTO deleteTrashLog(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null) {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.DELETE.name(), OperationLogModule.BUG_MANAGEMENT_RECYCLE, bug.getTitle());
            dto.setHistory(true);
            dto.setPath("/bug/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            return dto;
        }
        return null;
    }

    /**
     * 批量删除回收站缺陷日志
     * @param request 请求参数
     * @return 日志
     */
    public List<LogDTO> batchDeleteTrashLog(BugBatchRequest request) {
        request.setUseTrash(true);
        List<String> batchIds = bugService.getBatchIdsByRequest(request);
        if (CollectionUtils.isEmpty(batchIds)) {
            return null;
        }
        BugExample example = new BugExample();
        example.createCriteria().andIdIn(batchIds);
        List<Bug> bugs = bugMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bugs)) {
            return null;
        }
        List<LogDTO> logs = new ArrayList<>();
        bugs.forEach(bug -> {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.DELETE.name(), OperationLogModule.BUG_MANAGEMENT_RECYCLE, bug.getTitle());
            dto.setHistory(true);
            dto.setPath("/bug/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            logs.add(dto);
        });
        return logs;
    }

    /**
     * 获取历史缺陷
     * @param id 缺陷ID
     * @return 缺陷DTO
     */
    public BugDTO getOriginalValue(String id) {
        // 缺陷基础信息
        BugDTO originalBug = new BugDTO();
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug == null) {
            return null;
        }
        BeanUtils.copyBean(originalBug, bug);
        BugContent bugContent = bugContentMapper.selectByPrimaryKey(id);
        if (bugContent != null) {
            originalBug.setDescription(bugContent.getDescription());
        }
        // 缺陷自定义字段
        return bugService.handleCustomField(List.of(originalBug), originalBug.getProjectId()).getFirst();
    }

    /**
     * 获取历史缺陷
     * @param ids 缺陷ID集合
     * @return 缺陷DTO
     */
    public List<BugDTO> getOriginalValueByIds(List<String> ids) {
        // 缺陷基础信息
        List<BugDTO> bugOriginalList = new ArrayList<>();
        BugExample example = new BugExample();
        example.createCriteria().andIdIn(ids);
        List<Bug> bugs = bugMapper.selectByExample(example);
        BugContentExample contentExample = new BugContentExample();
        contentExample.createCriteria().andBugIdIn(ids);
        List<BugContent> bugContents = bugContentMapper.selectByExample(contentExample);
        bugs.forEach(bug -> {
            BugDTO originalBug = new BugDTO();
            BeanUtils.copyBean(originalBug, bug);
			bugContents.stream().filter(content -> StringUtils.equals(content.getBugId(), bug.getId())).findFirst().ifPresent(bugContent -> originalBug.setDescription(bugContent.getDescription()));
			bugOriginalList.add(originalBug);
        });
        // 缺陷自定义字段
        return bugService.handleCustomField(bugOriginalList, bugOriginalList.getFirst().getProjectId());
    }

    /**
     * 获取缺陷的标题
     * @param request 请求参数
     * @return 缺陷标题
     */
    private String getPlatformTitle(BugEditRequest request) {
        Optional<BugCustomFieldDTO> find = request.getCustomFields().stream().filter(field -> StringUtils.equalsAny(field.getId(), "summary", "title")).findFirst();
        BugCustomFieldDTO titleField = find.orElseGet(BugCustomFieldDTO::new);
        return StringUtils.isNotBlank(request.getTitle()) ? request.getTitle() : titleField.getValue();
    }


    @SuppressWarnings("unused")
    public void minderAddLog(BugEditRequest request, List<MultipartFile> files, String orgId, String bugId, String userId) {
        LogDTO dto = new LogDTO(request.getProjectId(), orgId, bugId, userId, OperationLogType.ADD.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, getPlatformTitle(request));
        dto.setHistory(true);
        dto.setPath("/test-plan/functional/case/minder/batch/add-bug");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        operationLogService.add(dto);
    }
}
