package io.metersphere.plan.service;

import com.google.common.collect.Maps;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.BaseCollectionAssociateRequest;
import io.metersphere.plan.dto.request.TestPlanCollectionMinderEditRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.CommonConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.service.CommonProjectPoolService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;

    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;

    @Resource
    private ExtTestPlanApiScenarioMapper exttestPlanApiScenarioMapper;

    @Resource
    private TestPlanService testPlanService;

    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    @Resource
    private ExtProjectMapper extProjectMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private CommonProjectPoolService commonProjectPoolService;

    @Resource
    private TestPlanMapper testPlanMapper;

    /**
     * 测试计划-脑图用例列表查询
     *
     * @return FunctionalMinderTreeDTO
     */
    public List<TestPlanCollectionMinderTreeDTO> getMindTestPlanCase(String planId) {
        List<TestPlanCollectionMinderTreeDTO> list = new ArrayList<>();
        List<TestPlanCollectionConfigDTO> testPlanCollections = extTestPlanCollectionMapper.getList(planId);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        List<TestResourcePool> apiTest;
        if (project.getAllResourcePool()) {
            apiTest = commonProjectPoolService.getProjectAllPoolsByEffect(project);
        } else {
            apiTest = extProjectMapper.getResourcePoolOption(testPlan.getProjectId(), "api_test");
        }
        Map<String, String> resourcePoolMap = apiTest.stream().collect(Collectors.toMap(TestResourcePool::getId, TestResourcePool::getName));
        testPlanCollections.forEach(t -> {
            if (StringUtils.isBlank(resourcePoolMap.get(t.getTestResourcePoolId()))) {
                t.setPoolName(Translator.get("resource_pool_not_exist"));
                t.setNoResourcePool(true);
            } else {
                t.setPoolName(resourcePoolMap.get(t.getTestResourcePoolId()));
            }
        });

        //构造根节点
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(planId);
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = buildRoot();
        // 根节点使用计划配置的执行方式
        testPlanCollectionMinderTreeNodeDTO.setExecuteMethod(testPlanConfig.getCaseRunMode());
        if (StringUtils.equalsIgnoreCase(testPlanCollectionMinderTreeNodeDTO.getExecuteMethod(), ApiBatchRunMode.PARALLEL.toString())) {
            testPlanCollectionMinderTreeNodeDTO.setPriority(3);
        } else {
            testPlanCollectionMinderTreeNodeDTO.setPriority(2);
        }
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
        typeTreeNodeDTO.setText(Translator.get(testPlanCollection.getName(), testPlanCollection.getName()));
        if (StringUtils.equalsIgnoreCase(testPlanCollection.getExecuteMethod(), ApiBatchRunMode.PARALLEL.toString())) {
            typeTreeNodeDTO.setPriority(3);
        } else {
            typeTreeNodeDTO.setPriority(2);
        }
        if (StringUtils.equalsIgnoreCase(CaseType.FUNCTIONAL_CASE.getKey(), testPlanCollection.getType())) {
            typeTreeNodeDTO.setPriority(null);
        }
        return typeTreeNodeDTO;
    }

    private Map<String, List<TestPlanApiScenario>> getTestPlanApiScenarios(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.SCENARIO_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> scenarioCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        List<TestPlanApiScenario> testPlanApiScenarios = exttestPlanApiScenarioMapper.getPlanScenarioCaseNotDeletedByCollectionIds(scenarioCollectIds);
        return testPlanApiScenarios.stream().collect(Collectors.groupingBy(TestPlanApiScenario::getTestPlanCollectionId));
    }

    private Map<String, List<TestPlanApiCase>> getTestPlanApiCases(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.API_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> apiCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        List<TestPlanApiCase> testPlanApiCases = extTestPlanApiCaseMapper.getPlanApiCaseNotDeletedByCollectionIds(apiCollectIds);
        return testPlanApiCases.stream().collect(Collectors.groupingBy(TestPlanApiCase::getTestPlanCollectionId));

    }

    private Map<String, List<TestPlanFunctionalCase>> getTestPlanFunctionalCases(Map<String, List<TestPlanCollectionConfigDTO>> typeChildren) {
        List<TestPlanCollectionConfigDTO> testPlanCollectionConfigDTOS = typeChildren.get(CaseType.FUNCTIONAL_CASE.getKey());
        if (CollectionUtils.isEmpty(testPlanCollectionConfigDTOS)) {
            return new HashMap<>();
        }
        List<String> functionalCollectIds = testPlanCollectionConfigDTOS.stream().map(TestPlanCollectionConfigDTO::getId).toList();
        List<TestPlanFunctionalCase> testPlanFunctionalCases = extTestPlanFunctionalCaseMapper.getPlanCaseNotDeletedByCollectionIds(functionalCollectIds);
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
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList, false);
        TestPlanCollectionMinderTreeDTO envTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO envTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(envTreeNodeDTO, StringUtils.equalsIgnoreCase(planCollection.getEnvironmentId(), ModuleConstants.ROOT_NODE_PARENT_ID) ? Translator.get("api_report_default_env") : planCollection.getEnvName(), "test_plan.mind.environment", envTreeDTO, endList, false);
        TestPlanCollectionMinderTreeDTO poolTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO poolTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(poolTreeNodeDTO, planCollection.getPoolName(), "test_plan.mind.test_resource_pool", poolTreeDTO, endList, planCollection.isNoResourcePool());
    }

    private static void buildChild(TestPlanCollectionMinderTreeNodeDTO treeNodeDTO, String text, String key, TestPlanCollectionMinderTreeDTO treeDTO, List<TestPlanCollectionMinderTreeDTO> endList, boolean noResourcePool) {
        treeNodeDTO.setText(text);
        treeNodeDTO.setResource(List.of(Translator.get(key)));
        if (noResourcePool) {
            treeNodeDTO.setPriority(0);
        }
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
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList, false);
        TestPlanCollectionMinderTreeDTO envTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO envTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(envTreeNodeDTO, StringUtils.equalsIgnoreCase(planCollection.getEnvironmentId(), ModuleConstants.ROOT_NODE_PARENT_ID) ? Translator.get("api_report_default_env") : planCollection.getEnvName(), "test_plan.mind.environment", envTreeDTO, endList, false);
        TestPlanCollectionMinderTreeDTO poolTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO poolTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(poolTreeNodeDTO, planCollection.getPoolName(), "test_plan.mind.test_resource_pool", poolTreeDTO, endList, planCollection.isNoResourcePool());
    }

    private static void buildFunctionalChild(Map<String, List<TestPlanFunctionalCase>> testPlanFunctionalCaseMap, TestPlanCollectionConfigDTO planCollection, List<TestPlanCollectionMinderTreeDTO> endList) {
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMap.get(planCollection.getId());
        int count = 0;
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            count = testPlanFunctionalCases.size();
        }
        TestPlanCollectionMinderTreeDTO countTreeDTO = new TestPlanCollectionMinderTreeDTO();
        TestPlanCollectionMinderTreeNodeDTO countTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        buildChild(countTreeNodeDTO, count + Translator.get("test_plan.mind.strip"), "test_plan.mind.case_count", countTreeDTO, endList, false);
    }

    @NotNull
    private static TestPlanCollectionMinderTreeNodeDTO buildRoot() {
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        testPlanCollectionMinderTreeNodeDTO.setId(ModuleConstants.DEFAULT_NODE_ID);
        testPlanCollectionMinderTreeNodeDTO.setText(Translator.get("test_plan.mind.test_plan"));
        testPlanCollectionMinderTreeNodeDTO.setResource(new ArrayList<>());
        return testPlanCollectionMinderTreeNodeDTO;
    }


    public void editMindTestPlanCase(TestPlanCollectionMinderEditRequest request, SessionUser user) {
        Map<String, List<BaseCollectionAssociateRequest>> associateMap = new HashMap<>();
        //处理新增与更新
        dealEditList(request, user.getId(), associateMap);
        //处理关联关系
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> {
            v.associateCollection(request.getPlanId(), associateMap, user);
        });
    }

    private void dealEditList(TestPlanCollectionMinderEditRequest request, String userId, Map<String, List<BaseCollectionAssociateRequest>> associateMap) {
        if (CollectionUtils.isNotEmpty(request.getEditList())) {
            // 根节点直接过滤后存在直接处理串/并行参数, 后续不一起处理
            request.getEditList().stream()
                    .filter(minderNode -> StringUtils.equals(minderNode.getId(), ModuleConstants.DEFAULT_NODE_ID))
                    .findFirst()
                    .ifPresent(rootNode -> {
                        String executeMethod = rootNode.getExecuteMethod();
                        TestPlanConfig config = new TestPlanConfig();
                        config.setTestPlanId(request.getPlanId());
                        config.setCaseRunMode(executeMethod);
                        testPlanConfigMapper.updateByPrimaryKeySelective(config);
                    });
            request.getEditList().removeIf(minderNode -> StringUtils.equals(minderNode.getId(), ModuleConstants.DEFAULT_NODE_ID));
            Map<String, List<TestPlanCollection>> parentMap = getParentMap(request);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            TestPlanCollectionMapper collectionMapper = sqlSession.getMapper(TestPlanCollectionMapper.class);
            // 临时的节点
            Map<String, String> tempCollectionMap = Maps.newHashMapWithExpectedSize(8);
            //新增
            Map<String, List<String>> addTypeNameMap = dealAddList(request, userId, associateMap, parentMap, collectionMapper, tempCollectionMap);
            //更新
            Map<String, List<String>> updateTypeNameMap = dealUpdateList(request, userId, associateMap, parentMap, collectionMapper, tempCollectionMap);
            //检查同一类型名称重复
            checkNameRepeat(updateTypeNameMap, addTypeNameMap);
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private static void checkNameRepeat(Map<String, List<String>> updateTypeNameMap, Map<String, List<String>> addTypeNameMap) {
        updateTypeNameMap.forEach((k, v) -> {
            List<String> nameList = addTypeNameMap.get(k);
            if (CollectionUtils.isNotEmpty(nameList)) {
                List<String> repeatList = v.stream().filter(nameList::contains).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(repeatList)) {
                    throw new MSException(Translator.get("test_plan.mind.collection_name_repeat"));
                }
            }
        });
    }

    private Map<String, List<String>> dealUpdateList(TestPlanCollectionMinderEditRequest request, String userId, Map<String, List<BaseCollectionAssociateRequest>> associateMap,
                                                     Map<String, List<TestPlanCollection>> parentMap, TestPlanCollectionMapper collectionMapper, Map<String, String> tempCollectionMap) {
        List<TestPlanCollectionMinderEditDTO> updateList = request.getEditList().stream().filter(t -> !t.getTempCollectionNode() && StringUtils.isNotBlank(t.getId())).toList();
        Map<String, List<String>> typeNamesMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(updateList)) {
            //处理删除
            deleteCollection(updateList, request.getPlanId());
            //处理更新
            for (TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO : updateList) {
                TestPlanCollection testPlanCollection = updateCollection(request, userId, testPlanCollectionMinderEditDTO, parentMap, collectionMapper);
                checkChangeDataRepeat(typeNamesMap, testPlanCollectionMinderEditDTO);
                setAssociateMap(testPlanCollectionMinderEditDTO, associateMap, testPlanCollection, tempCollectionMap);
            }
        }
        return typeNamesMap;
    }

    private void deleteCollection(List<TestPlanCollectionMinderEditDTO> updateList, String planId) {
        List<String> existIds = updateList.stream().map(TestPlanCollectionMinderEditDTO::getId).toList();
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andIdNotIn(existIds).andTestPlanIdEqualTo(planId);
        List<TestPlanCollection> collections = testPlanCollectionMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(collections)) {
            List<String> deletedIds = collections.stream().map(TestPlanCollection::getId).toList();
            testPlanService.deletePlanCollectionResource(deletedIds);
        }
    }

    private Map<String, List<String>> dealAddList(TestPlanCollectionMinderEditRequest request, String userId, Map<String, List<BaseCollectionAssociateRequest>> associateMap,
                                                  Map<String, List<TestPlanCollection>> parentMap, TestPlanCollectionMapper collectionMapper, Map<String, String> tempCollectionMap) {
        Map<String, List<String>> typeNamesMap = new HashMap<>();
        List<TestPlanCollectionMinderEditDTO> addList = request.getEditList().stream().filter(t -> t.getTempCollectionNode() && t.getLevel() == 2).collect(Collectors.toList());
        // 接口/场景 可能存在临时节点, 排序后先入库处理
        Collections.reverse(addList);
        if (CollectionUtils.isNotEmpty(addList)) {
            for (TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO : addList) {
                TestPlanCollection testPlanCollection = addCollection(request, userId, testPlanCollectionMinderEditDTO, parentMap, collectionMapper, tempCollectionMap);
                checkChangeDataRepeat(typeNamesMap, testPlanCollectionMinderEditDTO);
                setAssociateMap(testPlanCollectionMinderEditDTO, associateMap, testPlanCollection, tempCollectionMap);
            }
        }
        return typeNamesMap;
    }

    private static void checkChangeDataRepeat(Map<String, List<String>> typeNamesMap, TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO) {
        List<String> nameList = typeNamesMap.get(testPlanCollectionMinderEditDTO.getType());
        if (CollectionUtils.isNotEmpty(nameList)) {
            if (nameList.contains(testPlanCollectionMinderEditDTO.getText())) {
                throw new MSException(Translator.get("test_plan.mind.collection_name_repeat"));
            } else {
                if (testPlanCollectionMinderEditDTO.getLevel() == 2) {
                    nameList.add(testPlanCollectionMinderEditDTO.getText());
                    typeNamesMap.put(testPlanCollectionMinderEditDTO.getType(), nameList);
                }
            }
        } else {
            if (testPlanCollectionMinderEditDTO.getLevel() == 2) {
                nameList = new ArrayList<>(List.of(testPlanCollectionMinderEditDTO.getText()));
                typeNamesMap.put(testPlanCollectionMinderEditDTO.getType(), nameList);
            }
        }
    }

    @NotNull
    private Map<String, List<TestPlanCollection>> getParentMap(TestPlanCollectionMinderEditRequest request) {
        List<TestPlanCollectionMinderEditDTO> list = request.getEditList().stream().filter(t -> StringUtils.isNotBlank(t.getId()) && t.getLevel() == 1).toList();
        List<TestPlanCollection> parentList = new ArrayList<>();
        for (TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO : list) {
            TestPlanCollection testPlanCollection = new TestPlanCollection();
            BeanUtils.copyBean(testPlanCollection, testPlanCollectionMinderEditDTO);
            testPlanCollection.setName(testPlanCollectionMinderEditDTO.getText());
            testPlanCollection.setParentId(CommonConstants.DEFAULT_NULL_VALUE);
            parentList.add(testPlanCollection);
        }
        return parentList.stream().collect(Collectors.groupingBy(TestPlanCollection::getType));
    }

    @NotNull
    private static TestPlanCollection updateCollection(TestPlanCollectionMinderEditRequest request, String userId, TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO, Map<String, List<TestPlanCollection>> parentMap, TestPlanCollectionMapper collectionMapper) {
        TestPlanCollection testPlanCollection = new TestPlanCollection();
        TestPlanCollection parent = parentMap.get(testPlanCollectionMinderEditDTO.getType()).getFirst();
        if (testPlanCollectionMinderEditDTO.getExtended()) {
            BeanUtils.copyBean(testPlanCollection, parent);
        } else {
            BeanUtils.copyBean(testPlanCollection, testPlanCollectionMinderEditDTO);
        }
        if (StringUtils.equalsIgnoreCase(parent.getId(), testPlanCollectionMinderEditDTO.getId())) {
            testPlanCollection.setParentId(parent.getParentId());
        } else {
            testPlanCollection.setParentId(parent.getId());
        }
        if (testPlanCollectionMinderEditDTO.getText().length() > 255) {
            testPlanCollectionMinderEditDTO.setText(testPlanCollectionMinderEditDTO.getText().substring(0, 249));
        }
        testPlanCollection.setName(testPlanCollectionMinderEditDTO.getText());
        testPlanCollection.setTestPlanId(request.getPlanId());
        testPlanCollection.setType(testPlanCollectionMinderEditDTO.getType());
        testPlanCollection.setId(testPlanCollectionMinderEditDTO.getId());
        testPlanCollection.setCreateUser(userId);
        testPlanCollection.setCreateTime(null);
        testPlanCollection.setPos((testPlanCollectionMinderEditDTO.getNum() + 1) * 4096);
        collectionMapper.updateByPrimaryKeySelective(testPlanCollection);
        return testPlanCollection;
    }

    @NotNull
    private static TestPlanCollection addCollection(TestPlanCollectionMinderEditRequest request, String userId, TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO,
                                                    Map<String, List<TestPlanCollection>> parentMap, TestPlanCollectionMapper collectionMapper, Map<String, String> tempCollectionMap) {
        List<TestPlanCollection> testPlanCollections = parentMap.get(testPlanCollectionMinderEditDTO.getType());
        TestPlanCollection parent = testPlanCollections.getFirst();
        TestPlanCollection testPlanCollection = new TestPlanCollection();
        if (testPlanCollectionMinderEditDTO.getExtended()) {
            BeanUtils.copyBean(testPlanCollection, parent);
        } else {
            BeanUtils.copyBean(testPlanCollection, testPlanCollectionMinderEditDTO);
        }
        testPlanCollection.setParentId(parent.getId());
        if (testPlanCollectionMinderEditDTO.getText().length() > 255) {
            testPlanCollectionMinderEditDTO.setText(testPlanCollectionMinderEditDTO.getText().substring(0, 249));
        }
        testPlanCollection.setName(testPlanCollectionMinderEditDTO.getText());
        testPlanCollection.setId(IDGenerator.nextStr());
        testPlanCollection.setTestPlanId(request.getPlanId());
        testPlanCollection.setCreateUser(userId);
        testPlanCollection.setCreateTime(System.currentTimeMillis());
        testPlanCollection.setPos((testPlanCollectionMinderEditDTO.getNum() + 1) * 4096);
        collectionMapper.insert(testPlanCollection);
        // 维护新增临时节点 (接口/场景)
        if (StringUtils.equalsAny(testPlanCollectionMinderEditDTO.getType(), CaseType.API_CASE.getKey(), CaseType.SCENARIO_CASE.getKey())) {
            tempCollectionMap.put(testPlanCollectionMinderEditDTO.getId(), testPlanCollection.getId());
        }
        return testPlanCollection;
    }

    private static void setAssociateMap(TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO, Map<String, List<BaseCollectionAssociateRequest>> associateMap,
                                        TestPlanCollection testPlanCollection, Map<String, String> tempCollectionMap) {
        List<TestPlanCollectionAssociateDTO> associateDTOS = testPlanCollectionMinderEditDTO.getAssociateDTOS();
        if (CollectionUtils.isEmpty(associateDTOS)) {
            return;
        }
        for (TestPlanCollectionAssociateDTO associateDTO : associateDTOS) {
            String associateType = associateDTO.getAssociateType();
            List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests = associateMap.get(associateType);
            if (baseCollectionAssociateRequests == null) {
                baseCollectionAssociateRequests = new ArrayList<>();
                addAssociate(associateDTO, testPlanCollection, baseCollectionAssociateRequests, associateMap, associateType, tempCollectionMap);
            } else {
                addAssociate(associateDTO, testPlanCollection, baseCollectionAssociateRequests, associateMap, associateType, tempCollectionMap);
            }
        }
    }

    private static void addAssociate(TestPlanCollectionAssociateDTO associateDTO, TestPlanCollection testPlanCollection, List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests,
                                     Map<String, List<BaseCollectionAssociateRequest>> associateMap, String associateType, Map<String, String> tempCollectionMap) {
        BaseCollectionAssociateRequest baseCollectionAssociateRequest = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest.setCollectionId(testPlanCollection.getId());
        baseCollectionAssociateRequest.setModules(associateDTO);
        // 临时 接口/场景 同步节点ID需要替换
        if (tempCollectionMap.containsKey(associateDTO.getApiCaseCollectionId())) {
            associateDTO.setApiCaseCollectionId(tempCollectionMap.get(associateDTO.getApiCaseCollectionId()));
        }
        if (tempCollectionMap.containsKey(associateDTO.getApiScenarioCollectionId())) {
            associateDTO.setApiScenarioCollectionId(tempCollectionMap.get(associateDTO.getApiScenarioCollectionId()));
        }
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        associateMap.put(associateType, baseCollectionAssociateRequests);
    }

}
