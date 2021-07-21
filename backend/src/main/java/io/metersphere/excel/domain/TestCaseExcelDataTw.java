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
public class TestCaseExcelDataTw extends TestCaseExcelData {

//    @ExcelProperty("ID")
//    @NotRequired
//    private Integer num;

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("用例名稱")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("所屬模塊")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @ColumnWidth(50)
    @ExcelProperty("標簽")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

//    @NotBlank(message = "{cannot_be_null}")
//    @ExcelProperty("測試方式")
//    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
//    private String method;

    @ColumnWidth(50)
    @ExcelProperty("前置條件")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("備註")
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("步驟描述")
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("預期結果")
    private String stepResult;

    @ColumnWidth(50)
    @ExcelProperty("編輯模式")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)", message = "{test_case_step_model_validate}")
    private String stepModel;

    @ColumnWidth(50)
    @ExcelProperty("用例狀態")
    private String status;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("用例等級")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @ExcelProperty("維護人")
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
        list1.add("用例名稱");
        returnList.add(list1);

        List<String> list2 = new ArrayList<>();
        list2.add("所屬模塊");
        returnList.add(list2);

        List<String> list3 = new ArrayList<>();
        list3.add("標簽");
        returnList.add(list3);

        List<String> list4 = new ArrayList<>();
        list4.add("前置條件");
        returnList.add(list4);

        List<String> list5 = new ArrayList<>();
        list5.add("備註");
        returnList.add(list5);

        List<String> list6 = new ArrayList<>();
        list6.add("步驟描述");
        returnList.add(list6);

        List<String> list7 = new ArrayList<>();
        list7.add("預期結果");
        returnList.add(list7);

        List<String> list8 = new ArrayList<>();
        list8.add("編輯模式");
        returnList.add(list8);

        List<String> list9 = new ArrayList<>();
        list9.add("用例等級");
        returnList.add(list9);

        if(CollectionUtils.isNotEmpty(customFields)){
            for (CustomFieldDao dto:customFields) {
                if(StringUtils.equals(dto.getName(),"用例等級")){
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
            caseStatusValue = "進行中";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Prepare","未开始","未開始")){
            caseStatusValue = "未開始";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Completed","已完成","已完成")){
            caseStatusValue = "已完成";
        }else if(StringUtils.equalsAnyIgnoreCase(parseStatus,"Trash","废弃","廢棄")){
            caseStatusValue = "廢棄";
        }
        return caseStatusValue;
    }
}
