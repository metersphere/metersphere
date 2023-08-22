package io.metersphere.system.controller.user;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.*;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.user.UserChangeEnableRequest;
import io.metersphere.system.request.user.UserEditRequest;
import io.metersphere.system.request.user.UserRoleBatchRelationRequest;
import io.metersphere.system.response.user.UserImportResponse;
import io.metersphere.system.response.user.UserSelectOption;
import io.metersphere.system.response.user.UserTableResponse;
import io.metersphere.system.response.user.UserTreeSelectOption;
import io.metersphere.system.service.GlobalUserRoleRelationService;
import io.metersphere.system.service.UserService;
import io.metersphere.system.utils.user.UserParamUtils;
import io.metersphere.system.utils.user.UserRequestUtils;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests extends BaseTest {


    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GlobalUserRoleRelationService globalUserRoleRelationService;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    //失败请求返回编码
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    //测试过程中需要用到的数据
    private static final List<UserCreateInfo> USER_LIST = new ArrayList<>();
    private static final List<UserSelectOption> USER_ROLE_LIST = new ArrayList<>();
    private static final List<UserSelectOption> ORG_LIST = new ArrayList<>();
    private static final List<UserTreeSelectOption> PROJECT_LIST = new ArrayList<>();
    //默认数据
    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String USER_NONE_ROLE_EMAIL = "tianyang.none.role@163.com";
    //已删除的用户ID
    private static final List<String> DELETED_USER_ID_LIST = new ArrayList<>();

    UserRequestUtils userRequestUtils = null;

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
    public void testGetGlobalSystemUserRoleSuccess() throws Exception {
        MvcResult mvcResult = userRequestUtils.responseGet(userRequestUtils.URL_GET_GLOBAL_SYSTEM);
        this.setDefaultUserRoleList(mvcResult);
    }

    @Test
    @Order(1)
    public void testAddSuccess() throws Exception {
        if (CollectionUtils.isEmpty(USER_ROLE_LIST)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        //模拟前台批量添加用户
        UserBatchCreateDTO userMaintainRequest = UserParamUtils.getUserCreateDTO(
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
        MvcResult mvcResult = userRequestUtils.responsePost(userRequestUtils.URL_USER_CREATE, userMaintainRequest);
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
                Collections.singletonList(USER_ROLE_LIST.get(0)),
                userCreateInfoList
        );
        mvcResult = userRequestUtils.responsePost(userRequestUtils.URL_USER_CREATE, userMaintainRequest);
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
        mvcResult = userRequestUtils.responsePost(userRequestUtils.URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);
    }

    public final String URL_GET_ORGANIZATION = "/system/user/get/organization";

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
        String url = String.format(userRequestUtils.URL_USER_GET, email);
        return userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseGet(url), UserDTO.class);
    }

    @Test
    @Order(2)
    public void testGetByEmailError() throws Exception {
        //测试使用任意参数，不能获取到任何用户信息
        this.checkUserList();
        String url = userRequestUtils.URL_USER_GET + UUID.randomUUID();
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
    public void testPageSuccess() throws Exception {
        this.checkUserList();
        BasePageRequest basePageRequest = UserParamUtils.getDefaultPageRequest();
        MvcResult mvcResult = userRequestUtils.responsePost(userRequestUtils.URL_USER_PAGE, basePageRequest);
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
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = userRequestUtils.responsePost(userRequestUtils.URL_USER_PAGE, basePageRequest);
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
        this.requestPost(userRequestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //pageSize超过100
        basePageRequest = UserParamUtils.getDefaultPageRequest();
        basePageRequest.setPageSize(250);
        this.requestPost(userRequestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        this.requestPost(userRequestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(5);
        basePageRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(userRequestUtils.URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
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
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改邮箱
        user.setEmail("songtianyang-test-email@12138.com");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改手机号
        user.setPhone("18511112222");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改用户组(这里只改成用户成员权限)
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user,
                USER_ROLE_LIST.stream().filter(item -> StringUtils.equals(item.getId(), "member")).toList()
        );
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        checkLog(response.getId(), OperationLogType.UPDATE);
        UserParamUtils.compareUserDTO(response, checkDTO);
        //更改用户组(把上面的情况添加别的权限)
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
        //用户信息复原
        user = new UserCreateInfo();
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responsePost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkLog(response.getId(), OperationLogType.UPDATE);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserParamUtils.compareUserDTO(response, checkDTO);
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
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //email为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail("");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //手机号为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail("");
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组为空
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, new ArrayList<>());
        userMaintainRequest.setUserRoleIdList(new ArrayList<>());
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);

        // 500验证
        //邮箱重复
        this.checkUserList();
        BeanUtils.copyBean(user, USER_LIST.get(0));
        user.setEmail(USER_LIST.get(USER_LIST.size() - 1).getEmail());
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user, USER_ROLE_LIST);
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //用户组不包含系统成员
        BeanUtils.copyBean(user, USER_LIST.get(0));
        userMaintainRequest = UserParamUtils.getUserUpdateDTO(user,
                USER_ROLE_LIST.stream().filter(item -> !StringUtils.equals(item.getId(), "member")).toList()
        );
        this.requestPost(userRequestUtils.URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(6)
    public void testUserChangeEnableSuccess() throws Exception {
        this.checkUserList();
        //单独修改状态
        UserCreateInfo userInfo = USER_LIST.get(0);
        UserChangeEnableRequest userChangeEnableRequest = new UserChangeEnableRequest();
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add(userInfo.getId());
        }});
        userChangeEnableRequest.setEnable(false);
        this.requestPost(userRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, status().isOk());
        for (String item : userChangeEnableRequest.getSelectIds()) {
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
        this.requestPost(userRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, BAD_REQUEST_MATCHER);
        //含有非法用户
        userChangeEnableRequest.setSelectIds(new ArrayList<>() {{
            this.add("BCDEDIT");
        }});
        this.requestPost(userRequestUtils.URL_USER_UPDATE_ENABLE, userChangeEnableRequest, ERROR_REQUEST_MATCHER);
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
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success.xlsx")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        List<UserDTO> userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //导入空文件. 应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success_empty.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //文件内没有一条合格数据  应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_all.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //邮箱和数据库里的重复  应当导入成功的数据为8
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_db.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 8;
        errorDataIndex = new int[]{1, 7};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //文件内邮箱重复  应当导入成功的数据为8
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_error_email_repeat_in_file.xlsx")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        errorDataIndex = new int[]{9, 10};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            checkLog(item.getId(), OperationLogType.ADD);
        }

        //文件不符合规范 应当导入成功的数据为0
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/abcde.gif")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 0;
        errorDataIndex = new int[]{};
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);

        //测试03版excel正常导入 应当导入成功的数据为10
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/user_import_success_03.xls")).getPath();
        file = new MockMultipartFile("file", "userImport.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, UserParamUtils.getFileBytes(filePath));
        userImportReportDTOByFile = userService.getUserExcelParseDTO(file);
        response = userRequestUtils.parseObjectFromMvcResult(userRequestUtils.responseFile(userRequestUtils.URL_USER_IMPORT, file), UserImportResponse.class);
        importSuccessData = 10;//应该导入成功的数据数量
        errorDataIndex = new int[]{};//出错数据的行数
        UserParamUtils.checkImportResponse(response, importSuccessData, errorDataIndex);//检查返回值
        userDTOList = this.checkImportUserInDb(userImportReportDTOByFile);//检查数据已入库
        for (UserDTO item : userDTOList) {
            checkLog(item.getId(), OperationLogType.ADD);
        }
    }

    @Test
    @Order(8)
    public void testUserResetPasswordError() throws Exception {
        //用户不存在
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList("none user"));
            this.requestPostAndReturn(userRequestUtils.URL_USER_RESET_PASSWORD, request, ERROR_REQUEST_MATCHER);
        }
    }

    @Test
    @Order(1)
    public void testAddError() throws Exception {
        if (CollectionUtils.isEmpty(USER_ROLE_LIST)) {
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
        userMaintainRequest = UserParamUtils.getUserCreateDTO(null, null);
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组ID为空
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                null,
                errorUserList);
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //没有用户
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                null);
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组含有null
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                errorUserList);
        userMaintainRequest.getUserRoleIdList().add(null);
        userMaintainRequest.getUserRoleIdList().add("");
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户名称为空的数据
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setEmail("tianyang.name.empty@126.com");
        }});
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户邮箱为空的数据
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.email.empty");
        }});
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户邮箱不符合标准
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("用户邮箱放飞自我");
            setEmail("用户邮箱放飞自我");
        }});
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
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
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //含有重复的用户邮箱
        userMaintainRequest = UserParamUtils.getUserCreateDTO(
                USER_ROLE_LIST,
                errorUserList
        );
        String firstUserEmail = userMaintainRequest.getUserInfoList().get(0).getEmail();
        userMaintainRequest.getUserInfoList().add(new UserCreateInfo() {{
            setName("tianyang.no.error4");
            setEmail(firstUserEmail);
        }});
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
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
        this.requestPost(userRequestUtils.URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(8)
    public void testUserResetPasswordSuccess() throws Exception {
        this.checkUserList();
        //重置admin的密码
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList("admin"));
            this.requestPostAndReturn(userRequestUtils.URL_USER_RESET_PASSWORD, request);
            //检查数据库
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdEqualTo("admin").andPasswordEqualTo(CodingUtil.md5("metersphere"));
            Assertions.assertEquals(1, userMapper.countByExample(userExample));
            this.checkLog("admin", OperationLogType.UPDATE);
        }
        //重置普通用户密码
        {
            User paramUser = new User();
            String userId = USER_LIST.get(0).getId();
            paramUser.setId(userId);
            paramUser.setPassword("I can't say any dirty words");
            Assertions.assertEquals(1, userMapper.updateByPrimaryKeySelective(paramUser));
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList(userId));
            TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                    this.requestPostAndReturn(userRequestUtils.URL_USER_RESET_PASSWORD, request),
                    TableBatchProcessResponse.class
            );
            Assertions.assertEquals(response.getTotalCount(), response.getSuccessCount(), 1);
            List<User> userList = userService.selectByIdList(Collections.singletonList(userId));
            for (User checkUser : userList) {
                UserExample userExample = new UserExample();
                userExample.createCriteria().andIdEqualTo(checkUser.getId()).andPasswordEqualTo(CodingUtil.md5(checkUser.getEmail()));
                Assertions.assertEquals(1, userMapper.countByExample(userExample));
                this.checkLog(checkUser.getId(), OperationLogType.UPDATE);
            }
        }
        //重置非Admin用户的密码
        {
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setExcludeIds(Collections.singletonList("admin"));
            request.setSelectAll(true);
            TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                    this.requestPostAndReturn(userRequestUtils.URL_USER_RESET_PASSWORD, request),
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
                userExample.createCriteria().andIdEqualTo(checkUser.getId()).andPasswordEqualTo(CodingUtil.md5(checkUser.getEmail()));
                Assertions.assertEquals(1, userMapper.countByExample(userExample));
                this.checkLog(checkUser.getId(), OperationLogType.UPDATE);
            }
        }
    }

    @Test
    @Order(9)
    public void testUserRoleRelationSuccess() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (USER_LIST.size() < 50) {
            this.testAddSuccess();
        }
        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        //测试添加角色权限。 预期数据：每个用户都会增加对应的权限
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, null);
        //检查有权限的数据量是否一致
        Assertions.assertEquals(
                globalUserRoleRelationService.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds()).size(),
                request.getSelectIds().size() * request.getRoleIds().size()
        );
        //检查日志
        for (UserSelectOption option : USER_ROLE_LIST) {
            this.checkLog(option.getId(), OperationLogType.ADD);
        }

        //测试重复添加用户权限。预期结果：不会额外增加数据
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, null);
        //检查有权限的数据量是否一致
        Assertions.assertEquals(
                globalUserRoleRelationService.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds()).size(),
                request.getSelectIds().size() * request.getRoleIds().size()
        );
        //检查日志
        for (UserSelectOption option : USER_ROLE_LIST) {
            this.checkLog(option.getId(), OperationLogType.ADD);
        }
    }

    @Test
    @Order(9)
    public void testUserRoleRelationError() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (USER_LIST.size() < 50) {
            this.testAddSuccess();
        }
        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        // 用户ID为空
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(new ArrayList<>());
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 角色id为空
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 用户ID含有不存在的
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        request.getSelectIds().add("none user");
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
        // 角色ID含有不存在的
        request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(USER_ROLE_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        request.getRoleIds().add("none role");
        userRequestUtils.requestPost(userRequestUtils.URL_USER_ROLE_RELATION, request, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(10)
    @Sql(scripts = {"/dml/init_user_org_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetProjectAndOrganization() throws Exception {
        String str = userRequestUtils.responseGet(userRequestUtils.URL_GET_PROJECT).getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder rh = JSON.parseObject(str, ResultHolder.class);
        List<UserTreeSelectOption> userTreeSelectOptions = JSON.parseArray(
                JSON.toJSONString(rh.getData()),
                UserTreeSelectOption.class);
        //返回值不为空
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userTreeSelectOptions));
        PROJECT_LIST.addAll(userTreeSelectOptions);


        List<UserSelectOption> userSelectOptions = JSON.parseArray(
                JSON.toJSONString(
                        JSON.parseObject(
                                userRequestUtils.responseGet(userRequestUtils.URL_GET_ORGANIZATION).getResponse().getContentAsString(StandardCharsets.UTF_8),
                                ResultHolder.class).getData()),
                UserSelectOption.class);
        ORG_LIST.addAll(userSelectOptions);
    }

    @Test
    @Order(11)
    public void testAddProjectMember() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (CollectionUtils.isEmpty(USER_LIST)) {
            this.testAddSuccess();
        }
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }

        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(PROJECT_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        //排除树结构中的组织ID
        request.getRoleIds().removeAll(ORG_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        this.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, request);
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
        for (UserSelectOption option : USER_ROLE_LIST) {
            this.checkLog(option.getId(), OperationLogType.ADD);
        }
    }


    @Test
    @Order(13)
    public void testAddOrganization() throws Exception {
        //UserList中的部分角色是没有添加到某权限中的
        if (CollectionUtils.isEmpty(USER_LIST)) {
            this.testAddSuccess();
        }
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }

        List<UserCreateInfo> last50Users = USER_LIST.subList(USER_LIST.size() - 50, USER_LIST.size());
        UserRoleBatchRelationRequest request = new UserRoleBatchRelationRequest();
        request.setSelectIds(last50Users.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        request.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        this.requestPost(userRequestUtils.URL_ADD_ORGANIZATION_MEMBER, request);
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
        for (UserSelectOption option : USER_ROLE_LIST) {
            this.checkLog(option.getId(), OperationLogType.ADD);
        }
    }

    @Test
    @Order(11)
    public void testAddToOrgOrProjectError() throws Exception {
        if (CollectionUtils.isEmpty(PROJECT_LIST) || CollectionUtils.isEmpty(ORG_LIST)) {
            this.testGetProjectAndOrganization();
        }
        // 用户ID为空
        UserRoleBatchRelationRequest addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(new ArrayList<>());
        addToProjectRequest.setRoleIds(PROJECT_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);
        // 项目为空
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        addToProjectRequest.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, BAD_REQUEST_MATCHER);
        // 用户ID含有不存在的
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(Collections.singletonList("none user"));
        addToProjectRequest.setRoleIds(PROJECT_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);
        // 项目ID含有不存在的
        addToProjectRequest = new UserRoleBatchRelationRequest();
        addToProjectRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        addToProjectRequest.setRoleIds(Collections.singletonList("none role"));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, addToProjectRequest, ERROR_REQUEST_MATCHER);


        // 用户ID为空
        UserRoleBatchRelationRequest orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(new ArrayList<>());
        orgRequest.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
        // 项目为空
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        orgRequest.setRoleIds(new ArrayList<>());
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, BAD_REQUEST_MATCHER);
        // 用户ID含有不存在的
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(Collections.singletonList("none user"));
        orgRequest.setRoleIds(ORG_LIST.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
        // 项目ID含有不存在的
        orgRequest = new UserRoleBatchRelationRequest();
        orgRequest.setSelectIds(USER_LIST.stream().map(UserCreateInfo::getId).collect(Collectors.toList()));
        orgRequest.setRoleIds(Collections.singletonList("none role"));
        userRequestUtils.requestPost(userRequestUtils.URL_ADD_PROJECT_MEMBER, orgRequest, ERROR_REQUEST_MATCHER);
    }

    //本测试类中会用到很多次用户数据。所以测试删除的方法放于最后
    @Test
    @Order(99)
    public void testUserDeleteSuccess() throws Exception {
        this.checkUserList();
        //删除指定的用户
        {
            UserCreateInfo deleteUser = USER_LIST.get(0);
            TableBatchProcessDTO request = new TableBatchProcessDTO();
            request.setSelectIds(Collections.singletonList(deleteUser.getId()));
            TableBatchProcessResponse response = userRequestUtils.parseObjectFromMvcResult(
                    userRequestUtils.responsePost(userRequestUtils.URL_USER_DELETE, request), TableBatchProcessResponse.class);
            Assertions.assertEquals(request.getSelectIds().size(), response.getTotalCount());
            Assertions.assertEquals(request.getSelectIds().size(), response.getSuccessCount());
            //检查数据库
            User user = userMapper.selectByPrimaryKey(deleteUser.getId());
            Assertions.assertTrue(user.getDeleted());
            USER_LIST.remove(deleteUser);
        }
    }

    //删除失败的方法要放在删除成功方法后面执行
    @Test
    @Order(100)
    public void testUserDeleteError() throws Exception {
        //参数为空
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        this.requestPost(userRequestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);
        //用户不存在
        request.setSelectIds(Collections.singletonList("none user"));
        this.requestPost(userRequestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);
        //测试用户已经被删除的
        if (CollectionUtils.isEmpty(DELETED_USER_ID_LIST)) {
            this.testUserDeleteSuccess();
        }
        request.setSelectIds(DELETED_USER_ID_LIST);
        this.requestPost(userRequestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);

        //测试包含Admin用户
        request = new TableBatchProcessDTO();
        request.setSelectAll(true);
        this.requestPost(userRequestUtils.URL_USER_DELETE, request, ERROR_REQUEST_MATCHER);
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

    //成功入库的用户保存内存中，其他用例会使用到
    private void addUser2List(MvcResult mvcResult) throws Exception {
        UserBatchCreateDTO userMaintainRequest = userRequestUtils.parseObjectFromMvcResult(mvcResult, UserBatchCreateDTO.class);
        for (UserCreateInfo item : userMaintainRequest.getUserInfoList()) {
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

    private List<UserDTO> checkImportUserInDb(ExcelParseDTO<UserExcelRowDTO> userImportReportDTOByFile) throws Exception {
        List<UserDTO> returnList = new ArrayList<>();
        for (UserExcelRowDTO item : userImportReportDTOByFile.getDataList()) {
            UserDTO userDTO = this.getUserByEmail(item.getEmail());
            Assertions.assertNotNull(userDTO);
            returnList.add(userDTO);
        }
        return returnList;
    }
}
