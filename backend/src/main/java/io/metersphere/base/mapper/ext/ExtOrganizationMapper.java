package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Organization;
import io.metersphere.dto.OrganizationMemberDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtOrganizationMapper {

    int checkSourceRole(@Param("sourceId") String sourceId,@Param("userId") String userId,@Param("groupId") String groupId);

    List<OrganizationMemberDTO> findIdAndNameByOrganizationId(@Param("organizationId")String organizationID);
}
