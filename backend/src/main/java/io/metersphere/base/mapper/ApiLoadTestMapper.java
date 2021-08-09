package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiLoadTest;
import io.metersphere.base.domain.ApiLoadTestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiLoadTestMapper {
    long countByExample(ApiLoadTestExample example);

    int deleteByExample(ApiLoadTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiLoadTest record);

    int insertSelective(ApiLoadTest record);

    List<ApiLoadTest> selectByExample(ApiLoadTestExample example);

    ApiLoadTest selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiLoadTest record, @Param("example") ApiLoadTestExample example);

    int updateByExample(@Param("record") ApiLoadTest record, @Param("example") ApiLoadTestExample example);

    int updateByPrimaryKeySelective(ApiLoadTest record);

    int updateByPrimaryKey(ApiLoadTest record);
}
