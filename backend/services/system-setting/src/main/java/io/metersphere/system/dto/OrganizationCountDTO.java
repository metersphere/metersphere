package io.metersphere.system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationCountDTO {

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 项目数量
     */
    private Integer projectCount;

    /**
     * 组织ID
     */
    private String id;
}
