package io.metersphere.system.dto;

import io.metersphere.system.domain.Organization;
import lombok.Data;


/**
 * @author song-cc-rock
 */
@Data
public class OrganizationDTO extends Organization {

    private Integer memberCount;

    private Integer projectCount;
}
