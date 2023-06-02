package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugFollower;
import io.metersphere.bug.domain.BugFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugFollowerMapper {
    long countByExample(BugFollowerExample example);

    int deleteByExample(BugFollowerExample example);

    int deleteByPrimaryKey(@Param("bugId") String bugId, @Param("userId") String userId);

    int insert(BugFollower record);

    int insertSelective(BugFollower record);

    List<BugFollower> selectByExample(BugFollowerExample example);

    int updateByExampleSelective(@Param("record") BugFollower record, @Param("example") BugFollowerExample example);

    int updateByExample(@Param("record") BugFollower record, @Param("example") BugFollowerExample example);
}