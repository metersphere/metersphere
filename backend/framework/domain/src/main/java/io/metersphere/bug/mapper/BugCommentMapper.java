package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugComment;
import io.metersphere.bug.domain.BugCommentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugCommentMapper {
    long countByExample(BugCommentExample example);

    int deleteByExample(BugCommentExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugComment record);

    int insertSelective(BugComment record);

    List<BugComment> selectByExampleWithBLOBs(BugCommentExample example);

    List<BugComment> selectByExample(BugCommentExample example);

    BugComment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugComment record, @Param("example") BugCommentExample example);

    int updateByExampleWithBLOBs(@Param("record") BugComment record, @Param("example") BugCommentExample example);

    int updateByExample(@Param("record") BugComment record, @Param("example") BugCommentExample example);

    int updateByPrimaryKeySelective(BugComment record);

    int updateByPrimaryKeyWithBLOBs(BugComment record);

    int updateByPrimaryKey(BugComment record);

    int batchInsert(@Param("list") List<BugComment> list);

    int batchInsertSelective(@Param("list") List<BugComment> list, @Param("selective") BugComment.Column ... selective);
}