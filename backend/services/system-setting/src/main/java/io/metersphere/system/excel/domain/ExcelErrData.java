package io.metersphere.system.excel.domain;

import lombok.Data;

@Data
public class ExcelErrData<T> {



    private Integer rowNum;

    private String errMsg;

    public ExcelErrData() {
    }

    public ExcelErrData(Integer rowNum, String errMsg) {
        this.rowNum = rowNum;
        this.errMsg = errMsg;
    }
}