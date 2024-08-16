package io.metersphere.functional.xmind.utils;


import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.excel.domain.ExcelErrData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DetailUtil implements Serializable {

    private Map<String, StringBuilder> process;

    public DetailUtil() {
        process = new LinkedHashMap<>();
    }

    public void add(String type, String msContent) {
        if (process.containsKey(type)) {
            process.get(type).append(msContent + "；");
        } else {
            process.put(type, new StringBuilder(msContent + "；"));
        }
    }

    public List<ExcelErrData<FunctionalCaseExcelData>> parse(String content) {
        List<ExcelErrData<FunctionalCaseExcelData>> errList = new ArrayList<>();
        ExcelErrData excelErrData = new ExcelErrData(1, content);
        errList.add(excelErrData);
        return errList;
    }

    public List<ExcelErrData<FunctionalCaseExcelData>> parse() {
        List<ExcelErrData<FunctionalCaseExcelData>> errList = new ArrayList<>();
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

        int emptyName = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains(Translator.get("test_case_name") + Translator.get("incorrect_format"))) {
                emptyName +=1;
            } else {
                ExcelErrData excelErrData = new ExcelErrData(i, result.get(i));
                errList.add(excelErrData);
            }
        }
        if (emptyName>0) {
            ExcelErrData excelErrData = new ExcelErrData(result.size(), Translator.get("mind_import_case_name_empty"));
            errList.add(excelErrData);
        }
        return errList;
    }
}
