package io.metersphere.base.domain;

import java.io.Serializable;

public class User implements Serializable {
    private String id;

    private String name;

    private String email;

    private String password;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String language;

    private String lastWorkspaceId;

    private String lastOrganizationId;

    private String phone;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? null : language.trim();
    }

    public String getLastWorkspaceId() {
        return lastWorkspaceId;
    }

    public void setLastWorkspaceId(String lastWorkspaceId) {
        this.lastWorkspaceId = lastWorkspaceId == null ? null : lastWorkspaceId.trim();
    }

    public String getLastOrganizationId() {
        return lastOrganizationId;
    }

    public void setLastOrganizationId(String lastOrganizationId) {
        this.lastOrganizationId = lastOrganizationId == null ? null : lastOrganizationId.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }
}