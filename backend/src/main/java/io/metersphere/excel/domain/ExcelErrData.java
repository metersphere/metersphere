package io.metersphere.excel.domain;

public class ExcelErrData<T> {

    private T t;

    private Integer rowNum;

    private String errMsg;

    public ExcelErrData(){}

    public ExcelErrData(T t, Integer rowNum,String errMsg){
        this.t = t;
        this.rowNum = rowNum;
        this.errMsg = errMsg;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }
}