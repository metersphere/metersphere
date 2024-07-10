package io.metersphere.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Statistics {
    @ColumnWidth(60)
    @ExcelProperty(value = "Label")
    private String label;

    @ColumnWidth(20)
    @ExcelProperty("Samples")
    private BigDecimal samples;

    @ColumnWidth(20)
    @ExcelProperty("Fail")
    private BigDecimal fail;

    @ColumnWidth(20)
    @ExcelProperty("Error")
    private BigDecimal error;

    @ColumnWidth(20)
    @ExcelProperty("Average")
    private BigDecimal average;

    @ColumnWidth(20)
    @ExcelProperty("Min")
    private BigDecimal min;

    @ColumnWidth(20)
    @ExcelProperty("Max")
    private BigDecimal max;

    @ColumnWidth(20)
    @ExcelProperty("Median")
    private BigDecimal median;

    @ColumnWidth(20)
    @ExcelProperty("90% Line")
    private BigDecimal tp90;

    @ColumnWidth(20)
    @ExcelProperty("95% Line")
    private BigDecimal tp95;

    @ColumnWidth(20)
    @ExcelProperty("99% Line")
    private BigDecimal tp99;

    @ColumnWidth(20)
    @ExcelProperty("TPS")
    private BigDecimal transactions;

    @ColumnWidth(20)
    @ExcelProperty("Received")
    private BigDecimal received;

    @ColumnWidth(20)
    @ExcelProperty("Sent")
    private BigDecimal sent;

}
