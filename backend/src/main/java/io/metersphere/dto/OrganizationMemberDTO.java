package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationMemberDTO {
    private String id;

    private String name;

    private String email;

    private String phone;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String language;

    private String organizationId;

}
