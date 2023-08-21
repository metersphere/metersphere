package io.metersphere.excel.handler;

import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.metersphere.commons.utils.JSON;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/5/7 2:17 下午
 * @Description
 */
public class FunctionCaseTemplateWriteHandler implements RowWriteHandler {

    private boolean isNeedId;
    Map<String, List<String>> caseLevelAndStatusValueMap;

    private Integer idIndex;
    private Integer moduleIndex;
    private Integer maintainerIndex;
    private Integer priorityIndex;
    private Integer tagIndex;
    private Integer statusIndex;
    private Integer stepDescIndex;
    private Integer stepResultIndex;
    private Integer stepModelIndex;

    private Sheet sheet;
    private Drawing<?> drawingPatriarch;

    public FunctionCaseTemplateWriteHandler(boolean isNeedId, List<List<String>> headList, Map<String, List<String>> caseLevelAndStatusValueMap) {
        this.isNeedId = isNeedId;
        initIndex(headList);
        this.caseLevelAndStatusValueMap = caseLevelAndStatusValueMap;
    }

    private void initIndex(List<List<String>> headList) {
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
                } else if (TestCaseImportFiled.TAGS.containsHead(head)) {
                    tagIndex = index;
                } else if (TestCaseImportFiled.STATUS.containsHead(head)) {
                    statusIndex = index;
                } else if (TestCaseImportFiled.STEP_DESC.containsHead(head)) {
                    stepDescIndex = index;
                } else if (TestCaseImportFiled.STEP_RESULT.containsHead(head)) {
                    stepResultIndex = index;
                } else if (TestCaseImportFiled.STEP_MODEL.containsHead(head)) {
                    stepModelIndex = index;
                }
                index++;
            }
        }
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {

        if (BooleanUtils.isTrue(context.getHead())) {
            sheet = context.getWriteSheetHolder().getSheet();
            drawingPatriarch = sheet.createDrawingPatriarch();

            if (isNeedId) {
                setComment(idIndex, Translator.get("do_not_modify_header_order").concat("，").concat(Translator.get("id_required")));
            }

            setComment(moduleIndex, Translator.get("module_created_automatically"));
            setComment(maintainerIndex, Translator.get("please_input_project_member"));
            setComment(tagIndex, Translator.get("tag_tip_pattern"));
            setComment(stepDescIndex, Translator.get("step_desc_tip"));
            setComment(stepResultIndex, Translator.get("step_result_tip"));
            setComment(stepModelIndex, Translator.get("step_model_tip"));

            List<String> list = new ArrayList<>();
            if (MapUtils.isNotEmpty(caseLevelAndStatusValueMap) && caseLevelAndStatusValueMap.containsKey("caseLevel")) {
                list = caseLevelAndStatusValueMap.get("caseLevel");
            }
            if (CollectionUtils.isEmpty(list)) {
                setComment(priorityIndex, Translator.get("options").concat("（P0、P1、P2、P3）"));
            } else {
                setComment(priorityIndex, Translator.get("options").concat(JSON.toJSONString(list)).concat(Translator.get("cannot_ignore_case")));
            }

            list.clear();
            if (MapUtils.isNotEmpty(caseLevelAndStatusValueMap) && caseLevelAndStatusValueMap.containsKey("caseStatus")) {
                list = caseLevelAndStatusValueMap.get("caseStatus");
            }
            if (CollectionUtils.isNotEmpty(list)) {
                setComment(statusIndex, Translator.get("options").concat(JSON.toJSONString(list)).concat(Translator.get("cannot_ignore_case")));
            }
        }

    }

    private void setComment(Integer index, String text) {
        if (index == null) {
            return;
        }
        Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, index, 0, index + 3, 1));
        comment.setString(new XSSFRichTextString(text));
        sheet.getRow(0).getCell(1).setCellComment(comment);
    }

    public static HorizontalCellStyleStrategy getHorizontalWrapStrategy() {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 设置自动换行
        contentWriteCellStyle.setWrapped(true);
        return new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
    }
}
