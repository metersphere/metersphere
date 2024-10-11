package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.dto.definition.request.ApiDocShareEditRequest;
import io.metersphere.api.dto.definition.request.ApiDocSharePageRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDocShareControllerTests extends BaseTest {

	private static final String BASE_PATH = "/api/doc/share/";
	private final static String ADD = BASE_PATH + "add";
	private final static String UPDATE = BASE_PATH + "update";
	private final static String DELETE = BASE_PATH + "delete/";
	private final static String PAGE = BASE_PATH + "page";

	@Order(1)
	@Test
	public void addOrUpdate() throws Exception {
		ApiDocShareEditRequest request = new ApiDocShareEditRequest();
		request.setName("share-1");
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setApiRange("ALL");
		request.setIsPublic(false);
		request.setAllowExport(false);
		MvcResult mvcResult = this.requestPostWithOk(ADD, request).andReturn();
		String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
		ApiDocShare docShare = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiDocShare.class);
		request.setId(docShare.getId());
		request.setName("share-2");
		request.setApiRange("MODULE");
		request.setRangeMatchVal("module-1");
		request.setInvalidTime(1);
		request.setInvalidUnit("HOUR");
		this.requestPostWithOk(UPDATE, request);
		this.requestGetWithOk(DELETE + docShare.getId());
		// 不存在的ID
		this.requestGet(DELETE + "not-exist-id").andExpect(status().is5xxServerError());
	}

	@Order(2)
	@Test
	public void page() throws Exception {
		ApiDocShareEditRequest request = new ApiDocShareEditRequest();
		request.setName("share-1");
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setApiRange("ALL");
		request.setIsPublic(false);
		request.setAllowExport(false);
		this.requestPostWithOk(ADD, request);
		request.setInvalidTime(1);
		request.setInvalidUnit("HOUR");
		request.setApiRange("MODULE");
		request.setRangeMatchVal("module-1");
		this.requestPostWithOk(ADD, request);
		request.setApiRange("PATH");
		request.setRangeMatchSymbol("EQUALS");
		request.setRangeMatchVal("path-1");
		this.requestPostWithOk(ADD, request);
		request.setRangeMatchSymbol("CONTAINS");
		this.requestPostWithOk(ADD, request);
		request.setApiRange("TAG");
		request.setRangeMatchVal("tag-1,tag-2");
		this.requestPostWithOk(ADD, request);
		ApiDocSharePageRequest pageRequest = new ApiDocSharePageRequest();
		pageRequest.setProjectId(DEFAULT_PROJECT_ID);
		pageRequest.setCurrent(1);
		pageRequest.setPageSize(10);
		this.requestPostWithOk(PAGE, pageRequest);
	}
}
