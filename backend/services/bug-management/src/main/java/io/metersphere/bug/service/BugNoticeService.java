package io.metersphere.bug.service;

import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plugin.sdk.util.PluginUtils;
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

/**
 * @author song-cc-rock
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class BugNoticeService {

    public static final String CUSTOM_TITLE = "summary";
    public static final String CUSTOM_STATUS = "status";
    public static final String CUSTOM_HANDLE_USER = "handleUser";
    public static final String EMPTY_MULTIPLE = "[]";

    @Resource
    private BugService bugService;
    @Resource
    private BugLogService bugLogService;

    /**
     * 获取缺陷通知
     * @param request 请求参数
     * @return 缺陷通知
     */
    @SuppressWarnings("unused")
    public BugNoticeDTO getNoticeByRequest(BugEditRequest request) {
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
                } else if (StringUtils.equalsIgnoreCase(field.getId(), CUSTOM_STATUS)) {
                    // 状态 {从自定义字段中获取状态}
                    notice.setStatus(PluginUtils.parseArray(field.getText()).getFirst().toString());
                } else if (StringUtils.equalsIgnoreCase(field.getId(), CUSTOM_HANDLE_USER)) {
                    // 处理人 {从自定义字段中获取状态}
                    notice.setHandleUser(PluginUtils.parseArray(field.getText()).getFirst().toString());
                } else {
                    fields.add(buildNoticeOptionDTO(field));
                }
            });
            notice.setFields(fields);
        }
        return notice;
    }

    /**
     * 获取缺陷通知
     * @param id 缺陷ID
     */
    @SuppressWarnings("unused")
    public BugNoticeDTO getNoticeById(String id) {
        // 缺陷基础信息
        BugDTO bugDTO = bugLogService.getOriginalValue(id);
        if (bugDTO == null) {
            return null;
        }
        return buildNotice(bugDTO);
    }

    /**
     * 获取缺陷通知集合
     * @param ids 缺陷ID集合
     */
    public List<BugNoticeDTO> getNoticeByIds(List<String> ids) {
        List<BugDTO> bugs = bugLogService.getOriginalValueByIds(ids);
        if (CollectionUtils.isEmpty(bugs)) {
            return null;
        }
        List<BugNoticeDTO> notices = new ArrayList<>();
        bugs.forEach(bug -> notices.add(buildNotice(bug)));
        return notices;
    }

    /**
     * 获取批量操作的缺陷通知
     * @param request 批量请求参数
     * @return 缺陷通知集合
     */
    @SuppressWarnings("unused")
    public List<BugNoticeDTO> getBatchNoticeByRequest(BugBatchRequest request) {
        List<String> batchIds = bugService.getBatchIdsByRequest(request);
        if (CollectionUtils.isEmpty(batchIds)) {
            return null;
        }
        return getNoticeByIds(batchIds);
    }

    /**
     * 构建通知对象
     * @param bugDTO 缺陷DTO
     * @return 通知对象
     */
    private BugNoticeDTO buildNotice(BugDTO bugDTO) {
        // 构建通知对象
        BugNoticeDTO notice = new BugNoticeDTO();
        BeanUtils.copyBean(notice, bugDTO);
        notice.setHandleUser(bugDTO.getHandleUserName());
        notice.setStatus(bugDTO.getStatusName());
        // 自定义字段解析{name: value}
        if (CollectionUtils.isNotEmpty(bugDTO.getCustomFields())) {
            List<OptionDTO> fields = new ArrayList<>();
            bugDTO.getCustomFields().forEach(field -> fields.add(buildNoticeOptionDTO(field)));
            notice.setFields(fields);
        }
        return notice;
    }

    /**
     * 构建通知自定义字段
     * @param field 缺陷自定义字段
     * @return 通知自定义字段
     */
    private OptionDTO buildNoticeOptionDTO(BugCustomFieldDTO field) {
        // 封装通知自定义字段
        OptionDTO option = new OptionDTO();
        option.setId(field.getName());
        if (StringUtils.isNotEmpty(field.getText()) && !StringUtils.equals(field.getText(), EMPTY_MULTIPLE)) {
            option.setName(field.getText());
        } else {
            option.setName(field.getValue());
        }
        return option;
    }
}
