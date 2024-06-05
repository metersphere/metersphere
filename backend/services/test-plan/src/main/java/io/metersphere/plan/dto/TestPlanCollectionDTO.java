package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlanCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanCollectionDTO extends TestPlanCollection {

	@Schema(description = "测试子集")
	private List<TestPlanCollectionDTO> children;
}
