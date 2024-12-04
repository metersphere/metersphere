package io.metersphere.plan.dto.response;

import io.metersphere.plan.dto.ReportDetailCasePageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanReportDetailCollectionResponse {

	@Schema(description = "测试集ID")
	private String id;
	@Schema(description = "测试集名称")
	private String collectionName;
	@Schema(description = "测试集用例数量")
	private Long count;
	@Schema(description = "计划名称")
	private String planName;
	@Schema(description = "位置")
	private Long pos;

	@Schema(description = "用例数据")
	List<ReportDetailCasePageDTO> reportDetailCaseList;
}
