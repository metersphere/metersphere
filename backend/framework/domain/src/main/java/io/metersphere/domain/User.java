package io.metersphere.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "user", autoResultMap = true)
public class User implements Serializable {
    @NotBlank(message = "id不能为空")
    private String id;
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 50, message = "用户名长度必须在1-50之间")
    private String name;
    @Email
    private String email;

    private String password;
    @NotBlank(message = "状态不能为空")
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