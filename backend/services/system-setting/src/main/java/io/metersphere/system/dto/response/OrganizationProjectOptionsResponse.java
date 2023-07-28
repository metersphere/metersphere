package io.metersphere.system.dto.response;

import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationProjectOptionsResponse {

    List<OrganizationProjectOptionsDTO> organizationList;
    List<OrganizationProjectOptionsDTO> projectList;
}
