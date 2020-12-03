package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTag;
import io.metersphere.base.domain.ApiTagExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiTagMapper {
    long countByExample(ApiTagExample example);

    int deleteByExample(ApiTagExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTag record);

    int insertSelective(ApiTag record);

    List<ApiTag> selectByExample(ApiTagExample example);

    ApiTag selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTag record, @Param("example") ApiTagExample example);

    int updateByExample(@Param("record") ApiTag record, @Param("example") ApiTagExample example);

    int updateByPrimaryKeySelective(ApiTag record);

    int updateByPrimaryKey(ApiTag record);
}