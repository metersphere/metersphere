package io.metersphere.system.dto.sdk;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Data
public class ExcelParseDTO<T> {
    private List<T> dataList = new ArrayList<>();
    private TreeMap<Integer, T> errRowData = new TreeMap<>();

    public void addRowData(T t) {
        dataList.add(t);
    }

    public void addErrorRowData(Integer index, T t) {
        errRowData.put(index, t);
    }

    public void addErrorRowDataAll(TreeMap<Integer, T> errRowData) {
        this.errRowData.putAll(errRowData);
    }
}
