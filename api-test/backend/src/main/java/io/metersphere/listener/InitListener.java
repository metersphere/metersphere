package io.metersphere.listener;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RunInterface;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InitListener implements ApplicationRunner {

    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioService apiScenarioService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        this.initOnceOperate();
    }

    /**
     * 处理初始化数据、兼容数据
     * 只在第一次升级的时候执行一次
     *
     * @param initFuc
     * @param key
     */
    private void initOnceOperate(RunInterface initFuc, final String key) {
        try {
            String value = systemParameterService.getValue(key);
            if (StringUtils.isBlank(value)) {
                initFuc.run();
                systemParameterService.saveInitParam(key);
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void initOnceOperate() {
        initOnceOperate(apiDefinitionService::setProjectIdInExecutionInfo, "update.api.execution.projectId");
        initOnceOperate(apiTestCaseService::setProjectIdInExecutionInfo, "update.apiTestCase.execution.projectId");
        initOnceOperate(apiScenarioService::setProjectIdInExecutionInfo, "update.scenario.execution.projectId");
    }
}
