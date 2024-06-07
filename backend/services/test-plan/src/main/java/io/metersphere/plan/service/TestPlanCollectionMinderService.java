package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanCollectionConfigDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeNodeDTO;
import io.metersphere.plan.mapper.ExtTestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanCollectionMinderService {

    @Resource
    private ExtTestPlanCollectionMapper extTestPlanCollectionMapper;

    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;

    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;

    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    /**
     * 测试计划-脑图用例列表查询
     *
     * @return FunctionalMinderTreeDTO
     */
    public List<TestPlanCollectionMinderTreeDTO> getMindTestPlanCase(String planId) {
        List<TestPlanCollectionMinderTreeDTO> list = new ArrayList<>();
        List<TestPlanCollectionConfigDTO> testPlanCollections = extTestPlanCollectionMapper.getList(planId);
        //构造根节点
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = buildRoot();
        TestPlanCollectionMinderTreeDTO testPlanCollectionMinderTreeDTO = new TestPlanCollectionMinderTreeDTO();
        testPlanCollectionMinderTreeDTO.setData(testPlanCollectionMinderTreeNodeDTO);
        //构造type节点
        List<TestPlanCollectionMinderTreeDTO> children = new ArrayList<>();
        List<TestPlanCollectionConfigDTO> parentList = testPlanCollections.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)).sorted(Comparator.comparing(TestPlanCollection::getPos)).toList();
        buildTypeChildren(parentList, testPlanCollections, children);
        testPlanCollectionMinderTreeDTO.setChildren(children);
        list.add(testPlanCollectionMinderTreeDTO);
        return list;
    }

    private void buildTypeChildren(List<TestPlanCollectionConfigDTO> parentList, List<TestPlanCollectionConfigDTO> testPlanCollections, List<TestPlanCollectionMinderTreeDTO> children) {
        for (TestPlanCollection testPlanCollection : parentList) {
            TestPlanCollectionMinderTreeDTO typeTreeDTO = new TestPlanCollectionMinderTreeDTO();
            TestPlanCollectionMinderTreeNodeDTO typeTreeNodeDTO = buildTypeChildData(testPlanCollection);
            //构造子节点
            List<TestPlanCollectionMinderTreeDTO> collectionChildren = new ArrayList<>();
            List<TestPlanCollectionConfigDTO> childrenList = testPlanCollections.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getParentId(), testPlanCollection.getId())).sorted(Comparator.comparing(TestPlanCollection::getPos)).toList();
            Map<String, List<TestPlanCollectionConfigDTO>> typeChildren = childrenList.stream().collect(Collectors.groupingBy(TestPlanCollectionConfigDTO::getType));
            Map<String, List<TestPlanFunctionalCase>> testPlanFunctionalCaseMap = getTestPlanFunctionalCases(typeChildren);
            Map<String, List<TestPlanApiCase>> testPlanApiCaseMap = getTestPlanApiCases(typeChildren);
            Map<String, List<TestPlanApiScenario>> testPlanApiScenarioMap = getTestPlanApiScenarios(typeChildren);
            buildCollectionChildren(childrenList, collectionChildren, testPlanFunctionalCaseMap, testPlanApiCaseMap, testPlanApiScenarioMap);
            typeTreeDTO.setData(typeTreeNodeDTO);
            typeTreeDTO.setChildren(collectionChildren);
            children.add(typeTreeDTO);
        }
    }

    @NotNull
    private static TestPlanCollectionMinderTreeNodeDTO buildTypeChildData(TestPlanCollection testPlanCollection) {
        TestPlanCollectionMinderTreeNodeDTO typeTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        BeanUtils.copyBean(typeTreeNodeDTO, testPlanCollection);
        typeTreeNodeDTO.setText(testPlanCollection.getName());
        if (StringUtils.equalsIgnoreCase(testPlanCollection.getExecuteMethod(), ApiBatchRunMode.PARALLEL.toString())) {
            typeTreeNodeDTO.setPriority(Translator.get("test_plan.mind.serial"));
        } else {
            typeTreeNodeDTO.setPriority(Translator.get("test_plan.mind.parallel"));
        }
        return typeTreeNodeDTO;
    }

    private Map<String, List<TestPlanApiScenario>> getTestPlanApiScenarios(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.SCENARIO_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> scenarioCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
        testPlanApiScenarioExample.createCriteria().andTestPlanCollectionIdIn(scenarioCollectIds);
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExample(testPlanApiScenarioExample);
        return testPlanApiScenarios.stream().collect(Collectors.groupingBy(TestPlanApiScenario::getTestPlanCollectionId));
    }

    private Map<String, List<TestPlanApiCase>> getTestPlanApiCases(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.API_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> apiCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
        testPlanApiCaseExample.createCriteria().andTestPlanCollectionIdIn(apiCollectIds);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
        return testPlanApiCases.stream().collect(Collectors.groupingBy(TestPlanApiCase::getTestPlanCollectionId));

    }

    private Map<String, List<TestPlanFunctionalCase>> getTestPlanFunctionalCases(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.FUNCTIONAL_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> functionalCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andTestPlanCollectionIdIn(functionalCollectIds);
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        return testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanCollectionId));

    }


    private void buildCollectionChildren(List<TestPlanCollectionConfigDTO> childrenList, List<TestPlanCollectionMinderTreeDTO> collectionChildren, Map<String, List<TestPlanFunctionalCase>> testPlanFunctionalCaseMap, Map<String, List<TestPlanApiCase>> testPlanApiCaseMap, Map<String, List<TestPlanApiScenario>> testPlanApiScenarioMap) {
        for (TestPlanCollectionConfigDTO planCollection : childrenList) {
            TestPlanCollectionMinderTreeDTO collectionTreeDTO = new TestPlanCollectionMinderTreeDTO();
            TestPlanCollectionMinderTreeNodeDTO collectionTreeNodeDTO = buildTypeChildData(planCollection);
            collectionTreeNodeDTO.setResource(new ArrayList<>());
            List<TestPlanCollectionMinderTreeDTO> endList = getEndList(testPlanFunctionalCaseMap, testPlanApiCaseMap, testPlanApiScenarioMap, planCollection);
            collectionTreeDTO.setData(collectionTreeNodeDTO);
            collectionTreeDTO.setChildren(endList);
            collectionChildren.add(collectionTreeDTO);
        }
    }

    @NotNull
    private static List<TestPlanCollectionMinderTreeDTO> getEndList(Map<String, List<TestPlanFunctionalCase>> testPlanFunctionalCaseMap, Map<String, List<TestPlanApiCase>> testPlanApiCaseMap, Map<String, List<TestPlanApiScenario>> testPlanApiScenarioMap, TestPlanCollectionConfigDTO planCollection) {
        List<TestPlanCollectionMinderTreeDTO> endList = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(planCollection.getType(), CaseType.FUNCTIONAL_CASE.getKey())) {
            buildFunctionalChild(testPlanFunctionalCaseMap, planCollection, endList);
        } else if (StringUtils.equalsIgnoreCase(planCollection.getType(), CaseType.API_CASE.getKey())) {
            buildApiCaseChild(testPlanApiCaseMap, planCollection, endList);
        } else {
            buildScenarioChild(testPlanApiScenarioMap, planCollection, endList);
        }
        return endList;
    }

    private static void buildScenarioChild(Map<String, List<TestPlanApiScenario>> testPlanApiScenarioMap, TestPlanCollectionConfigDTO planCollection, List<TestPlanCollectionMinderTreeDTO> endList) {
        TestPlanCollectionMinderTreeDTO countTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO countTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMap.get(planCollection.getId());
        int count = 0;
        if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
            count = testPlanApiScenarios.size();
        }
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList);
        TestPlanCollectionMinderTreeDTO envTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO envTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(envTreeNodeDTO, planCollection.getEnvName(), "test_plan.mind.environment", envTreeDTO, endList);
        TestPlanCollectionMinderTreeDTO poolTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO poolTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(poolTreeNodeDTO, planCollection.getPoolName(), "test_plan.mind.test_resource_pool", poolTreeDTO, endList);
    }

    private static void buildChild(TestPlanCollectionMinderTreeNodeDTO treeNodeDTO, String text, String key, TestPlanCollectionMinderTreeDTO treeDTO, List<TestPlanCollectionMinderTreeDTO> endList) {
        treeNodeDTO.setText(text);
        treeNodeDTO.setResource(List.of(Translator.get(key)));
        treeDTO.setData(treeNodeDTO);
        treeDTO.setChildren(new ArrayList<>());
        if (StringUtils.isNotBlank(text)) {
            endList.add(treeDTO);
        }
    }

    private static void buildApiCaseChild(Map<String, List<TestPlanApiCase>> testPlanApiCaseMap, TestPlanCollectionConfigDTO planCollection, List<TestPlanCollectionMinderTreeDTO> endList) {
        TestPlanCollectionMinderTreeDTO countTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO countTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMap.get(planCollection.getId());
        int count = 0;
        if (CollectionUtils.isNotEmpty(testPlanApiCases)) {
            count = testPlanApiCases.size();
        }
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList);
        TestPlanCollectionMinderTreeDTO envTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO envTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(envTreeNodeDTO, planCollection.getEnvName(), "test_plan.mind.environment", envTreeDTO, endList);
        TestPlanCollectionMinderTreeDTO poolTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO poolTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(poolTreeNodeDTO, planCollection.getPoolName(), "test_plan.mind.test_resource_pool", poolTreeDTO, endList);
    }

    private static void buildFunctionalChild(Map<String, List<TestPlanFunctionalCase>> testPlanFunctionalCaseMap, TestPlanCollectionConfigDTO planCollection, List<TestPlanCollectionMinderTreeDTO> endList) {
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMap.get(planCollection.getId());
        int count = 0;
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            count = testPlanFunctionalCases.size();
        }
        TestPlanCollectionMinderTreeDTO countTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO countTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList);
    }

    @NotNull
    private static TestPlanCollectionMinderTreeNodeDTO buildRoot() {
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        testPlanCollectionMinderTreeNodeDTO.setId(ModuleConstants.DEFAULT_NODE_ID);
        testPlanCollectionMinderTreeNodeDTO.setText(Translator.get("test_plan.mind.test_plan"));
        testPlanCollectionMinderTreeNodeDTO.setResource(new ArrayList<>());
        return testPlanCollectionMinderTreeNodeDTO;
    }


}
