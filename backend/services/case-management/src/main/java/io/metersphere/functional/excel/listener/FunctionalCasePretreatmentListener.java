package io.metersphere.functional.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import io.metersphere.functional.excel.domain.ExcelMergeInfo;
import io.metersphere.sdk.util.BeanUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 数据预处理，读取合并的单元格信息
 * @author wx
 */
public class FunctionalCasePretreatmentListener extends AnalysisEventListener {

    Set<ExcelMergeInfo> mergeInfoSet;
    private Integer lastRowIndex = 0;
    Map<Integer, Integer> emptyRowIndexMap = new HashMap<>();

    public FunctionalCasePretreatmentListener(Set<ExcelMergeInfo> mergeInfoSet) {
        this.mergeInfoSet = mergeInfoSet;
    }

    @Override
    public void invoke(Object integerStringMap, AnalysisContext analysisContext) {
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        if (rowIndex - lastRowIndex > 1) {
            // 记录空行的行号
            for (int i = lastRowIndex + 1; i < rowIndex; i++) {
                emptyRowIndexMap.put(i, lastRowIndex);
            }
        }
        this.lastRowIndex = rowIndex;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        if (extra.getType() == CellExtraTypeEnum.MERGE) {
            // 将合并单元格信息保留
            ExcelMergeInfo mergeInfo = new ExcelMergeInfo();
            BeanUtils.copyBean(mergeInfo, extra);
            if (emptyRowIndexMap.keySet().contains(mergeInfo.getLastRowIndex())) {
                // 如果合并单元格的最后一行是空行，则将最后一行设置成非空的行
                mergeInfo.setLastRowIndex(emptyRowIndexMap.get(mergeInfo.getLastRowIndex()));
            }
            this.mergeInfoSet.add(mergeInfo);
        }
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {}
}
