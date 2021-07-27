package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.excel.annotation.NotRequired;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;


@Data
@ColumnWidth(15)
public class TestCaseExcelDataUs extends TestCaseExcelData {

//    @ExcelProperty("ID")
//    @NotRequired
//    private Integer num;

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Name")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("Module")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @ColumnWidth(50)
    @ExcelProperty("Tag")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

//    @NotBlank(message = "{cannot_be_null}")
//    @ExcelProperty("Method")
//    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
//    private String method;

    @ColumnWidth(50)
    @ExcelProperty("Prerequisite")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("Remark")
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("Step description")
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("Step result")
    private String stepResult;

    @ColumnWidth(50)
    @ExcelProperty("Edit Model")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)", message = "{test_case_step_model_validate}")
    private String stepModel;

    @ColumnWidth(50)
    @ExcelProperty("Case status")
    private String status;

    @ExcelProperty("Priority")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @ExcelProperty("Maintainer")
    private String maintainer;

    @Override
    public List<List<String>> getHead(boolean needNum, List<CustomFieldDao> customFields){
        List<List<String>> returnList = new ArrayList<>();
        if(needNum){
            List<String> list = new ArrayList<>();
            list.add("ID");
            returnList.add(list);
        }

        List<String> list1 = new ArrayList<>();
        list1.add("Name");
        returnList.add(list1);

        List<String> list2 = new ArrayList<>();
        list2.add("Module");
        returnList.add(list2);

        List<String> list3 = new ArrayList<>();
        list3.add("Tag");
        returnList.add(list3);

        List<String> list4 = new ArrayList<>();
        list4.add("Prerequisite");
        returnList.add(list4);

        List<String> list5 = new ArrayList<>();
        list5.add("Remark");
        returnList.add(list5);

        List<String> list6 = new ArrayList<>();
        list6.add("Step description");
        returnList.add(list6);

        List<String> list7 = new ArrayList<>();
        list7.add("Step result");
        returnList.add(list7);

        List<String> list8 = new ArrayList<>();
        list8.add("Edit Model");
        returnList.add(list8);

        List<String> list9 = new ArrayList<>();
        list9.add("Priority");
        returnList.add(list9);

        if(CollectionUtils.isNotEmpty(customFields)){
            for (CustomFieldDao dto:customFields) {
                if(StringUtils.equals(dto.getName(),"Priority")){
                    continue;
                }
                List<String> list = new ArrayList<>();
                list.add(dto.getName());
                returnList.add(list);
            }
        }
        return returnList;
    }

    @Override
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
}
