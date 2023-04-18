package io.metersphere.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "user", autoResultMap = true)
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

    private String phone;

    private String source;

    private String lastProjectId;

    private String createUser;

    private String platformInfo;

    private String seleniumServer;

    private static final long serialVersionUID = 1L;
}