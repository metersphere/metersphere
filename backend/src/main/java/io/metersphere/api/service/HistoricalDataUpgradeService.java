package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.EnvironmentDTO;
import io.metersphere.api.dto.SaveHistoricalDataUpgrade;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.definition.request.timer.MsConstantTimer;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.request.*;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class HistoricalDataUpgradeService {
    @Resource
    private ApiTestMapper apiTestMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    ApiTestEnvironmentService apiTestEnvironmentService;
    private Map<String, EnvironmentDTO> environmentDTOMap;

    private int getNextNum(String projectId) {
        ApiScenario apiScenario = extApiScenarioMapper.getNextNum(projectId);
        if (apiScenario == null) {
            return 100001;
        } else {
            return Optional.of(apiScenario.getNum() + 1).orElse(100001);
        }
    }

    private MsScenario createScenario(Scenario oldScenario) {
        MsScenario scenario = new MsScenario();
        scenario.setVariables(oldScenario.getVariables());
        scenario.setName(oldScenario.getName());
        scenario.setEnableCookieShare(oldScenario.isEnableCookieShare());
        scenario.setEnvironmentId(oldScenario.getEnvironmentId());
        scenario.setReferenced("Upgrade");
        scenario.setId(oldScenario.getId());
        scenario.setResourceId(UUID.randomUUID().toString());
        LinkedList<MsTestElement> testElements = new LinkedList<>();
        int index = 1;
        for (Request request : oldScenario.getRequests()) {
            // 条件控制器
            MsIfController ifController = null;
            if (request.getController() != null && StringUtils.isNotEmpty(request.getController().getValue())
                    && StringUtils.isNotEmpty(request.getController().getVariable())) {
                ifController = new MsIfController();
                BeanUtils.copyBean(ifController, request.getController());
                ifController.setType("IfController");
                ifController.setName("IfController");
                ifController.setIndex(index + "");
                ifController.setResourceId(UUID.randomUUID().toString());
            }
            // 等待控制器
            if (request.getTimer() != null && StringUtils.isNotEmpty(request.getTimer().getDelay())) {
                MsConstantTimer constantTimer = new MsConstantTimer();
                BeanUtils.copyBean(constantTimer, request.getTimer());
                constantTimer.setType("ConstantTimer");
                constantTimer.setIndex(index + "");
                constantTimer.setResourceId(UUID.randomUUID().toString());
                testElements.add(constantTimer);
            }

            MsTestElement element = null;
            if (request instanceof HttpRequest) {
                element = new MsHTTPSamplerProxy();
                HttpRequest request1 = (HttpRequest) request;
                if (StringUtils.isEmpty(request1.getPath()) && StringUtils.isNotEmpty(request1.getUrl())) {
                    try {
                        URL urlObject = new URL(request1.getUrl());
                        String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                        request1.setPath(envPath);
                    } catch (Exception ex) {
                        LogUtil.error(ex.getMessage());
                    }
                }
                if (request1.getBody() != null && request1.getBody().isOldKV()) {
                    request1.getBody().setType(Body.FORM_DATA);
                }
                BeanUtils.copyBean(element, request1);
                ((MsHTTPSamplerProxy) element).setProtocol(RequestType.HTTP);
                ((MsHTTPSamplerProxy) element).setArguments(request1.getParameters());
                if (StringUtils.isEmpty(element.getName())) {
                    element.setName(request1.getPath());
                }

                element.setType("HTTPSamplerProxy");
            }
            if (request instanceof DubboRequest) {
                String requestJson = JSON.toJSONString(request);
                element = JSON.parseObject(requestJson, MsDubboSampler.class);
                element.setType("DubboSampler");
            }
            if (request instanceof SqlRequest) {
                element = new MsJDBCSampler();
                SqlRequest request1 = (SqlRequest) request;
                BeanUtils.copyBean(element, request1);
                EnvironmentDTO dto = environmentDTOMap.get(request1.getDataSource());
                if (dto != null) {
                    ((MsJDBCSampler) element).setEnvironmentId(dto.getEnvironmentId());
                    ((MsJDBCSampler) element).setDataSourceId(dto.getDatabaseConfig().getId());
                    ((MsJDBCSampler) element).setDataSource(dto.getDatabaseConfig());
                }
                element.setType("JDBCSampler");
            }
            if (request instanceof TCPRequest) {
                element = new MsTCPSampler();
                TCPRequest request1 = (TCPRequest) request;
                BeanUtils.copyBean(element, request1);
                element.setType("TCPSampler");
            }
            element.setIndex(index + "");
            element.setResourceId(UUID.randomUUID().toString());
            LinkedList<MsTestElement> msTestElements = new LinkedList<>();
            // 断言规则
            if (request.getAssertions() != null && ((request.getAssertions().getDuration() != null && request.getAssertions().getDuration().getValue() > 0) ||
                    CollectionUtils.isNotEmpty(request.getAssertions().getJsonPath()) || CollectionUtils.isNotEmpty(request.getAssertions().getJsr223()) ||
                    CollectionUtils.isNotEmpty(request.getAssertions().getRegex()) || CollectionUtils.isNotEmpty(request.getAssertions().getXpath2()))) {
                String assertions = JSON.toJSONString(request.getAssertions());
                MsAssertions msAssertions = JSON.parseObject(assertions, MsAssertions.class);
                msAssertions.setType("Assertions");
                msAssertions.setIndex(index + "");
                msAssertions.setResourceId(UUID.randomUUID().toString());
                msTestElements.add(msAssertions);
            }
            // 提取规则
            if (request.getExtract() != null && (CollectionUtils.isNotEmpty(request.getExtract().getJson()) ||
                    CollectionUtils.isNotEmpty(request.getExtract().getRegex()) || CollectionUtils.isNotEmpty(request.getExtract().getXpath()))) {
                String extractJson = JSON.toJSONString(request.getExtract());
                MsExtract extract = JSON.parseObject(extractJson, MsExtract.class);
                extract.setType("Extract");
                extract.setIndex(index + "");
                extract.setResourceId(UUID.randomUUID().toString());
                msTestElements.add(extract);
            }
            // 前置脚本
            if (request.getJsr223PreProcessor() != null && StringUtils.isNotEmpty(request.getJsr223PreProcessor().getScript())) {
                String preJson = JSON.toJSONString(request.getJsr223PreProcessor());
                MsJSR223PreProcessor preProcessor = JSON.parseObject(preJson, MsJSR223PreProcessor.class);
                preProcessor.setType("JSR223PreProcessor");
                preProcessor.setIndex(index + "");
                preProcessor.setResourceId(UUID.randomUUID().toString());
                msTestElements.add(preProcessor);
            }
            // 后置脚本
            if (request.getJsr223PostProcessor() != null && StringUtils.isNotEmpty(request.getJsr223PostProcessor().getScript())) {
                String preJson = JSON.toJSONString(request.getJsr223PostProcessor());
                MsJSR223PostProcessor preProcessor = JSON.parseObject(preJson, MsJSR223PostProcessor.class);
                preProcessor.setType("JSR223PostProcessor");
                preProcessor.setIndex(index + "");
                preProcessor.setResourceId(UUID.randomUUID().toString());
                msTestElements.add(preProcessor);
            }
            if (CollectionUtils.isNotEmpty(msTestElements)) {
                element.setHashTree(msTestElements);
            }
            if (ifController != null) {
                LinkedList<MsTestElement> elements = new LinkedList<>();
                elements.add(element);
                ifController.setHashTree(elements);
                testElements.add(ifController);
            } else {
                testElements.add(element);
            }
            index++;
        }
        scenario.setHashTree(testElements);
        return scenario;
    }

    private ApiScenarioWithBLOBs checkNameExist(Scenario oldScenario, String projectId, ApiScenarioMapper mapper) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdEqualTo(oldScenario.getId());
        List<ApiScenarioWithBLOBs> list = mapper.selectByExampleWithBLOBs(example);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    //文件的拷贝
    private static void copyFile(String sourcePath, String newPath) {
        File readfile = new File(sourcePath);
        File newFile = new File(newPath);
        BufferedWriter bufferedWriter = null;
        Writer writer = null;
        FileOutputStream fileOutputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileOutputStream = new FileOutputStream(newFile, true);
            writer = new OutputStreamWriter(fileOutputStream, "UTF-8");
            bufferedWriter = new BufferedWriter(writer);

            bufferedReader = new BufferedReader(new FileReader(readfile));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyDir(String sourcePathDir, String newPathDir) {
        File start = new File(sourcePathDir);
        File end = new File(newPathDir);
        String[] filePath = start.list();
        if (!end.exists()) {
            end.mkdir();
        }
        if (filePath != null) {
            for (String temp : filePath) {
                //添加满足情况的条件
                if (new File(sourcePathDir + File.separator + temp).isFile()) {
                    //为文件则进行拷贝
                    copyFile(sourcePathDir + File.separator + temp, newPathDir + File.separator + temp);
                }
            }
        }
    }

    private void createBodyFiles(String testId) {
        String dir = BODY_FILE_DIR + "/" + testId;
        File testDir = new File(dir);
        if (testDir.exists()) {
            testDir.mkdirs();
        }
        copyDir(dir, BODY_FILE_DIR);
    }

    private void createApiScenarioWithBLOBs(SaveHistoricalDataUpgrade saveHistoricalDataUpgrade, Scenario oldScenario, String scenarioDefinition, ApiScenarioMapper mapper) {
        if (StringUtils.isEmpty(oldScenario.getName())) {
            oldScenario.setName("默认名称-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        }
        ApiScenarioWithBLOBs scenario = checkNameExist(oldScenario, saveHistoricalDataUpgrade.getProjectId(), mapper);
        if (scenario != null) {
            scenario.setName(oldScenario.getName());
            scenario.setProjectId(saveHistoricalDataUpgrade.getProjectId());
            scenario.setTags(scenario.getTags());
            scenario.setLevel("P0");
            scenario.setModulePath(saveHistoricalDataUpgrade.getModulePath());
            scenario.setApiScenarioModuleId(saveHistoricalDataUpgrade.getModuleId());
            scenario.setPrincipal(Objects.requireNonNull(SessionUtils.getUser()).getId());
            scenario.setStepTotal(oldScenario.getRequests().size());
            scenario.setScenarioDefinition(scenarioDefinition);
            scenario.setUpdateTime(System.currentTimeMillis());
            scenario.setStatus(ScenarioStatus.Underway.name());
            scenario.setUserId(SessionUtils.getUserId());
            scenario.setNum(getNextNum(saveHistoricalDataUpgrade.getProjectId()));
            mapper.updateByPrimaryKeySelective(scenario);
        } else {
            scenario = new ApiScenarioWithBLOBs();
            scenario.setId(oldScenario.getId());
            scenario.setName(oldScenario.getName());
            scenario.setProjectId(saveHistoricalDataUpgrade.getProjectId());
            scenario.setTags(scenario.getTags());
            scenario.setLevel("P0");
            scenario.setModulePath(saveHistoricalDataUpgrade.getModulePath());
            scenario.setApiScenarioModuleId(saveHistoricalDataUpgrade.getModuleId());
            scenario.setPrincipal(Objects.requireNonNull(SessionUtils.getUser()).getId());
            scenario.setStepTotal(oldScenario.getRequests().size());
            scenario.setScenarioDefinition(scenarioDefinition);
            scenario.setCreateTime(System.currentTimeMillis());
            scenario.setUpdateTime(System.currentTimeMillis());
            scenario.setStatus(ScenarioStatus.Underway.name());
            scenario.setUserId(SessionUtils.getUserId());
            scenario.setNum(getNextNum(saveHistoricalDataUpgrade.getProjectId()));
            mapper.insert(scenario);
        }
    }

    public String upgrade(SaveHistoricalDataUpgrade saveHistoricalDataUpgrade) {
        // 初始化环境，获取数据源
        getDataSource(saveHistoricalDataUpgrade.getProjectId());

        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andIdIn(saveHistoricalDataUpgrade.getTestIds());
        List<ApiTest> blobs = apiTestMapper.selectByExampleWithBLOBs(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);
        for (ApiTest test : blobs) {
            // 附件迁移
            createBodyFiles(test.getId());

            List<Scenario> scenarios = JSON.parseArray(test.getScenarioDefinition(), Scenario.class);
            if (CollectionUtils.isNotEmpty(scenarios)) {
                // 批量处理
                for (Scenario scenario : scenarios) {
                    MsScenario scenario1 = createScenario(scenario);
                    String scenarioDefinition = JSON.toJSONString(scenario1);
                    createApiScenarioWithBLOBs(saveHistoricalDataUpgrade, scenario, scenarioDefinition, mapper);
                }
            }
        }
        sqlSession.flushStatements();
        return null;
    }

    private void getDataSource(String projectId) {
        List<ApiTestEnvironmentWithBLOBs> environments = apiTestEnvironmentService.list(projectId);
        environmentDTOMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(environments)) {
            environments.forEach(environment -> {
                EnvironmentConfig envConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                    envConfig.getDatabaseConfigs().forEach(item -> {
                        EnvironmentDTO dto = new EnvironmentDTO();
                        dto.setDatabaseConfig(item);
                        dto.setEnvironmentId(environment.getId());
                        environmentDTOMap.put(item.getId(), dto);
                    });
                }
            });
        }
    }

}
