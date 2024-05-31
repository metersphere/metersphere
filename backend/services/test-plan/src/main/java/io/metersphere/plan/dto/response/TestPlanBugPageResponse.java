package io.metersphere.plan.dto.response;

import io.metersphere.plan.dto.TestPlanBugCaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanBugPageResponse {

	@Schema(description = "缺陷ID")
	private String id;
	@Schema(description = "缺陷业务ID")
	private String num;
	@Schema(description = "缺陷标题")
	private String title;
	@Schema(description = "缺陷内容(预览)")
	private String content;
	@Schema(description = "关联用例集合")
	private List<TestPlanBugCaseDTO> relateCases;
	@Schema(description = "处理人")
	private String handleUser;
	@Schema(description = "状态")
	private String status;
	@Schema(description = "创建人")
	private String createUser;
	@Schema(description = "创建时间")
	private Long createTime;
	@Schema(description = "测试计划id")
	private String testPlanId;
}
