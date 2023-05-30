package io.metersphere.issue.mapper;

import io.metersphere.issue.domain.CustomFieldIssues;
import io.metersphere.issue.domain.CustomFieldIssuesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldIssuesMapper {
    long countByExample(CustomFieldIssuesExample example);

    int deleteByExample(CustomFieldIssuesExample example);

    int deleteByPrimaryKey(@Param("resourceId") String resourceId, @Param("fieldId") String fieldId);

    int insert(CustomFieldIssues record);

    int insertSelective(CustomFieldIssues record);

    List<CustomFieldIssues> selectByExampleWithBLOBs(CustomFieldIssuesExample example);

    List<CustomFieldIssues> selectByExample(CustomFieldIssuesExample example);

    CustomFieldIssues selectByPrimaryKey(@Param("resourceId") String resourceId, @Param("fieldId") String fieldId);

    int updateByExampleSelective(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByExample(@Param("record") CustomFieldIssues record, @Param("example") CustomFieldIssuesExample example);

    int updateByPrimaryKeySelective(CustomFieldIssues record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldIssues record);

    int updateByPrimaryKey(CustomFieldIssues record);
}