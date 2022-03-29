package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtMockExpectConfigMapper {

    List<MockExpectConfigWithBLOBs> selectByProjectIdAndStatusIsOpen(String projectId);

    List<MockExpectConfigWithBLOBs> selectByApiId(String apiId);

    List<MockExpectConfigWithBLOBs> selectByApiIdIn(@Param("values") List<String> apiIds);

    List<String> selectExlectNumByMockConfigId(String mockConfigId);

    String selectApiNumberByMockConfigId(String mockConfigId);

    void insertNewVersionMockExpectConfigs(@Param("api") ApiDefinition apiDefinition, @Param("old") ApiDefinition old);

    void insertNewVersionMockConfigs(@Param("api") ApiDefinition apiDefinition, @Param("old") ApiDefinition old);
}