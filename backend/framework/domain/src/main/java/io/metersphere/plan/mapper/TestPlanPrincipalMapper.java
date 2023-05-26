package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanPrincipal;
import io.metersphere.plan.domain.TestPlanPrincipalExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanPrincipalMapper {
    long countByExample(TestPlanPrincipalExample example);

    int deleteByExample(TestPlanPrincipalExample example);

    int deleteByPrimaryKey(@Param("testPlanId") String testPlanId, @Param("userId") String userId);

    int insert(TestPlanPrincipal record);

    int insertSelective(TestPlanPrincipal record);

    List<TestPlanPrincipal> selectByExample(TestPlanPrincipalExample example);

    int updateByExampleSelective(@Param("record") TestPlanPrincipal record, @Param("example") TestPlanPrincipalExample example);

    int updateByExample(@Param("record") TestPlanPrincipal record, @Param("example") TestPlanPrincipalExample example);
}