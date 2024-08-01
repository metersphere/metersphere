package io.metersphere.functional.excel.handler;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import io.metersphere.functional.excel.constants.FunctionalCaseImportFiled;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class FunctionCaseMergeWriteHandler implements RowWriteHandler {

    /**
     * 存储需要合并单元格的信息，key 是需要合并的第一条的行号，值是需要合并多少行
     */
    Map<Integer, Integer> rowMergeInfo;
    List<List<String>> headList;
    int textDescriptionRowIndex;
    int expectedResultRowIndex;

    public FunctionCaseMergeWriteHandler(Map<Integer, Integer> rowMergeInfo, List<List<String>> headList) {
        this.rowMergeInfo = rowMergeInfo;
        this.headList = headList;
        for (int i = 0; i < headList.size(); i++) {
            List<String> list = headList.get(i);
            for (String head : list) {
                if (FunctionalCaseImportFiled.TEXT_DESCRIPTION.containsHead(head)) {
                    textDescriptionRowIndex = i;
                } else if (FunctionalCaseImportFiled.EXPECTED_RESULT.containsHead(head)) {
                    expectedResultRowIndex = i;
                }
            }
        }
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        if (context.getHead() || context.getRelativeRowIndex() == null) {
            return;
        }

        Integer mergeCount = rowMergeInfo.get(context.getRowIndex());

        if (mergeCount == null || mergeCount <= 0) {
            return;
        }

        for (int i = 0; i < headList.size(); i++) {
            // 除了描述其他数据合并多行
            if (i != textDescriptionRowIndex && i != expectedResultRowIndex) {
                CellRangeAddress cellRangeAddress =
                        new CellRangeAddress(context.getRowIndex(), context.getRowIndex() + mergeCount - 1, i, i);
                context.getWriteSheetHolder().getSheet().addMergedRegionUnsafe(cellRangeAddress);
            }
        }
    }
}
