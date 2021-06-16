package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Data
@ColumnWidth(15)
public class UserExcelDataUs extends UserExcelData {
    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Id")
    private String id;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ColumnWidth(20)
    @ExcelProperty("Name")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("E-mail")
    @ColumnWidth(30)
    @Pattern(regexp = "^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "{user_import_format_wrong}")
    private String email;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Password")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?![0-9]+$)(?![^0-9]+$)(?![a-zA-Z]+$)(?![^a-zA-Z]+$)(?![a-zA-Z0-9]+$)[a-zA-Z0-9\\S]{8,30}$", message = "{user_import_format_wrong}")
    private String password;

    @ExcelProperty("Phone")
    @Length(max = 11)
    @ColumnWidth(30)
    @Pattern(regexp = "^1(3|4|5|6|7|8|9)\\d{9}$", message = "{user_import_format_wrong}")
    private String phone;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is administrator(Yes/No)")
    private String userIsAdmin;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is organization manager(Yes/No)")
    private String userIsOrgAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Manager in witch organization")
    private String orgAdminOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is organization member(Yes/No)")
    private String userIsOrgMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Member in witch organization")
    private String orgMemberOrganization;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is test manager(Yes/No)")
    private String userIsTestManager;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Workspace of test manager")
    private String testManagerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is tester(Yes/No)")
    private String userIsTester;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Workspace of tester")
    private String testerWorkspace;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is project admin(Yes/No)")
    private String userIsProjectAdmin;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Project admin of project admin")
    private String proAdminProject;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is project member(Yes/No)")
    private String userIsProjectMember;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Project member of project member")
    private String proMemberProject;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(30)
    @ExcelProperty("User is read-only user(Yes/No)")
    private String userIsViewer;

    @Length(max = 100)
    @ColumnWidth(30)
    @ExcelProperty("Project of read-only user")
    private String viewerProject;
}
