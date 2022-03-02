package io.metersphere.base.mapper;

import io.metersphere.base.domain.CustomFieldIssues;
import io.metersphere.base.domain.CustomFieldIssuesExample;
import io.metersphere.base.domain.CustomFieldIssuesKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldIssuesMapper {
    long countByExample(CustomFieldIssuesExample example);

    int deleteByExample(CustomFieldIssuesExample example);

    int deleteByPrimaryKey(CustomFieldIssuesKey key);

    int insert(CustomFieldIssues record);

    int insertSelective(CustomFieldIssues record);

    List<CustomFieldIssues> selectByExampleWithBLOBs(CustomFieldIssuesExample example);

    List<CustomFieldIssues> selectByExample(CustomFieldIssuesExample example);

    CustomFieldIssues selectByPrimaryKey(CustomFieldIssuesKey key);

    int updateByExampleSelective(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByExample(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByPrimaryKeySelective(CustomFieldIssues record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldIssues record);

    int updateByPrimaryKey(CustomFieldIssues record);
}