package io.metersphere.plan.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanMapper {
    List<String> selectByParentId(String parentId);

    List<String> selectByParentIdList(@Param("list") List<String> parentTestPlanId);
}
