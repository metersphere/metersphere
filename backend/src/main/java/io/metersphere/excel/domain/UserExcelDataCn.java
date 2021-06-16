package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ColumnWidth(15)
public class UserExcelDataCn extends UserExcelData {
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
    @ExcelProperty("电子邮箱")
    @ColumnWidth(30)
    @Pattern(regexp = "^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "{user_import_format_wrong}")
    private String email;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("密码")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?![0-9]+$)(?![^0-9]+$)(?![a-zA-Z]+$)(?![^a-zA-Z]+$)(?![a-zA-Z0-9]+$)[a-zA-Z0-9\\S]{8,30}$", message = "{user_import_format_wrong}")
    private String password;

    @ExcelProperty("电话")
    @Length(max = 11)
    @ColumnWidth(20)
    @Pattern(regexp = "^1(3|4|5|6|7|8|9)\\d{9}$", message = "{user_import_format_wrong}")
    private String phone;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是系统管理员(是/否)")
    private String userIsAdmin;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是组织管理员(是/否)")
    private String userIsOrgAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("组织管理员组织名称")
    private String orgAdminOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是组织成员(是/否)")
    @ColumnWidth(30)
    private String userIsOrgMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("组织成员组织名称")
    private String orgMemberOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是工作空间管理员(是/否)")
    @ColumnWidth(30)
    private String userIsTestManager;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("工作空间管理员应用工作空间")
    private String testManagerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是工作空间成员(是/否)")
    @ColumnWidth(30)
    private String userIsTester;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("工作空间成员应用工作空间")
    private String testerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是项目管理员(是/否)")
    @ColumnWidth(30)
    private String userIsProjectAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("项目管理员应用项目")
    private String proAdminProject;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("是否是项目成员(是/否)")
    @ColumnWidth(30)
    private String userIsProjectMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("项目成员应用项目")
    private String proMemberProject;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("是否是只读用戶(是/否)")
    private String userIsViewer;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("只读用戶应用项目")
    private String viewerProject;
}
