package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionFollow;
import io.metersphere.api.domain.ApiDefinitionFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionFollowMapper {
    long countByExample(ApiDefinitionFollowExample example);

    int deleteByExample(ApiDefinitionFollowExample example);

    int deleteByPrimaryKey(@Param("apiDefinitionId") String apiDefinitionId, @Param("followId") String followId);

    int insert(ApiDefinitionFollow record);

    int insertSelective(ApiDefinitionFollow record);

    List<ApiDefinitionFollow> selectByExample(ApiDefinitionFollowExample example);

    int updateByExampleSelective(@Param("record") ApiDefinitionFollow record, @Param("example") ApiDefinitionFollowExample example);

    int updateByExample(@Param("record") ApiDefinitionFollow record, @Param("example") ApiDefinitionFollowExample example);
}