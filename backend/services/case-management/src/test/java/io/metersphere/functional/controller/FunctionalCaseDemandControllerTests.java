package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.request.FunctionalCaseDemandRequest;
import io.metersphere.functional.request.QueryDemandListRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.OperationLog;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseDemandControllerTests extends BaseTest {

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private OperationLogMapper operationLogMapper;

    private static final String URL_DEMAND_PAGE = "/functional/case/demand/page";
    private static final String URL_DEMAND_ADD = "/functional/case/demand/add";
    private static final String URL_DEMAND_UPDATE = "/functional/case/demand/update";
    private static final String URL_DEMAND_CANCEL = "/functional/case/demand/cancel/";
    private static final String URL_DEMAND_BATCH_RELEVANCE = "/functional/case/demand/batch/relevance";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_demand.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addDemandSuccess() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
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
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入孩子");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertFalse(functionalCaseDemands.isEmpty());
    }

    @Test
    @Order(2)
    public void addDemandEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_ADD, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(functionalCaseDemands.isEmpty());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
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
    }

    @Test
    @Order(3)
    public void addDemandFalse() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
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
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
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
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCaseDemandExample functionalCaseDemandExample;
        List<FunctionalCaseDemand> functionalCaseDemands;
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId(id);
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandName("手动加入2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.get(0).getDemandName(), "手动加入2"));
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
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_UPDATE, functionalCaseDemandRequest);
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(StringUtils.equals(functionalCaseDemands.get(0).getDemandName(), "手动加入2"));
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
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        List<DemandDTO> demandList = new ArrayList<>();
        DemandDTO demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertNull(functionalCaseDemands.get(0).getDemandId());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setId("hehe");
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
        demandList = new ArrayList<>();
        demandDTO = new DemandDTO();
        demandDTO.setDemandId("111");
        demandDTO.setDemandName("手动执行2");
        demandList.add(demandDTO);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_UPDATE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());

        functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        functionalCaseDemandRequest.setDemandPlatform("LOCAL");
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
        return functionalCaseDemands.get(0).getId();
    }

    @Test
    @Order(7)
    public void getDemandList() throws Exception {
        QueryDemandListRequest queryDemandListRequest = new QueryDemandListRequest();
        queryDemandListRequest.setCurrent(1);
        queryDemandListRequest.setPageSize(5);
        queryDemandListRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        Pager<List<FunctionalDemandDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        List<FunctionalDemandDTO> list = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalDemandDTO.class);
        for (FunctionalDemandDTO functionalDemandDTO : list) {
            Assertions.assertTrue(CollectionUtils.isNotEmpty(functionalDemandDTO.getChildren()));
        }
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
        queryDemandListRequest = new QueryDemandListRequest();
        queryDemandListRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        queryDemandListRequest.setCurrent(1);
        queryDemandListRequest.setPageSize(5);
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量为空
        Assertions.assertTrue(CollectionUtils.isEmpty(tableData.getList()));

        queryDemandListRequest = new QueryDemandListRequest();
        queryDemandListRequest.setCurrent(1);
        queryDemandListRequest.setPageSize(5);
        queryDemandListRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID3");
        mvcResult = this.requestPostWithOkAndReturn(URL_DEMAND_PAGE, queryDemandListRequest);
        tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), queryDemandListRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= queryDemandListRequest.getPageSize());
        List<FunctionalDemandDTO> list1 = JSON.parseArray(JSON.toJSONString(tableData.getList()), FunctionalDemandDTO.class);
        for (FunctionalDemandDTO functionalDemandDTO : list1) {
            Assertions.assertTrue(CollectionUtils.isEmpty(functionalDemandDTO.getChildren()));
        }
    }

    @Test
    @Order(8)
    public void cancelDemand() throws Exception {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> beforeList = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        String id = getId("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        mockMvc.perform(MockMvcRequestBuilders.get(URL_DEMAND_CANCEL+id).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID");
        List<FunctionalCaseDemand> after = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(beforeList.size()>after.size());
        checkLog("DEMAND_TEST_FUNCTIONAL_CASE_ID", OperationLogType.DISASSOCIATE);
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
        demandDTO2.setDemandName("手动加入Tapd1");
        demandDTO2.setDemandUrl("https://www.tapd.cn/55049933/prong/stories/view/1155049933001012783");
        demandList.add(demandDTO2);
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("100003");
        demandDTO3.setDemandName("手动加入Tapd2");
        demandList.add(demandDTO3);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest);
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertEquals(functionalCaseDemands.size(), demandList.size());
    }

    @Test
    @Order(10)
    public void batchRelevanceEmpty() throws Exception {
        FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
        functionalCaseDemandRequest.setCaseId("DEMAND_TEST_FUNCTIONAL_CASE_ID2");
        functionalCaseDemandRequest.setDemandPlatform("ZanDao");
        List<DemandDTO> demandList = new ArrayList<>();
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPostWithOkAndReturn(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest);
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
        DemandDTO demandDTO3 = new DemandDTO();
        demandDTO3.setDemandId("100007");
        demandDTO3.setDemandName("手动加入jira2");
        demandList.add(demandDTO3);
        functionalCaseDemandRequest.setDemandList(demandList);
        this.requestPost(URL_DEMAND_BATCH_RELEVANCE, functionalCaseDemandRequest).andExpect(status().is5xxServerError());
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo("DEMAND_TEST_FUNCTIONAL_CASE_ID2").andDemandPlatformEqualTo("jira");
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(functionalCaseDemands));
    }

    @Test
    @Order(12)
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
}
