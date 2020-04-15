package io.metersphere.excel.domain;

import java.util.List;

public class ExcelResponse<T> {

    private Boolean success;
    private List<ExcelErrData<T>> errList;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<ExcelErrData<T>> getErrList() {
        return errList;
    }

    public void setErrList(List<ExcelErrData<T>> errList) {
        this.errList = errList;
    }
}
