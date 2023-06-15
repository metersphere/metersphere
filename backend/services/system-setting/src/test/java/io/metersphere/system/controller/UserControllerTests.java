package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserEditRequest;
import io.metersphere.system.dto.UserInfo;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.utils.UserTestUtils;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;

    //涉及到的url
    private static final String URL_USER_CREATE = "/system/user/add";
    private static final String URL_USER_UPDATE = "/system/user/update";
    private static final String URL_USER_GET = "/system/user/get/%s";
    private static final String URL_USER_PAGE = "/system/user/page";
    private static final String URL_GET_GLOBAL_SYSTEM = "/system/user/get/global/system/role";
    //失败请求返回编码
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    //测试过程中需要用到的数据
    private static final List<UserInfo> USER_LIST = new ArrayList<>();
    private static List<UserRoleOption> defaultUserRoleList = new ArrayList<>();
    //默认数据
    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String USER_NONE_ROLE_EMAIL = "tianyang.none.role@163.com";

    //记录查询到的组织信息
    private void setDefaultUserRoleList(MvcResult mvcResult) throws Exception {
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<UserRoleOption> userRoleList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserRoleOption.class);
        //返回值不为空
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userRoleList));
        defaultUserRoleList.addAll(userRoleList);
    }

    //成功入库的用户保存内存中，其他用例会使用到
    private void addUser2List(MvcResult mvcResult) {
        UserBatchCreateDTO userMaintainRequest = UserTestUtils.parseObjectFromMvcResult(mvcResult, UserBatchCreateDTO.class);
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
        MvcResult mvcResult = this.responseGet(URL_GET_GLOBAL_SYSTEM);
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
                    add(new UserInfo() {{
                        setName(USER_DEFAULT_NAME);
                        setEmail(USER_DEFAULT_EMAIL);
                        setSource("LOCAL");
                    }});
                    add(new UserInfo() {{
                        setName("tianyang.no.2");
                        setEmail("tianyang.no.2@126.com");
                        setSource("LOCAL");
                    }});
                }}
        );
        MvcResult mvcResult = this.responsePost(URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);

        //含有重复的用户名称
        userMaintainRequest = UserTestUtils.getUserCreateDTO(

                defaultUserRoleList,
                new ArrayList<>() {{
                    add(new UserInfo() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.1@126.com");
                        setSource("LOCAL");
                    }});
                    add(new UserInfo() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.2@126.com");
                        setSource("LOCAL");
                    }});
                }}
        );
        mvcResult = this.responsePost(URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);
    }

    @Test
    @Order(1)
    public void testAddError() throws Exception {
        if (CollectionUtils.isEmpty(defaultUserRoleList)) {
            this.testGetGlobalSystemUserRoleSuccess();
        }
        UserBatchCreateDTO userMaintainRequest;
        List<UserInfo> errorUserList = new ArrayList<>() {{
            add(new UserInfo() {{
                setName("tianyang.error.1");
                setEmail("tianyang.error.name.1@126.com");
                setSource("LOCAL");
            }});
            add(new UserInfo() {{
                setName("tianyang.error.2");
                setEmail("tianyang.error.name.2@126.com");
                setSource("LOCAL");
            }});
        }};

        /*
         * 校验参数不合法的反例
         * 每一次校验，使用getErrorUserCreateDTO方法重新获取参数，避免上一步的参数干扰
         */
        //所有参数都为空
        userMaintainRequest = UserTestUtils.getUserCreateDTO(null, null);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组ID为空
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                null,
                errorUserList);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //没有用户
        userMaintainRequest = UserTestUtils.getUserCreateDTO(

                defaultUserRoleList,
                null);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有不存在的用户组
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList);
        userMaintainRequest.getUserRoleIdList().add(null);
        userMaintainRequest.getUserRoleIdList().add("");
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户名称为空的数据
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserInfo() {{
            setEmail("tianyang.name.empty@126.com");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户邮箱为空的数据
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserInfo() {{
            setName("tianyang.email.empty");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户邮箱不符合标准
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserInfo() {{
            setName("用户邮箱放飞自我");
            setEmail("用户邮箱放飞自我");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户来源为空
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                new ArrayList<>()
        );
        userMaintainRequest.getUserInfoList().add(new UserInfo() {{
            setName("tianyang.source.empty");
            setEmail("tianyang.source.empty@126.com");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);

        /*
         * 校验业务判断出错的反例 （500 error)
         * 需要保证数据库有正常数据
         */
        this.checkUserList();
        //不含有系统成员用户组
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList.stream().filter(item -> !StringUtils.equals(item.getId(), "member")).toList(),
                errorUserList);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //含有重复的用户邮箱
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList
        );
        String firstUserEmail = userMaintainRequest.getUserInfoList().get(0).getEmail();
        userMaintainRequest.getUserInfoList().add(new UserInfo() {{
            setName("tianyang.no.error4");
            setEmail(firstUserEmail);
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //测试请求参数中含有数据库中已存在的邮箱情况
        userMaintainRequest = UserTestUtils.getUserCreateDTO(
                defaultUserRoleList,
                errorUserList
        );
        userMaintainRequest.setUserInfoList(
                new ArrayList<>() {{
                    add(new UserInfo() {{
                        setName("tianyang.repeat.email.db");
                        setEmail(USER_DEFAULT_EMAIL);
                        setSource("LOCAL");
                    }});
                }}
        );
        this.requestPost(URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_user.sql"},
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
        String url = String.format(URL_USER_GET, email);
        return UserTestUtils.parseObjectFromMvcResult(this.responseGet(url), UserDTO.class);
    }

    @Test
    @Order(2)
    public void testGetByEmailError() throws Exception {
        //测试使用任意参数，不能获取到任何用户信息
        this.checkUserList();
        String url = URL_USER_GET + UUID.randomUUID();
        MvcResult mvcResult = this.responseGet(url);

        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
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
        MvcResult mvcResult = this.responsePost(URL_USER_PAGE, basePageRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<UserInfo> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), basePageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiDefinition>) returnPager.getList()).size() <= basePageRequest.getPageSize());

        //测试根据创建时间倒叙排列
        basePageRequest = UserTestUtils.getDefaultPageRequest();
        basePageRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(URL_USER_PAGE, basePageRequest);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //第一个数据的createTime是最大的
        List<UserInfo> userInfoList = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserInfo.class);
        long firstCreateTime = userInfoList.get(0).getCreateTime();
        for (UserInfo userInfo : userInfoList) {
            Assertions.assertFalse(userInfo.getCreateTime() > firstCreateTime);
        }
    }

    @Test
    @Order(3)
    public void testPageError() throws Exception {
        //当前页码不大于0
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setPageSize(5);
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(5);
        basePageRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
    }

    @Test
    @Order(4)
    public void testUserUpdateSuccess() throws Exception {
        this.checkUserList();
        UserInfo user = USER_LIST.get(0);
        UserEditRequest userMaintainRequest;
        UserEditRequest response;
        UserDTO checkDTO;
        //更改名字
        user.setName("TEST-UPDATE");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改邮箱
        user.setEmail("songtianyang-test-email@12138.com");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改手机号
        user.setPhone("18511112222");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改用户组(这里只改成用户成员权限)
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user,
                defaultUserRoleList.stream().filter(item -> StringUtils.equals(item.getId(), "member")).toList()
        );
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //更改用户组(把上面的情况添加别的权限)
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
        //用户信息复原
        user = USER_LIST.get(0);
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        response = UserTestUtils.parseObjectFromMvcResult(this.responsePost(URL_USER_UPDATE, userMaintainRequest), UserEditRequest.class);
        checkDTO = this.getUserByEmail(user.getEmail());
        UserTestUtils.compareUserDTO(response, checkDTO);
    }

    @Test
    @Order(5)
    public void testUserUpdateError() throws Exception {
        // 4xx 验证
        UserInfo user = USER_LIST.get(0);
        UserEditRequest userMaintainRequest;
        UserEditRequest response;
        //更改名字
        user.setName("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //email为空
        user = USER_LIST.get(0);
        user.setEmail("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //手机号为空
        user = USER_LIST.get(0);
        user.setEmail("");
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组为空
        user = USER_LIST.get(0);
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        userMaintainRequest.setUserRoleList(new ArrayList<>());
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, BAD_REQUEST_MATCHER);

        // 500验证
        //邮箱重复
        user = USER_LIST.get(0);
        user.setEmail(USER_LIST.get(USER_LIST.size() - 1).getEmail());
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user, defaultUserRoleList);
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //用户组不包含系统成员
        user = USER_LIST.get(0);
        userMaintainRequest = UserTestUtils.getUserUpdateDTO(user,
                defaultUserRoleList.stream().filter(item -> !StringUtils.equals(item.getId(), "member")).toList()
        );
        this.requestPost(URL_USER_UPDATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }
}
