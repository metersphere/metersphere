package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.param.OperationLogRequestDefinition;
import io.metersphere.system.log.vo.OrgOperationLogRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationLogControllerTests extends BaseTest {

    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String ORGANIZATION_OPTIONS_LIST = "/organization/log/get/options";
    public static final String ORGANIZATION_USER_LIST = "/organization/log/user/list";
    public static final String ORGANIZATION_LOG_LIST = "/organization/log/list";


    /**
     * 组织菜单-操作日志接口 测试用例
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    public void testGetOrganizationOptions() throws Exception {
        this.requestGetWithOkAndReturn(ORGANIZATION_OPTIONS_LIST + "/organization_id_001");
    }


    @Test
    @Order(2)
    public void testOrganizationUserList() throws Exception {
        String keyword = "o";
        this.requestGetWithOkAndReturn(ORGANIZATION_USER_LIST + "/organization_id_001" + "?keyword=" + keyword);
    }


    @Test
    @Order(3)
    public void testOrganizationLogList() throws Exception {
        OrgOperationLogRequest request = buildParam(ORGANIZATION);
        //项目级别 全部
        this.requestPostWithOkAndReturn(ORGANIZATION_LOG_LIST, request);

        //其他查询条件
        request.setOperUser("admin");
        request.setType("add");
        request.setModule("SYSTEM_PARAMETER_SETTING");
        request.setContent("认证配置");
        this.requestPostWithOkAndReturn(ORGANIZATION_LOG_LIST, request);

        //项目级别 指定项目查询
        request.setProjectIds(Arrays.asList("project_id_001", "project_id_002"));
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});

        this.requestPostWithOkAndReturn(ORGANIZATION_LOG_LIST, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(OperationLogRequestDefinition.class, ORGANIZATION_LOG_LIST);

        requestPostPermissionTest(PermissionConstants.ORGANIZATION_LOG_READ, ORGANIZATION_LOG_LIST, request);

    }

    private OrgOperationLogRequest buildParam(String level) {
        OrgOperationLogRequest request = new OrgOperationLogRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setStartTime(1689131059000l);
        request.setEndTime(1689149059000l);
        request.setLevel(level);
        return request;
    }
}
