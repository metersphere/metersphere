package io.metersphere.api.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.service.NodeTreeService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiModuleService extends NodeTreeService<ApiModuleDTO> {

    @Resource
    ApiModuleMapper apiModuleMapper;
    @Resource
    ExtApiModuleMapper extApiModuleMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private TestPlanProjectService testPlanProjectService;
    @Resource
    private ProjectService projectService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    public ApiModuleService() {
        super(ApiModuleDTO.class);
    }

    public ApiModule get(String id) {
        return apiModuleMapper.selectByPrimaryKey(id);
    }


    public List<ApiModuleDTO> getApiModulesByProjectAndPro(String projectId, String protocol) {
        return extApiModuleMapper.getNodeTreeByProjectId(projectId, protocol);
    }

    public List<ApiModuleDTO> getTrashNodeTreeByProtocolAndProjectId(String projectId, String protocol, String versionId) {
        //回收站数据初始化：检查是否存在模块被删除的接口，则把接口挂再默认节点上
        initTrashDataModule(projectId, protocol, versionId);
        //通过回收站里的接口模块进行反显
        Map<String, List<ApiDefinition>> trashApiMap = apiDefinitionService.selectApiBaseInfoGroupByModuleId(projectId, protocol, versionId, ScenarioStatus.Trash.name());
        //查找回收站里的模块
        List<ApiModuleDTO> trashModuleList = this.selectTreeStructModuleById(trashApiMap.keySet());
        this.initApiCount(trashModuleList, trashApiMap);
        return getNodeTrees(trashModuleList);
    }

    private void initApiCount(List<ApiModuleDTO> apiModules, Map<String, List<ApiDefinition>> trashApiMap) {
        if (CollectionUtils.isNotEmpty(apiModules) && MapUtils.isNotEmpty(trashApiMap)) {
            apiModules.forEach(node -> {
                List<String> moduleIds = new ArrayList<>();
                moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
                moduleIds.add(node.getId());
                int countNum = 0;
                for (String moduleId : moduleIds) {
                    if (trashApiMap.containsKey(moduleId)) {
                        countNum += trashApiMap.get(moduleId).size();
                    }
                }
                node.setCaseNum(countNum);
            });
        }
    }

    private List<ApiModuleDTO> selectTreeStructModuleById(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        } else {
            List<String> parentIdList = new ArrayList<>();
            List<ApiModuleDTO> apiModuleList = extApiModuleMapper.selectByIds(ids);
            apiModuleList.forEach(apiModuleDTO -> {
                if (StringUtils.isNotBlank(apiModuleDTO.getParentId()) && !parentIdList.contains(apiModuleDTO.getParentId())) {
                    parentIdList.add(apiModuleDTO.getParentId());
                }
            });
            apiModuleList.addAll(0, this.selectTreeStructModuleById(parentIdList));
            List<ApiModuleDTO> returnList = new ArrayList<>(apiModuleList.stream().collect(Collectors.toMap(ApiModuleDTO::getId, Function.identity(), (t1, t2) -> t1)).values());
            return returnList;
        }
    }

    private void initTrashDataModule(String projectId, String protocol, String versionId) {
        ApiModule defaultModule = this.getDefaultNode(projectId, protocol);
        if (defaultModule != null) {
            apiDefinitionService.updateNoModuleApiToDefaultModule(projectId, protocol, ScenarioStatus.Trash.name(), versionId, defaultModule.getId());
        }
    }

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol, String versionId) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNode(projectId, protocol);
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        List<ApiModuleDTO> apiModules = getApiModulesByProjectAndPro(projectId, protocol);
        request.setProjectId(projectId);
        request.setProtocol(protocol);
        List<String> list = new ArrayList<>();
        list.add("Prepare");
        list.add("Underway");
        list.add("Completed");
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);

        //优化： 所有统计SQL一次查询出来
        List<String> allModuleIdList = new ArrayList<>();
        for (ApiModuleDTO node : apiModules) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            for (String moduleId : moduleIds) {
                if (!allModuleIdList.contains(moduleId)) {
                    allModuleIdList.add(moduleId);
                }
            }
        }
        request.setModuleIds(allModuleIdList);
        if (StringUtils.isNotBlank(versionId)) {
            request.setVersionId(versionId);
        }
        List<Map<String, Object>> moduleCountList = extApiDefinitionMapper.moduleCountByCollection(request);
        Map<String, Integer> moduleCountMap = this.parseModuleCountList(moduleCountList);
        apiModules.forEach(node -> {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            int countNum = 0;
            for (String moduleId : moduleIds) {
                if (moduleCountMap.containsKey(moduleId)) {
                    countNum += moduleCountMap.get(moduleId).intValue();
                }
            }
            node.setCaseNum(countNum);
        });
        return getNodeTrees(apiModules);
    }

    private Map<String, Integer> parseModuleCountList(List<Map<String, Object>> moduleCountList) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (Map<String, Object> map : moduleCountList) {
            Object moduleIdObj = map.get("moduleId");
            Object countNumObj = map.get("countNum");
            if (moduleIdObj != null && countNumObj != null) {
                String moduleId = String.valueOf(moduleIdObj);
                try {
                    Integer countNumInteger = Integer.parseInt(String.valueOf(countNumObj));
                    returnMap.put(moduleId, countNumInteger);
                } catch (Exception e) {
                    LogUtil.error("method parseModuleCountList has error:", e);
                }
            }
        }
        return returnMap;
    }

    public static List<String> nodeList(List<ApiModuleDTO> apiNodes, String pid, List<String> list) {
        for (ApiModuleDTO node : apiNodes) {
            //遍历出父id等于参数的id，add进子节点集合
            if (StringUtils.equals(node.getParentId(), pid)) {
                list.add(node.getId());
                //递归遍历下一级
                nodeList(apiNodes, node.getId(), list);
            }
        }
        return list;
    }

    public String addNode(ApiModule node) {
        validateNode(node);
        return addNodeWithoutValidate(node);
    }

    public double getNextLevelPos(String projectId, int level, String parentId) {
        List<ApiModule> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<ApiModule> getPos(String projectId, int level, String parentId, String order) {
        ApiModuleExample example = new ApiModuleExample();
        ApiModuleExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return apiModuleMapper.selectByExample(example);
    }

    public String addNodeWithoutValidate(ApiModule node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        if (StringUtils.isBlank(node.getCreateUser())) {
            node.setCreateUser(SessionUtils.getUserId());
        }
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        apiModuleMapper.insertSelective(node);
        return node.getId();
    }

    public List<ApiModuleDTO> getNodeByPlanId(String planId, String protocol) {
        List<ApiModuleDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        projectIds.forEach(id -> {
            Project project = projectService.getProjectById(id);
            String name = project.getName();
            List<ApiModuleDTO> nodeList = getNodeDTO(id, planId, protocol);
            ApiModuleDTO apiModuleDTO = new ApiModuleDTO();
            apiModuleDTO.setId(project.getId());
            apiModuleDTO.setName(name);
            apiModuleDTO.setLabel(name);
            apiModuleDTO.setChildren(nodeList);
            if (!org.springframework.util.CollectionUtils.isEmpty(nodeList)) {
                list.add(apiModuleDTO);
            }
        });
        return list;
    }

    private List<ApiModuleDTO> getNodeDTO(String projectId, String planId, String protocol) {
        List<TestPlanApiCase> apiCases = testPlanApiCaseService.getCasesByPlanId(planId);
        if (apiCases.isEmpty()) {
            return null;
        }
        List<ApiModuleDTO> testCaseNodes = getApiModulesByProjectAndPro(projectId, protocol);

        List<String> caseIds = apiCases.stream()
                .map(TestPlanApiCase::getApiCaseId)
                .collect(Collectors.toList());

        List<String> definitionIds = apiTestCaseService.selectCasesBydIds(caseIds).stream()
                .filter(apiTestCase -> apiTestCase.getStatus() == null || !CommonConstants.TrashStatus.equals(apiTestCase.getStatus()))
                .map(ApiTestCase::getApiDefinitionId)
                .collect(Collectors.toList());

        List<String> dataNodeIds = apiDefinitionService.selectApiDefinitionBydIds(definitionIds).stream()
                .filter(apiDefinition -> apiDefinition.getStatus() == null || !CommonConstants.TrashStatus.equals(apiDefinition.getStatus()))
                .map(ApiDefinition::getModuleId)
                .collect(Collectors.toList());

        List<ApiModuleDTO> nodeTrees = getNodeTrees(testCaseNodes);

        Iterator<ApiModuleDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ApiModuleDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }


    public ApiModule getNewModule(String name, String projectId, int level) {
        ApiModule node = new ApiModule();
        buildNewModule(node);
        node.setLevel(level);
        node.setName(name);
        node.setProjectId(projectId);
        return node;
    }

    public ApiModule buildNewModule(ApiModule node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        return node;
    }

    private void validateNode(ApiModule node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            MSException.throwException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkApiModuleExist(node);
    }

    private void checkApiModuleExist(ApiModule node) {
        if (node.getName() != null) {
            ApiModuleExample example = new ApiModuleExample();
            ApiModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName())
                    .andProjectIdEqualTo(node.getProjectId());

            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andLevelEqualTo(node.getLevel());
            }

            if (StringUtils.isNotBlank(node.getProtocol())) {
                criteria.andProtocolEqualTo(node.getProtocol());
            }

            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (apiModuleMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists") + ": " + node.getName());
            }
        }
    }

    public List<ApiModule> selectSameModule(ApiModule node) {
        ApiModuleExample example = new ApiModuleExample();
        ApiModuleExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(node.getName())
                .andProjectIdEqualTo(node.getProjectId())
                .andLevelEqualTo(node.getLevel());

        if (StringUtils.isNotBlank(node.getId())) {
            criteria.andIdNotEqualTo(node.getId());
        }
        if (StringUtils.isNotEmpty(node.getProtocol())) {
            criteria.andProtocolEqualTo(node.getProtocol());
        }
        //同一个模块下不能有相同名字的子模块
        if (StringUtils.isNotBlank(node.getParentId())) {
            criteria.andParentIdEqualTo(node.getParentId());
        }
        return apiModuleMapper.selectByExample(example);
    }

    private List<ApiDefinitionResult> queryByModuleIds(List<String> nodeIds) {
        ApiDefinitionRequest apiDefinitionRequest = new ApiDefinitionRequest();
        apiDefinitionRequest.setModuleIds(nodeIds);
        return extApiDefinitionMapper.list(apiDefinitionRequest);
    }

    public int editNode(DragModuleRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkApiModuleExist(request);
        List<ApiDefinitionResult> apiDefinitionResults = queryByModuleIds(request.getNodeIds());
        if (CollectionUtils.isNotEmpty(apiDefinitionResults)) {
            apiDefinitionResults.forEach(apiDefinition -> {
                if (apiDefinition != null && StringUtils.isNotBlank(apiDefinition.getModulePath())) {
                    StringBuilder path = new StringBuilder(apiDefinition.getModulePath());
                    List<String> pathLists = Arrays.asList(path.toString().split("/"));
                    if (pathLists.size() > request.getLevel()) {
                        pathLists.set(request.getLevel(), request.getName());
                        path.delete(0, path.length());
                        for (int i = 1; i < pathLists.size(); i++) {
                            path = path.append("/").append(pathLists.get(i));
                        }
                        apiDefinition.setModulePath(path.toString());
                    }
                }
            });
            batchUpdateApiDefinition(apiDefinitionResults);
        }
        return apiModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            //删除case
            ApiTestCaseRequest request = new ApiTestCaseRequest();
            request.setIds(nodeIds);
            request.setDeleteUserId(SessionUtils.getUserId());
            request.setDeleteTime(System.currentTimeMillis());
            extApiTestCaseMapper.deleteCaseToGc(request);
            //删除api
            ApiDefinitionRequest apiDefinitionRequest = new ApiDefinitionRequest();
            apiDefinitionRequest.setIds(nodeIds);
            apiDefinitionRequest.setDeleteUserId(SessionUtils.getUserId());
            apiDefinitionRequest.setDeleteTime(System.currentTimeMillis());
            extApiDefinitionMapper.deleteApiToGc(apiDefinitionRequest);

            ApiModuleExample apiDefinitionNodeExample = new ApiModuleExample();
            apiDefinitionNodeExample.createCriteria().andIdIn(nodeIds);
            return apiModuleMapper.deleteByExample(apiDefinitionNodeExample);
        }
        return 0;
    }

    private void batchUpdateApiDefinition(List<ApiDefinitionResult> apiModule) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper apiDefinitionMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        apiModule.forEach((value) -> {
            apiDefinitionMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    @Override
    public ApiModuleDTO getNode(String id) {
        ApiModule module = apiModuleMapper.selectByPrimaryKey(id);
        if (module == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(module), ApiModuleDTO.class);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extApiModuleMapper.updatePos(id, pos);
    }

    public void dragNode(DragModuleRequest request) {

        checkApiModuleExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<ApiDefinitionResult> apiModule = queryByModuleIds(nodeIds);

        ApiModuleDTO nodeTree = request.getNodeTree();

        List<ApiModule> updateNodes = new ArrayList<>();
        if (nodeTree == null) {
            return;
        }
        buildUpdateDefinition(nodeTree, apiModule, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateModule(updateNodes);

        batchUpdateApiDefinition(apiModule);
    }

    private void buildUpdateDefinition(ApiModuleDTO rootNode, List<ApiDefinitionResult> apiDefinitions,
                                       List<ApiModule> updateNodes, String rootPath, String pId, int level) {
        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }
        if ("root".equals(rootNode.getId())) {
            rootPath = "";
        }
        ApiModule apiDefinitionNode = new ApiModule();
        apiDefinitionNode.setId(rootNode.getId());
        apiDefinitionNode.setLevel(level);
        apiDefinitionNode.setParentId(pId);
        updateNodes.add(apiDefinitionNode);

        for (ApiDefinitionResult item : apiDefinitions) {
            if (StringUtils.equals(item.getModuleId(), rootNode.getId())) {
                item.setModulePath(rootPath);
            }
        }

        List<ApiModuleDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateDefinition(children.get(i), apiDefinitions, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    private void batchUpdateModule(List<ApiModule> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);
        updateNodes.forEach((value) -> {
            apiModuleMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public String getLogDetails(List<String> ids) {
        ApiModuleExample example = new ApiModuleExample();
        ApiModuleExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        List<ApiModule> nodes = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(ApiModule::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ApiModule node) {
        ApiModule module = null;
        if (StringUtils.isNotEmpty(node.getId())) {
            module = apiModuleMapper.selectByPrimaryKey(node.getId());
        }
        if (module == null && StringUtils.isNotEmpty(node.getName())) {
            ApiModuleExample example = new ApiModuleExample();
            ApiModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotEmpty(node.getProtocol())) {
                criteria.andProtocolEqualTo(node.getProtocol());
            }
            if (StringUtils.isNotEmpty(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotEmpty(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            List<ApiModule> list = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(list)) {
                module = list.get(0);
            }
        }
        if (module != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(module, ModuleReference.moduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(module.getId()), module.getProjectId(), module.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public long countById(String nodeId) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andIdEqualTo(nodeId);
        return apiModuleMapper.countByExample(example);
    }

    public ApiModule getDefaultNode(String projectId, String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo("未规划接口").andParentIdIsNull();
        List<ApiModule> list = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            ApiModule record = new ApiModule();
            record.setId(UUID.randomUUID().toString());
            record.setName("未规划接口");
            record.setProtocol(protocol);
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            record.setCreateUser(SessionUtils.getUserId());
            apiModuleMapper.insert(record);
            return record;
        } else {
            return list.get(0);
        }
    }

    public ApiModule getDefaultNodeUnCreateNew(String projectId, String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo("未规划接口").andParentIdIsNull();
        List<ApiModule> list = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public long countTrashApiData(String projectId, String protocol) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andStatusEqualTo("Trash");
        return extApiDefinitionMapper.countByExample(example);
    }

    public String getModuleNameById(String moduleId) {
        return extApiModuleMapper.getNameById(moduleId);
    }

    public Map<String, String> getApiModuleNameDicByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedHashMap<>(0);
        }
        Map<String, String> returnMap = new LinkedHashMap<>(ids.size());
        List<ApiModuleDTO> apiModuleList = extApiModuleMapper.selectNameByIds(ids);
        apiModuleList.forEach(item -> {
            returnMap.put(item.getId(), item.getName());
        });
        return returnMap;
    }

    /**
     * 上传文件时对文件的模块进行检测
     *
     * @param data         文件转化后的数据
     * @param fullCoverage 是否覆盖接口
     * @return Return to the newly added module list and api list
     */
    public UpdateApiModuleDTO checkApiModule(ApiTestImportRequest request, ApiDefinitionImport apiImport, List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, boolean urlRepeat) {

        String projectId = request.getProjectId();
        String protocol = request.getProtocol();

        fullCoverage = getFullCoverage(apiImport, fullCoverage);

        //获取当前项目的当前协议下的所有模块的Tree
        List<ApiModuleDTO> apiModules = this.getApiModulesByProjectAndPro(projectId, protocol);
        List<ApiModuleDTO> nodeTreeByProjectId = this.getNodeTrees(apiModules);

        //所有模块的ID 及其自身 的map
        Map<String, ApiModuleDTO> idModuleMap = apiModules.stream().collect(Collectors.toMap(ApiModuleDTO::getId, apiModuleDTO -> apiModuleDTO));

        //父级ID与其子模块集合的map
        Map<String, List<ApiModule>> pidChildrenMap = new HashMap<>();
        //所有模块的ID 及其全路径的map
        Map<String, String> idPathMap = new HashMap<>();

        String initParentModulePath = "/root";
        Map<String, String> initParentModulePathMap = new HashMap<>();
        initParentModulePathMap.put("root", initParentModulePath);
        buildProcessData(nodeTreeByProjectId, pidChildrenMap, idPathMap, initParentModulePathMap);

        //导入的case,导入的接口是有ID的，所以导入的case已经标记过一遍接口ID了,这里是处理覆盖时接口ID的变动
        List<ApiTestCaseWithBLOBs> importCases = apiImport.getCases();

        if (protocol.equals("HTTP")) {
            return dealHttp(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, urlRepeat, importCases);
        } else {
            Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap = apiImport.getEsbApiParamsMap();
            return delOtherProtocol(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, importCases, esbApiParamsMap);
        }

    }

    private UpdateApiModuleDTO delOtherProtocol(List<ApiDefinitionWithBLOBs> data,
                                                Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap,
                                                Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request,
                                                Boolean fullCoverage, List<ApiTestCaseWithBLOBs> importCases, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        List<ApiDefinitionWithBLOBs> optionDatas = new ArrayList<>();
        //系统原有的需要更新的list，
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //去重，TCP,SQL,DUBBO 模块下名称唯一
        removeRepeatOrigin(data, fullCoverage, optionDatas);
        //上传文件时选的模块ID
        String chooseModuleId = request.getModuleId();
        //获取选中的模块
        ApiModuleDTO chooseModule = null;
        if (chooseModuleId != null) {
            chooseModule = idModuleMap.get(chooseModuleId);
        }
        List<ApiTestCaseWithBLOBs> optionDataCases = new ArrayList<>();
        //将ID,num全部置于null,覆盖的时候会增加ID，用以区分更新还是新增，处理导入文件里的重复的case，如果覆盖，则取重复的最后一个，否则取第一个
        removeRepeatCase(fullCoverage, importCases, optionDataCases);
        //需要新增的模块，key 为模块路径
        Map<String, ApiModule> moduleMap = new HashMap<>();
        //处理模块
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionDatas, chooseModule);

        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = getApiDefinitionWithBLOBsList(request, optionDatas);
        //重复接口的case
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs);
        }
        Map<String, ApiDefinitionWithBLOBs> repeatDataMap = new HashMap<>();
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();

        if (chooseModule != null) {
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                String chooseModuleParentId = getChooseModuleParentId(chooseModule);
                String chooseModulePath = getChooseModulePath(idPathMap, chooseModule, chooseModuleParentId);
                if (fullCoverage) {
                    List<ApiDefinitionWithBLOBs> singleOptionDatas = new ArrayList<>();
                    removeOtherChooseModuleRepeat(optionDatas, singleOptionDatas, chooseModulePath);
                    optionDatas = singleOptionDatas;
                    optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName().concat(chooseModulePath), api -> api));
                } else {
                    getNoHChooseModuleUrlRepeatOptionMap(optionDatas, optionMap, chooseModulePath);
                }
                repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModuleId)).collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
            }
        } else {
            buildOptionMap(optionDatas, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
        }
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        //处理数据
        if (fullCoverage) {
            if (fullCoverageApi) {
                coverModule(toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, esbApiParamsMap);
            } else {
                moduleMap = cover(moduleMap, toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, esbApiParamsMap);
            }
        } else {
            //不覆盖
            removeRepeat(optionDatas, optionMap, repeatDataMap, moduleMap, versionId, optionDataCases);
        }

        //系统内检查重复
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
            optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
            if (fullCoverage) {
                cover(moduleMap, toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, esbApiParamsMap);
            } else {
                //不覆盖,同一接口不做更新
                removeRepeat(optionDatas, optionMap, repeatDataMap, moduleMap, versionId, optionDataCases);
            }
        }

        if (optionDatas.isEmpty()) {
            moduleMap = new HashMap<>();
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
        return getUpdateApiModuleDTO(moduleMap, toUpdateList, optionDatas, optionDataCases);
    }

    private void getNoHChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(chooseModulePath), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(chooseModulePath).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private void removeOtherChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionDatas, List<ApiDefinitionWithBLOBs> singleOptionDatas, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionDatas.stream().collect(Collectors.groupingBy(t -> t.getName().concat(chooseModulePath), LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> {
            singleOptionDatas.add(v.get(v.size() - 1));
        });
    }

    private void buildOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName(), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private List<ApiDefinitionWithBLOBs> getApiDefinitionWithBLOBsList(ApiTestImportRequest request, List<ApiDefinitionWithBLOBs> optionDatas) {
        //处理数据
        List<String> nameList = optionDatas.stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
        String projectId = request.getProjectId();
        String protocol = request.getProtocol();
        //获取系统内重复数据
        return extApiDefinitionMapper.selectRepeatByProtocol(nameList, protocol, projectId);
    }

    private UpdateApiModuleDTO dealHttp(List<ApiDefinitionWithBLOBs> data,
                                        Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap,
                                        Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request,
                                        Boolean fullCoverage, boolean urlRepeat, List<ApiTestCaseWithBLOBs> importCases) {

        List<ApiDefinitionWithBLOBs> optionDatas = new ArrayList<>();
        //系统原有的需要更新的list，
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //去重 如果url可重复 则模块+名称+请求方式+路径 唯一，否则 请求方式+路径唯一，
        //覆盖模式留重复的最后一个，不覆盖留第一个
        removeHttpRepeat(data, fullCoverage, urlRepeat, optionDatas);

        //上传文件时选的模块ID
        String chooseModuleId = request.getModuleId();
        //获取选中的模块
        ApiModuleDTO chooseModule = null;
        if (chooseModuleId != null) {
            chooseModule = idModuleMap.get(chooseModuleId);
        }
        List<ApiTestCaseWithBLOBs> optionDataCases = new ArrayList<>();
        //将ID,num全部置于null,覆盖的时候会增加ID，用以区分更新还是新增，处理导入文件里的重复的case，如果覆盖，则取重复的最后一个，否则取第一个
        removeRepeatCase(fullCoverage, importCases, optionDataCases);
        //需要新增的模块，key 为模块路径
        Map<String, ApiModule> moduleMap = new HashMap<>();
        //处理模块
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionDatas, chooseModule);

        if (urlRepeat) {
            optionDatas = dealHttpUrlRepeat(chooseModule, idPathMap, optionDatas, fullCoverage, request, moduleMap, toUpdateList, optionDataCases);
        } else {
            dealHttpUrlNoRepeat(optionDatas, fullCoverage, request, moduleMap, toUpdateList, optionDataCases);
        }

        if (optionDatas.isEmpty()) {
            moduleMap = new HashMap<>();
        }

        return getUpdateApiModuleDTO(moduleMap, toUpdateList, optionDatas, optionDataCases);

    }

    private void dealHttpUrlNoRepeat(List<ApiDefinitionWithBLOBs> optionDatas,
                                     Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap,
                                     List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases) {

        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap;

        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionDatas, projectId);

        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getMethod().concat(t.getPath())));

        //按照原来的顺序
        optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getMethod().concat(t.getPath()), api -> api));

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();

        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs);
        }

        if (fullCoverage) {
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            } else {
                //不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCover(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            }
        } else {
            //不覆盖,同一接口不做更新
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionDatas, moduleMap, versionId, optionDataCases);
            }
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
    }

    private List<ApiDefinitionWithBLOBs> dealHttpUrlRepeat(ApiModuleDTO chooseModule, Map<String, String> idPathMap, List<ApiDefinitionWithBLOBs> optionDatas,
                                                           Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap,
                                                           List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases) {
        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionDatas, projectId);

        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();
        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap;
        //按照原来的顺序
        if (chooseModule != null) {
            //如果有选中的模块，则在选中的模块下过滤 过滤规则是 选择的模块路径+名称+method+path
            String chooseModuleParentId = getChooseModuleParentId(chooseModule);
            String chooseModulePath = getChooseModulePath(idPathMap, chooseModule, chooseModuleParentId);
            //这样的过滤规则下可能存在重复接口，如果是覆盖模块，需要按照去重规则再次去重，否则就加上接口原有的模块
            if (fullCoverage) {
                List<ApiDefinitionWithBLOBs> singleOptionDatas = new ArrayList<>();
                removeHttpChooseModuleRepeat(optionDatas, singleOptionDatas, chooseModulePath);
                optionDatas = singleOptionDatas;
                optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(chooseModulePath), api -> api));
            } else {
                getChooseModuleUrlRepeatOptionMap(optionDatas, optionMap, chooseModulePath);
            }
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModule.getId())).collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
        } else {
            //否则在整个系统中过滤
            getUrlRepeatOptionMap(optionDatas, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
        }
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs);
        }
        //覆盖接口
        if (fullCoverage) {
            //允许覆盖模块，用导入的重复数据的最后一条覆盖查询的所有重复数据; case 在覆盖的时候，是拼接到原来的case，name唯一；不覆盖，就用原来的
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            } else {
                //覆盖但不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    //过滤同一层级重复模块，导入文件没有新增接口无需创建接口模块
                    moduleMap = judgeModuleMap(moduleMap, optionMap, repeatDataMap);
                    startCover(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            }
        } else {
            //不覆盖,同一接口不做更新;可能创建新版本，case也直接创建，
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionDatas, moduleMap, versionId, optionDataCases);
            }
        }
        //最后在整个体统内检查一遍（防止在有选择的模块时，未找到重复，直接创建的情况）
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
            optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath()), api -> api));
            if (fullCoverage) {
                startCover(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
            } else {
                //不覆盖,同一接口不做更新
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    removeSameData(repeatDataMap, optionMap, optionDatas, moduleMap, versionId, optionDataCases);
                }
            }
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
        return optionDatas;
    }

    private void removeHttpChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionDatas, List<ApiDefinitionWithBLOBs> singleOptionDatas, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionDatas.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(chooseModulePath), LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> singleOptionDatas.add(v.get(v.size() - 1)));
    }

    private void getChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(chooseModulePath), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(chooseModulePath).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private void getUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    @NotNull
    private Boolean getFullCoverageApi(ApiTestImportRequest request) {
        Boolean fullCoverageApi = request.getCoverModule();
        if (fullCoverageApi == null) {
            fullCoverageApi = false;
        }
        return fullCoverageApi;
    }

    @NotNull
    private Boolean getFullCoverage(ApiDefinitionImport apiImport, Boolean fullCoverage) {
        if (fullCoverage == null) {
            fullCoverage = false;
        }

        //标准版ESB数据导入不区分是否覆盖，默认都为覆盖
        if (apiImport.getEsbApiParamsMap() != null) {
            fullCoverage = true;
        }
        return fullCoverage;
    }

    private void buildCases(List<ApiTestCaseWithBLOBs> optionDataCases, Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, List<String> caseIds) {
        if (MapUtils.isNotEmpty(oldCaseMap)) {
            List<ApiTestCaseWithBLOBs> oldCaseList = new ArrayList<>();
            Collection<List<ApiTestCaseWithBLOBs>> values = oldCaseMap.values();
            for (List<ApiTestCaseWithBLOBs> value : values) {
                oldCaseList.addAll(value);
            }
            List<ApiTestCaseWithBLOBs> collect = oldCaseList.stream().filter(t -> !caseIds.contains(t.getId())).collect(Collectors.toList());
            optionDataCases.addAll(collect);
        }
    }

    private void removeRepeatCase(Boolean fullCoverage, List<ApiTestCaseWithBLOBs> importCases, List<ApiTestCaseWithBLOBs> optionDataCases) {
        LinkedHashMap<String, List<ApiTestCaseWithBLOBs>> apiIdNameMap = importCases.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getApiDefinitionId()), LinkedHashMap::new, Collectors.toList()));
        if (fullCoverage) {
            apiIdNameMap.forEach((k, v) -> {
                v.get(v.size() - 1).setId(null);
                v.get(v.size() - 1).setNum(null);
                optionDataCases.add(v.get(v.size() - 1));
            });
        } else {
            apiIdNameMap.forEach((k, v) -> {
                v.get(0).setId(null);
                v.get(0).setNum(null);
                optionDataCases.add(v.get(0));
            });
        }
    }

    private Map<String, List<ApiTestCaseWithBLOBs>> getOldCaseMap(List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs) {
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap;
        List<String> definitionIds = repeatApiDefinitionWithBLOBs.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdIn(definitionIds);
        testCaseExample.or(testCaseExample.createCriteria().andStatusNotEqualTo(ScenarioStatus.Trash.name()).andStatusIsNull());
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(testCaseExample);
        oldCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
        return oldCaseMap;
    }


    private UpdateApiModuleDTO getUpdateApiModuleDTO(Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionDatas, List<ApiTestCaseWithBLOBs> optionDataCases) {
        UpdateApiModuleDTO updateApiModuleDTO = new UpdateApiModuleDTO();
        updateApiModuleDTO.setModuleList(new ArrayList<>(moduleMap.values()));
        updateApiModuleDTO.setNeedUpdateList(toUpdateList);
        updateApiModuleDTO.setDefinitionWithBLOBs(optionDatas);
        updateApiModuleDTO.setCaseWithBLOBs(optionDataCases);
        return updateApiModuleDTO;
    }

    private void removeRepeat(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> nameModuleMap,
                              Map<String, ApiDefinitionWithBLOBs> repeatDataMap, Map<String, ApiModule> moduleMap,
                              String versionId,
                              List<ApiTestCaseWithBLOBs> optionDataCases) {
        if (nameModuleMap != null) {
            Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionDatas.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
            repeatDataMap.forEach((k, v) -> {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
                if (apiDefinitionWithBLOBs != null) {
                    Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                    List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
                    String modulePath = apiDefinitionWithBLOBs.getModulePath();
                    List<ApiDefinitionWithBLOBs> moduleDatas = moduleOptionData.get(modulePath);
                    if (moduleDatas != null && moduleDatas.size() <= 1) {
                        moduleMap.remove(modulePath);
                        removeModulePath(moduleMap, moduleOptionData, modulePath);
                        moduleDatas.remove(apiDefinitionWithBLOBs);
                    }
                    //不覆盖选择版本，如果被选版本有同接口，不导入，否则创建新版本接口
                    if (v.getVersionId().equals(versionId)) {
                        optionDatas.remove(apiDefinitionWithBLOBs);
                        if (CollectionUtils.isNotEmpty(distinctNameCases)) {
                            distinctNameCases.forEach(optionDataCases::remove);
                        }
                    } else {
                        //这里是为了标识当前数据是需要创建版本的，不是全新增的数据
                        addNewVersionApi(apiDefinitionWithBLOBs, v, "new");
                    }
                }
            });
        }
    }

    private void addNewVersionApi(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, ApiDefinitionWithBLOBs v, String version) {
        apiDefinitionWithBLOBs.setVersionId(version);
        apiDefinitionWithBLOBs.setNum(v.getNum());
        apiDefinitionWithBLOBs.setStatus(v.getStatus());
        apiDefinitionWithBLOBs.setOrder(v.getOrder());
        apiDefinitionWithBLOBs.setRefId(v.getRefId());
        apiDefinitionWithBLOBs.setLatest(v.getLatest());
    }

    private Map<String, ApiModule> cover(Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList,
                                         Map<String, ApiDefinitionWithBLOBs> nameModuleMap, Map<String, ApiDefinitionWithBLOBs> repeatDataMap,
                                         String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                         Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        //覆盖但不覆盖模块
        if (nameModuleMap != null) {
            //导入文件没有新增接口无需创建接口模块
            moduleMap = judgeModule(moduleMap, nameModuleMap, repeatDataMap);
            repeatDataMap.forEach((k, v) -> {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
                if (apiDefinitionWithBLOBs != null) {
                    //系统内重复的数据的版本如果不是选择的数据更新版本，则在数据更新版本新增，否则更新这个版本的数据
                    if (!v.getVersionId().equals(updateVersionId)) {
                        addNewVersionApi(apiDefinitionWithBLOBs, v, "update");
                        return;
                    }
                    Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                    //该接口的case
                    Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                    updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                    }
                    apiDefinitionWithBLOBs.setId(v.getId());
                    apiDefinitionWithBLOBs.setModuleId(v.getModuleId());
                    apiDefinitionWithBLOBs.setModulePath(v.getModulePath());
                    setApiParam(apiDefinitionWithBLOBs, updateVersionId, v);
                    toUpdateList.add(v);
                }
            });
        }
        return moduleMap;
    }

    private void updateEsb(Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap, String newId, String oldId) {
        if (MapUtils.isNotEmpty(esbApiParamsMap)) {
            EsbApiParamsWithBLOBs esbApiParamsWithBLOBs = esbApiParamsMap.get(oldId);
            if (esbApiParamsWithBLOBs != null) {
                esbApiParamsMap.remove(oldId);
                esbApiParamsWithBLOBs.setResourceId(newId);
                esbApiParamsMap.put(newId, esbApiParamsWithBLOBs);
            }
        }
    }

    private Map<String, ApiModule> judgeModule(Map<String, ApiModule> moduleMap, Map<String, ApiDefinitionWithBLOBs> nameModuleMap, Map<String, ApiDefinitionWithBLOBs> repeatDataMap) {
        AtomicBoolean remove = new AtomicBoolean(true);

        if (repeatDataMap.size() >= nameModuleMap.size()) {
            repeatDataMap.forEach((k, v) -> {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
                if (apiDefinitionWithBLOBs == null) {
                    remove.set(false);
                }
            });
            if (remove.get()) {
                moduleMap = new HashMap<>();
            }
        }
        return moduleMap;
    }

    private void coverModule(List<ApiDefinitionWithBLOBs> toUpdateList, Map<String, ApiDefinitionWithBLOBs> nameModuleMap,
                             Map<String, ApiDefinitionWithBLOBs> repeatDataMap, String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                             Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        if (nameModuleMap != null) {
            repeatDataMap.forEach((k, v) -> {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
                if (apiDefinitionWithBLOBs != null) {
                    //系统内重复的数据的版本如果不是选择的数据更新版本，则在数据更新版本新增，否则更新这个版本的数据
                    if (!v.getVersionId().equals(updateVersionId)) {
                        addNewVersionApi(apiDefinitionWithBLOBs, v, "update");
                        return;
                    }
                    //该接口的case
                    Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                    Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                    updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                    }
                    apiDefinitionWithBLOBs.setId(v.getId());
                    setApiParam(apiDefinitionWithBLOBs, updateVersionId, v);
                    toUpdateList.add(v);
                }
            });
        }
    }

    private void removeRepeatOrigin(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, List<ApiDefinitionWithBLOBs> optionDatas) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + (t.getModulePath() == null ? "" : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
        if (fullCoverage) {
            methodPathMap.forEach((k, v) -> optionDatas.add(v.get(v.size() - 1)));
        } else {
            methodPathMap.forEach((k, v) -> optionDatas.add(v.get(0)));
        }
    }

    private void removeHttpRepeat(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, boolean urlRepeat, List<ApiDefinitionWithBLOBs> optionDatas) {
        if (urlRepeat) {
            LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + (t.getModulePath() == null ? "" : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
            if (fullCoverage) {
                methodPathMap.forEach((k, v) -> optionDatas.add(v.get(v.size() - 1)));
            } else {
                methodPathMap.forEach((k, v) -> optionDatas.add(v.get(0)));
            }
        } else {
            LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getMethod().concat(t.getPath()), LinkedHashMap::new, Collectors.toList()));
            if (fullCoverage) {
                methodPathMap.forEach((k, v) -> optionDatas.add(v.get(v.size() - 1)));
            } else {
                methodPathMap.forEach((k, v) -> optionDatas.add(v.get(0)));
            }
        }
    }

    private String getVersionId(ApiTestImportRequest request) {
        String versionId;
        if (request.getVersionId() == null) {
            versionId = request.getDefaultVersion();
        } else {
            versionId = request.getVersionId();
        }
        return versionId;
    }

    private String getUpdateVersionId(ApiTestImportRequest request) {
        String updateVersionId;
        if (request.getUpdateVersionId() != null) {
            updateVersionId = request.getUpdateVersionId();
        } else {
            updateVersionId = request.getDefaultVersion();
        }
        return updateVersionId;
    }

    private void removeSameData(Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap, Map<String, ApiDefinitionWithBLOBs> methodPathMap,
                                List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiModule> moduleMap, String versionId,
                                List<ApiTestCaseWithBLOBs> optionDataCases) {

        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionDatas.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
                String modulePath = apiDefinitionWithBLOBs.getModulePath();
                List<ApiDefinitionWithBLOBs> moduleDatas = moduleOptionData.get(modulePath);
                if (moduleDatas != null && moduleDatas.size() <= 1) {
                    moduleMap.remove(modulePath);
                    removeModulePath(moduleMap, moduleOptionData, modulePath);
                    moduleDatas.remove(apiDefinitionWithBLOBs);
                }
                //不覆盖选择版本，如果被选版本有同接口，不导入，否则创建新版本接口
                List<ApiDefinitionWithBLOBs> sameVersionList = v.stream().filter(t -> t.getVersionId().equals(versionId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sameVersionList)) {
                    optionDatas.remove(apiDefinitionWithBLOBs);
                    if (CollectionUtils.isNotEmpty(distinctNameCases)) {
                        distinctNameCases.forEach(optionDataCases::remove);
                    }
                } else {
                    for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                        addNewVersionApi(apiDefinitionWithBLOBs, definitionWithBLOBs, "new");
                    }
                }
            }
        });
    }

    private void setApiParam(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, String versionId, ApiDefinitionWithBLOBs definitionWithBLOBs) {
        apiDefinitionWithBLOBs.setVersionId(versionId);
        apiDefinitionWithBLOBs.setNum(definitionWithBLOBs.getNum());
        apiDefinitionWithBLOBs.setStatus(definitionWithBLOBs.getStatus());
        apiDefinitionWithBLOBs.setOrder(definitionWithBLOBs.getOrder());
        apiDefinitionWithBLOBs.setRefId(definitionWithBLOBs.getRefId());
        apiDefinitionWithBLOBs.setLatest(definitionWithBLOBs.getLatest());
        apiDefinitionWithBLOBs.setCreateTime(definitionWithBLOBs.getCreateTime());
        apiDefinitionWithBLOBs.setUpdateTime(definitionWithBLOBs.getUpdateTime());
    }

    private void removeModulePath(Map<String, ApiModule> moduleMap, Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData, String modulePath) {
        if (StringUtils.isBlank(modulePath)) {
            return;
        }
        String[] pathTree = getPathTree(modulePath);
        String lastPath = pathTree[pathTree.length - 1];
        String substring = modulePath.substring(0, modulePath.indexOf("/" + lastPath));
        if (moduleOptionData.get(substring) == null || moduleOptionData.get(substring).size() == 0) {
            moduleMap.remove(substring);
            removeModulePath(moduleMap, moduleOptionData, substring);
        }

    }

    private void startCoverModule(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionDatas,
                                  Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap,
                                  String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                  Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap) {
        List<ApiDefinitionWithBLOBs> coverApiList = new ArrayList<>();
        List<ApiDefinitionWithBLOBs> updateApiList = new ArrayList<>();
        repeatDataMap.forEach((k, v) -> {
            //导入的与系统是相同接口的数据
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                //循环系统内重复接口
                int i = 0;
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    }
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, definitionWithBLOBs, optionDataCases);
                    }

                    ApiDefinitionWithBLOBs api = new ApiDefinitionWithBLOBs();
                    BeanUtils.copyBean(api, apiDefinitionWithBLOBs);
                    api.setId(definitionWithBLOBs.getId());
                    setApiParam(api, updateVersionId, definitionWithBLOBs);
                    coverApiList.add(api);
                    updateApiList.add(definitionWithBLOBs);
                }
                if (i == v.size()) {
                    //如果系统内的所有版本都不是当前选择的数据更新版本，则在数据更新版本这里新建数据
                    addNewVersionApi(apiDefinitionWithBLOBs, v.get(0), "update");

                } else {
                    optionDatas.remove(apiDefinitionWithBLOBs);
                }
            }
        });
        buildOtherParam(toUpdateList, optionDatas, coverApiList, updateApiList);
    }

    private void buildCaseList(Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap,
                               Map<String, ApiTestCaseWithBLOBs> caseNameMap,
                               ApiDefinitionWithBLOBs definitionWithBLOBs, List<ApiTestCaseWithBLOBs> optionDataCases) {
        //找出系统内重复接口的case，表里可能一个接口有多个同名case的可能
        List<ApiTestCaseWithBLOBs> oldApiTestCases = oldCaseMap.get(definitionWithBLOBs.getId());

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseNameMap;
        //如果重复用例有多个，则覆盖最后的那个
        if (CollectionUtils.isNotEmpty(oldApiTestCases)) {
            oldCaseNameMap = oldApiTestCases.stream().collect(Collectors.groupingBy(ApiTestCase::getName));
            caseNameMap.forEach((name, importCaseWithBLOBs) -> {
                //如果导入的有重名，覆盖，接口ID替换成系统内的
                importCaseWithBLOBs.setApiDefinitionId(definitionWithBLOBs.getId());
                List<ApiTestCaseWithBLOBs> caseWithBLOBs = oldCaseNameMap.get(name);
                if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
                    for (int i = 0; i < caseWithBLOBs.size(); i++) {
                        int version = 0;
                        if (caseWithBLOBs.get(i).getVersion() != null) {
                            version = caseWithBLOBs.get(i).getVersion() + 1;
                        }
                        if (i == 0) {
                            //被覆盖数据
                            importCaseWithBLOBs.setId(caseWithBLOBs.get(i).getId());
                            importCaseWithBLOBs.setNum(caseWithBLOBs.get(i).getNum());
                            importCaseWithBLOBs.setVersion(version);
                            importCaseWithBLOBs.setCreateUserId(caseWithBLOBs.get(i).getCreateUserId());
                            importCaseWithBLOBs.setUpdateUserId(caseWithBLOBs.get(i).getCreateUserId());
                        } else {
                            //同名的旧数据处理
                            caseWithBLOBs.get(i).setVersionId("old_case");
                            optionDataCases.add(caseWithBLOBs.get(i));
                        }
                    }
                    oldCaseNameMap.remove(name);
                }
                //不同名的旧数据处理
                oldCaseNameMap.forEach((k, v) -> {
                    if (!StringUtils.equals(k, name)) {
                        if (CollectionUtils.isNotEmpty(v)) {
                            for (ApiTestCaseWithBLOBs apiTestCaseWithBLOBs : v) {
                                apiTestCaseWithBLOBs.setVersionId("old_case");
                                optionDataCases.add(apiTestCaseWithBLOBs);
                            }
                        }
                    }
                });
            });
        } else {
            //否则直接给新增用例赋值新的接口ID
            caseNameMap.forEach((name, caseWithBLOBs1) -> {
                caseWithBLOBs1.setApiDefinitionId(definitionWithBLOBs.getId());
                caseWithBLOBs1.setVersion(0);
            });
        }
    }

    private Map<String, ApiTestCaseWithBLOBs> getDistinctCaseNameMap(Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        if (MapUtils.isEmpty(definitionIdCaseMAp)) {
            return null;
        }
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
        if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
            return caseWithBLOBs.stream().filter(t -> !StringUtils.equalsIgnoreCase("old_case", t.getVersionId())).collect(Collectors.toMap(ApiTestCase::getName, testCase -> testCase));
        } else {
            return null;
        }
    }

    private void startCover(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionDatas,
                            Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap,
                            String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                            Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap) {
        List<ApiDefinitionWithBLOBs> coverApiList = new ArrayList<>();
        List<ApiDefinitionWithBLOBs> updateApiList = new ArrayList<>();
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                int i = 0;
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    }
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, definitionWithBLOBs, optionDataCases);
                    }

                    ApiDefinitionWithBLOBs api = new ApiDefinitionWithBLOBs();
                    BeanUtils.copyBean(api, apiDefinitionWithBLOBs);
                    api.setId(definitionWithBLOBs.getId());
                    api.setModuleId(definitionWithBLOBs.getModuleId());
                    api.setModulePath(definitionWithBLOBs.getModulePath());
                    setApiParam(api, updateVersionId, definitionWithBLOBs);
                    coverApiList.add(api);
                    updateApiList.add(definitionWithBLOBs);
                }
                if (i == v.size()) {
                    //如果系统内的所有版本都不是当前选择的数据更新版本，则在数据更新版本这里新建数据
                    addNewVersionApi(apiDefinitionWithBLOBs, v.get(0), "update");

                } else {
                    optionDatas.remove(apiDefinitionWithBLOBs);
                }
            }
        });
        buildOtherParam(toUpdateList, optionDatas, coverApiList, updateApiList);
    }

    private Map<String, ApiModule> judgeModuleMap(Map<String, ApiModule> moduleMap, Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap) {
        Set<String> repeatKeys = repeatDataMap.keySet();
        Set<String> importKeys = methodPathMap.keySet();
        List<String> repeatKeyList = new ArrayList<>(repeatKeys);
        List<String> importKeysList = new ArrayList<>(importKeys);
        List<String> intersection = repeatKeyList.stream().filter(importKeysList::contains).collect(Collectors.toList());
        if (intersection.size() == importKeysList.size()) {
            //导入文件没有新增接口无需创建接口模块
            moduleMap = new HashMap<>();
        }
        return moduleMap;
    }

    private void buildOtherParam(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionDatas, List<ApiDefinitionWithBLOBs> coverApiList, List<ApiDefinitionWithBLOBs> updateApiList) {
        optionDatas.addAll(coverApiList);
        toUpdateList.addAll(updateApiList);

    }

    private void setModule(Map<String, ApiModule> moduleMap, Map<String, List<ApiModule>> pidChildrenMap,
                           Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap, List<ApiDefinitionWithBLOBs> data, ApiModuleDTO chooseModule) {
        for (ApiDefinitionWithBLOBs datum : data) {
            String modulePath = datum.getModulePath();
            ApiModule apiModule = moduleMap.get(modulePath);
            if (chooseModule != null) {
                dealChooseModuleData(moduleMap, pidChildrenMap, idPathMap, chooseModule, datum, modulePath);
            } else {
                dealNoModuleData(moduleMap, pidChildrenMap, idPathMap, idModuleMap, datum, modulePath, apiModule);
            }
        }
    }

    private void dealNoModuleData(Map<String, ApiModule> moduleMap, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap, ApiDefinitionWithBLOBs datum, String modulePath, ApiModule apiModule) {
        String[] pathTree;
        if (StringUtils.isNotBlank(modulePath)) {
            //导入时没选模块但接口有模块的，根据modulePath，和当前协议查询当前项目里是否有同名称模块，如果有，就在该模块下建立接口，否则新建模块
            pathTree = getPathTree(modulePath);
            if (apiModule != null) {
                datum.setModuleId(apiModule.getId());
                datum.setModulePath(modulePath);
            } else {
                List<ApiModule> moduleList = pidChildrenMap.get("root");
                ApiModule minModule = getMinModule(pathTree, moduleList, pidChildrenMap, moduleMap, idPathMap, idModuleMap);
                String id = minModule.getId();
                datum.setModuleId(id);
                datum.setModulePath(idPathMap.get(id));
            }
        } else {
            //导入时即没选中模块，接口自身也没模块的，直接返会当前项目，当前协议下的默认模块
            List<ApiModule> moduleList = pidChildrenMap.get("root");
            for (ApiModule module : moduleList) {
                if (module.getName().equals("未规划接口")) {
                    datum.setModuleId(module.getId());
                    datum.setModulePath("/" + module.getName());
                }
            }
        }
    }

    private void dealChooseModuleData(Map<String, ApiModule> moduleMap, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, ApiModuleDTO chooseModule, ApiDefinitionWithBLOBs datum, String modulePath) {
        String[] pathTree;
        //导入时选了模块，且接口有模块的
        if (StringUtils.isNotBlank(modulePath)) {
            pathTree = getPathTree(modulePath);
            ApiModule chooseModuleOne = JSON.parseObject(JSON.toJSONString(chooseModule), ApiModule.class);
            ApiModule minModule = getChooseMinModule(pathTree, chooseModuleOne, pidChildrenMap, moduleMap, idPathMap);
            String id = minModule.getId();
            datum.setModuleId(id);
            datum.setModulePath(idPathMap.get(id));
        } else {
            //导入时选了模块，且接口没有模块的
            datum.setModuleId(chooseModule.getId());
            datum.setModulePath(idPathMap.get(chooseModule.getId()));
        }
    }

    private String getChooseModulePath(Map<String, String> idPathMap, ApiModuleDTO chooseModule, String chooseModuleParentId) {
        String s;
        if (chooseModuleParentId.equals("root")) {
            s = "/" + chooseModule.getName();
        } else {
            s = idPathMap.get(chooseModule.getId());
        }
        return s;
    }

    private String getChooseModuleParentId(ApiModuleDTO chooseModule) {
        if (chooseModule.getParentId() == null) {
            chooseModule.setParentId("root");
        }
        return chooseModule.getParentId();
    }

    private String[] getPathTree(String modulePath) {
        String substring = modulePath.substring(0, 1);
        if (substring.equals("/")) {
            modulePath = modulePath.substring(1);
        }
        if (modulePath.contains("/")) {
            //如果模块有层级，逐级查找，如果某一级不在当前项目了，则新建该层级的模块及其子集
            return modulePath.split("/");
        } else {
            return new String[]{modulePath};
        }
    }

    private ApiModule getMinModule(String[] tagTree, List<ApiModule> parentModuleList, Map<String, List<ApiModule>> pidChildrenMap, Map<String, ApiModule> moduleMap
            , Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap) {
        //如果parentModule==null 则证明需要创建根目录同级的模块
        ApiModule parentModule = null;
        for (int i = 0; i < tagTree.length; i++) {
            int finalI = i;
            List<ApiModule> collect = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(parentModuleList)) {
                collect = parentModuleList.stream().filter(t -> t.getName().equals(tagTree[finalI])).collect(Collectors.toList());
            }
            if (collect.isEmpty()) {
                if (i == 0) {
                    //证明需要在根目录创建，
                    parentModule = new ApiModule();
                    parentModule.setProjectId(pidChildrenMap.get("root").get(0).getProjectId());
                    parentModule.setId("root");
                    parentModule.setLevel(0);
                    parentModule.setProtocol(pidChildrenMap.get("root").get(0).getProtocol());
                } else {
                    if (CollectionUtils.isNotEmpty(parentModuleList) && parentModule == null) {
                        String parentId = parentModuleList.get(0).getParentId();
                        ApiModuleDTO apiModuleDTO = idModuleMap.get(parentId);
                        parentModule = JSON.parseObject(JSON.toJSONString(apiModuleDTO), ApiModule.class);
                    }
                }
                return createModule(tagTree, i, parentModule, moduleMap, pidChildrenMap, idPathMap);
            } else {
                parentModule = collect.get(0);
                parentModuleList = pidChildrenMap.get(collect.get(0).getId());
            }
        }
        return parentModule;
    }

    private ApiModule getChooseMinModule(String[] tagTree, ApiModule parentModule, Map<String, List<ApiModule>> pidChildrenMap, Map<String, ApiModule> moduleMap
            , Map<String, String> idPathMap) {
        //如果parentModule==null 则证明需要创建根目录同级的模块
        ApiModule returnModule = null;
        for (int i = 0; i < tagTree.length; i++) {
            int finalI = i;
            //在选择的模块下建模块，查看选择的模块下有没有同名的模块
            List<ApiModule> moduleList = pidChildrenMap.get(parentModule.getId());
            if (moduleList != null) {
                List<ApiModule> collect1 = moduleList.stream().filter(t -> t.getName().equals(tagTree[finalI])).collect(Collectors.toList());
                if (collect1.isEmpty()) {
                    return createModule(tagTree, i, parentModule, moduleMap, pidChildrenMap, idPathMap);
                } else {
                    returnModule = collect1.get(0);
                    parentModule = collect1.get(0);
                }
            } else {
                return createModule(tagTree, i, parentModule, moduleMap, pidChildrenMap, idPathMap);
            }
        }
        return returnModule;
    }

    private ApiModule createModule(String[] tagTree, int i, ApiModule parentModule, Map<String, ApiModule> moduleMap, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap) {
        ApiModule returnModule = null;
        for (int i1 = i; i1 < tagTree.length; i1++) {
            String pathName = tagTree[i1];
            ApiModule newModule = this.getNewModule(pathName, parentModule.getProjectId(), parentModule.getLevel() + 1);
            String parentId;
            if (parentModule.getId().equals("root")) {
                parentId = null;
            } else {
                parentId = parentModule.getId();
            }
            double pos = this.getNextLevelPos(parentModule.getProjectId(), parentModule.getLevel() + 1, parentId);
            newModule.setPos(pos);
            newModule.setProtocol(parentModule.getProtocol());
            newModule.setParentId(parentId);
            List<ApiModule> moduleList = pidChildrenMap.get(parentModule.getId());
            if (moduleList != null) {
                moduleList.add(newModule);
            } else {
                moduleList = new ArrayList<>();
                moduleList.add(newModule);
                pidChildrenMap.put(parentModule.getId(), moduleList);
            }

            String parentPath = idPathMap.get(parentModule.getId());
            String path;
            if (StringUtils.isNotBlank(parentPath)) {
                path = parentPath + "/" + pathName;
            } else {
                path = "/" + pathName;
            }
            idPathMap.put(newModule.getId(), path);
            moduleMap.putIfAbsent(path, newModule);
            parentModule = newModule;
            returnModule = newModule;
        }
        return returnModule;
    }

    private void buildProcessData(List<ApiModuleDTO> nodeTreeByProjectId, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, String> parentModulePathMap) {
        //当前层级的模块的所有子模块的集合
        Map<String, List<ApiModuleDTO>> idChildrenMap = new HashMap<>();
        int i = 0;
        Map<String, List<ApiModule>> idModuleMap = new HashMap<>();
        for (ApiModuleDTO apiModuleDTO : nodeTreeByProjectId) {
            if (StringUtils.isBlank(apiModuleDTO.getParentId())) {
                apiModuleDTO.setParentId("root");
            }
            String parentModulePath = parentModulePathMap.get(apiModuleDTO.getParentId());
            if (parentModulePath != null) {
                if (parentModulePath.equals("/root")) {
                    apiModuleDTO.setPath("/" + apiModuleDTO.getName());
                } else {
                    apiModuleDTO.setPath(parentModulePath + "/" + apiModuleDTO.getName());
                }
            } else {
                apiModuleDTO.setPath("/" + apiModuleDTO.getName());
            }
            idPathMap.put(apiModuleDTO.getId(), apiModuleDTO.getPath());

            ApiModule apiModule = buildModule(idModuleMap, apiModuleDTO);
            if (pidChildrenMap.get(apiModuleDTO.getParentId()) != null) {
                pidChildrenMap.get(apiModuleDTO.getParentId()).add(apiModule);
            } else {
                pidChildrenMap.put(apiModuleDTO.getParentId(), idModuleMap.get(apiModuleDTO.getId()));
            }
            i = i + 1;
            List<ApiModuleDTO> childrenList = idChildrenMap.get(apiModuleDTO.getId());
            if (apiModuleDTO.getChildren() != null) {
                if (childrenList != null) {
                    childrenList.addAll(apiModuleDTO.getChildren());
                } else {
                    idChildrenMap.put(apiModuleDTO.getId(), apiModuleDTO.getChildren());
                }
            } else {
                if (childrenList == null) {
                    pidChildrenMap.put(apiModuleDTO.getId(), new ArrayList<>());
                }
            }
            parentModulePathMap.put(apiModuleDTO.getId(), apiModuleDTO.getPath());
        }
        if (i == nodeTreeByProjectId.size() && nodeTreeByProjectId.size() > 0) {
            Collection<List<ApiModuleDTO>> values = idChildrenMap.values();
            List<ApiModuleDTO> childrenList = new ArrayList<>();
            for (List<ApiModuleDTO> value : values) {
                childrenList.addAll(value);
            }
            buildProcessData(childrenList, pidChildrenMap, idPathMap, parentModulePathMap);
        }
    }

    private ApiModule buildModule(Map<String, List<ApiModule>> idModuleMap, ApiModuleDTO apiModuleDTO) {
        ApiModule apiModule = new ApiModule();
        apiModule.setId(apiModuleDTO.getId());
        apiModule.setName(apiModuleDTO.getName());
        apiModule.setParentId(apiModuleDTO.getParentId());
        apiModule.setProjectId(apiModuleDTO.getProjectId());
        apiModule.setProtocol(apiModuleDTO.getProtocol());
        apiModule.setLevel(apiModuleDTO.getLevel());
        List<ApiModule> moduleList = idModuleMap.get(apiModuleDTO.getId());
        if (moduleList != null) {
            moduleList.add(apiModule);
        } else {
            moduleList = new ArrayList<>();
            moduleList.add(apiModule);
            idModuleMap.put(apiModuleDTO.getId(), moduleList);
        }
        return apiModule;
    }

}
