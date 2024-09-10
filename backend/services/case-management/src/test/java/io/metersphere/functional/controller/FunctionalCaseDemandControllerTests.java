package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.DemandSyncService;
import io.metersphere.functional.service.FunctionalCaseDemandService;
import io.metersphere.plugin.platform.dto.response.PlatformDemandDTO;
import io.metersphere.plugin.platform.utils.PluginPager;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.OperationLog;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.SystemParameterMapper;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseDemandControllerTests extends BaseTest {

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private FunctionalCaseDemandService functionalCaseDemandService;
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Resource
    private DemandSyncService demandSyncService;
    @Resource
    private MockServerClient mockServerClient;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;



    private static final String URL_DEMAND_PAGE = "/functional/case/demand/page";
    private static final String URL_DEMAND_ADD = "/functional/case/demand/add";
    private static final String URL_DEMAND_UPDATE = "/functional/case/demand/update";
    private static final String URL_DEMAND_CANCEL = "/functional/case/demand/cancel/";
    private static final String URL_DEMAND_BATCH_RELEVANCE = "/functional/case/demand/batch/relevance";
    private static final String URL_DEMAND_PAGE_DEMAND = "/functional/case/demand/third/list/page";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_demand.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addDemandSuccess() throws Exception {
        basePluginTestService.addJiraPlugin();
        basePluginTestService.addServiceIntegration(DEFAULT_ORGANIZATION_ID);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/search"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"id\":\"123456\",\"name\":\"test\", \"issues\": [{\"key\": \"TES-1\",\"fields\": {\"summary\": \"Test\"}}], \"total\": 1}")

                );
        FunctionalDemandBatchRequest functionalDemandBatchRequest = new FunctionalDemandBatchRequest();
        functionalDemandBatchRequest.setSelectAll(false);
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("001");
        demandDTO.setDemandName("手动加入1");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入孩子");
        demandDTO.setDemandId("001001");
        demandDTO.setParent("001");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动加入孩子超长名字看看能不能截到255的速度和温度都会为fdhfjhdsfjhdsfjdshfjdsfhdsfhufkfjdfkgdgbdfjgdfgjbdfjbdfgjbdfgjkbdfjkgbdfjkgbdfjkbgdfjkbgdfjbgdfjgbdfjgbdfjbgdfjgbdfjgbdfjkgbdfjkgbdfkjgb返回武汉无法回我fhdfjdsfhsdhfdsfhdsjfhsdjfhdsfjdshfjdshfjdshfjdshfdjsfhdsjfhdjfhdsjfhdjksfhsdjfdsjfhdjfhdsjfh");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID").andDemandNameLike("%手动加入孩子超长名字%");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setFunctionalDemandBatchRequest(functionalDemandBatchRequest);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID5");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        functionalDemandBatchRequest.setSelectAll(true);
        functionalCaseDemandRequest.setFunctionalDemandBatchRequest(functionalDemandBatchRequest);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);

        PluginPager<PlatformDemandDTO> platformDemandDTOPluginPager = new PluginPager<>();
        platformDemandDTOPluginPager.setCurrent(1);
        platformDemandDTOPluginPager.setPageSize(500);
        platformDemandDTOPluginPager.setTotal(1);
        PlatformDemandDTO platformDemandDTO = new PlatformDemandDTO();
        platformDemandDTO.setCustomHeaders(new ArrayList<>());
        List<PlatformDemandDTO.Demand> demands = new ArrayList<>();
        PlatformDemandDTO.Demand demand = new PlatformDemandDTO.Demand();
        demand.setDemandUrl("https://www.baidu.com/");
        demand.setDemandId("10233");
        demand.setDemandName("白度需求");
        List<PlatformDemandDTO.Demand> children = new ArrayList<>();
        PlatformDemandDTO.Demand child = new PlatformDemandDTO.Demand();
        child.setDemandUrl("https://www.baidu.com/");
        child.setDemandId("10234");
        child.setDemandName("白度需求");
        child.setParent("10233");
        children.add(child);
        demand.setChildren(children);
        demands.add(demand);
        platformDemandDTO.setList(demands);
        platformDemandDTOPluginPager.setData(platformDemandDTO);
        functionalCaseDemandService.getDemandDTOS(platformDemandDTOPluginPager);
    }

    @Test
    @Order(2)
    public void addDemandEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入3");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("001");
        demandDTO2.setDemandName("手动加入1");
        demandList = new ArrayList<>();
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);

        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertEquals(3,functionalCaseDemands.size());
    }

    @Test
    @Order(3)
    public void addDemandFalse() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is4xxClientError());
    }

    @Test
    @Order(4)
    public void updateDemandSuccess() throws Exception {
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setParamKey("ui.platformName");
        systemParameter.setParamValue("Metersphere");
        systemParameter.setType("text");
        systemParameterMapper.insertSelective(systemParameter);
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入2");
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.getFirst().getDemandName(), "手动加入2"));
    }

    @Test
    @Order(5)
    public void updateDemandEmpty() throws Exception {
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.getFirst().getDemandName(), "手动加入2"));
    }

    @Test
    @Order(6)
    public void updateDemandFalse() throws Exception {
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertNotNull(functionalCaseDemands.getFirst().getDemandId());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId("hehe");
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动执行2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("Metersphere");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动执行2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is4xxClientError());
    }

    private String getId(String caseId) {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(caseId);
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        return functionalCaseDemands.getFirst().getId();
    }

    @Test
    @Order(7)
    public void getDemandList() throws Exception {
        QueryDemandListRequest queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());

        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
        queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量为空
        Assertions.assertTrue(CollectionUtils.isEmpty(tableData.getList()));

        queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
    }

    @NotNull
    private static QueryDemandListRequest getQueryDemandListRequest(String caseId) {
        QueryDemandListRequest queryDemandListRequest = new QueryDemandListRequest();
        queryDemandListRequest.setProjectId("project-case-demand-test");
        queryDemandListRequest.setCurrent(1);
        queryDemandListRequest.setPageSize(5);
        queryDemandListRequest.setCaseId(caseId);
        return queryDemandListRequest;
    }

    @Test
    @Order(8)
    public void cancelDemand() throws Exception {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemand functionalCaseDemandInDb = functionalCaseDemandMapper.selectByPrimaryKey(id);
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemand.setParent(functionalCaseDemandInDb.getDemandId());
        functionalCaseDemand.setDemandId("qwerr");
        functionalCaseDemand.setDemandUrl("dddd");
        functionalCaseDemand.setDemandPlatform("Metersphere");
        functionalCaseDemand.setId(UUID.randomUUID().toString());
        functionalCaseDemand.setWithParent(false);
        functionalCaseDemand.setDemandName("加一下副需求");
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setCreateUser("admin");
        functionalCaseDemand.setUpdateUser("admin");
        functionalCaseDemandMapper.insert(functionalCaseDemand);
        List<FunctionalCaseDemand> beforeList = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+id).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> after = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(beforeList.size()>after.size());
        checkLog("DEMAND_TEST_FUNCTIONAL_CASE_ID", OperationLogType.DISASSOCIATE);
        id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+id).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    public void batchRelevanceSuccess() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("TAPD");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("100001");
        demandDTO.setDemandName("手动加入Tapd");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("100002");
        demandDTO2.setParent("100001");
        demandDTO2.setDemandName("手动加入Tapd1");
        demandDTO2.setDemandUrl("https://www.tapd.cn/55049933/prong/stories/view/1155049933001012783");
        demandList.add(demandDTO2);
        DemandDTO demandDTO4 = new DemandDTO();
        demandDTO4.setDemandId("100003");
        demandDTO4.setParent("100001");
        demandDTO4.setDemandName("手动加入Tapd2");
        demandList.add(demandDTO4);
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("100004");
        demandDTO3.setParent("100002");
        demandDTO3.setDemandName("手动加入Tapd2-1");
        demandList.add(demandDTO3);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertEquals(functionalCaseDemands.size(), demandList.size());

        QueryDemandListRequest queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<FunctionalDemandDTO> list1 = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalDemandDTO.class);
        for (FunctionalDemandDTO functionalDemandDTO : list1) {
            Assertions.assertTrue(CollectionUtils.isNotEmpty(functionalDemandDTO.getChildren()));
        }
    }

    @Test
    @Order(10)
    public void batchRelevanceEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("ZanDao");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("ZanDao");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));

    }

    @Test
    @Order(11)
    public void batchRelevanceFalse() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("jira");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("100005");
        demandDTO.setDemandName("手动加入jira");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("100006");
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("jira");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("jira");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("100007");
        demandDTO.setDemandName("手动加入jira");
        demandList.add(demandDTO);
        demandDTO2 = new DemandDTO();
        demandDTO2.setDemandName("手动加入jira2");
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_ADD, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(12)
    public void batchCaseRelevance() throws Exception {
        FunctionalCaseDemandBatchRequest functionalCaseDemandBatchRequest = new FunctionalCaseDemandBatchRequest();
        functionalCaseDemandBatchRequest.setSelectAll(true);
        functionalCaseDemandBatchRequest.setExcludeIds(List.of("DEMAND_TEST_FUNCTIONAL_CASE_ID4"));
        functionalCaseDemandBatchRequest.setProjectId("project-case-demand-test");
        functionalCaseDemandBatchRequest.setDemandPlatform("jira");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("100008");
        demandDTO.setDemandName("批量手动加入jira");
        demandDTO.setParent("100007");
        demandDTO.setDemandUrl("http://www.baidu.com");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("100007");
        demandDTO2.setDemandName("批量手动加入jira爸爸");
        demandDTO2.setDemandUrl("http://www.baidu.com");
        demandList.add(demandDTO2);
        functionalCaseDemandBatchRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandBatchRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andDemandPlatformEqualTo("jira");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertEquals(6,functionalCaseDemands.size());
        String jsonString = JSON.toJSONString(functionalCaseDemands);
        System.out.println(jsonString);

        functionalCaseDemandBatchRequest.setExcludeIds(List.of("DEMAND_TEST_FUNCTIONAL_CASE_ID3","DEMAND_TEST_FUNCTIONAL_CASE_ID4"));
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("100009");
        demandDTO.setDemandName("批量手动加入jira2");
        demandDTO.setParent("100007");
        demandDTO.setDemandUrl("http://www.baidu.com");
        demandList.add(demandDTO);
        functionalCaseDemandBatchRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandBatchRequest);
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        jsonString = JSON.toJSONString(functionalCaseDemands);
        System.out.println(jsonString);
        functionalCaseDemandBatchRequest.setSelectAll(false);
        functionalCaseDemandBatchRequest.setSelectIds(List.of("DEMAND_TEST_FUNCTIONAL_CASE_ID3"));
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandBatchRequest);
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        jsonString = JSON.toJSONString(functionalCaseDemands);
        System.out.println(jsonString);
        functionalCaseDemandBatchRequest.setDemandList(new ArrayList<>());
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandBatchRequest);

        functionalCaseDemandBatchRequest = new FunctionalCaseDemandBatchRequest();
        functionalCaseDemandBatchRequest.setSelectAll(true);
        functionalCaseDemandBatchRequest.setProjectId("gyq_project-case-demand-test");
        functionalCaseDemandBatchRequest.setDemandPlatform("jira");
        FunctionalDemandBatchRequest functionalDemandBatchRequest = new FunctionalDemandBatchRequest();
        functionalDemandBatchRequest.setSelectAll(true);
        functionalCaseDemandBatchRequest.setFunctionalDemandBatchRequest(functionalDemandBatchRequest);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandBatchRequest);

    }

    @Test
    @Order(13)
    public void cancelDemandNoLog() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+"DEMAND_TEST_FUNCTIONAL_CASE_X").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_X");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andSourceIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_X").andTypeEqualTo(OperationLogType.DISASSOCIATE.name());
        List<OperationLog> operationLogs = operationLogMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(operationLogs));
    }

    @Test
    @Order(14)
    public void addNoParent() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID4");
        functionalCaseDemandRequest.setDemandPlatform("TAPD");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("789");
        demandDTO.setDemandName("单独的孩子");
        demandDTO.setParent("788");
        demandList.add(demandDTO);
        DemandDTO demandDTO2 = new DemandDTO();
        demandDTO2.setDemandId("790");
        demandDTO2.setDemandName("单独的孩子2");
        demandDTO2.setParent("789");
        demandList.add(demandDTO2);
        DemandDTO demandDTO7 = new DemandDTO();
        demandDTO7.setDemandId("792");
        demandDTO7.setDemandName("单独的孩子3");
        demandDTO7.setParent("789");
        demandList.add(demandDTO7);
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("666");
        demandDTO3.setDemandName("单独的父亲");
        demandList.add(demandDTO3);
        DemandDTO demandDTO6 = new DemandDTO();
        demandDTO6.setDemandId("666");
        demandDTO6.setParent("999");
        demandDTO6.setDemandName("单独的父亲");
        demandList.add(demandDTO6);
        DemandDTO demandDTO4 = new DemandDTO();
        demandDTO4.setDemandId("888");
        demandDTO4.setDemandName("单独的另一个");
        demandDTO4.setParent("uuuu");
        demandList.add(demandDTO4);
        DemandDTO demandDTO5 = new DemandDTO();
        demandDTO5.setDemandId("791");
        demandDTO5.setDemandName("单独的孩子3");
        demandDTO5.setParent("790");
        demandList.add(demandDTO5);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID4");
        functionalCaseDemandRequest.setDemandPlatform("jira");
        demandList = new ArrayList<>();
        demandDTO2.setDemandId("790");
        demandDTO2.setDemandName("单独的孩子2");
        demandDTO2.setParent("789");
        demandList.add(demandDTO2);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        QueryDemandListRequest queryDemandListRequest = getQueryDemandListRequest("DEMAND_TEST_FUNCTIONAL_CASE_ID4");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<FunctionalDemandDTO> list1 = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalDemandDTO.class);
        Assertions.assertEquals(5, list1.size());
    }

    @Test
    @Order(15)
    public void pageDemandSuccess() throws Exception {
        FunctionalThirdDemandPageRequest functionalThirdDemandPageRequest = new FunctionalThirdDemandPageRequest();
        functionalThirdDemandPageRequest.setProjectId("gyq_project-case-demand-test");
        functionalThirdDemandPageRequest.setPageSize(10);
        functionalThirdDemandPageRequest.setCurrent(1);
        functionalThirdDemandPageRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        MvcResult mvcResultDemand= this.requestPostWithOkAndReturn(URL_DEMAND_PAGE_DEMAND, functionalThirdDemandPageRequest);
        PluginPager<PlatformDemandDTO> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultDemand.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                PluginPager.class);
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setId("测试过滤ID");
        functionalCaseDemand.setDemandName("Test");
        functionalCaseDemand.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemand.setDemandId("TES-1");
        functionalCaseDemand.setDemandUrl("http://localhost:57767/jira/software/projects/TES/issues/TES-1");
        functionalCaseDemand.setDemandPlatform("Metersphere");
        functionalCaseDemand.setUpdateUser("admin");
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        functionalCaseDemand.setWithParent(false);
        functionalCaseDemand.setCreateUser("admin");
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setParent("NONE");
        functionalCaseDemandMapper.insert(functionalCaseDemand);
        mvcResultDemand= this.requestPostWithOkAndReturn(URL_DEMAND_PAGE_DEMAND, functionalThirdDemandPageRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultDemand.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                PluginPager.class);

    }

    @Test
    @Order(16)
    public void syncDemandSuccess() {
        List<ProjectApplication> relatedConfigs = new ArrayList<>();
        ProjectApplication projectApplication1 = new ProjectApplication("gyq_project-case-demand-test", ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED + "_cron_expression", "0 0 0 * * ?");
        ProjectApplication projectApplication4 = new ProjectApplication("gyq_project-case-demand-test", ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED + "_sync_enable", "true");
        relatedConfigs.add(projectApplication1);
        relatedConfigs.add(projectApplication4);
        projectApplicationMapper.batchInsert(relatedConfigs);

        demandSyncService.syncPlatformDemandBySchedule("gyq_project-case-demand-test", "admin");

        String demandPlatformId = projectApplicationService.getDemandPlatformId("gyq_project-case-demand-test");
        Assertions.assertTrue(StringUtils.isNotBlank(demandPlatformId));
    }

}
