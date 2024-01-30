package io.metersphere.system.utils.user;

import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.utils.Pager;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRequestUtils {

    //用户管理URL
    public static final String URL_USER_CREATE = "/system/user/add";
    public static final String URL_USER_UPDATE = "/system/user/update";
    public static final String URL_USER_GET = "/system/user/get/%s";
    public static final String URL_USER_PAGE = "/system/user/page";
    public static final String URL_GET_GLOBAL_SYSTEM = "/system/user/get/global/system/role";
    public static final String URL_USER_UPDATE_ENABLE = "/system/user/update/enable";
    public static final String URL_USER_IMPORT = "/system/user/import";
    public static final String URL_USER_DELETE = "/system/user/delete";
    public static final String URL_USER_RESET_PASSWORD = "/system/user/reset/password";
    public static final String URL_USER_ROLE_RELATION = "/system/user/add/batch/user-role";

    //查找组织、项目
    public static final String URL_GET_ORGANIZATION = "/system/user/get/organization";
    public static final String URL_GET_PROJECT = "/system/user/get/project";
    public static final String URL_ADD_PROJECT_MEMBER = "/system/user/add-project-member";
    public static final String URL_ADD_ORGANIZATION_MEMBER = "/system/user/add-org-member";

    //用户邀请
    public static final String URL_INVITE = "/system/user/invite";
    //用户邀请
    public static final String URL_INVITE_REGISTER = "/system/user/register-by-invite";

    private final MockMvc mockMvc;
    public final String sessionId;
    private final String csrfToken;

    public UserRequestUtils(MockMvc mockMvc, String sessionId, String csrfToken) {
        this.mockMvc = mockMvc;
        this.sessionId = sessionId;
        this.csrfToken = csrfToken;
    }


    //解析返回值
    public <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        String returnData = "";
        try {
            returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        } catch (Exception ignore) {
        }
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
    }

    //参数是对象
    public void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        if (resultMatcher == null) {
            //默认检查200
            resultMatcher = status().isOk();
        }
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    public void requestGet(String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    public void requestFile(String url, MockMultipartFile file, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    //以下是带返回值的请求
    public MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    public MvcResult responseFile(String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    public void responseFileError(String url, MockMultipartFile file) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError());
    }

    public Pager<?> selectUserPage(BasePageRequest basePageRequest) throws Exception {
        MvcResult mvcResult = this.responsePost(this.URL_USER_PAGE, basePageRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
    }
}
