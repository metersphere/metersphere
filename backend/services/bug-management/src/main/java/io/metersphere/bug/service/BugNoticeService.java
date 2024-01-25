package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.mapper.ExtBugCustomFieldMapper;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.BugNoticeDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
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
    private UserMapper userMapper;
    @Resource
    private BugService bugService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ExtBugCustomFieldMapper extBugCustomFieldMapper;

    /**
     * 获取缺陷通知
     * @param request 请求参数
     * @return 缺陷通知
     */
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

    /**
     * 发送删除缺陷通知
     * @param bug 缺陷
     * @param currentUser 当前用户
     */
    public void sendDeleteNotice(Bug bug, String currentUser) {
        Map<String, String> statusMap = getStatusMap(bug.getProjectId());
        Map<String, String> handlerMap = getHandleMap(bug.getProjectId());
        // 缺陷相关内容
        BugNoticeDTO notice = new BugNoticeDTO();
        notice.setTitle(bug.getTitle());
        notice.setStatus(statusMap.get(bug.getStatus()));
        notice.setHandleUser(handlerMap.get(bug.getHandleUser()));
        List<BugCustomFieldDTO> customFields = extBugCustomFieldMapper.getBugAllCustomFields(List.of(bug.getId()), bug.getProjectId());
        List<OptionDTO> fields = customFields.stream().map(field -> {
            OptionDTO fieldDTO = new OptionDTO();
            fieldDTO.setId(field.getName());
            fieldDTO.setName(field.getValue());
            return fieldDTO;
        }).toList();
        notice.setCustomFields(fields);
        BeanMap beanMap = new BeanMap(notice);
        User user = userMapper.selectByPrimaryKey(currentUser);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TemplateText.BUG_TASK_DELETE);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TemplateText.BUG_TASK_DELETE);
        NoticeModel noticeModel = NoticeModel.builder().operator(currentUser)
                .context(template).subject(subject).paramMap(paramMap).event(NoticeConstants.Event.DELETE).build();
        noticeSendService.send(NoticeConstants.TaskType.BUG_TASK, noticeModel);
    }

    /**
     * 获取状态集合
     * @param projectId 项目ID
     * @return
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
        List<SelectOption> handlerOption = bugService.getHeaderHandlerOption(projectId);
        return handlerOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
    }
}
