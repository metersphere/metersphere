package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseFollow;
import io.metersphere.base.domain.TestCaseFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseFollowMapper {
    long countByExample(TestCaseFollowExample example);

    int deleteByExample(TestCaseFollowExample example);

    int insert(TestCaseFollow record);

    int insertSelective(TestCaseFollow record);

    List<TestCaseFollow> selectByExample(TestCaseFollowExample example);

    int updateByExampleSelective(@Param("record") TestCaseFollow record, @Param("example") TestCaseFollowExample example);

    int updateByExample(@Param("record") TestCaseFollow record, @Param("example") TestCaseFollowExample example);
}