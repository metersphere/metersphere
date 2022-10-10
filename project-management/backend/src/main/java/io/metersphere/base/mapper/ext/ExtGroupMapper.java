package io.metersphere.base.mapper.ext;

import io.metersphere.dto.GroupDTO;
import io.metersphere.request.group.EditGroupRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtGroupMapper {

    List<GroupDTO> getGroupList(@Param("request") EditGroupRequest request);


}
