package io.metersphere.functional.excel.domain;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author wx
 */
@Data
public class ExcelMergeInfo implements Comparable<ExcelMergeInfo> {

    /**
     * 合并单元格的第一行
     */
    private Integer firstRowIndex;
    /**
     * 合并单元格的最后一行
     */
    private Integer lastRowIndex;
    /**
     * 合并单元格的第一列，不考虑同一行合并单元格的情况
     */
    private Integer firstColumnIndex;

    /**
     * 根据 firstRowIndex, firstColumnIndex 重写 compareTo
     * 使用 TreeSet 按 Excel 表格顺序查找时，可以优化效率
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NotNull ExcelMergeInfo o) {
        int compare = Integer.compare(this.getFirstRowIndex(), o.getFirstRowIndex());
        if (compare == 0) {
            return Integer.compare(this.getFirstColumnIndex(), o.getFirstColumnIndex());
        }
        return compare;
    }
}
