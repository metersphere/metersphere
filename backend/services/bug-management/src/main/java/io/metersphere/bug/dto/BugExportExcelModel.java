package io.metersphere.bug.dto;

import io.metersphere.bug.constants.BugExportMultipleField;
import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.dto.response.BugCommentDTO;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plugin.platform.dto.PlatformCustomFieldMultiLevelOption;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.CommentUserInfo;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 缺陷导出数据结构模型
 */
@Data
public class BugExportExcelModel {

    /**
     * <key,text>. 如果是自定义字段，则是 <id,字段显示>
     */
    private LinkedHashMap<String, String> excelHeader;

    /**
     * <key,value>. 如果是自定义字段，则是 <id,数据>
     */
    private List<LinkedHashMap<String, String>> excelRows;

    public BugExportExcelModel(BugExportHeaderModel headerModel,
                               List<BugDTO> bugList,
                               Map<String, List<BugCommentDTO>> bugComment,
                               Map<String, BugContent> bugContents) {
        this.excelHeader = new LinkedHashMap<>();
        // 生成表头
        for (BugExportColumn exportColumn : headerModel.getExportColumns()) {
            this.excelHeader.put(exportColumn.getKey(), exportColumn.getText());
        }
        this.excelRows = new ArrayList<>();
        for (BugDTO bugDTO : bugList) {
            LinkedHashMap<String, String> excelRow = new LinkedHashMap<>();
            for (String key : excelHeader.keySet()) {
				switch (key) {
					case "name" -> excelRow.put(key, bugDTO.getTitle());
					case "num" -> excelRow.put(key, bugDTO.getNum().toString());
					case "content" -> excelRow.put(key, this.getBugContent(bugContents, bugDTO.getId()));
					case "status" -> excelRow.put(key, headerModel.getStatusMap().get(bugDTO.getStatus()));
					case "handle_user" -> excelRow.put(key, headerModel.getHandleUserMap().get(bugDTO.getHandleUser()));
					case "create_user" -> excelRow.put(key, bugDTO.getCreateUserName());
					case "create_time" -> excelRow.put(key, DateUtils.getTimeString(bugDTO.getCreateTime()));
					case "case_count" -> excelRow.put(key, String.valueOf(bugDTO.getRelationCaseCount()));
					case "comment" -> excelRow.put(key, this.getBugComment(bugComment.get(bugDTO.getId())));
					case "platform" -> excelRow.put(key, bugDTO.getPlatform());
					default -> excelRow.put(key, this.getCustomFieldValue(bugDTO.getCustomFields(), key, headerModel.getHeaderCustomFields()));
				}
            }
            excelRows.add(excelRow);
        }
    }

    private String getCustomFieldValue(List<BugCustomFieldDTO> customFields, String key, List<TemplateCustomFieldDTO> headerCustomFields) {
        Map<String, TemplateCustomFieldDTO> fieldMap = headerCustomFields.stream().collect(Collectors.toMap(TemplateCustomFieldDTO::getFieldId, f -> f));
        if (CollectionUtils.isNotEmpty(customFields)) {
            for (BugCustomFieldDTO customField : customFields) {
                if (key.equals(customField.getId())) {
                    TemplateCustomFieldDTO templateField = fieldMap.get(key);
                    return parseOptionVal(templateField, customField.getValue());
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 解析自定义字段的值
     * @param customField 模板字段(包含选项值)
     * @param val 字段值
     * @return 字段文本
     */
    private String parseOptionVal(TemplateCustomFieldDTO customField, String val) {
        if (CollectionUtils.isEmpty(customField.getOptions()) && StringUtils.isBlank(customField.getPlatformOptionJson())) {
            return val;
        }
        if (CollectionUtils.isNotEmpty(customField.getOptions())) {
            return filterOptionVal(customField.getOptions(), val, customField.getType());
        }
        if (StringUtils.isNotBlank(customField.getPlatformOptionJson())) {
            List<PlatformCustomFieldMultiLevelOption> platformOptions = JSON.parseArray(customField.getPlatformOptionJson(), PlatformCustomFieldMultiLevelOption.class);
            List<CustomFieldOption> options = new ArrayList<>();
            buildOptionFromPlatformOption(platformOptions, options);
            return filterOptionVal(options, val, customField.getType());
        }
        return StringUtils.EMPTY;
    }

    /**
     * 递归解析平台选项
     * @param platformOptions 平台选项
     * @param options 选项值
     */
    private void buildOptionFromPlatformOption(List<PlatformCustomFieldMultiLevelOption> platformOptions, List<CustomFieldOption> options) {
        for (PlatformCustomFieldMultiLevelOption platformOption : platformOptions) {
            CustomFieldOption option = new CustomFieldOption();
            option.setValue(platformOption.getValue());
            option.setText(platformOption.getText());
            options.add(option);
            if (CollectionUtils.isNotEmpty(platformOption.getChildren())) {
                buildOptionFromPlatformOption(platformOption.getChildren(), options);
            }
        }
    }

    /**
     * 解析自定义字段的值
     * @param options 选项值
     * @param val 值
     * @param fieldType 字段类型
     * @return 字段文本
     */
    private String filterOptionVal(List<CustomFieldOption> options, String val, String fieldType) {
        if (startsWithAnyMultipleField(fieldType)) {
            // 多选字段
            List<String> valList = JSON.parseArray(val, String.class);
            List<String> textList = new ArrayList<>();
            valList.forEach(value -> {
                Optional<CustomFieldOption> targetOption = options.stream().filter(option -> StringUtils.equals(value, option.getValue())).findFirst();
                targetOption.ifPresent(option -> textList.add(option.getText()));
            });
            if (CollectionUtils.isEmpty(textList)) {
                return StringUtils.EMPTY;
            } else {
                return String.join(",", textList);
            }
        } else {
            // 单选字段
            Optional<CustomFieldOption> findOption = options.stream().filter(option -> StringUtils.equals(val, option.getValue())).findFirst();
            if (findOption.isPresent()) {
                return findOption.get().getText();
            } else {
                return StringUtils.EMPTY;
            }
        }
    }

    /**
     * 是否是多选字段
     * @param fieldType 字段类型
     * @return 是否是多选字段
     */
    private boolean startsWithAnyMultipleField(String fieldType) {
        return StringUtils.startsWithIgnoreCase(fieldType, BugExportMultipleField.MULTIPLE_FIELD) ||
                StringUtils.startsWithIgnoreCase(fieldType, BugExportMultipleField.CASCADING_FIELD) ||
                StringUtils.startsWithIgnoreCase(fieldType, BugExportMultipleField.CHECKBOX_FIELD);
    }

    private String getBugContent(Map<String, BugContent> bugContents, String id) {
        if (bugContents.containsKey(id)) {
            return replaceRichTextHtmlTag(bugContents.get(id).getDescription());
        } else {
            return StringUtils.EMPTY;
        }
    }

    public String getBugComment(List<BugCommentDTO> bugCommentList) {
        if (CollectionUtils.isEmpty(bugCommentList)) {
            return StringUtils.EMPTY;
        } else {
            StringBuilder commentBuilder = new StringBuilder();
            for (BugCommentDTO bugCommentDTO : bugCommentList) {
                buildComment(bugCommentDTO, commentBuilder);
            }
            return commentBuilder.toString();
        }
    }

    public void buildComment(BugCommentDTO bugCommentDTO, StringBuilder commentBuilder) {
        commentBuilder.append("【");
        commentBuilder.append(parseCommentUser(bugCommentDTO, bugCommentDTO.getCreateUser()));
        commentBuilder.append(StringUtils.SPACE);
        commentBuilder.append(DateUtils.getTimeString(bugCommentDTO.getCreateTime()));
        commentBuilder.append(StringUtils.SPACE);
        commentBuilder.append(StringUtils.isBlank(bugCommentDTO.getReplyUser()) ? "评论" :
                "回复" + StringUtils.SPACE + parseCommentUser(bugCommentDTO, bugCommentDTO.getReplyUser()));
        commentBuilder.append("】");
        commentBuilder.append(StringUtils.LF);
        commentBuilder.append(replaceRichTextHtmlTag(bugCommentDTO.getContent()));
        commentBuilder.append(StringUtils.LF);
        if (CollectionUtils.isNotEmpty(bugCommentDTO.getChildComments())) {
            for (BugCommentDTO childComment : bugCommentDTO.getChildComments()) {
                buildComment(childComment, commentBuilder);
            }
        }
    }

    public List<String> getHeadTexts() {
        return new ArrayList<>(excelHeader.values());
    }

    public List<List<String>> getData() {
        List<List<String>> returnList = new ArrayList<>();
        returnList.add(this.getHeadTexts());
        for (LinkedHashMap<String, String> excelRow : excelRows) {
            List<String> row = new ArrayList<>();
            for (String key : excelHeader.keySet()) {
                row.add(excelRow.get(key));
            }
            returnList.add(row);
        }
        return returnList;
    }

    /**
     * 用户ID -> 用户名
     * @param commentDTO 评论DTO
     * @param userId 用户ID
     * @return 用户名
     */
    private String parseCommentUser(BugCommentDTO commentDTO, String userId) {
        Optional<CommentUserInfo> findUser = commentDTO.getCommentUserInfos().stream().filter(user -> StringUtils.equals(user.getId(), userId)).findFirst();
        if (findUser.isPresent()) {
            return findUser.get().getName();
        } else {
            return userId;
        }
    }

    /**
     * 替换富文本HTML标签(<img>除外)
     * @param sourceStr
     * @return
     */
    private String replaceRichTextHtmlTag(String sourceStr) {
        if (StringUtils.isBlank(sourceStr)) {
            return StringUtils.EMPTY;
        }
        return sourceStr.replaceAll("<(?!img)[^>]*>",  StringUtils.EMPTY);
    }
}