package io.metersphere.system.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationDeleteRequest implements Serializable {

    private String id;

    private String deleteUserId;

    private Long deleteTime;
}
