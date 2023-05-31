package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestFollower;
import io.metersphere.load.domain.LoadTestFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestFollowerMapper {
    long countByExample(LoadTestFollowerExample example);

    int deleteByExample(LoadTestFollowerExample example);

    int insert(LoadTestFollower record);

    int insertSelective(LoadTestFollower record);

    List<LoadTestFollower> selectByExample(LoadTestFollowerExample example);

    int updateByExampleSelective(@Param("record") LoadTestFollower record, @Param("example") LoadTestFollowerExample example);

    int updateByExample(@Param("record") LoadTestFollower record, @Param("example") LoadTestFollowerExample example);
}