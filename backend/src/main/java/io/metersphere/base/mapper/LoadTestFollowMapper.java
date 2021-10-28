package io.metersphere.base.mapper;

import io.metersphere.base.domain.LoadTestFollow;
import io.metersphere.base.domain.LoadTestFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestFollowMapper {
    long countByExample(LoadTestFollowExample example);

    int deleteByExample(LoadTestFollowExample example);

    int insert(LoadTestFollow record);

    int insertSelective(LoadTestFollow record);

    List<LoadTestFollow> selectByExample(LoadTestFollowExample example);

    int updateByExampleSelective(@Param("record") LoadTestFollow record, @Param("example") LoadTestFollowExample example);

    int updateByExample(@Param("record") LoadTestFollow record, @Param("example") LoadTestFollowExample example);
}