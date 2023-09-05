package io.metersphere.project.controller;


import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectLogControllerTests extends BaseTest {

    public static final String PROJECT = "PROJECT";
    public static final String PROJECT_USER_LIST = "/project/log/user/list";
    public static final String PROJECT_LOG_LIST = "/project/log/list";


    @Test
    @Order(1)
    public void testProjectUserList() throws Exception {
        String keyword = "o";
        this.requestGetWithOkAndReturn(PROJECT_USER_LIST+ "/default-organization-member-test" + "?keyword=" + keyword);
    }

    @Test
    @Order(3)
    public void testProjectLogList() throws Exception {
        OperationLogRequest request = buildParam(PROJECT);
        //项目级别 全部
        this.requestPostWithOkAndReturn(PROJECT_LOG_LIST, request);

        //其他查询条件
        request.setOperUser("admin");
        request.setType("add");
        request.setModule("SYSTEM_PARAMETER_SETTING");
        request.setContent("认证配置");
        request.setProjectIds(Arrays.asList("project_id_001", "project_id_002"));
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        this.requestPostWithOkAndReturn(PROJECT_LOG_LIST, request);

        requestPostPermissionTest(PermissionConstants.PROJECT_LOG_READ, PROJECT_LOG_LIST, request);

    }


    private OperationLogRequest buildParam(String level) {
        OperationLogRequest request = new OperationLogRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setStartTime(1689131059000l);
        request.setEndTime(1689149059000l);
        request.setLevel(level);
        return request;
    }
}
