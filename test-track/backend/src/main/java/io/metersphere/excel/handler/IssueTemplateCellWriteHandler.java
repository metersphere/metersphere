package io.metersphere.excel.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.Map;

public class IssueTemplateCellWriteHandler implements SheetWriteHandler {

    private static final Integer ROW_SIZE = 200;

    /**
     * 下拉框集合
     */
    private Map<Integer, String[]> dropDownIndexMap;

    public IssueTemplateCellWriteHandler(Map<Integer, String[]> dropDownIndexMap) {
        this.dropDownIndexMap = dropDownIndexMap;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        dropDownIndexMap.forEach((celIndex, options) -> {
            // 区间设置
            CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, ROW_SIZE, celIndex, celIndex);
            // 下拉内容
            DataValidationConstraint constraint = helper.createExplicitListConstraint(options);
            DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
            sheet.addValidationData(dataValidation);
        });
    }
}
