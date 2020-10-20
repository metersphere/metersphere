package io.metersphere.xmind.utils;

import com.sun.deploy.util.StringUtils;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.i18n.Translator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProcMessage implements Serializable {

    private Map<String, StringBuilder> process;

    public ProcMessage() {
        process = new LinkedHashMap<>();
    }

    public void add(String type, String msContent) {
        if (process.containsKey(type)) {
            process.get(type).append(msContent + "；");
        } else {
            process.put(type, new StringBuilder(msContent+"；"));
        }
    }

    public List<ExcelErrData<TestCaseExcelData>> parse(String content) {
        List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();
        ExcelErrData excelErrData = new ExcelErrData(null, 1, content);
        errList.add(excelErrData);
        return errList;
    }

    public List<ExcelErrData<TestCaseExcelData>> parse() {
        List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();
        List<String> result = new ArrayList<>();
        process.entrySet().parallelStream().reduce(result, (first, second) -> {
            first.add(second.getKey() + "：" + second.getValue());
            return first;
        }, (first, second) -> {
            if (first == second) {
                return first;
            }
            first.addAll(second);
            return first;
        });

        for (int i = 0; i < result.size(); i++) {
            ExcelErrData excelErrData = new ExcelErrData(null, i, result.get(i));
            errList.add(excelErrData);
        }
        return errList;
    }

    @Override
    public String toString() {
        List<String> result = new ArrayList<>();
        process.entrySet().parallelStream().reduce(result, (first, second) -> {
            first.add(second.getKey() + "：" + second.getValue());
            return first;
        }, (first, second) -> {
            if (first == second) {
                return first;
            }
            first.addAll(second);
            return first;
        });

        return StringUtils.join(result, "\n");
    }
}
