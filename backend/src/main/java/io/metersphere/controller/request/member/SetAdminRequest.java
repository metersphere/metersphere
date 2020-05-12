package io.metersphere.controller.request.member;

import lombok.Data;

@Data
public class SetAdminRequest {
    private String id;
    private String adminId;
    private String password;
}
