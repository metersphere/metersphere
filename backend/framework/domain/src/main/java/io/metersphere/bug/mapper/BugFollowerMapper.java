package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugFollower;
import io.metersphere.bug.domain.BugFollowerExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugFollowerMapper {
    long countByExample(BugFollowerExample example);

    int deleteByExample(BugFollowerExample example);

    int deleteByPrimaryKey(@Param("bugId") String bugId, @Param("userId") String userId);

    int insert(BugFollower record);

    int insertSelective(BugFollower record);

    List<BugFollower> selectByExample(BugFollowerExample example);

    int updateByExampleSelective(@Param("record") BugFollower record, @Param("example") BugFollowerExample example);

    int updateByExample(@Param("record") BugFollower record, @Param("example") BugFollowerExample example);

    int batchInsert(@Param("list") List<BugFollower> list);

    int batchInsertSelective(@Param("list") List<BugFollower> list, @Param("selective") BugFollower.Column ... selective);
}