package io.metersphere.system.mapper;

import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.dto.sdk.QueryResourcePoolRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtResourcePoolMapper {

    List<TestResourcePool> getResourcePoolList(@Param("request") QueryResourcePoolRequest request);

    List<TestResourcePool> selectAllResourcePool(@Param("poolIds")List<String> poolIds);

    List<TestResourcePool> selectProjectAllResourcePool(@Param("poolIds")List<String> poolIds);
}
