package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.ExcelParseDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.dto.request.UserBatchProcessRequest;
import io.metersphere.system.dto.request.UserChangeEnableRequest;
import io.metersphere.system.dto.request.UserEditRequest;
import io.metersphere.system.dto.response.UserBatchProcessResponse;
import io.metersphere.system.dto.response.UserImportResponse;
import io.metersphere.system.dto.response.UserTableResponse;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.UserService;
import io.metersphere.system.utils.UserTestUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;

    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;

    //失败请求返回编码
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    //测试过程中需要用到的数据
    private static final List<UserCreateInfo> USER_LIST = new ArrayList<>();
    private static final List<UserRoleOption> defaultUserRoleList = new ArrayList<>();
    //默认数据
    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String USER_NONE_ROLE_EMAIL = "tianyang.none.role@163.com";
    //已删除的用户ID
    private static final List<String> DELETED_USER_ID_LIST = new ArrayList<>();

    //记录查询到的组织信息
    private void setDefaultUserRoleList(MvcResult mvcResult) throws Exception {
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<UserRoleOption> userRoleList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserRoleOption.class);
        //返回值不为空
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userRoleList));
        defaultUserRoleList.addAll(userRoleList);
    }

    //成功入库的用户保存内存中，其他用例会使用到
    private void addUser2List(MvcResult mvcResult) throws Exception {
        UserBatchCreateDTO userMaintainRequest = UserTestUtils.parseObjectFromMvcResult(mvcResult, UserBatchCreateDTO.class);
        for(UserCreateInfo item : userMaintainRequest.getUserInfoList()){
            checkLog(item.getId(), OperationLogType.ADD);
        }
        //返回值不为空
        Assertions.assertNotNull(userMaintainRequest);
        USER_LIST.addAll(userMaintainRequest.getUserInfoList());
    }

    private void checkUserList() throws Exception {
        if (CollectionUtils.isEmpty(USER_LIST)) {
            //测试数据初始化入库
            this.testAddSuccess();
        }
    }

    private void checkUserDeleted() throws Exception {
        if (CollectionUtils.isEmpty(DELETED_USER_ID_LIST)) {
            //测试数据初始化入库
            this.testUserDeleteSuccess();
        }
    }

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }


    private MvcResult responseByString(String url, String param,ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(param)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }


    private MvcResult responseFile(String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(0)
    public void testGetGlobalSystemUserRoleSuccess() throws Exception {
        MvcResult mvcResult = this.responseGet(UserTestUtils.URL_GET_GLOBAL_SYSTEM);
        this.setDefaultUserRoleList(mvcResult);
    }

    @Test
    @Order(1)
    public void testAddSuccess() throws Exception {
        if (CollectionUtils.isEmpty(defaultUserRoleList)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        //模拟前台批量添加用户
        UserBatchCreateDTO userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>() {{
                    add(new UserCreateInfo() {{
                        setName(USER_DEFAULT_NAME);
                        setEmail(USER_DEFAULT_EMAIL);
                    }});
                    add(new UserCreateInfo() {{
                        setName("tianyang.no.2");
                        setEmail("tianyang.no.2@126.com");
                    }});
                }}
        );
        MvcResult mvcResult = this.responsePost(UserTestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);

        //含有重复的用户名称
        userMaintainRequest = UserTestUtils.getUserCreateDTO(

                defaultUserRoleList,
                new ArrayList<>() {{
                    add(new UserCreateInfo() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.1@126.com");
                    }});
                    add(new UserCreateInfo() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.2@126.com");
                    }});
                }}
        );
        mvcResult = this.responsePost(UserTestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);
    }

    @Test
    @Order(1)
    public void testAddError() throws Exception {
        if (CollectionUtils.isEmpty(defaultUserRoleList)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        UserBatchCreateDTO userMaintainRequest;
        List<UserCreateInfo> errorUserList = new ArrayList<>() {{
            add(new UserCreateInfo() {{
                setName("tianyang.error.1");
                setEmail("tianyang.error.name.1@126.com");
            }});
            add(new UserCreateInfo() {{
                setName("tianyang.error.2");
                setEmail("tianyang.error.name.2@126.com");
            }});
        }};

        /*
         * 校验参数不合法的反例
         * 每一次校验，使用getErrorUserCreateDTO方法重新获取参数，避免上一步的参数干扰
         */
        //所有参数都为空
        userMaintainRequest = UserTestUtils.getUserCreateDTO(null, null);
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组ID为空
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                null,
                errorUserList);
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //没有用户
        userMaintainRequest = UserTestUtils.getUserCreateDTO(

                defaultUserRoleList,
                null);
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有不存在的用户组
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList);
        userMaintainRequest.getUserRoleIdList().add(null);
        userMaintainRequest.getUserRoleIdList().add("");
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户名称为空的数据
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setEmail("tianyang.name.empty@126.com");
        }});
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户邮箱为空的数据
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.email.empty");
        }});
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户邮箱不符合标准
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("用户邮箱放飞自我");
            setEmail("用户邮箱放飞自我");
        }});
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        /*
         * 校验业务判断出错的反例 （500 error)
         * 需要保证数据库有正常数据
         */
        this.checkUserList();
        //不含有系统成员用户组
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList.stream().filter(item -> !StringUtils.equals(item.getId(), "member")).toList(),
                errorUserList);
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //含有重复的用户邮箱
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList
        );
        String firstUserEmail = userMaintainRequest.getUserInfoList().get(0).getEmail();
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.no.error4");
            setEmail(firstUserEmail);
        }});
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //测试请求参数中含有数据库中已存在的邮箱情况
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList
        );
        userMaintainRequest.setUserInfoList(
                new ArrayList<>() {{
                    add(new UserCreateInfo() {{
                        setName("tianyang.repeat.email.db");
                        setEmail(USER_DEFAULT_EMAIL);
                    }});
                }}
        );
        this.requestPost(UserTestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_user_controller_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetByEmailSuccess() throws Exception {
        this.checkUserList();
        UserDTO userDTO = this.getUserByEmail(USER_DEFAULT_EMAIL);
        //返回值不为空
        Assertions.assertNotNull(userDTO);
        //返回邮箱等于参数邮箱，且用户名合法
        Assertions.assertEquals(userDTO.getEmail(), USER_DEFAULT_EMAIL);
        Assertions.assertNotNull(userDTO.getName());

        //查询脏数据：没有关联任何组织的用户，也应该正常查询出来
        userDTO = this.getUserByEmail(USER_NONE_ROLE_EMAIL);
        //返回值不为空
        Assertions.assertNotNull(userDTO);
        //返回邮箱等于参数邮箱，且用户名合法
        Assertions.assertEquals(userDTO.getEmail(), USER_NONE_ROLE_EMAIL);
        Assertions.assertNotNull(userDTO.getName());
    }

    private UserDTO getUserByEmail(String email) throws Exception {
        String url = String.format(UserTestUtils.URL_USER_GET, email);
        return UserTestUtils.parseObjectFromMvcResult(this.responseGet(url), UserDTO.class);
    }

    @Test
    @Order(2)
    public void testGetByEmailError() throws Exception {
        //测试使用任意参数，不能获取到任何用户信息
        this.checkUserList();
        String url = UserTestUtils.URL_USER_GET + UUID.randomUUID();
        MvcResult mvcResult = this.responseGet(url);

        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        //返回值为空
        Assertions.assertNull(resultHolder.getData());
    }

    @Test
    @Order(3)
    public void testPageSuccess() throws Exception {
        this.checkUserList();
        BasePageRequest basePageRequest = UserTestUtils.getDefaultPageRequest();
        MvcResult mvcResult = this.responsePost(UserTestUtils.URL_USER_PAGE, basePageRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), basePageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(returnPager.getList())).size() <= basePageRequest.getPageSize());

        //测试根据创建时间倒叙排列
        basePageRequest = UserTestUtils.getDefaultPageRequest();
        basePageRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(UserTestUtils.URL_USER_PAGE, basePageRequest);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //第一个数据的createTime是最大的
        List<UserTableResponse> userInfoList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        long firstCreateTime = userInfoList.get(0).getCreateTime();
        for (UserTableResponse userInfo : userInfoList) {
            Assertions.assertFalse(userInfo.getCreateTime() > firstCreateTime);
        }
    }

    @Test
    @Order(3)
    public void testPageError() throws Exception {
        //当前页码不大于0
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setPageSize(5);
        this.requestPost(UserTestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //pageSize超过100
        basePageRequest = UserTestUtils.getDefaultPageRequest();
        basePageRequest.setPageSize(250);
        this.requestPost(UserTestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        this.requestPost(UserTestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(5);
        basePageRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(UserTestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
    }

    @Test
    @Order(4)
    public void testUserUpdateSuccess() throws Exception {
        this.checkUserList();
        UserCreateInfo user = new UserCreateInfo();
        BeanUtils.copyBean(user, USER_LIST.get(0));
        UserEditRequest userMaintainRequest;
        UserEditRequest response;
        UserDTO checkDTO;
        //更改名字
        user.setName("TEST-UPDATE");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改邮箱
        user.setEmail("songtianyang-test-email@12138.com");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改手机号
        user.setPhone("18511112222");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改用户组(这里只改成用户成员权限)
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user,
                defaultUserRoleList.stream().filter(item -> StringUtils.equals(item.getId(), "member")).toList()
        );
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        checkLog(response.getId(), OperationLogType.UPDATE);
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改用户组(把上面的情况添加别的权限)
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //用户信息复原
        user = new UserCreateInfo();
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
    }

    @Test
    @Order(5)
    public void testUserUpdateError() throws Exception {
        // 4xx 验证
        UserCreateInfo user = new UserCreateInfo();
        UserEditRequest userMaintainRequest;
        //更改名字
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setName("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //email为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //手机号为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, new ArrayList<>());
        userMaintainRequest.setUserRoleIdList(new ArrayList<>());
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);

        // 500验证
        //邮箱重复
        this.checkUserList();
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail(USER_LIST.get(USER_LIST.size() - 1).getEmail());
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //用户组不包含系统成员
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user,
                defaultUserRoleList.stream().filter(item -> !StringUtils.equals(item.getId(), "member")).toList()
        );
        this.requestPost(UserTestUtils.URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(6)
    public void testUserChangeEnableSuccess() throws Exception {
        this.checkUserList();
        //单独修改状态
        UserCreateInfo userInfo = USER_LIST.get(0);
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setUserIdList(new ArrayList<>() {{
            this.add(userInfo.getId());
        }});
        userChangeEnableRequest.setEnable(false);
        this.requestPost(UserTestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, status().isOk());
        for(String item : userChangeEnableRequest.getUserIdList()){
            checkLog(item, OperationLogType.UPDATE);
        }

        UserDTO userDTO = this.getUserByEmail(userInfo.getEmail());
        Assertions.assertEquals(userDTO.getEnable(), userChangeEnableRequest.isEnable());
    }

    @Test
    @Order(6)
    public void testUserChangeEnableError() throws Exception {
        this.checkUserList();
        //用户不存在
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setEnable(false);
        this.requestPost(UserTestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, BAD_REQUEST_MATCHER);
        //含有非法用户
        userChangeEnableRequest.setUserIdList(new ArrayList<>() {{
            this.add("BCDEDIT");
        }});
        this.requestPost(UserTestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, ERROR_REQUEST_MATCHER);
    }


    @Test
    @Order(7)
    public void testUserImportSuccess() throws Exception {
        this.checkUserList();
        //测试用户数据导入。  每个导入文件都有10条数据，不同文件出错的数据不同。
        int importSuccessData = 10;//应该导入成功的数据数量
        int[] errorDataIndex = {};//出错数据的行数
        UserImportResponse response;//导入返回值
        //导入正常文件
        String filePath = this.getClass().getClassLoader().getResource("file/user_import_success.xlsx").getPath();
        MockMultipartFile file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        List<UserDTO> userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList){
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //导入空文件. 应当导入成功的数据为0
        filePath = this.getClass().getClassLoader().getResource("file/user_import_success_empty.xlsx").getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //文件内没有一条合格数据  应当导入成功的数据为0
        filePath = this.getClass().getClassLoader().getResource("file/user_import_error_all.xlsx").getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //邮箱和数据库里的重复  应当导入成功的数据为8
        filePath = this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_db.xlsx").getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 8;
        errorDataIndex = new int[]{1, 7};
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList){
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //文件内邮箱重复  应当导入成功的数据为8
        filePath = this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_in_file.xlsx").getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{9, 10};
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList){
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //文件不符合规范 应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/abcde.gif")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //测试03版excel正常导入 应当导入成功的数据为10
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success_03.xls")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserTestUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = UserTestUtils.parseObjectFromMvcResult(this.responseFile(UserTestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 10;//应该导入成功的数据数量
        errorDataIndex = new int[]{};//出错数据的行数
        UserTestUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList){
            checkLog(item.getId(), OperationLogType.ADD);
        }
    }


    @Test
    @Order(8)
    public void testUserResetPasswordSuccess() throws Exception {
        this.checkUserList();
        String userId = USER_LIST.get(0).getId();
        String userEmail = USER_LIST.get(0).getEmail();
        //重置普通用户密码
        this.resetPasswordAndCheck(userId,userEmail);
        this.checkLog(userId, OperationLogType.UPDATE);
        //重置admin的密码
        this.resetPasswordAndCheck("admin","metersphere");
        this.checkLog(userId, OperationLogType.UPDATE);
    }

    private void resetPasswordAndCheck(String userId,String userEmail) throws  Exception{
        User user = new User();
        user.setId(userId);
        user.setPassword("I can't say any dirty words");
        Assertions.assertEquals(1, userMapper.updateByPrimaryKeySelective(user));

        //调用重置密码的接口
        this.responseByString(UserTestUtils.URL_USER_RESET_PASSWORD, userId,status().isOk());
        //检查数据库
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtil.md5(userEmail));
        Assertions.assertEquals(1, userMapper.countByExample(example));
        checkLog(userId, OperationLogType.UPDATE);
    }

    @Test
    @Order(8)
    public void testUserResetPasswordError() throws Exception {
        //用户不存在
        this.responseByString(UserTestUtils.URL_USER_RESET_PASSWORD, "none user",ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(9)
    public void testUserDeleteSuccess() throws Exception {
        this.checkUserList();
        //删除已存的所有用户
        UserBatchProcessRequest request = new UserBatchProcessRequest();
        request.setUserIdList(USER_LIST.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        UserBatchProcessResponse response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(UserTestUtils.URL_USER_DELETE, request), UserBatchProcessResponse.class);
        Assertions.assertEquals(request.getUserIdList().size(), response.getTotalCount());
        Assertions.assertEquals(request.getUserIdList().size(), response.getSuccessCount());
        //检查数据库
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(request.getUserIdList());
        List<User> userList = userMapper.selectByExample(example);
        for (User user : userList) {
            Assertions.assertTrue(user.getDeleted());
        }
        //记录已经删除了的用户，用于反例
        DELETED_USER_ID_LIST.clear();
        DELETED_USER_ID_LIST.addAll(request.getUserIdList());
    }

    //删除失败的方法要放在删除成功方法后面执行
    @Test
    @Order(10)
    public void testUserDeleteError() throws Exception {
        this.checkUserDeleted();
        //参数为空
        UserBatchProcessRequest request = new UserBatchProcessRequest();
        this.requestPost(UserTestUtils.URL_USER_DELETE, request, BAD_REQUEST_MATCHER);
        //用户不存在
        request.getUserIdList().add("123456789012345678901234");
        this.requestPost(UserTestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);
        //用户已经被删除
        request.setUserIdList(DELETED_USER_ID_LIST);
        this.requestPost(UserTestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);
    }


    public List<UserDTO> checkImportUserInDb(ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile) throws Exception {
        List<UserDTO> returnList = new ArrayList<>();
        for (UserExcelRowDTO item : userImportReportDTOByFile.getDataList()) {
            UserDTO userDTO = this.getUserByEmail(item.getEmail());
            Assertions.assertNotNull(userDTO);
            returnList.add(userDTO);
        }
        return returnList;
    }

}
