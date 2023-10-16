package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugContent;
import io.metersphere.bug.domain.BugContentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugContentMapper {
    long countByExample(BugContentExample example);

    int deleteByExample(BugContentExample example);

    int deleteByPrimaryKey(String bugId);

    int insert(BugContent record);

    int insertSelective(BugContent record);

    List<BugContent> selectByExampleWithBLOBs(BugContentExample example);

    List<BugContent> selectByExample(BugContentExample example);

    BugContent selectByPrimaryKey(String bugId);

    int updateByExampleSelective(@Param("record") BugContent record, @Param("example") BugContentExample example);

    int updateByExampleWithBLOBs(@Param("record") BugContent record, @Param("example") BugContentExample example);

    int updateByExample(@Param("record") BugContent record, @Param("example") BugContentExample example);

    int updateByPrimaryKeySelective(BugContent record);

    int updateByPrimaryKeyWithBLOBs(BugContent record);

    int batchInsert(@Param("list") List<BugContent> list);

    int batchInsertSelective(@Param("list") List<BugContent> list, @Param("selective") BugContent.Column ... selective);
}