package io.metersphere.excel.handler;

import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import io.metersphere.commons.constants.CustomFieldScene;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldOptionDTO;
import io.metersphere.excel.constants.IssueExportHeadField;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表头, 单元格后置处理
 */
public class IssueTemplateHeadWriteHandler implements RowWriteHandler, SheetWriteHandler {

    private Sheet sheet;
    private Drawing<?> drawingPatriarch;
    private Map<String, String> memberMap;
    private Map<Integer, String> headCommentIndexMap = new HashMap<>();

    public IssueTemplateHeadWriteHandler(Map<String, String> memberMap, List<List<String>> headList, List<CustomFieldDao> customFields) {
        this.memberMap = memberMap;
        this.initCustomFieldIndexMap(headList, customFields);
    }

    private void initCustomFieldIndexMap(List<List<String>> headList, List<CustomFieldDao> customFields) {
        int index = 0;
        for (List<String> list : headList) {
            for (String head : list) {
                CustomFieldDao customFieldDao;
                if (StringUtils.equalsAnyIgnoreCase(head, IssueExportHeadField.TITLE.getName(), IssueExportHeadField.DESCRIPTION.getName())) {
                    customFieldDao = new CustomFieldDao();
                    customFieldDao.setRequired(Boolean.TRUE);
                    customFieldDao.setType(CustomFieldType.INPUT.getValue());
                } else if (StringUtils.equalsAnyIgnoreCase(head, IssueExportHeadField.ID.getName())) {
                    customFieldDao = new CustomFieldDao();
                    customFieldDao.setRequired(Boolean.FALSE);
                    customFieldDao.setType(CustomFieldType.INT.getValue());
                } else if (StringUtils.equalsAnyIgnoreCase(head, IssueExportHeadField.CREATOR.getName())) {
                    customFieldDao = new CustomFieldDao();
                    customFieldDao.setRequired(Boolean.FALSE);
                    customFieldDao.setType(CustomFieldType.MEMBER.getValue());
                } else {
                    // 自定义字段
                    List<CustomFieldDao> fields = customFields.stream().filter(field -> StringUtils.equals(field.getName(), head)).collect(Collectors.toList());
                    if (fields.size() > 0) {
                        customFieldDao = fields.get(0);
                    } else {
                        customFieldDao = null;
                    }
                }
                headCommentIndexMap.put(index, customFieldDao == null ? StringUtils.EMPTY : getCommentByCustomField(customFieldDao));
                index++;
            }
        }
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        if (BooleanUtils.isTrue(context.getHead())) {
            sheet = context.getWriteSheetHolder().getSheet();
            drawingPatriarch = sheet.createDrawingPatriarch();
            // 设置表头内容
            headCommentIndexMap.forEach(this::setComment);
        }
    }

    private String getCommentByCustomField(CustomFieldDao field) {
        String commentText = "";
        if (StringUtils.equalsAnyIgnoreCase(field.getType(),
                CustomFieldType.SELECT.getValue(), CustomFieldType.RADIO.getValue())) {
            if (StringUtils.equalsAnyIgnoreCase(field.getScene(), CustomFieldScene.ISSUE.name()) &&
                    StringUtils.equalsAnyIgnoreCase(field.getName(), "状态", "严重程度")) {
                commentText = Translator.get("options").concat(JSON.toJSONString(getOptionValues(field)));
            } else {
                commentText = Translator.get("options").concat(JSON.toJSONString(getOptionsText(field.getOptions())));
            }
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.MEMBER.getValue())) {
            commentText = Translator.get("options").concat(memberMap.keySet().toString());
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.DATE.getValue())) {
            commentText = Translator.get("date_import_cell_format_comment");
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.DATETIME.getValue())) {
            commentText = Translator.get("datetime_import_cell_format_comment");
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.INT.getValue())) {
            commentText = Translator.get("int_import_cell_format_comment");
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.FLOAT.getValue())) {
            commentText = Translator.get("float_import_cell_format_comment");
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.MULTIPLE_INPUT.getValue())) {
            commentText = Translator.get("multiple_input_import_cell_format_comment");
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(),
                CustomFieldType.MULTIPLE_SELECT.getValue(), CustomFieldType.CHECKBOX.getValue())) {
            commentText = Translator.get("multiple_input_import_cell_format_comment").concat(", " +
                    Translator.get("options").concat(JSON.toJSONString(getOptionsText(field.getOptions()))));
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.MULTIPLE_MEMBER.getValue())) {
            commentText = Translator.get("multiple_input_import_cell_format_comment").concat(", " +
                    Translator.get("options").concat(memberMap.keySet().toString()));
        }
        if (StringUtils.equalsAnyIgnoreCase(field.getType(), CustomFieldType.CASCADING_SELECT.getValue())) {
            commentText = Translator.get("cascading_select_import_cell_format_comment").concat(", " +
                    Translator.get("options_key_tips").concat(JSON.toJSONString(getCascadSelect(field.getOptions()))));
        }
        return field.getRequired() ? Translator.get("required").concat("; " + commentText) : commentText;
    }

    private void setComment(Integer index, String text) {
        if (index == null || StringUtils.isEmpty(text)) {
            return;
        }
        Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, index, 0, index + 3, 1));
        comment.setString(new XSSFRichTextString(text));
        sheet.getRow(0).getCell(1).setCellComment(comment);
    }

    @NotNull
    private List<String> getOptionValues(CustomFieldDao field) {
        List<CustomFieldOptionDTO> options = JSON.parseArray(field.getOptions(), CustomFieldOptionDTO.class);
        List<String> values = new ArrayList<>();
        if (options != null) {
            for (CustomFieldOptionDTO option : options) {
                values.add(option.getValue());
            }
        }
        return values;
    }

    private List<String> getOptionsText(String optionStr) {
        if (StringUtils.isEmpty(optionStr)) {
            return Collections.emptyList();
        }

        List<String> options = new ArrayList<>();
        List<Map> optionMapList = JSON.parseArray(optionStr, Map.class);
        optionMapList.forEach(optionMap -> {
            String optionText = optionMap.get("text").toString();
            options.add(optionText);
        });
        return options;
    }

    private List<String> getCascadSelect(String optionStr) {
        if (StringUtils.isEmpty(optionStr)) {
            return Collections.emptyList();
        }

        List<String> options = new ArrayList<>();
        List<Map> optionMapList = JSON.parseArray(optionStr, Map.class);
        optionMapList.forEach(optionMap -> {
            StringBuffer option = new StringBuffer();
            if (optionMap.get("children") != null) {
                String children = getCascadSelect(JSON.toJSONString(optionMap.get("children"))).toString();
                option.append("{").append(optionMap.get("text"))
                        .append(":").append(optionMap.get("value")).append(":").append(children).append("}");
            } else {
                option.append("{").append(optionMap.get("text"))
                        .append(":").append(optionMap.get("value")).append("}");
            }
            options.add(option.toString());
        });
        return options;
    }
}
