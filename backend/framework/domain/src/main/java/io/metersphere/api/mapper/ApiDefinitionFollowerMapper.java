package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionFollower;
import io.metersphere.api.domain.ApiDefinitionFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionFollowerMapper {
    long countByExample(ApiDefinitionFollowerExample example);

    int deleteByExample(ApiDefinitionFollowerExample example);

    int deleteByPrimaryKey(@Param("apiDefinitionId") String apiDefinitionId, @Param("userId") String userId);

    int insert(ApiDefinitionFollower record);

    int insertSelective(ApiDefinitionFollower record);

    List<ApiDefinitionFollower> selectByExample(ApiDefinitionFollowerExample example);

    int updateByExampleSelective(@Param("record") ApiDefinitionFollower record, @Param("example") ApiDefinitionFollowerExample example);

    int updateByExample(@Param("record") ApiDefinitionFollower record, @Param("example") ApiDefinitionFollowerExample example);
}