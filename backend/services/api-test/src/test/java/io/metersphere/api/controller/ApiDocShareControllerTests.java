package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
	private final static String CHECK = BASE_PATH + "check";
	private final static String DETAIL = BASE_PATH + "detail/";
	private final static String MODULE_TREE = BASE_PATH + "module/tree";
	private final static String MODULE_COUNT = BASE_PATH + "module/count";
	private final static String EXPORT = BASE_PATH + "export/Swagger";
	private final static String DOWNLOAD = BASE_PATH + "download/file/";
	private final static String GET_DETAIL = BASE_PATH + "get-detail/";
	private final static String GET_PLUGIN_SCRIPT = BASE_PATH + "plugin/script/";

	@Order(1)
	@Test
	@Sql(scripts = {"/dml/init_api_doc_share.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
	public void addOrUpdate() throws Exception {
		ApiDocShareEditRequest request = new ApiDocShareEditRequest();
		request.setName("share-1");
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setApiRange("ALL");
		request.setIsPrivate(false);
		request.setPassword("123456");
		request.setAllowExport(false);
		request.setInvalidTime(0L);
		MvcResult mvcResult = this.requestPostWithOk(ADD, request).andReturn();
		String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
		ApiDocShare docShare = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiDocShare.class);
		// check pwd
		ApiDocShareCheckRequest checkRequest = new ApiDocShareCheckRequest();
		checkRequest.setDocShareId(docShare.getId());
		checkRequest.setPassword("123456");
		this.requestPostWithOk(CHECK, checkRequest);
		this.requestGetWithOk(DETAIL + docShare.getId());
		ApiDocShareModuleRequest moduleRequest = new ApiDocShareModuleRequest();
		moduleRequest.setShareId(docShare.getId());
		moduleRequest.setProjectId(DEFAULT_PROJECT_ID);
		moduleRequest.setOrgId(DEFAULT_ORGANIZATION_ID);
		this.requestPostWithOk(MODULE_TREE, moduleRequest);
		request.setId(docShare.getId());
		request.setName("share-2");
		request.setPassword(StringUtils.EMPTY);
		request.setApiRange("MODULE");
		request.setRangeMatchVal("module-1");
		request.setInvalidTime(System.currentTimeMillis());
		request.setInvalidUnit("HOUR");
		this.requestPostWithOk(UPDATE, request);
		this.requestPostWithOk(CHECK, checkRequest);
		this.requestGetWithOk(DETAIL + docShare.getId());
		this.requestPostWithOk(MODULE_TREE, moduleRequest);
		ApiDocShareExportRequest exportRequest = new ApiDocShareExportRequest();
		BeanUtils.copyBean(exportRequest, moduleRequest);
		exportRequest.setSelectAll(true);
		this.requestPostWithOk(EXPORT, exportRequest);
		exportRequest.setSelectAll(false);
		exportRequest.setSelectIds(List.of("export-id"));
		this.requestPost(EXPORT, exportRequest);
		this.download(DEFAULT_PROJECT_ID, "export-id");
		this.requestGetWithOk(DELETE + docShare.getId());
		// 不存在的ID
		this.requestGet(DELETE + "not-exist-id").andExpect(status().is5xxServerError());
		this.requestGetWithOk(GET_DETAIL + "doc-share-id");
		getPluginScript("doc-share-id", DEFAULT_ORGANIZATION_ID);
	}

	@Order(2)
	@Test
	public void page() throws Exception {
		ApiDocShareEditRequest request = new ApiDocShareEditRequest();
		request.setName("share-2");
		request.setProjectId(DEFAULT_PROJECT_ID);
		request.setApiRange("ALL");
		request.setIsPrivate(false);
		request.setAllowExport(false);
		this.requestPost(ADD, request);
		request.setName("share-3");
		request.setInvalidTime(System.currentTimeMillis());
		request.setInvalidUnit("HOUR");
		request.setApiRange("MODULE");
		request.setRangeMatchVal("module-1");
		this.requestPostWithOk(ADD, request);
		request.setName("share-4");
		request.setApiRange("PATH");
		request.setRangeMatchSymbol("EQUALS");
		request.setRangeMatchVal("path-1");
		MvcResult mvcResult = this.requestPostWithOk(ADD, request).andReturn();
		String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
		ApiDocShare docShare = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiDocShare.class);
		ApiDocShareModuleRequest moduleRequest = new ApiDocShareModuleRequest();
		moduleRequest.setShareId(docShare.getId());
		moduleRequest.setProjectId(DEFAULT_PROJECT_ID);
		moduleRequest.setOrgId(DEFAULT_ORGANIZATION_ID);
		this.requestPostWithOk(MODULE_COUNT, moduleRequest);
		request.setName("share-5");
		request.setRangeMatchSymbol("CONTAINS");
		MvcResult mvcResult1 = this.requestPostWithOk(ADD, request).andReturn();
		String returnData1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
		ResultHolder resultHolder1 = JSON.parseObject(returnData1, ResultHolder.class);
		ApiDocShare docShare1 = JSON.parseObject(JSON.toJSONString(resultHolder1.getData()), ApiDocShare.class);
		moduleRequest.setShareId(docShare1.getId());
		this.requestPostWithOk(MODULE_COUNT, moduleRequest);
		request.setName("share-6");
		request.setApiRange("TAG");
		request.setRangeMatchVal("tag-1,tag-2");
		MvcResult mvcResult2 = this.requestPostWithOk(ADD, request).andReturn();
		String returnData2 = mvcResult2.getResponse().getContentAsString(StandardCharsets.UTF_8);
		ResultHolder resultHolder2 = JSON.parseObject(returnData2, ResultHolder.class);
		ApiDocShare docShare2 = JSON.parseObject(JSON.toJSONString(resultHolder2.getData()), ApiDocShare.class);
		moduleRequest.setShareId(docShare2.getId());
		this.requestPostWithOk(MODULE_COUNT, moduleRequest);
		this.requestPost(ADD, request).andExpect(status().is5xxServerError());
		ApiDocSharePageRequest pageRequest = new ApiDocSharePageRequest();
		pageRequest.setProjectId(DEFAULT_PROJECT_ID);
		pageRequest.setCurrent(1);
		pageRequest.setPageSize(10);
		this.requestPostWithOk(PAGE, pageRequest);
	}

	private MvcResult download(String projectId, String fileId) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(DOWNLOAD + projectId + "/" + fileId)
				.header(SessionConstants.HEADER_TOKEN, sessionId)
				.header(SessionConstants.CSRF_TOKEN, csrfToken)).andReturn();
	}

	private MvcResult getPluginScript(String id, String orgId) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(GET_PLUGIN_SCRIPT + id + "/" + orgId)
				.header(SessionConstants.HEADER_TOKEN, sessionId)
				.header(SessionConstants.CSRF_TOKEN, csrfToken)).andReturn();
	}
}
