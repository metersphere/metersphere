package io.metersphere.system.dto.response;

import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationProjectOptionsResponse {

    List<OrganizationProjectOptionsDto> organizationList;
    List<OrganizationProjectOptionsDto> projectList;
}
