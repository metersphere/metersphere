package com.test;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.Application;
import io.metersphere.xpack.ui.dto.SideDTO;
import io.metersphere.xpack.ui.impl.CommandConfig;
import io.metersphere.xpack.ui.service.UiAutomationService;
import io.metersphere.xpack.ui.util.TemplateUtils;
import io.metersphere.xpack.ui.util.WebDriverSamplerHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/**
 * UI 相关单元测试
 */
public class UITest {

    @Resource
    private UiAutomationService uiAutomationService;

    /**
     * 测试脚本转换是否正常
     */
    @Test
    public void testWebdriverScript() {
        // 获取由 SELENIUM IDE 导出的 json 文件地址
        String sidePath = this.getClass().getClassLoader().getResource("selenium-example/baidu.side").getPath();
        // 读取文件内容
        String sideDefinition = TemplateUtils.readContent(sidePath);
        // 将 json 字符串转化成 SideDTO 对象，再调用 getFullWebDriverScript 方法翻译成最终的 webdriver 脚本
        // 全局脚本配置
        CommandConfig globalConfig = new CommandConfig();
        globalConfig.setSecondsWaitWindowOnLoad(10);
        String str = WebDriverSamplerHelper.getFullWebDriverScript(JSONObject.parseObject(sideDefinition, SideDTO.class), globalConfig);
        System.out.println(str);
    }

    @Test
    public void testUIRun() {
        uiAutomationService.run("");
        try {
            Thread.sleep(1000 * 60 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
