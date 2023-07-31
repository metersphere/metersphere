package io.metersphere.project.mapper;

import io.metersphere.project.domain.BugTemplate;
import io.metersphere.project.domain.BugTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugTemplateMapper {
    long countByExample(BugTemplateExample example);

    int deleteByExample(BugTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugTemplate record);

    int insertSelective(BugTemplate record);

    List<BugTemplate> selectByExample(BugTemplateExample example);

    BugTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugTemplate record, @Param("example") BugTemplateExample example);

    int updateByExample(@Param("record") BugTemplate record, @Param("example") BugTemplateExample example);

    int updateByPrimaryKeySelective(BugTemplate record);

    int updateByPrimaryKey(BugTemplate record);

    int batchInsert(@Param("list") List<BugTemplate> list);

    int batchInsertSelective(@Param("list") List<BugTemplate> list, @Param("selective") BugTemplate.Column ... selective);
}