package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.base.domain.ApiTemplateExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiTemplateMapper {
    long countByExample(ApiTemplateExample example);

    int deleteByExample(ApiTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTemplate record);

    int insertSelective(ApiTemplate record);

    List<ApiTemplate> selectByExample(ApiTemplateExample example);

    ApiTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTemplate record, @Param("example") ApiTemplateExample example);

    int updateByExample(@Param("record") ApiTemplate record, @Param("example") ApiTemplateExample example);

    int updateByPrimaryKeySelective(ApiTemplate record);

    int updateByPrimaryKey(ApiTemplate record);
}