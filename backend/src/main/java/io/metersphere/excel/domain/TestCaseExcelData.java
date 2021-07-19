package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import io.metersphere.dto.CustomFieldDao;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Getter
@Setter
public class TestCaseExcelData {
    @ExcelIgnore
    private String id;
    @ExcelIgnore
    private Integer num;
    @ExcelIgnore
    private String customNum;
    @ExcelIgnore
    private String name;
    @ExcelIgnore
    private String nodePath;
    @ExcelIgnore
    private String tags;
    @ExcelIgnore
    private String prerequisite;
    @ExcelIgnore
    private String remark;
    @ExcelIgnore
    private String stepDesc;
    @ExcelIgnore
    private String stepResult;
    @ExcelIgnore
    private String stepModel;

    /**
     * 责任人
     * 用例状态
     * 用例等级
     */
    @ExcelIgnore
    private String status;
    @ExcelIgnore
    private String maintainer;
    @ExcelIgnore
    private String priority;
    @ExcelIgnore
    Map<String,String> customDatas = new LinkedHashMap<>();

    public Set<String> getExcludeColumnFiledNames(boolean needNum){
        Set<String> excludeColumnFiledNames = new HashSet<>();
        if(!needNum){
            excludeColumnFiledNames.add("customNum");
        }
        excludeColumnFiledNames.add("id");
        excludeColumnFiledNames.add("num");
        return excludeColumnFiledNames;
    }

    public String parseStatus(String parseStatus){
        String caseStatusValue = "";
        if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Underway","进行中","進行中")){
            caseStatusValue = "Underway";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Prepare","未开始","未開始")){
            caseStatusValue = "Prepare";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Completed","已完成","已完成")){
            caseStatusValue = "Completed";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Trash")){
            caseStatusValue = "Trash";
        }
        return caseStatusValue;
    }

    public List<List<String>> getHead(boolean needNum, List<CustomFieldDao> customFields){
        return new ArrayList<>();
    }
}
