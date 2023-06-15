package io.metersphere.system.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
public class OrganizationDeleteRequest implements Serializable {

    /**
     * 删除组织ID
     */
    private String organizationId;

    /**
     * 删除人ID
     */
    private String deleteUserId;

    /**
     * 删除时间
     */
    private Long deleteTime;
}
