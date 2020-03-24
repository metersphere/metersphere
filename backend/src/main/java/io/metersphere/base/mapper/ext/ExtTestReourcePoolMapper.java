package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestReourcePoolMapper {
    List<TestResourcePoolDTO> listResourcePools(@Param("request") QueryResourcePoolRequest request);

//    List<TestResource> listResourcesByPoolId(@Param("poolId") String poolId);
}
