package io.metersphere.plan.dto.request;

import io.metersphere.plan.domain.TestPlanAllocation;
import io.metersphere.plan.dto.TestPlanAllocationTypeDTO;
import io.metersphere.plan.dto.TestPlanCollectionInitDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 规划创建请求参数-脑图使用
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanAllocationCreateRequest {

	@Schema(description = "规划节点(分类/类型-第二级)")
	private List<TestPlanAllocationTypeDTO> typeNodes;

	@Schema(description = "规划节点(功能集-第三级)")
	private List<TestPlanCollectionInitDTO> collectionNodes;

	@Schema(description = "规划配置")
	private TestPlanAllocation config;
}
