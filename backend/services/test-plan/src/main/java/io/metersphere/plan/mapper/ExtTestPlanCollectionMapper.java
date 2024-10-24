package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.dto.TestPlanCollectionConfigDTO;
import io.metersphere.plan.dto.TestPlanCollectionEnvDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanCollectionMapper {

    List<TestPlanCollectionConfigDTO> getList(@Param("planId") String planId);

    List<TestPlanCollectionEnvDTO> selectSecondCollectionEnv(@Param("type") String type, @Param("parentId") String parentId, @Param("testPlanId") String testPlanId);

    TestPlanCollectionEnvDTO selectFirstCollectionEnv(@Param("type") String type, @Param("parentId") String parentId, @Param("testPlanId") String testPlanId);

    String selectDefaultCollectionId(@Param("testPlanId")String newTestPlanId,@Param("type") String key);

    boolean getParentStopOnFailure(String collectionId);

    List<TestPlanCollection> selectByItemParentId(String collectionId);
}
