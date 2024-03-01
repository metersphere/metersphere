package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.mapper.BugContentMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugLogService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private BugService bugService;
    @Resource
    private BugContentMapper bugContentMapper;


    /**
     * 新增缺陷日志
     *
     * @param request 请求参数
     * @param files 文件
     * @return 日志
     */
    public LogDTO addLog(BugEditRequest request, List<MultipartFile> files) {
        LogDTO dto = new LogDTO(request.getProjectId(), null, null, null, OperationLogType.ADD.name(), OperationLogModule.BUG_MANAGEMENT, request.getTitle());
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
    public LogDTO updateLog(BugEditRequest request, List<MultipartFile> files) {
        BugDTO history = getOriginalValue(request.getId());
        LogDTO dto = new LogDTO(request.getProjectId(), null, request.getId(), null, OperationLogType.UPDATE.name(), OperationLogModule.BUG_MANAGEMENT, request.getTitle());
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
    public LogDTO deleteLog(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null) {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.DELETE.name(), OperationLogModule.BUG_MANAGEMENT, bug.getTitle());
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
    public LogDTO recoverLog(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug != null) {
            LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.RECOVER.name(), OperationLogModule.BUG_MANAGEMENT, bug.getTitle());
            dto.setPath("/bug/trash/recover");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(bug));
            return dto;
        }
        return null;
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
        return bugService.handleCustomField(List.of(originalBug), originalBug.getProjectId()).get(0);
    }
}
