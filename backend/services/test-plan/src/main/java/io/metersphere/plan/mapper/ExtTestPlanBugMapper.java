package io.metersphere.plan.mapper;

import io.metersphere.plan.dto.TestPlanBugCaseDTO;
import io.metersphere.plan.dto.request.TestPlanBugPageRequest;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanBugMapper {

	/**
	 * 查询计划-关联缺陷列表
	 * @param request 请求参数
	 * @return 缺陷列表
	 */
	List<TestPlanBugPageResponse> list(@Param("request") TestPlanBugPageRequest request);

	/**
	 * 根据缺陷ID集合获取计划下缺陷关联的用例集合
	 * @param bugIds 缺陷ID集合
	 * @param planId 计划ID
	 * @return 用例集合
	 */
	List<TestPlanBugCaseDTO> getBugRelatedCase(@Param("ids") List<String> bugIds, @Param("planId") String planId);


	List<TestPlanBugPageResponse> countBugByIds(@Param("planIds") List<String> planIds);
}
