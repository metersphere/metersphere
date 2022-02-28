package com.test;

import com.google.gson.Gson;
import io.metersphere.Application;
import io.metersphere.xpack.ui.domain.SideDTO;
import io.metersphere.xpack.ui.util.TemplateUtils;
import io.metersphere.xpack.ui.util.WebDriverSamplerHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/**
 * UI 相关单元测试
 */
public class UITest {

    /**
     * 测试脚本转换是否正常
     */
    @Test
    public void testWebdriverScript() throws IOException {
        String sidePath = this.getClass().getClassLoader().getResource("selenium-example/baidu.side").getPath();
        String sideDefinition = TemplateUtils.readContent(sidePath);
        System.out.println(WebDriverSamplerHelper.getFullWebDriverScript(new Gson().fromJson(sideDefinition, SideDTO.class)));
    }
}
