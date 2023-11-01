package io.metersphere.system.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
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
