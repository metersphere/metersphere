package io.metersphere.system.controller.user;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.RsaUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.domain.UserInvite;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.dto.request.UserRegisterRequest;
import io.metersphere.system.dto.request.user.UserChangeEnableRequest;
import io.metersphere.system.dto.request.user.UserEditRequest;
import io.metersphere.system.dto.request.user.UserRoleBatchRelationRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.ExcelParseDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.table.TableBatchProcessResponse;
import io.metersphere.system.dto.user.UserCreateInfo;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.request.UserBatchCreateRequest;
import io.metersphere.system.dto.user.response.*;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserInviteMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.GlobalUserRoleRelationService;
import io.metersphere.system.service.UserInviteService;
import io.metersphere.system.service.UserService;
import io.metersphere.system.service.UserToolService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.user.UserParamUtils;
import io.metersphere.system.utils.user.UserRequestUtils;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests extends BaseTest {

    @Resource
    private UserService userService;
    @Resource
    private UserToolService userToolService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    //邀请记录
    private static final List<String> INVITE_RECORD_ID_LIST = new ArrayList<>();

    //失败请求返回编码
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    //测试过程中需要用到的数据
    private static final List<UserCreateInfo> USER_LIST = new ArrayList<>();
    private static final List<UserSelectOption> USER_ROLE_LIST = new ArrayList<>();
    private static final List<UserSelectOption> ORG_LIST = new ArrayList<>();
    private static final List<BaseTreeNode> PROJECT_LIST = new ArrayList<>();
    //默认数据
    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String USER_NONE_ROLE_EMAIL = "tianyang.none.role@163.com";
    //已删除的用户ID
    private static final List<String> DELETED_USER_ID_LIST = new ArrayList<>();
    @Resource
    private UserInviteMapper userInviteMapper;

    UserRequestUtils userRequestUtils = null;

    private static final List<CheckLogModel> LOG_CHECK_LIST = new ArrayList<>();

    @Override
    @BeforeEach
    public void login() throws Exception {
        if (userRequestUtils == null) {
            super.login();
            userRequestUtils = new UserRequestUtils(mockMvc, sessionId, csrfToken);
        }
    }

    @Test
    @Order(0)
    void testGetGlobalSystemUserRoleSuccess() throws Exception {
        MvcResult mvcResult = userRequestUtils.responseGet(UserRequestUtils.URL_GET_GLOBAL_SYSTEM);
        this.setDefaultUserRoleList(mvcResult);
    }

    @Test
    @Order(1)
    void testAddSuccess() throws Exception {
        if (CollectionUtils.isEmpty(USER_ROLE_LIST)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        //模拟前台批量添加用户
        UserBatchCreateRequest userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
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
        MvcResult mvcResult = userRequestUtils.responsePost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);


        //批量添加一百多个用户,只赋予其中1个权限。 这批用户用于后续的批量添加到用户组/组织/项目
        List<UserCreateInfo> userCreateInfoList = new ArrayList<>();
        for (int i = 0; i < 123; i++) {
            int finalI = i;
            userCreateInfoList.add(new UserCreateInfo() {{
                setName("tianyang.no.batch" + finalI);
                setEmail("tianyang.no.batch" + finalI + "@126.com");
            }});
        }
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                Collections.singletonList(USER_ROLE_LIST.getFirst()),
                userCreateInfoList
        );
        mvcResult = userRequestUtils.responsePost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);

        //含有重复的用户名称
        userMaintainRequest = UserParamUtils.getUserCreateDTO(

                USER_ROLE_LIST,
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
        mvcResult = userRequestUtils.responsePost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);
    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_user_controller_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetByEmailSuccess() throws Exception {
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
        String url = String.format(UserRequestUtils.URL_USER_GET, email);
        return userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseGet(url), UserDTO.class);
    }

    @Test
    @Order(2)
    void testGetByEmailError() throws Exception {
        //测试使用任意参数，不能获取到任何用户信息
        this.checkUserList();
        String url = UserRequestUtils.URL_USER_GET + IDGenerator.nextNum();
        MvcResult mvcResult = userRequestUtils.responseGet(url);

        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        //返回值为空
        Assertions.assertNull(resultHolder.getData());
    }

    @Test
    @Order(3)
    void testPageSuccess() throws Exception {
        if (CollectionUtils.isEmpty(USER_ROLE_LIST)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }

        List<String> userRoleIdList = USER_ROLE_LIST.stream().map(UserSelectOption::getId).toList();
        this.checkUserList();
        BasePageRequest basePageRequest = UserParamUtils.getDefaultPageRequest();

        Pager<?> returnPager = userRequestUtils.selectUserPage(basePageRequest);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), basePageRequest.getCurrent());
        Assertions.assertEquals(returnPager.getPageSize(), basePageRequest.getPageSize());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(returnPager.getList())).size() <= basePageRequest.getPageSize());
        List<UserTableResponse> userList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        //用户组不存在非全局用户组
        for (UserTableResponse response : userList) {
            if (CollectionUtils.isNotEmpty(response.getUserRoleList())) {
                response.getUserRoleList().forEach(role -> Assertions.assertTrue(userRoleIdList.contains(role.getId())));
            }
        }

        //页码为50
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setPageSize(50);
        returnPager = userRequestUtils.selectUserPage(basePageRequest);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), basePageRequest.getCurrent());
        Assertions.assertEquals(returnPager.getPageSize(), basePageRequest.getPageSize());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(returnPager.getList())).size() <= basePageRequest.getPageSize());
        //用户组不存在非全局用户组
        userList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        for (UserTableResponse response : userList) {
            if (CollectionUtils.isNotEmpty(response.getUserRoleList())) {
                response.getUserRoleList().forEach(role -> Assertions.assertTrue(userRoleIdList.contains(role.getId())));
            }
        }

        //测试根据创建时间倒叙排列
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        returnPager = userRequestUtils.selectUserPage(basePageRequest);
        //第一个数据的createTime是最大的
        List<UserTableResponse> userInfoList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        long firstCreateTime = userInfoList.getFirst().getCreateTime();
        for (UserTableResponse userInfo : userInfoList) {
            Assertions.assertFalse(userInfo.getCreateTime() > firstCreateTime);
        }
        //用户组不存在非全局用户组
        userList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        for (UserTableResponse response : userList) {
            if (CollectionUtils.isNotEmpty(response.getUserRoleList())) {
                response.getUserRoleList().forEach(role -> Assertions.assertTrue(userRoleIdList.contains(role.getId())));
            }
        }

        //查找不存在的用户
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setKeyword(IDGenerator.nextStr());
        returnPager = userRequestUtils.selectUserPage(basePageRequest);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getTotal(), 0);
        Assertions.assertEquals(returnPager.getPageSize(), basePageRequest.getPageSize());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertEquals(0, JSON.parseArray(JSON.toJSONString(returnPager.getList())).size());
    }

    @Test
    @Order(3)
    void testPageError() throws Exception {
        //当前页码不大于0
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setPageSize(5);
        this.requestPost(UserRequestUtils.URL_USER_PAGE, basePageRequest).andExpect(BAD_REQUEST_MATCHER);

        //pageSize超过501
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setPageSize(501);
        this.requestPost(UserRequestUtils.URL_USER_PAGE, basePageRequest).andExpect(BAD_REQUEST_MATCHER);

        //当前页数不大于5
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        this.requestPost(UserRequestUtils.URL_USER_PAGE, basePageRequest).andExpect(BAD_REQUEST_MATCHER);

        //排序字段不合法
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(5);
        basePageRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(UserRequestUtils.URL_USER_PAGE, basePageRequest).andExpect(BAD_REQUEST_MATCHER);

    }

    @Test
    @Order(4)
    void testUserUpdateSuccess() throws Exception {
        this.checkUserList();
        UserCreateInfo user = new UserCreateInfo();
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        UserEditRequest userMaintainRequest;
        UserEditRequest response;
        UserDTO checkDTO;
        //更改名字
        user.setName("TEST-UPDATE");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改邮箱
        user.setEmail("songtianyang-test-email@12138.com");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改手机号
        user.setPhone("18511112222");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改用户组(这里只改成用户成员权限)
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user,
                USER_ROLE_LIST.stream().filter(item -> StringUtils.equals(item.getId(), "member")).toList()
        );
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改用户组(把上面的情况添加别的权限)
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //用户信息复原
        user = new UserCreateInfo();
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        LOG_CHECK_LIST.add(
                new CheckLogModel(response.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE)
        );
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
    }

    @Test
    @Order(5)
    void testUserUpdateError() throws Exception {
        this.checkUserList();
        // 4xx 验证
        UserCreateInfo user = new UserCreateInfo();
        UserEditRequest userMaintainRequest;
        //更改名字
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        user.setName("");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);

        //email为空
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        user.setEmail("");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);

        //手机号为空
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        user.setEmail("");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);

        //用户组为空
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, new ArrayList<>());
        userMaintainRequest.setUserRoleIdList(new ArrayList<>());
        this.requestPost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);


        // 500验证
        //邮箱重复
        this.checkUserList();
        BeanUtils.copyBean(user, USER_LIST.getFirst());
        user.setEmail(USER_LIST.getLast().getEmail());
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        MvcResult mvcResult = this.requestPost(UserRequestUtils.URL_USER_UPDATE, userMaintainRequest).andExpect(ERROR_REQUEST_MATCHER).andReturn();
        ResultHolder resultHolder = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertEquals(resultHolder.getMessage(), "用户邮箱已存在");
    }

    @Test
    @Order(6)
    void testUserChangeEnableSuccess() throws Exception {
        this.checkUserList();

        UserCreateInfo userInfo = USER_LIST.getFirst();

        //先使用要操作的用户登录,用于检查会不会把账户踢出
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", userInfo.getEmail(), userInfo.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        //检查该用户状态登录中
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/is-login");
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        MvcResult loginResult = mockMvc.perform(requestBuilder).andReturn();
        ResultHolder checkLoginHolder = JSON.parseObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertNotNull(checkLoginHolder.getData());

        //修改状态关闭
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add(userInfo.getId());
        }});
        userChangeEnableRequest.setEnable(false);
        this.requestPostWithOk(UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest);
        for (String item : userChangeEnableRequest.getSelectIds()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE_ENABLE)
            );
        }
        UserDTO userDTO = this.getUserByEmail(userInfo.getEmail());
        Assertions.assertEquals(userDTO.getEnable(), userChangeEnableRequest.isEnable());

        //检查该用户被踢出
        requestBuilder = MockMvcRequestBuilders.get("/is-login");
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        loginResult = mockMvc.perform(requestBuilder).andReturn();
        checkLoginHolder = JSON.parseObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertNull(checkLoginHolder.getData());

        //修改状态开启
        userChangeEnableRequest.setEnable(true);
        this.requestPostWithOk(UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest);
        for (String item : userChangeEnableRequest.getSelectIds()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.UPDATE, UserRequestUtils.URL_USER_UPDATE_ENABLE)
            );
        }

        userDTO = this.getUserByEmail(userInfo.getEmail());
        Assertions.assertEquals(userDTO.getEnable(), userChangeEnableRequest.isEnable());
    }

    @Test
    @Order(6)
    void testUserChangeEnableError() throws Exception {
        this.checkUserList();
        //用户不存在
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setEnable(false);
        this.requestPost(UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest).andExpect(ERROR_REQUEST_MATCHER);

        //含有非法用户
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add("BCDEDIT");
        }});
        this.requestPost(UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest).andExpect(ERROR_REQUEST_MATCHER);
        //含有当前用户
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add("admin");
        }});
        this.requestPost(UserRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest).andExpect(ERROR_REQUEST_MATCHER);
    }


    @Test
    @Order(7)
    void testUserImport() throws Exception {
        this.checkUserList();
        //测试用户数据导入。  每个导入文件都有10条数据，不同文件出错的数据不同。
        int importSuccessData = 10;//应该导入成功的数据数量
        int[] errorDataIndex = {};//出错数据的行数
        UserImportResponse response;//导入返回值
        //导入正常文件
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success.xlsx")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        List<UserDTO> userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, UserRequestUtils.URL_USER_IMPORT)
            );
        }

        //导入空文件. 应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success_empty.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);


        //导入中文文件
        importSuccessData = 19;
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_cn_success.xls")).getPath();
        file = new MockMultipartFile("file", "userImportCn.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, UserRequestUtils.URL_USER_IMPORT)
            );
        }


        //文件内没有一条合格数据  应当导入成功的数据为0
        importSuccessData = 0;
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_all.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //邮箱和数据库里的重复  应当导入成功的数据为8
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_db.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 8;
        errorDataIndex = new int[]{1, 7};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, UserRequestUtils.URL_USER_IMPORT)
            );
        }

        //文件内邮箱重复  应当导入成功的数据为8
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_in_file.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{9, 10};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, UserRequestUtils.URL_USER_IMPORT)
            );
        }

        //文件不符合规范 应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/abcde.gif")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //测试03版excel正常导入 应当导入成功的数据为10
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success_03.xls")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(UserRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 10;//应该导入成功的数据数量
        errorDataIndex = new int[]{};//出错数据的行数
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, UserRequestUtils.URL_USER_IMPORT)
            );
        }
    }

    @Test
    @Order(8)
    void testUserResetPasswordError() throws Exception {
        //用户不存在
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList("none user"));
            this.requestPostAndReturn(UserRequestUtils.URL_USER_RESET_PASSWORD, request, ERROR_REQUEST_MATCHER);
        }
    }

    @Test
    @Order(1)
    void testAddError() throws Exception {
        if (CollectionUtils.isEmpty(USER_ROLE_LIST)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        UserBatchCreateRequest userMaintainRequest;
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
        userMaintainRequest = UserParamUtils.getUserCreateDTO(null, null);
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //用户组ID为空
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                null,
                errorUserList);
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //没有用户
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                null);
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //用户组含有null
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                errorUserList);
        userMaintainRequest.getUserRoleIdList().add(null);
        userMaintainRequest.getUserRoleIdList().add("");
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //含有用户名称为空的数据
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setEmail("tianyang.name.empty@126.com");
        }});
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //含有用户邮箱为空的数据
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.email.empty");
        }});
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        //用户邮箱不符合标准
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("用户邮箱放飞自我");
            setEmail("用户邮箱放飞自我");
        }});
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(BAD_REQUEST_MATCHER);
        /*
         * 校验业务判断出错的反例 （500 error)
         * 需要保证数据库有正常数据
         */
        this.checkUserList();
        //含有非法用户组
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                new ArrayList<>() {{
                    this.add(new UserSelectOption() {{
                        this.setId("not system global user role id");
                    }});
                }},
                errorUserList);
        this.requestPost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest).andExpect(ERROR_REQUEST_MATCHER);
        //含有重复的用户邮箱
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                errorUserList
        );
        String firstUserEmail = userMaintainRequest.getUserInfoList().getFirst().getEmail();
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.no.error4");
            setEmail(firstUserEmail);
        }});
        MvcResult errorEmailResult = this.requestPostWithOkAndReturn(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);
        ResultHolder errorEmailResultHolder = JSON.parseObject(errorEmailResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        UserBatchCreateResponse errorEmailResponse = JSON.parseObject(JSON.toJSONString(errorEmailResultHolder.getData()), UserBatchCreateResponse.class);
        Assertions.assertTrue(MapUtils.isNotEmpty(errorEmailResponse.getErrorEmails()));
        //测试请求参数中含有数据库中已存在的邮箱情况
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
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
        errorEmailResult = this.requestPostWithOkAndReturn(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);
        errorEmailResultHolder = JSON.parseObject(errorEmailResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        errorEmailResponse = JSON.parseObject(JSON.toJSONString(errorEmailResultHolder.getData()), UserBatchCreateResponse.class);
        Assertions.assertTrue(MapUtils.isNotEmpty(errorEmailResponse.getErrorEmails()));
    }

    @Test
    @Order(8)
    void testUserResetPasswordSuccess() throws Exception {
        this.checkUserList();
        //重置admin的密码
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList("admin"));
            this.requestPostAndReturn(UserRequestUtils.URL_USER_RESET_PASSWORD, request);
            //检查数据库
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdEqualTo("admin").andPasswordEqualTo(CodingUtils.md5("metersphere"));
            Assertions.assertEquals(1, userMapper.countByExample(userExample));
            LOG_CHECK_LIST.add(
                    new CheckLogModel("admin", OperationLogType.UPDATE, UserRequestUtils.URL_USER_RESET_PASSWORD)
            );
        }
        //重置普通用户密码
        {
            User paramUser = new User();
            String userId = USER_LIST.getFirst().getId();
            paramUser.setId(userId);
            paramUser.setPassword("I can't say any dirty words");
            Assertions.assertEquals(1, userMapper.updateByPrimaryKeySelective(paramUser));
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList(userId));
            TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                    this.requestPostAndReturn(UserRequestUtils.URL_USER_RESET_PASSWORD, request),
                    TableBatchProcessResponse.class
            );
            Assertions.assertEquals(response.getTotalCount(), response.getSuccessCount(), 1);
            List<User> userList = userToolService.selectByIdList(Collections.singletonList(userId));
            for (User checkUser : userList) {
                UserExample userExample = new UserExample();
                userExample.createCriteria().andIdEqualTo(checkUser.getId()).andPasswordEqualTo(CodingUtils.md5(checkUser.getEmail()));
                Assertions.assertEquals(1, userMapper.countByExample(userExample));
                LOG_CHECK_LIST.add(
                        new CheckLogModel(checkUser.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_RESET_PASSWORD)
                );
            }
        }
        //重置非Admin用户的密码
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setExcludeIds(Collections.singletonList("admin"));
            request.setSelectAll(true);
            TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                    this.requestPostAndReturn(UserRequestUtils.URL_USER_RESET_PASSWORD, request),
                    TableBatchProcessResponse.class
            );
            UserExample example = new UserExample();
            example.createCriteria().andIdNotEqualTo("admin");
            long count = userMapper.countByExample(example);
            Assertions.assertEquals(response.getTotalCount(), response.getSuccessCount(), count);

            example.clear();
            example.createCriteria().andIdNotEqualTo("admin");
            List<User> userList = userMapper.selectByExample(example);
            for (User checkUser : userList) {
                UserExample userExample = new UserExample();
                userExample.createCriteria().andIdEqualTo(checkUser.getId()).andPasswordEqualTo(CodingUtils.md5(checkUser.getEmail()));
                Assertions.assertEquals(1, userMapper.countByExample(userExample));
                LOG_CHECK_LIST.add(
                        new CheckLogModel(checkUser.getId(), OperationLogType.UPDATE, UserRequestUtils.URL_USER_RESET_PASSWORD)
                );
            }
        }
    }

    @Test
    @Order(9)
    void testUserRoleRelationSuccess() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (USER_LIST.size() < 50) {
            this.testAddSuccess();
        }
        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        //测试添加角色权限。 预期数据：每个用户都会增加对应的权限
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).toList());
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, null);
        //检查有权限的数据量是否一致
        Assertions.assertEquals(
                globalUserRoleRelationService.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds()).size(),
                request.getSelectIds().size() * request.getRoleIds().size()
        );
        //检查日志
        for (String userID : request.getSelectIds()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(userID, OperationLogType.UPDATE, UserRequestUtils.URL_USER_ROLE_RELATION)
            );
        }

        //测试重复添加用户权限。预期结果：不会额外增加数据
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, null);
        //检查有权限的数据量是否一致
        Assertions.assertEquals(
                globalUserRoleRelationService.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds()).size(),
                request.getSelectIds().size() * request.getRoleIds().size()
        );
    }

    @Test
    @Order(9)
    void testUserRoleRelationError() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (USER_LIST.size() < 50) {
            this.testAddSuccess();
        }
        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        // 用户ID为空
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(new ArrayList<>());
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 角色id为空
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).toList());
        request.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 用户ID含有不存在的
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.getSelectIds().add("none user");
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 角色ID含有不存在的
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).toList());
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        request.getRoleIds().add("none role");
        userRequestUtils.requestPost(UserRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(9)
    void testGetEmptyProject() throws Exception {
        //测试如果没有项目系统不会报错
        List<Project> allProjectList = projectMapper.selectByExample(null);
        projectMapper.deleteByExample(null);
        String str = userRequestUtils.responseGet(UserRequestUtils.URL_GET_PROJECT).getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder rh = JSON.parseObject(str, ResultHolder.class);
        List<BaseTreeNode> userTreeSelectOptions = JSON.parseArray(
                JSON.toJSONString(rh.getData()),
                BaseTreeNode.class);
        Assertions.assertTrue(CollectionUtils.isEmpty(userTreeSelectOptions));
        //还原数据
        for (Project project : allProjectList) {
            projectMapper.insert(project);
        }

        //开始正常获取数据。 有可能在整体运行单元测试的过程中，会被默认插入了项目测试数据。所以这里根据上面数据库查到的来做判断
        str = userRequestUtils.responseGet(UserRequestUtils.URL_GET_PROJECT).getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(str, ResultHolder.class);
        userTreeSelectOptions = JSON.parseArray(
                JSON.toJSONString(resultHolder.getData()),
                BaseTreeNode.class);
        Assertions.assertEquals(CollectionUtils.isEmpty(userTreeSelectOptions), CollectionUtils.isEmpty(allProjectList));
    }

    @Test
    @Order(10)
    @Sql(scripts = {"/dml/init_user_org_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetProjectAndOrganization() throws Exception {
        String str = userRequestUtils.responseGet(UserRequestUtils.URL_GET_PROJECT).getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder rh = JSON.parseObject(str, ResultHolder.class);
        List<BaseTreeNode> userTreeSelectOptions = JSON.parseArray(
                JSON.toJSONString(rh.getData()),
                BaseTreeNode.class);
        //返回值不为空
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userTreeSelectOptions));

        userTreeSelectOptions.forEach(item -> PROJECT_LIST.addAll(item.getChildren()));

        List<UserSelectOption> userSelectOptions = JSON.parseArray(
                JSON.toJSONString(
                        JSON.parseObject(
                                userRequestUtils.responseGet(UserRequestUtils.URL_GET_ORGANIZATION).getResponse().getContentAsString(StandardCharsets.UTF_8),
                                ResultHolder.class).getData()),
                UserSelectOption.class);
        ORG_LIST.addAll(userSelectOptions);
    }

    @Test
    @Order(11)
    void testAddProjectMember() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (CollectionUtils.isEmpty(USER_LIST)) {
            this.testAddSuccess();
        }
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }

        List<String> userIds = this.selectUserTableIds(100);

        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(userIds);
        request.setRoleIds(PROJECT_LIST.stream().map(BaseTreeNode::getId).toList());
        this.requestPostWithOk(UserRequestUtils.URL_ADD_PROJECT_MEMBER, request);
        //检查有权限的数据量是否一致
        UserRoleRelationExample checkExample = new UserRoleRelationExample();

        for (String projectId : request.getRoleIds()) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            String orgId = project.getOrganizationId();
            for (String userId : request.getSelectIds()) {
                checkExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(projectId);
                //检查是否在对应的项目下
                Assertions.assertEquals(
                        userRoleRelationMapper.countByExample(checkExample), 1
                );
                checkExample.clear();
                //检查是否在对应的组织下
                checkExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(orgId);
                Assertions.assertEquals(
                        userRoleRelationMapper.countByExample(checkExample), 1
                );
            }
        }
        //检查日志
        for (String userID : request.getSelectIds()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(userID, OperationLogType.UPDATE, UserRequestUtils.URL_ADD_PROJECT_MEMBER)
            );
        }
        //获取用户信息
        for (String userID : request.getSelectIds()) {
            userService.getPersonalById(userID);
        }

        //检查用户表格不会加载出来非全局用户组
        this.testPageSuccess();
    }

    @Test
    @Order(13)
    void testAddOrganization() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (CollectionUtils.isEmpty(USER_LIST)) {
            this.testAddSuccess();
        }
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }

        List<String> userIds = this.selectUserTableIds(50);

        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(userIds);
        request.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).toList());
        this.requestPostWithOk(UserRequestUtils.URL_ADD_ORGANIZATION_MEMBER, request);
        //检查有权限的数据量是否一致
        UserRoleRelationExample checkExample = new UserRoleRelationExample();
        for (String orgId : request.getRoleIds()) {
            for (String userId : request.getSelectIds()) {
                checkExample.clear();
                //检查是否在对应的组织下
                checkExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(orgId);
                Assertions.assertEquals(
                        userRoleRelationMapper.countByExample(checkExample), 1
                );
            }
        }
        //检查日志
        for (String userID : request.getSelectIds()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(userID, OperationLogType.UPDATE, UserRequestUtils.URL_ADD_ORGANIZATION_MEMBER)
            );
        }

        //获取用户信息
        for (String userID : request.getSelectIds()) {
            userService.getPersonalById(userID);
        }
        //检查用户表格加载组织
        this.testPageSuccess();
    }

    @Test
    @Order(11)
    void testAddToOrgOrProjectError() throws Exception {
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }
        // 用户ID为空
        UserRoleBatchRelationRequest addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(new ArrayList<>());
        addToProjectRequest.setRoleIds(PROJECT_LIST.stream().map(BaseTreeNode::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);
        // 项目为空
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).toList());
        addToProjectRequest.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, BAD_REQUEST_MATCHER);
        // 用户ID含有不存在的
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(Collections.singletonList("none user"));
        addToProjectRequest.setRoleIds(PROJECT_LIST.stream().map(BaseTreeNode::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);
        // 项目ID含有不存在的
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).toList());
        addToProjectRequest.setRoleIds(Collections.singletonList("none role"));
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);


        // 用户ID为空
        UserRoleBatchRelationRequest orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(new ArrayList<>());
        orgRequest.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
        // 项目为空
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).toList());
        orgRequest.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, BAD_REQUEST_MATCHER);
        // 用户ID含有不存在的
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(Collections.singletonList("none user"));
        orgRequest.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).toList());
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
        // 项目ID含有不存在的
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).toList());
        orgRequest.setRoleIds(Collections.singletonList("none role"));
        userRequestUtils.requestPost(UserRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(12)
    void testUserInvite() throws Exception {
        if (CollectionUtils.isEmpty(USER_LIST)) {
            this.testAddSuccess();
        }
        this.testUserInviteSuccess();
        this.testUserInviteError();
    }

    @Test
    @Order(13)
    void testUserRegister() throws Exception {
        if (CollectionUtils.isEmpty(INVITE_RECORD_ID_LIST)) {
            this.testUserInvite();
        }
        this.testUserRegisterSuccess();
        this.testUserRegisterError();
    }

    @Test
    @Order(14)
    void testGetKey() throws Exception {
        this.requestGetWithOk("/get-key");
    }

    //本测试类中会用到很多次用户数据。所以测试删除的方法放于最后
    @Test
    @Order(99)
    void testUserDeleteSuccess() throws Exception {
        this.checkUserList();

        //先使用要操作的用户登录,用于检查会不会把账户踢出
        UserCreateInfo userInfo = USER_LIST.getFirst();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", userInfo.getEmail(), userInfo.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        //检查该用户状态登录中
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/is-login");
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        MvcResult loginResult = mockMvc.perform(requestBuilder).andReturn();
        ResultHolder checkLoginHolder = JSON.parseObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertNotNull(checkLoginHolder.getData());

        //删除USER_LIST用户
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).toList());
        TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                userRequestUtils.responsePost(UserRequestUtils.URL_USER_DELETE, request), TableBatchProcessResponse.class);
        Assertions.assertEquals(request.getSelectIds().size(), response.getTotalCount());
        Assertions.assertEquals(request.getSelectIds().size(), response.getSuccessCount());

        //检查该用户被踢出
        requestBuilder = MockMvcRequestBuilders.get("/is-login");
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        loginResult = mockMvc.perform(requestBuilder).andReturn();
        checkLoginHolder = JSON.parseObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertNull(checkLoginHolder.getData());
        
        //检查数据库
        List<UserCreateInfo> removeList = new ArrayList<>();
        for (UserCreateInfo deleteUser : USER_LIST) {
            User user = userMapper.selectByPrimaryKey(deleteUser.getId());
            Assertions.assertTrue(user.getDeleted());
            //检查日志
            LOG_CHECK_LIST.add(
                    new CheckLogModel(deleteUser.getId(), OperationLogType.DELETE, UserRequestUtils.URL_USER_DELETE)
            );
            removeList.add(deleteUser);
        }
        USER_LIST.removeAll(removeList);
    }

    //删除失败的方法要放在删除成功方法后面执行
    @Test
    @Order(100)
    void testUserDeleteError() throws Exception {
        //参数为空
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        this.requestPost(UserRequestUtils.URL_USER_DELETE, request).andExpect(ERROR_REQUEST_MATCHER);
        //用户不存在
        request.setSelectIds(Collections.singletonList("none user"));
        this.requestPost(UserRequestUtils.URL_USER_DELETE, request).andExpect(ERROR_REQUEST_MATCHER);
        //测试用户已经被删除的
        if (CollectionUtils.isEmpty(DELETED_USER_ID_LIST)) {
            this.testUserDeleteSuccess();
        }
        request.setSelectIds(DELETED_USER_ID_LIST);
        this.requestPost(UserRequestUtils.URL_USER_DELETE, request).andExpect(ERROR_REQUEST_MATCHER);

        //测试包含Admin用户
        request = new TableBatchProcessDTO();
        request.setSelectAll(true);
        this.requestPost(UserRequestUtils.URL_USER_DELETE, request).andExpect(ERROR_REQUEST_MATCHER);
    }


    @Test
    @Order(101)
    void testLog() throws Exception {
        Thread.sleep(5000);
        for (CheckLogModel checkLogModel : LOG_CHECK_LIST) {
            if (StringUtils.isEmpty(checkLogModel.getUrl())) {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType());
            } else {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType(), checkLogModel.getUrl());
            }
        }
    }

    //记录查询到的组织信息
    private void setDefaultUserRoleList(MvcResult mvcResult) throws Exception {
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<UserSelectOption> userRoleList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserSelectOption.class);
        //返回值不为空
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userRoleList));
        USER_ROLE_LIST.addAll(userRoleList);
    }

    //查找表格用户信息的ID
    private List<String> selectUserTableIds(int pageSize) throws Exception {
        BasePageRequest basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setPageSize(pageSize);
        Pager<?> returnPager = userRequestUtils.selectUserPage(basePageRequest);
        //用户组不存在非全局用户组
        List<UserTableResponse> userList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserTableResponse.class);
        return userList.stream().map(User::getId).toList();
    }

    //成功入库的用户保存内存中，其他用例会使用到
    private void addUser2List(MvcResult mvcResult) {
        UserBatchCreateResponse userMaintainRequest = userRequestUtils.parseObjectFromMvcResult(mvcResult, UserBatchCreateResponse.class);
        for (UserCreateInfo item : userMaintainRequest.getSuccessList()) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item.getId(), OperationLogType.ADD, null)
            );
        }
        //返回值不为空
        Assertions.assertNotNull(userMaintainRequest);
        USER_LIST.addAll(userMaintainRequest.getSuccessList());
    }

    private void checkUserList() throws Exception {
        if (CollectionUtils.isEmpty(USER_LIST)) {
            //测试数据初始化入库
            this.testAddSuccess();
        }
    }

    private List<UserDTO> checkImportUserInDb(ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile) throws Exception {
        List<UserDTO> returnList = new ArrayList<>();
        for (UserExcelRowDTO item : userImportReportDTOByFile.getDataList()) {
            UserDTO userDTO = this.getUserByEmail(item.getEmail());
            Assertions.assertNotNull(userDTO);
            returnList.add(userDTO);
        }
        return returnList;
    }

    void testUserInviteSuccess() throws Exception {
        UserInviteRequest userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                new ArrayList<>() {{
                    add("tianyang.song.invite.1@test.email");
                    add("tianyang.song.invite.2@test.email");
                }}
        );
        //这里无法测试邮箱是否发出，所以不针对结果进行校验
        this.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest);

        List<UserInvite> inviteList = userInviteService.batchInsert(userInviteRequest.getInviteEmails(), "admin", userInviteRequest.getUserRoleIds());
        UserInviteResponse response = new UserInviteResponse(inviteList);
        INVITE_RECORD_ID_LIST.addAll(response.getInviteIds());
    }

    @Resource
    private UserInviteService userInviteService;

    void testUserInviteError() throws Exception {
        List<String> inviteEmailList = new ArrayList<>() {{
            add("tianyang.song.invite.error.1@test.email");
            add("tianyang.song.invite.error.2@test.email");
        }};
        //400-用户角色为空
        UserInviteRequest userInviteRequest = UserParamUtils.getUserInviteRequest(
                new ArrayList<>(),
                inviteEmailList
        );
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, BAD_REQUEST_MATCHER);
        userInviteRequest.setUserRoleIds(null);
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, BAD_REQUEST_MATCHER);

        //400-邀请用户为空
        userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, BAD_REQUEST_MATCHER);
        userInviteRequest.setInviteEmails(null);
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, BAD_REQUEST_MATCHER);

        //400-邀请邮箱又非正确的格式的
        userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                new ArrayList<>() {{
                    this.addAll(inviteEmailList);
                    this.add("tianyang.song.invite.error.3");
                }}
        );
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, BAD_REQUEST_MATCHER);

        //500-包含无效权限
        userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                inviteEmailList
        );
        userInviteRequest.getUserRoleIds().add("none role");
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, ERROR_REQUEST_MATCHER);

        //500-用户邮箱数据内重复
        userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                inviteEmailList
        );
        userInviteRequest.getInviteEmails().addAll(inviteEmailList);
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, ERROR_REQUEST_MATCHER);

        //500-用户邮箱在数据库中已存在
        userInviteRequest = UserParamUtils.getUserInviteRequest(
                USER_ROLE_LIST,
                inviteEmailList
        );
        userInviteRequest.getInviteEmails().add(USER_LIST.getFirst().getEmail());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE, userInviteRequest, ERROR_REQUEST_MATCHER);
    }

    private void testUserRegisterSuccess() throws Exception {
        String inviteId = INVITE_RECORD_ID_LIST.getFirst();
        UserRegisterRequest request = new UserRegisterRequest();
        request.setInviteId(inviteId);
        request.setPassword(RsaUtils.publicEncrypt("Cao..12138", RsaUtils.getRsaKey().getPublicKey()));

        //先测试反例：名称超过255
        StringBuilder overSizeName = new StringBuilder();
        while (overSizeName.length() < 256) {
            overSizeName.append("i");
        }
        request.setName(overSizeName.toString());
        this.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request).andExpect(BAD_REQUEST_MATCHER);

        //测试正常创建
        request.setName("建国通过邮箱邀请");
        MvcResult mvcResult = userRequestUtils.responsePost(UserRequestUtils.URL_INVITE_REGISTER, request);
        String resultHolderStr = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(resultHolderStr, ResultHolder.class);

        //检查日志  此处日志的资源是邀请的用户，即admin
        LOG_CHECK_LIST.add(
                new CheckLogModel(resultHolder.getData().toString(), OperationLogType.ADD, UserRequestUtils.URL_INVITE_REGISTER)
        );


    }

    private void testUserRegisterError() throws Exception {
        if (INVITE_RECORD_ID_LIST.isEmpty()) {
            this.testUserInviteSuccess();
        }
        String inviteId = INVITE_RECORD_ID_LIST.get(1);
        //400-用户名为空
        UserRegisterRequest request = new UserRegisterRequest();
        request.setInviteId(inviteId);
        request.setPassword(IDGenerator.nextStr());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);
        request.setName("");
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);
        //400-用户密码为空
        request = new UserRegisterRequest();
        request.setInviteId(inviteId);
        request.setName("建国通过邮箱邀请2");
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);
        request.setPassword("");
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);

        //400-邀请ID为空
        request = new UserRegisterRequest();
        request.setName("建国通过邮箱邀请2");
        request.setPassword(IDGenerator.nextStr());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);
        request.setInviteId("");
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, BAD_REQUEST_MATCHER);

        //500-邀请ID不存在
        request = new UserRegisterRequest();
        request.setInviteId(IDGenerator.nextStr());
        request.setName("建国通过邮箱邀请2");
        request.setPassword(IDGenerator.nextStr());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, ERROR_REQUEST_MATCHER);

        //500-邀请ID已过期，且暂未删除
        UserInvite invite = userInviteMapper.selectByPrimaryKey(inviteId);
        invite.setInviteTime(invite.getInviteTime() - 1000 * 60 * 60 * 24);
        userInviteMapper.updateByPrimaryKeySelective(invite);

        request = new UserRegisterRequest();
        request.setInviteId(inviteId);
        request.setName("建国通过邮箱邀请2");
        request.setPassword(IDGenerator.nextStr());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, ERROR_REQUEST_MATCHER);

        //500-用户邮箱在用户注册之前已经被注册过了
        //首先还原邀请时间
        invite = userInviteMapper.selectByPrimaryKey(inviteId);
        invite.setInviteTime(System.currentTimeMillis());
        userInviteMapper.updateByPrimaryKeySelective(invite);

        String insertEmail = invite.getEmail();
        UserBatchCreateRequest userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>() {{
                    add(new UserCreateInfo() {{
                        setName(insertEmail);
                        setEmail(insertEmail);
                    }});
                }}
        );
        userRequestUtils.responsePost(UserRequestUtils.URL_USER_CREATE, userMaintainRequest);

        //测试
        request = new UserRegisterRequest();
        request.setInviteId(inviteId);
        request.setName("建国通过邮箱邀请2");
        request.setPassword(IDGenerator.nextStr());
        userRequestUtils.requestPost(UserRequestUtils.URL_INVITE_REGISTER, request, ERROR_REQUEST_MATCHER);
    }
}

@Data
@AllArgsConstructor
class CheckLogModel {
    private String resourceId;
    private OperationLogType operationType;
    private String url;
}
