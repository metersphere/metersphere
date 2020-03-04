package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.UserRole;

import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private String id;

    private String name;

    private String email;

    private String phone;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String language;

    private String lastWorkspaceId;

    private String lastOrganizationId;

    private List<Role> roles = new ArrayList<>();

    private List<UserRole> userRoles = new ArrayList<>();

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLastWorkspaceId() {
        return lastWorkspaceId;
    }

    public void setLastWorkspaceId(String lastWorkspaceId) {
        this.lastWorkspaceId = lastWorkspaceId;
    }

    public String getLastOrganizationId() {
        return lastOrganizationId;
    }

    public void setLastOrganizationId(String lastOrganizationId) {
        this.lastOrganizationId = lastOrganizationId;
    }
}
