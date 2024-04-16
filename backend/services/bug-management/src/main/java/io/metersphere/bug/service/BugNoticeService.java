package io.metersphere.bug.service;

import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.BugNoticeDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugNoticeService {

    public static final String CUSTOM_TITLE = "summary";
    public static final String CUSTOM_STATUS = "status";
    public static final String CUSTOM_HANDLE_USER = "处理人";

    @Resource
    private BugService bugService;
    @Resource
    private BugLogService bugLogService;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private BugStatusService bugStatusService;

    /**
     * 获取缺陷通知
     * @param request 请求参数
     * @return 缺陷通知
     */
    public BugNoticeDTO getNoticeByRequest(BugEditRequest request) {
        // 获取状态选项, 处理人选项
        Map<String, String> statusMap = getStatusMap(request.getProjectId());
        Map<String, String> handlerMap = getHandleMap(request.getProjectId());
        if (StringUtils.isEmpty(request.getId())) {
            // 构建通知对象
            BugNoticeDTO notice = new BugNoticeDTO();
            notice.setTitle(request.getTitle());
            // 自定义字段解析{name: value}
            if (CollectionUtils.isNotEmpty(request.getCustomFields())) {
                List<OptionDTO> fields = new ArrayList<>();
                request.getCustomFields().forEach(field -> {
                    if (StringUtils.equals(field.getId(), CUSTOM_TITLE)) {
                        // TITLE {标题为空时, 从自定义字段中获取标题}
                        notice.setTitle(field.getValue());
                    } else if (StringUtils.equals(field.getId(), CUSTOM_STATUS)) {
                        // 状态 {从自定义字段中获取状态}
                        notice.setStatus(statusMap.get(field.getValue()));
                    } else if (StringUtils.equals(field.getName(), CUSTOM_HANDLE_USER)) {
                        // 处理人 {从自定义字段中获取状态}
                        notice.setHandleUser(handlerMap.get(field.getValue()));
                    } else {
                        // 其他自定义字段
                        OptionDTO fieldDTO = new OptionDTO();
                        fieldDTO.setId(field.getName());
                        fieldDTO.setName(field.getValue());
                        fields.add(fieldDTO);
                    }
                });
                notice.setFields(fields);
            }
            return notice;
        } else {
            // 需设置业务ID(用来通知关注人), 创建人
            return getNoticeById(request.getId());
        }
    }

    /**
     * 获取缺陷通知
     * @param id 缺陷ID
     */
    public BugNoticeDTO getNoticeById(String id) {
        // 缺陷基础信息
        BugDTO bugDTO = bugLogService.getOriginalValue(id);
        if (bugDTO == null) {
            return null;
        }
        // 构建通知对象
        BugNoticeDTO notice = new BugNoticeDTO();
        BeanUtils.copyBean(notice, bugDTO);
        // 自定义字段解析{name: value}
        if (CollectionUtils.isNotEmpty(bugDTO.getCustomFields())) {
            List<OptionDTO> fields = new ArrayList<>();
            bugDTO.getCustomFields().forEach(field -> {
                // 其他自定义字段
                OptionDTO fieldDTO = new OptionDTO();
                fieldDTO.setId(field.getName());
                fieldDTO.setName(field.getValue());
                fields.add(fieldDTO);
            });
            notice.setFields(fields);
        }
        return notice;
    }

    /**
     * 获取批量操作的缺陷通知
     * @param request 批量请求参数
     * @return 缺陷通知集合
     */
    public List<BugNoticeDTO> getBatchNoticeByRequest(BugBatchRequest request) {
        List<BugNoticeDTO> notices = new ArrayList<>();
        List<String> batchIds = bugService.getBatchIdsByRequest(request);
        batchIds.forEach(id -> notices.add(getNoticeById(id)));
        return notices;
    }

    /**
     * 获取状态集合
     * @param projectId 项目ID
     * @return 状态集合
     */
    private Map<String, String> getStatusMap(String projectId) {
        List<SelectOption> statusOption = bugStatusService.getHeaderStatusOption(projectId);
        return statusOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
    }

    /**
     * 获取处理人集合
     * @param projectId 项目ID
     * @return 处理人集合
     */
    private Map<String, String> getHandleMap(String projectId) {
        List<SelectOption> handlerOption = bugCommonService.getHeaderHandlerOption(projectId);
        return handlerOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
    }
}
