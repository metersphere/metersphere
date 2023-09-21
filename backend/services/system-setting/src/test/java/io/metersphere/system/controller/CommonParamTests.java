package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.param.BasePageRequestDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommonParamTests extends BaseTest {

    /**
     * 校验 BasePageRequestDefinition 参数
     * @throws Exception
     */
    @Test
    void testBasePageRequestDefinition() throws Exception {
        String url = GlobalUserRoleRelationControllerTests.BASE_URL + "list";
        paramValidateTest(BasePageRequestDefinition.class, url);
        BasePageRequestDefinition basePageRequestDefinition = new BasePageRequestDefinition();
        // @@校验 sort 字段 sql 防注入,有点复杂,手动写校验
        String sortName = "SELECT * FROM user";
        basePageRequestDefinition.setSort(new HashMap<>() {{
            put(sortName, "asc");
        }});
        MvcResult mvcResult = this.requestPostAndReturn(url, basePageRequestDefinition);
        // 校验错误是否是参数错误
        Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
        Map messageDetail = getResultMessageDetail(mvcResult, Map.class);
        // 校验错误信息中包含了该字段
        Assertions.assertTrue(messageDetail.containsKey(String.format("sort[%s]", sortName)));
    }
}