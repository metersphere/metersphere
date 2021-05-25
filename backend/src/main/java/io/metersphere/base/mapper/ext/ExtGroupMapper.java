package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.group.EditGroupRequest;
import io.metersphere.dto.GroupDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtGroupMapper {

    List<GroupDTO> getGroupList(@Param("request") EditGroupRequest request);


}
