package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExcelData {

    @ExcelIgnore
    private String id;
    @ExcelIgnore
    private String name;
    @ExcelIgnore
    private String email;
    @ExcelIgnore
    private String password;
    @ExcelIgnore
    private String phone;
    @ExcelIgnore
    private String userIsAdmin;
    @ExcelIgnore
    private String userIsOrgAdmin;
    @ExcelIgnore
    private String orgAdminOrganization;
    @ExcelIgnore
    private String userIsOrgMember;
    @ExcelIgnore
    private String orgMemberOrganization;
    @ExcelIgnore
    private String userIsTestManager;
    @ExcelIgnore
    private String testManagerWorkspace;
    @ExcelIgnore
    private String userIsTester;
    @ExcelIgnore
    private String testerWorkspace;
    @ExcelIgnore
    private String userIsProjectAdmin;
    @ExcelIgnore
    private String proAdminProject;
    @ExcelIgnore
    private String userIsProjectMember;
    @ExcelIgnore
    private String proMemberProject;
    @ExcelIgnore
    private String userIsViewer;
    @ExcelIgnore
    private String viewerProject;

}