package io.metersphere.project.mapper;

import io.metersphere.project.domain.BugTemplateExtend;
import io.metersphere.project.domain.BugTemplateExtendExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugTemplateExtendMapper {
    long countByExample(BugTemplateExtendExample example);

    int deleteByExample(BugTemplateExtendExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugTemplateExtend record);

    int insertSelective(BugTemplateExtend record);

    List<BugTemplateExtend> selectByExampleWithBLOBs(BugTemplateExtendExample example);

    List<BugTemplateExtend> selectByExample(BugTemplateExtendExample example);

    BugTemplateExtend selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugTemplateExtend record, @Param("example") BugTemplateExtendExample example);

    int updateByExampleWithBLOBs(@Param("record") BugTemplateExtend record, @Param("example") BugTemplateExtendExample example);

    int updateByExample(@Param("record") BugTemplateExtend record, @Param("example") BugTemplateExtendExample example);

    int updateByPrimaryKeySelective(BugTemplateExtend record);

    int updateByPrimaryKeyWithBLOBs(BugTemplateExtend record);

    int updateByPrimaryKey(BugTemplateExtend record);
}