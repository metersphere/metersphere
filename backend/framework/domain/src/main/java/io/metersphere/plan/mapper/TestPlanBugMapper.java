package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanBug;
import io.metersphere.plan.domain.TestPlanBugExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanBugMapper {
    long countByExample(TestPlanBugExample example);

    int deleteByExample(TestPlanBugExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanBug record);

    int insertSelective(TestPlanBug record);

    List<TestPlanBug> selectByExample(TestPlanBugExample example);

    TestPlanBug selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanBug record, @Param("example") TestPlanBugExample example);

    int updateByExample(@Param("record") TestPlanBug record, @Param("example") TestPlanBugExample example);

    int updateByPrimaryKeySelective(TestPlanBug record);

    int updateByPrimaryKey(TestPlanBug record);

    int batchInsert(@Param("list") List<TestPlanBug> list);

    int batchInsertSelective(@Param("list") List<TestPlanBug> list, @Param("selective") TestPlanBug.Column ... selective);
}