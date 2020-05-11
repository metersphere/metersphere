package io.metersphere.dto;

import lombok.Data;

@Data
public class UserPassDTO {
    private String password;
    private String newpassword;
    private String id;
}
