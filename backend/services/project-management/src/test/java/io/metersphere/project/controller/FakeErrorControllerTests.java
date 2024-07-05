package io.metersphere.project.controller;

import io.metersphere.project.domain.FakeError;
import io.metersphere.project.domain.FakeErrorExample;
import io.metersphere.project.dto.FakeErrorDTO;
import io.metersphere.project.dto.filemanagement.request.FakeErrorDelRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorStatusRequest;
import io.metersphere.project.mapper.FakeErrorMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FakeErrorControllerTests extends BaseTest {

    @Resource
    private FakeErrorMapper fakeErrorMapper;

    private static final String prefix = "/fake/error";
    private static final String ADD = prefix + "/add";
    private static final String UPDATE = prefix + "/update";
    private static final String UPDATE_ENABLE = UPDATE + "/enable";
    private static final String DELETE = prefix + "/delete";
    private static final String LIST = prefix + "/list";

    @Test
    @Order(1)
    public void addFakeErrorRuleSuccess() throws Exception {
        List<FakeErrorDTO> dtoList = new ArrayList<>();
        FakeErrorDTO dto = new FakeErrorDTO();
        dto.setName("测试新增误报规则");
        dto.setExpression("ok");
        dto.setProjectId(DEFAULT_PROJECT_ID);
        dto.setRelation("equal");
        dto.setType("dd");
        dto.setRespType("data");
        dtoList.add(dto);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, dtoList);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertEquals(1,fakeErrors.size());
        FakeErrorDTO dto1 = new FakeErrorDTO();
        dto1.setName("测试新增误报规则");
        dto1.setExpression("ok");
        dto1.setProjectId(DEFAULT_PROJECT_ID);
        dto1.setRelation("equal");
        dto1.setType("dd");
        dto1.setRespType("headers");
        dtoList.add(dto1);
        mvcResult = this.requestPostWithOkAndReturn(ADD, dtoList);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        //名字重复不添加
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertEquals(1,fakeErrors.size());
    }

    @Test
    @Order(2)
    public void getListFakeErrorRuleSuccess() throws Exception {
        FakeErrorRequest request = new FakeErrorRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(LIST, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

        request.setKeyword("测试");
        mvcResult = this.requestPostWithOkAndReturn(LIST, request);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(3)
    public void updateFakeErrorRuleSuccess() throws Exception {
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        List<FakeErrorDTO> dtoList = new ArrayList<>();
        FakeErrorDTO dto = new FakeErrorDTO();
        dto.setId(fakeErrors.getFirst().getId());
        dto.setName("测试新增误报规则更新");
        dto.setExpression("ok");
        dto.setProjectId(DEFAULT_PROJECT_ID);
        dto.setRelation("equal");
        dto.setType("dd");
        dto.setRespType("data");
        dto.setEnable(true);
        dtoList.add(dto);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE, dtoList);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertTrue(fakeErrors.getFirst().getEnable());

        mvcResult = this.requestPostWithOkAndReturn(ADD, dtoList);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertEquals(1, fakeErrors.size());

        FakeErrorDTO dtoTwo = new FakeErrorDTO();
        dtoTwo.setName("测试新增误报规则更新2");
        dtoTwo.setExpression("ok");
        dtoTwo.setProjectId(DEFAULT_PROJECT_ID);
        dtoTwo.setRelation("equal");
        dtoTwo.setType("dd");
        dtoTwo.setRespType("data");
        dtoTwo.setEnable(true);
        List<FakeErrorDTO> dtoListTwo = new ArrayList<>();

        dtoListTwo.add(dtoTwo);
        mvcResult = this.requestPostWithOkAndReturn(ADD, dtoListTwo);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertEquals(2, fakeErrors.size());

        List<FakeErrorDTO> updateList = new ArrayList<>();
        for (FakeError fakeError : fakeErrors) {
            FakeErrorDTO fakeErrorDTO = new FakeErrorDTO();
            BeanUtils.copyBean(fakeErrorDTO,fakeError);
            fakeErrorDTO.setName("更新同名");
            updateList.add(fakeErrorDTO);
        }
        mvcResult = this.requestPostWithOkAndReturn(UPDATE, updateList);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        //名字重复不更新
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        Assertions.assertFalse(StringUtils.equalsIgnoreCase(fakeErrors.getFirst().getName(),fakeErrors.get(1).getName()));
    }

    @Test
    @Order(4)
    public void updateEnableFakeErrorRuleSuccess() throws Exception {
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        List<String> ids = fakeErrors.stream().map(FakeError::getId).toList();
        FakeErrorStatusRequest fakeErrorStatusRequest = new FakeErrorStatusRequest();
        fakeErrorStatusRequest.setProjectId(DEFAULT_PROJECT_ID);
        fakeErrorStatusRequest.setEnable(false);
        fakeErrorStatusRequest.setSelectAll(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE_ENABLE, fakeErrorStatusRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        //名字重复不更新
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        List<FakeError> list = fakeErrors.stream().filter(FakeError::getEnable).toList();
        Assertions.assertEquals(0,list.size());
        fakeErrorStatusRequest = new FakeErrorStatusRequest();
        fakeErrorStatusRequest.setProjectId(DEFAULT_PROJECT_ID);
        fakeErrorStatusRequest.setEnable(true);
        fakeErrorStatusRequest.setSelectAll(false);
        mvcResult = this.requestPostWithOkAndReturn(UPDATE_ENABLE, fakeErrorStatusRequest);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        list = fakeErrors.stream().filter(FakeError::getEnable).toList();
        Assertions.assertEquals(0,list.size());
        String oneId = ids.getFirst();
        fakeErrorStatusRequest = new FakeErrorStatusRequest();
        fakeErrorStatusRequest.setProjectId(DEFAULT_PROJECT_ID);
        fakeErrorStatusRequest.setEnable(true);
        fakeErrorStatusRequest.setSelectAll(false);
        fakeErrorStatusRequest.setSelectIds(List.of(oneId));
        mvcResult = this.requestPostWithOkAndReturn(UPDATE_ENABLE, fakeErrorStatusRequest);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        fakeErrors = fakeErrorMapper.selectByExample(new FakeErrorExample());
        list = fakeErrors.stream().filter(FakeError::getEnable).toList();
        Assertions.assertEquals(1,list.size());

    }

    @Test
    @Order(5)
    public void deleteFakeErrorRuleSuccess() throws Exception {
        List<FakeErrorDTO> dtoList = new ArrayList<>();
        FakeErrorDTO dto = new FakeErrorDTO();
        dto.setName("用来删除");
        dto.setExpression("ok");
        dto.setProjectId(DEFAULT_PROJECT_ID);
        dto.setRelation("equal");
        dto.setType("dd");
        dto.setRespType("data");
        dtoList.add(dto);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, dtoList);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        FakeErrorExample fakeErrorExample = new FakeErrorExample();
        fakeErrorExample.createCriteria().andNameLike("用来删除");
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(fakeErrorExample);
        String deleteId = fakeErrors.getFirst().getId();
        Assertions.assertEquals(1,fakeErrors.size());
        FakeErrorDelRequest request = new FakeErrorDelRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        mvcResult = this.requestPostWithOkAndReturn(DELETE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        request.setSelectAll(true);
        fakeErrorExample = new FakeErrorExample();
        fakeErrorExample.createCriteria().andNameLike("测试");
        fakeErrors = fakeErrorMapper.selectByExample(fakeErrorExample);
        List<String> ids = fakeErrors.stream().map(FakeError::getId).toList();
        request.setExcludeIds(ids);
        mvcResult = this.requestPostWithOkAndReturn(DELETE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        FakeError fakeError = fakeErrorMapper.selectByPrimaryKey(deleteId);
        Assertions.assertNull(fakeError);

    }




}
