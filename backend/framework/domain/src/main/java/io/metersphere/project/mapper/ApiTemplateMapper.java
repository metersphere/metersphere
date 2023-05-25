package io.metersphere.project.mapper;

import io.metersphere.project.domain.ApiTemplate;
import io.metersphere.project.domain.ApiTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

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