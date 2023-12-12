package io.metersphere.bug.dto;

import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.dto.response.BugCommentDTO;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.sdk.util.DateUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 缺陷导出数据结构模型
 */
@Data
public class BugExportExcelModel {
    // <key,text>. 如果是自定义字段，则是 <id,字段显示>
    private LinkedHashMap<String, String> excelHeader;

    // <key,value>. 如果是自定义字段，则是 <id,数据>
    private List<LinkedHashMap<String, String>> excelRows;

    public BugExportExcelModel(List<BugExportColumn> exportColumns,
                               List<BugDTO> bugList,
                               Map<String, List<BugCommentDTO>> bugComment,
                               Map<String, BugContent> bugContents,
                               Map<String, Long> bugCountMap) {
        this.excelHeader = new LinkedHashMap<>();
        //注入表头
        for (BugExportColumn exportColumn : exportColumns) {
            this.excelHeader.put(exportColumn.getKey(), exportColumn.getText());
        }
        this.excelRows = new ArrayList<>();
        for (BugDTO bugDTO : bugList) {
            LinkedHashMap<String, String> excelRow = new LinkedHashMap<>();
            for (String key : excelHeader.keySet()) {
                switch (key) {
                    case "name" -> excelRow.put(key, bugDTO.getTitle());
                    case "id" -> excelRow.put(key, bugDTO.getId());
                    case "content" -> excelRow.put(key, this.getBugContent(bugContents, bugDTO.getId()));
                    case "status" -> excelRow.put(key, bugDTO.getStatus());
                    case "handleUser" -> excelRow.put(key, bugDTO.getHandleUserName());
                    case "createUser" -> excelRow.put(key, bugDTO.getCreateUserName());
                    case "createTime" -> excelRow.put(key, DateUtils.getTimeString(bugDTO.getCreateTime()));
                    case "caseCount" -> excelRow.put(key, this.getBugCaseCount(bugCountMap, bugDTO.getId()));
                    case "comment" -> excelRow.put(key, this.getBugComment(bugComment.get(bugDTO.getId())));
                    case "platform" -> excelRow.put(key, bugDTO.getPlatform());
                    default -> excelRow.put(key, this.getCustomFieldValue(bugDTO.getCustomFields(), key));
                }
            }
            excelRows.add(excelRow);
        }
    }

    private String getCustomFieldValue(List<BugCustomFieldDTO> customFields, String key) {
        if (CollectionUtils.isNotEmpty(customFields)) {
            for (BugCustomFieldDTO customField : customFields) {
                if (key.equals(customField.getId())) {
                    return customField.getValue();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private String getBugCaseCount(Map<String, Long> bugCountMap, String id) {
        long count = 0;
        if (bugCountMap.containsKey(id)) {
            count = bugCountMap.get(id);
        }
        return String.valueOf(count);
    }

    private String getBugContent(Map<String, BugContent> bugContents, String id) {
        if (bugContents.containsKey(id)) {
            return bugContents.get(id).getDescription();
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
                commentBuilder.append(bugCommentDTO.getCreateUser());
                commentBuilder.append(StringUtils.SPACE);
                commentBuilder.append(DateUtils.getTimeString(bugCommentDTO.getCreateTime()));
                commentBuilder.append(StringUtils.LF);
                commentBuilder.append(bugCommentDTO.getContent());
                commentBuilder.append(StringUtils.LF);
            }
            return commentBuilder.toString();
        }
    }

    public List<String> getHeadTexts() {
        return new ArrayList<>(excelHeader.values());
    }

    public List<String> getHeadKeys() {
        return new ArrayList<>(excelHeader.keySet());
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
}