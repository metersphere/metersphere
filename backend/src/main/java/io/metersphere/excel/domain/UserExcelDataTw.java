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
    @ColumnWidth(20)
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
    @ColumnWidth(30)
    @Pattern(regexp = "^1(3|4|5|6|7|8|9)\\d{9}$", message = "{user_import_format_wrong}")
    private String phone;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是系統管理員(是/否)")
    private String userIsAdmin;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是組織管理員(是/否)")
    private String userIsOrgAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("組織管理員組織名稱")
    private String orgAdminOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是組織成員(是/否)")
    private String userIsOrgMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("組織成員組織名稱")
    private String orgMemberOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是工作空間管理員(是/否)")
    private String userIsTestManager;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("工作空間管理員應用工作空間")
    private String testManagerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是工作空間成員(是/否)")
    private String userIsTester;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("工作空間成員應用工作空間")
    private String testerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是項目管理員(是/否)")
    private String userIsProjectAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("項目管理員應用項目")
    private String proAdminProject;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是項目成員(是/否)")
    private String userIsProjectMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("項目成員應用項目")
    private String proMemberProject;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是只讀用戶(是/否)")
    private String userIsViewer;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("只讀用戶應用項目")
    private String viewerProject;
}
