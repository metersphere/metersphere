package io.metersphere.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.excel.domain.ExcelMergeInfo;

import java.util.Set;

/**
 * 数据预处理，读取合并的单元格信息
 */
public class TestCasePretreatmentListener extends AnalysisEventListener {


    Set<ExcelMergeInfo> mergeInfoSet;

    public TestCasePretreatmentListener(Set<ExcelMergeInfo> mergeInfoSet) {
        this.mergeInfoSet = mergeInfoSet;
    }

    @Override
    public void invoke(Object integerStringMap, AnalysisContext analysisContext) {}

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        if (extra.getType() == CellExtraTypeEnum.MERGE) {
            // 将合并单元格信息保留
            ExcelMergeInfo mergeInfo = new ExcelMergeInfo();
            BeanUtils.copyBean(mergeInfo, extra);
            this.mergeInfoSet.add(mergeInfo);
        }
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {}
}
