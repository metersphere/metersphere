package io.metersphere.bug.service;

import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.plugin.platform.dto.SelectOption;
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
    private BugStatusService bugStatusService;

    public BugNoticeDTO getNoticeByRequest(BugEditRequest request) {
        // 获取状态选项, 处理人选项
        Map<String, String> statusMap = getStatusMap(request.getProjectId());
        Map<String, String> handlerMap = getHandleMap(request.getProjectId());
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
            notice.setCustomFields(fields);
        }
        return notice;
    }

    private Map<String, String> getStatusMap(String projectId) {
        List<SelectOption> statusOption = bugStatusService.getHeaderStatusOption(projectId);
        return statusOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
    }

    private Map<String, String> getHandleMap(String projectId) {
        List<SelectOption> handlerOption = bugService.getHeaderHandlerOption(projectId);
        return handlerOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
    }
}
