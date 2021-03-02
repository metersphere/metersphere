package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ColumnWidth(15)
public class UserExcelDataTw extends TestCaseExcelData {
    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("ID")
    private String id;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("姓名")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("電子郵箱")
    @ColumnWidth(30)
    @Pattern(regexp = "^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "{user_import_format_wrong}")
    private String email;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("密碼")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?![0-9]+$)(?![^0-9]+$)(?![a-zA-Z]+$)(?![^a-zA-Z]+$)(?![a-zA-Z0-9]+$)[a-zA-Z0-9\\S]{8,30}$", message = "{user_import_format_wrong}")
    private String password;

    @ExcelProperty("電話")
    @Length(max = 11)
    @Pattern(regexp = "^1(3|4|5|6|7|8|9)\\d{9}$", message = "{user_import_format_wrong}")
    private String phone;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是系統管理員(是/否)")
    private String userIsAdmin;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是組織管理員(是/否)")
    private String userIsOrgAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("組織管理員工作空間")
    private String orgAdminOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是組織成員(是/否)")
    private String userIsOrgMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("組織成員工作空間")
    private String orgMemberOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是測試經理(是/否)")
    private String userIsTestManager;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("測試經理工作空間")
    private String testManagerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是測試成員(是/否)")
    private String userIsTester;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("測試成員工作空間")
    private String testerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是只讀用戶(是/否)")
    private String userIsViewer;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("只讀用戶工作空間")
    private String viewerWorkspace;
}
