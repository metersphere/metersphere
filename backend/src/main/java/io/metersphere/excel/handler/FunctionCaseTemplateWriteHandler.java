package io.metersphere.excel.handler;

import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.fastjson.JSONArray;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/5/7 2:17 下午
 * @Description
 */
public class FunctionCaseTemplateWriteHandler implements RowWriteHandler {

    private boolean isNeedId;
    Map<String, Integer> rowDisposeIndexMap;
    Map<String, List<String>> caseLevelAndStatusValueMap;

    private Integer idIndex;
    private Integer moduleIndex;
    private Integer maintainerIndex;
    private Integer priorityIndex;
    private Integer tagIndex;
    private Integer statusIndex;
    private Integer stepModelIndex;

    public FunctionCaseTemplateWriteHandler(boolean isNeedId, List<List<String>> headList, Map<String, List<String>> caseLevelAndStatusValueMap) {
        this.isNeedId = isNeedId;
        rowDisposeIndexMap = this.buildFiledMap(headList);
        this.caseLevelAndStatusValueMap = caseLevelAndStatusValueMap;
    }

    private Map<String, Integer> buildFiledMap(List<List<String>> headList) {
        Map<String, Integer> returnMap = new HashMap<>();

        int index = 0;
        for (List<String> list : headList) {
            for (String head : list) {
                if (TestCaseImportFiled.ID.containsHead(head)) {
                    idIndex = index;
                } else if (TestCaseImportFiled.MODULE.containsHead(head)) {
                    moduleIndex = index;
                } else if (TestCaseImportFiled.MAINTAINER.containsHead(head)) {
                    maintainerIndex = index;
                } else if (TestCaseImportFiled.PRIORITY.containsHead(head)) {
                    priorityIndex = index;
                } else if (TestCaseImportFiled.TAG.containsHead(head)) {
                    tagIndex = index;
                } else if (TestCaseImportFiled.STATUS.containsHead(head)) {
                    statusIndex = index;
                } else if (TestCaseImportFiled.STEP_MODEL.containsHead(head)) {
                    stepModelIndex = index;
                }
                index++;
            }
        }
        return returnMap;
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {

        if (BooleanUtils.isTrue(context.getHead())) {
            Sheet sheet = context.getWriteSheetHolder().getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();

            if (rowDisposeIndexMap != null) {
                if (isNeedId) {
                    setComment(sheet, drawingPatriarch, idIndex, Translator.get("do_not_modify_header_order") + "，" + Translator.get("id_required"));
                }

                setComment(sheet, drawingPatriarch, moduleIndex, Translator.get("module_created_automatically"));
                setComment(sheet, drawingPatriarch, maintainerIndex, Translator.get("please_input_project_member"));
                setComment(sheet, drawingPatriarch, tagIndex, Translator.get("tag_tip_pattern"));
                setComment(sheet, drawingPatriarch, stepModelIndex, Translator.get("step_model_tip"));

                List<String> list = new ArrayList<>();
                if (caseLevelAndStatusValueMap != null && caseLevelAndStatusValueMap.containsKey("caseLevel")) {
                    list = caseLevelAndStatusValueMap.get("caseLevel");
                }
                if (CollectionUtils.isEmpty(list)) {
                    setComment(sheet, drawingPatriarch, priorityIndex, Translator.get("options") + "（P0、P1、P2、P3）");
                } else {
                    setComment(sheet, drawingPatriarch, priorityIndex, Translator.get("options") + JSONArray.toJSONString(list));
                }

                list.clear();
                if (caseLevelAndStatusValueMap != null && caseLevelAndStatusValueMap.containsKey("caseStatus")) {
                    list = caseLevelAndStatusValueMap.get("caseStatus");
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    setComment(sheet, drawingPatriarch, statusIndex, Translator.get("options") + JSONArray.toJSONString(list));
                }
            }
        }
    }

    private void setComment(Sheet sheet, Drawing<?> drawingPatriarch, Integer index, String text) {
        if (index == null) {
            return;
        }
        Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, index, 0, index + 3, 1));
        comment.setString(new XSSFRichTextString(text));
        sheet.getRow(0).getCell(1).setCellComment(comment);
    }
}
