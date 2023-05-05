package io.metersphere.api.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiModuleMapper;
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
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    SqlSessionFactory sqlSessionFactory;

    private static final String HTTP_PROTOCOL = "HTTP";

    public ApiModuleService() {
        super(ApiModuleDTO.class);
    }

    public ApiModule get(String id) {
        return apiModuleMapper.selectByPrimaryKey(id);
    }


    public List<ApiModuleDTO> getApiModulesByProjectAndPro(String projectId, String protocol) {
        return extApiModuleMapper.getNodeTreeByProjectId(projectId, protocol);
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
                    Integer countNumInteger = new Integer(String.valueOf(countNumObj));
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
                .filter(apiTestCase -> !StringUtils.equals(CommonConstants.TrashStatus, apiTestCase.getStatus()))
                .map(ApiTestCase::getApiDefinitionId)
                .collect(Collectors.toList());

        List<String> dataNodeIds = apiDefinitionService.selectApiDefinitionBydIds(definitionIds).stream()
                .filter(apiDefinition -> !StringUtils.equals(CommonConstants.TrashStatus, apiDefinition.getStatus()))
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
        ApiDefinitionExampleWithOperation apiDefinitionExample = new ApiDefinitionExampleWithOperation();
        apiDefinitionExample.createCriteria().andModuleIdIn(nodeIds);
        apiDefinitionExample.setOperator(SessionUtils.getUserId());
        apiDefinitionExample.setOperationTime(System.currentTimeMillis());
        apiDefinitionService.removeToGcByExample(apiDefinitionExample);
//        extApiDefinitionMapper.removeToGcByExample(apiDefinitionExample);   //  删除模块，则模块下的接口放入回收站

        ApiModuleExample apiDefinitionNodeExample = new ApiModuleExample();
        apiDefinitionNodeExample.createCriteria().andIdIn(nodeIds);
        return apiModuleMapper.deleteByExample(apiDefinitionNodeExample);
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
        ApiModuleDTO dto = JSON.parseObject(JSON.toJSONString(module), ApiModuleDTO.class);
        return dto;
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

    /**
     * 返回数据库中存在的id
     *
     * @param protocalModuleIdMap <protocol , List<moduleId>>
     * @return
     */
    public Map<String, List<String>> checkModuleIds(Map<String, List<String>> protocalModuleIdMap) {
        Map<String, List<String>> returnMap = new HashMap<>();
        if (MapUtils.isNotEmpty(protocalModuleIdMap)) {
            ApiModuleExample example = new ApiModuleExample();
            for (Map.Entry<String, List<String>> entry : protocalModuleIdMap.entrySet()) {
                String protocol = entry.getKey();
                List<String> moduleIds = entry.getValue();
                if (CollectionUtils.isNotEmpty(moduleIds)) {
                    example.clear();
                    example.createCriteria().andIdIn(moduleIds).andProtocolEqualTo(protocol);
                    List<ApiModule> moduleList = apiModuleMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(moduleList)) {
                        List<String> idLIst = new ArrayList<>();
                        moduleList.forEach(module -> {
                            idLIst.add(module.getId());
                        });
                        returnMap.put(protocol, idLIst);
                    }
                }
            }
        }
        return returnMap;
    }

    /**
     * 上传文件时对文件的模块进行检测
     *
     * @param data
     * @param fullCoverage 是否覆盖接口
     * @return Return to the newly added module list and api list
     */
    public UpdateApiModuleDTO checkApiModule(ApiTestImportRequest request, ApiDefinitionImport apiImport, List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, boolean urlRepeat) {
        String projectId = request.getProjectId();
        String protocol = request.getProtocol();

        if (StringUtils.isBlank(protocol)) {
            protocol = HTTP_PROTOCOL;
        }

        if (fullCoverage == null) {
            fullCoverage = false;
        }

        //标准版ESB数据导入不区分是否覆盖，默认都为覆盖
        if (MapUtils.isNotEmpty(apiImport.getEsbApiParamsMap())) {
            fullCoverage = true;
        }

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

        if (protocol.equals(HTTP_PROTOCOL)) {
            return dealHttp(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, urlRepeat, importCases);
        } else {
            Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap = apiImport.getEsbApiParamsMap();
            return delOtherProtocol(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, importCases, esbApiParamsMap);
        }
    }

    private UpdateApiModuleDTO delOtherProtocol(List<ApiDefinitionWithBLOBs> data, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request, Boolean fullCoverage, List<ApiTestCaseWithBLOBs> importCases, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        List<ApiDefinitionWithBLOBs> optionDatas = new ArrayList<>();
        //系统原有的需要更新的list，
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //去重，TCP,SQL,DUBBO 模块下名称唯一
        // 此外，用集合收集其他重复的接口用于在检验是第一次导入时作为其case导入进来
        Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap = new HashMap<>();
        removeRepeatOrigin(data, fullCoverage, optionDatas, repeatApiMap);
        //上传文件时选的模块ID
        String chooseModuleId = request.getModuleId();
        //获取选中的模块
        ApiModuleDTO chooseModule = null;
        if (chooseModuleId != null) {
            chooseModule = idModuleMap.get(chooseModuleId);
        }
        List<ApiTestCaseWithBLOBs> optionDataCases = new ArrayList<>();
        //将ID,num全部置于null,覆盖的时候会增加ID，用以区分更新还是新增，处理导入文件里的重复的case，如果覆盖，则取重复的最后一个，否则取第一个
        // 用集合收集其他重复的Case用于在检验是第一次导入时作为相关接口的case导入进来
        List<ApiTestCaseWithBLOBs> repeatCaseList = new ArrayList<>();
        removeRepeatCase(fullCoverage, importCases, optionDataCases, repeatCaseList);
        //需要新增的模块，key 为模块路径
        Map<String, ApiModule> moduleMap = new HashMap<>();
        //处理模块
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionDatas, chooseModule);
        //系统内重复数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = getApiDefinitionWithBLOBsList(request, optionDatas);
        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionDatas, repeatApiMap, repeatCaseList, optionDataCases);
        }
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
                //这样的过滤规则下可能存在重复接口，如果是覆盖模块，需要按照去重规则再次去重，否则就加上接口原有的模块
                if (fullCoverage) {
                    List<ApiDefinitionWithBLOBs> singleOptionDatas = new ArrayList<>();
                    removeOtherChooseModuleRepeat(optionDatas, singleOptionDatas, chooseModulePath);
                    optionDatas = singleOptionDatas;
                    optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName().concat(chooseModulePath), api -> api));
                } else {
                    getNoHChooseModuleUrlRepeatOptionMap(optionDatas, optionMap, chooseModulePath);
                }
                repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModuleId)).collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), api -> api));
            }
        } else {
            buildOptionMap(optionDatas, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), api -> api));
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
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), api -> api));
            optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), api -> api));
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
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionDatas.stream().collect(Collectors.groupingBy(t -> t.getName() + chooseModulePath, LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> {
            singleOptionDatas.add(v.get(v.size() - 1));
        });
    }

    private List<ApiDefinitionWithBLOBs> getApiDefinitionWithBLOBsList(ApiTestImportRequest request, List<ApiDefinitionWithBLOBs> optionDatas) {
        //处理数据
        List<String> nameList = optionDatas.stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
        String projectId = request.getProjectId();
        String protocol = request.getProtocol();
        //获取系统内重复数据
        return extApiDefinitionMapper.selectRepeatByProtocol(nameList, protocol, projectId);
    }

    private void buildOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName(), optionDatum);
            } else {
                optionMap.put(optionDatum.getName() + optionDatum.getModulePath(), optionDatum);
            }
        }
    }

    private UpdateApiModuleDTO dealHttp(List<ApiDefinitionWithBLOBs> data, Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request, Boolean fullCoverage, boolean urlRepeat, List<ApiTestCaseWithBLOBs> importCases) {
        List<ApiDefinitionWithBLOBs> optionDatas = new ArrayList<>();
        //系统原有的需要更新的list，
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //去重 如果url可重复 则模块+名称+请求方式+路径 唯一，否则 请求方式+路径唯一，
        //覆盖模式留重复的最后一个，不覆盖留第一个
       /*
         接口首次导入逻辑：
         接口导入时，系统没有重复文件：
         按照当前系统里是否是同一接口的逻辑判断--->导入的文件里是否有重复接口
         有重复：
         第一个接口作为接口，所有与该接口重复的接口作为用例；
         举例： 文件中有重复的 接口 A 、 A'、A''、 A'''  ， 则 A为接口，  A 、 A'、A''、 A'''  为 A 接口的用例
        */
        // 此外，用集合收集其他重复的接口用于在检验是第一次导入时作为其case导入进来
        Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap = new HashMap<>();
        removeHttpRepeat(data, fullCoverage, urlRepeat, optionDatas, repeatApiMap);

        //上传文件时选的模块ID
        String chooseModuleId = request.getModuleId();
        //获取选中的模块
        ApiModuleDTO chooseModule = null;
        if (chooseModuleId != null) {
            chooseModule = idModuleMap.get(chooseModuleId);
        }
        List<ApiTestCaseWithBLOBs> optionDataCases = new ArrayList<>();
        //将ID,num全部置于null,覆盖的时候会增加ID，用以区分更新还是新增，处理导入文件里的重复的case，如果覆盖，则取重复的最后一个，否则取第一个
        // 用集合收集其他重复的Case用于在检验是第一次导入时作为相关接口的case导入进来
        List<ApiTestCaseWithBLOBs> repeatCaseList = new ArrayList<>();
        removeRepeatCase(fullCoverage, importCases, optionDataCases, repeatCaseList);
        //需要新增的模块，key 为模块路径
        Map<String, ApiModule> moduleMap = new HashMap<>();
        //处理模块
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionDatas, chooseModule);
        ApiImportParamDto apiImportParamDto = new ApiImportParamDto(chooseModule, idPathMap, optionDatas, fullCoverage, request, moduleMap, toUpdateList, optionDataCases);
        apiImportParamDto.setRepeatApiMap(repeatApiMap);
        apiImportParamDto.setRepeatCaseList(repeatCaseList);
        if (urlRepeat) {
            optionDatas = dealHttpUrlRepeat(apiImportParamDto);
        } else {
            dealHttpUrlNoRepeat(apiImportParamDto);
        }

        if (optionDatas.isEmpty()) {
            moduleMap = new HashMap<>();
        }

        return getUpdateApiModuleDTO(moduleMap, toUpdateList, optionDatas, optionDataCases);
    }

    private void getUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName() + optionDatum.getMethod() + optionDatum.getPath(), optionDatum);
            } else {
                optionMap.put(optionDatum.getName() + optionDatum.getMethod() + optionDatum.getPath() + optionDatum.getModulePath(), optionDatum);
            }
        }
    }

    private void dealHttpUrlNoRepeat(ApiImportParamDto apiImportParamDto) {
        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap;
        ApiTestImportRequest request = apiImportParamDto.getRequest();
        List<ApiDefinitionWithBLOBs> optionDatas = apiImportParamDto.getOptionDatas();
        List<ApiDefinitionWithBLOBs> toUpdateList = apiImportParamDto.getToUpdateList();
        List<ApiTestCaseWithBLOBs> optionDataCases = apiImportParamDto.getOptionDataCases();
        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionDatas, projectId);

        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionDatas, apiImportParamDto.getRepeatApiMap(), apiImportParamDto.getRepeatCaseList(), optionDataCases);
        }

        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getMethod() + t.getPath()));

        //按照原来的顺序
        optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), api -> api));

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();

        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs);
        }

        if (apiImportParamDto.getFullCoverage()) {
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
                removeSameData(repeatDataMap, optionMap, optionDatas, apiImportParamDto.getModuleMap(), versionId, optionDataCases);
            }
        }
    }

    private List<ApiDefinitionWithBLOBs> dealHttpUrlRepeat(ApiImportParamDto apiImportParamDto) {
        ApiTestImportRequest request = apiImportParamDto.getRequest();
        ApiModuleDTO chooseModule = apiImportParamDto.getChooseModule();
        List<ApiDefinitionWithBLOBs> optionDatas = apiImportParamDto.getOptionDatas();
        Boolean fullCoverage = apiImportParamDto.getFullCoverage();
        Map<String, String> idPathMap = apiImportParamDto.getIdPathMap();
        List<ApiDefinitionWithBLOBs> toUpdateList = apiImportParamDto.getToUpdateList();
        List<ApiTestCaseWithBLOBs> optionDataCases = apiImportParamDto.getOptionDataCases();
        Map<String, ApiModule> moduleMap = apiImportParamDto.getModuleMap();

        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionDatas, projectId);

        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionDatas, apiImportParamDto.getRepeatApiMap(), apiImportParamDto.getRepeatCaseList(), optionDataCases);
        }

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
                optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName() + t.getMethod() + t.getPath() + chooseModulePath, api -> api));
            } else {
                getChooseModuleUrlRepeatOptionMap(optionDatas, optionMap, chooseModulePath);
            }
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModule.getId())).collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + t.getModulePath()));
        } else {
            //否则在整个系统中过滤
            getUrlRepeatOptionMap(optionDatas, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + t.getModulePath()));
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
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + t.getModulePath()));
            optionMap = optionDatas.stream().collect(Collectors.toMap(t -> t.getName() + t.getMethod() + t.getPath() + t.getModulePath(), api -> api));
            if (fullCoverage) {
                startCover(toUpdateList, optionDatas, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
            } else {
                //不覆盖,同一接口不做更新
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    removeSameData(repeatDataMap, optionMap, optionDatas, moduleMap, versionId, optionDataCases);
                }
            }
        }
        return optionDatas;
    }

    /**
     * @param optionDatas     操作过可以导入的文件里的接口
     * @param repeatApiMap    文件里与可操作的接口重复的接口
     * @param repeatCaseList  文件里与可操作的case重复的case
     * @param optionDataCases 操作过可以导入的文件里的case
     */
    private void setRepeatApiToCase(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap, List<ApiTestCaseWithBLOBs> repeatCaseList, List<ApiTestCaseWithBLOBs> optionDataCases) {
        Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = repeatCaseList.stream().collect(Collectors.groupingBy(ApiTestCaseWithBLOBs::getApiDefinitionId));
        Map<String, List<ApiTestCaseWithBLOBs>> importCaseMap = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCaseWithBLOBs::getApiDefinitionId));
        for (ApiDefinitionWithBLOBs optionData : optionDatas) {
            //文件里和当前导入数据的接口重复的数据
            String apiId = optionData.getId();
            //文件重复的case,这里取出来是为了和api组成caseList导入进去
            List<ApiTestCaseWithBLOBs> apiTestCaseWithBLOBs = apiIdCaseMap.get(apiId);
            List<ApiDefinitionWithBLOBs> apiDefinitionWithBLOBs = repeatApiMap.get(apiId);
            if (CollectionUtils.isEmpty(apiTestCaseWithBLOBs)) {
                apiTestCaseWithBLOBs = new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(apiDefinitionWithBLOBs)) {
                for (ApiDefinitionWithBLOBs apiDefinitionWithBLOB : apiDefinitionWithBLOBs) {
                    apiTestCaseWithBLOBs.add(apiToCase(apiDefinitionWithBLOB, apiId));
                }
            }
            /*apiTestCaseWithBLOBs.add(apiToCase(optionData,apiId));*/
            //取出当前操作接口应该导入的case用于对比名字重复加序号
            List<String> nameList = new ArrayList<>();
            List<ApiTestCaseWithBLOBs> importCaseList = importCaseMap.get(apiId);
            if (CollectionUtils.isNotEmpty(nameList)) {
                nameList = importCaseList.stream().map(ApiTestCaseWithBLOBs::getName).collect(Collectors.toList());
            }
            for (int i = 0; i < apiTestCaseWithBLOBs.size(); i++) {
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs1 = apiTestCaseWithBLOBs.get(i);
                if (nameList.contains(apiTestCaseWithBLOBs1.getName())) {
                    apiTestCaseWithBLOBs1.setName(apiTestCaseWithBLOBs1.getName() + "0" + i);
                    nameList.add(apiTestCaseWithBLOBs1.getName() + "0" + i);
                } else {
                    nameList.add(apiTestCaseWithBLOBs1.getName());
                }
            }
            optionDataCases.addAll(apiTestCaseWithBLOBs);
        }

    }

    /**
     * 将接口转成用例
     *
     * @param apiDefinitionWithBLOB
     * @return
     */
    private ApiTestCaseWithBLOBs apiToCase(ApiDefinitionWithBLOBs apiDefinitionWithBLOB, String apiId) {
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setName(apiDefinitionWithBLOB.getName());
        apiTestCase.setApiDefinitionId(apiId);
        apiTestCase.setCreateUserId(apiDefinitionWithBLOB.getCreateUser());
        apiTestCase.setTags(apiDefinitionWithBLOB.getTags());
        apiTestCase.setStatus(apiDefinitionWithBLOB.getStatus());
        apiTestCase.setOriginalStatus(apiDefinitionWithBLOB.getOriginalState());
        apiTestCase.setVersionId(apiDefinitionWithBLOB.getVersionId());
        apiTestCase.setOrder(apiDefinitionWithBLOB.getOrder());
        apiTestCase.setCaseStatus(apiDefinitionWithBLOB.getCaseStatus());
        apiTestCase.setDescription(apiDefinitionWithBLOB.getDescription());
        apiTestCase.setRequest(apiDefinitionWithBLOB.getRequest());
        return apiTestCase;
    }

    private void removeHttpChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionDatas, List<ApiDefinitionWithBLOBs> singleOptionDatas, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionDatas.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + chooseModulePath, LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> singleOptionDatas.add(v.get(v.size() - 1)));
    }

    private void getChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionDatas) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName() + optionDatum.getMethod() + optionDatum.getPath() + chooseModulePath, optionDatum);
            } else {
                optionMap.put(optionDatum.getName() + optionDatum.getMethod() + optionDatum.getPath() + chooseModulePath + optionDatum.getModulePath(), optionDatum);
            }
        }
    }


    private void removeRepeatCase(Boolean fullCoverage, List<ApiTestCaseWithBLOBs> importCases, List<ApiTestCaseWithBLOBs> optionDataCases, List<ApiTestCaseWithBLOBs> repeatCaseList) {
        LinkedHashMap<String, List<ApiTestCaseWithBLOBs>> apiIdNameMap = importCases.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getApiDefinitionId(), LinkedHashMap::new, Collectors.toList()));
        if (fullCoverage) {
            apiIdNameMap.forEach((k, v) -> {
                v.get(v.size() - 1).setId(null);
                v.get(v.size() - 1).setNum(null);
                optionDataCases.add(v.get(v.size() - 1));
                for (int i = 0; i < v.size() - 1; i++) {
                    v.get(i).setId(null);
                    v.get(i).setNum(null);
                    repeatCaseList.add(v.get(i));
                }
            });
        } else {
            apiIdNameMap.forEach((k, v) -> {
                v.get(0).setId(null);
                v.get(0).setNum(null);
                optionDataCases.add(v.get(0));
                for (int i = 1; i < v.size(); i++) {
                    v.get(i).setId(null);
                    v.get(i).setNum(null);
                    repeatCaseList.add(v.get(i));
                }
            });
        }
    }

    private Map<String, List<ApiTestCaseWithBLOBs>> getOldCaseMap(List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs) {
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap;
        List<String> definitionIds = repeatApiDefinitionWithBLOBs.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdIn(definitionIds).andStatusNotEqualTo("Trash");
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(testCaseExample);
        //ArrayList<ApiTestCaseWithBLOBs> testCases = getDistinctNameCases(caseWithBLOBs);
        oldCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
        return oldCaseMap;
    }

    private Boolean getFullCoverageApi(ApiTestImportRequest request) {
        Boolean fullCoverageApi = request.getCoverModule();
        if (fullCoverageApi == null) {
            fullCoverageApi = false;
        }
        return fullCoverageApi;
    }

    private UpdateApiModuleDTO getUpdateApiModuleDTO(Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionDatas, List<ApiTestCaseWithBLOBs> optionDataCases) {
        UpdateApiModuleDTO updateApiModuleDTO = new UpdateApiModuleDTO();
        updateApiModuleDTO.setModuleMap(moduleMap);
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
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                    }
                    updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                    apiDefinitionWithBLOBs.setId(v.getId());
                    apiDefinitionWithBLOBs.setVersionId(updateVersionId);
                    apiDefinitionWithBLOBs.setModuleId(v.getModuleId());
                    apiDefinitionWithBLOBs.setModulePath(v.getModulePath());
                    apiDefinitionWithBLOBs.setNum(v.getNum());
                    apiDefinitionWithBLOBs.setStatus(v.getStatus());
                    apiDefinitionWithBLOBs.setOrder(v.getOrder());
                    apiDefinitionWithBLOBs.setRefId(v.getRefId());
                    apiDefinitionWithBLOBs.setLatest(v.getLatest());
                    apiDefinitionWithBLOBs.setCreateTime(v.getCreateTime());
                    apiDefinitionWithBLOBs.setUpdateTime(v.getUpdateTime());

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
                    Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                    //该接口的case
                    Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                    }
                    updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                    apiDefinitionWithBLOBs.setId(v.getId());
                    setApiParam(apiDefinitionWithBLOBs, updateVersionId, v);
                    toUpdateList.add(v);
                }
            });
        }
    }

    private void removeRepeatOrigin(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, List<ApiDefinitionWithBLOBs> optionDatas, Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + (t.getModulePath() == null ? "" : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
        buildData(fullCoverage, optionDatas, methodPathMap, repeatApiMap);
    }

    private void removeHttpRepeat(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, boolean urlRepeat, List<ApiDefinitionWithBLOBs> optionDatas, Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap;
        if (urlRepeat) {
            methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + (t.getModulePath() == null ? "" : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
        } else {
            methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getMethod() + t.getPath(), LinkedHashMap::new, Collectors.toList()));
        }
        buildData(fullCoverage, optionDatas, methodPathMap, repeatApiMap);
    }

    private static void buildData(Boolean fullCoverage, List<ApiDefinitionWithBLOBs> optionDatas, LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap) {
        if (fullCoverage) {
            methodPathMap.forEach((k, v) -> {
                optionDatas.add(v.get(v.size() - 1));
                List<ApiDefinitionWithBLOBs> repeatList = new ArrayList<>();
                for (int i = 0; i < v.size() - 1; i++) {
                    repeatList.add(v.get(i));
                }
                if (CollectionUtils.isNotEmpty(repeatList)) {
                    repeatApiMap.put(v.get(v.size() - 1).getId(), repeatList);
                }
            });
        } else {
            methodPathMap.forEach((k, v) -> {
                optionDatas.add(v.get(0));
                List<ApiDefinitionWithBLOBs> repeatList = new ArrayList<>();
                for (int i = 1; i < v.size(); i++) {
                    repeatList.add(v.get(i));
                }
                if (CollectionUtils.isNotEmpty(repeatList)) {
                    repeatApiMap.put(v.get(0).getId(), repeatList);
                }
            });
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
                                List<ApiDefinitionWithBLOBs> optionDatas, Map<String, ApiModule> moduleMap, String versionId, List<ApiTestCaseWithBLOBs> optionDataCases) {
        Map<String, ApiModule> parentIdModuleMap = new HashMap<>();
        for (ApiModule value : moduleMap.values()) {
            parentIdModuleMap.put(value.getParentId(), value);
        }
        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionDatas.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
                String modulePath = apiDefinitionWithBLOBs.getModulePath();
                List<ApiDefinitionWithBLOBs> moduleDatas = moduleOptionData.get(modulePath);
                if (moduleDatas != null && moduleDatas.size() <= 1) {
                    ApiModule apiModule = moduleMap.get(modulePath);
                    if (apiModule != null && parentIdModuleMap.get(apiModule.getId()) == null) {
                        moduleMap.remove(modulePath);
                    }
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
                    api.setVersionId(updateVersionId);
                    api.setOrder(definitionWithBLOBs.getOrder());
                    api.setRefId(apiDefinitionWithBLOBs.getRefId());
                    api.setLatest(apiDefinitionWithBLOBs.getLatest());
                    api.setNum(definitionWithBLOBs.getNum());
                    api.setStatus(definitionWithBLOBs.getStatus());
                    api.setCreateTime(definitionWithBLOBs.getCreateTime());
                    api.setUpdateTime(definitionWithBLOBs.getUpdateTime());
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
        //找出每个接口的case
        List<ApiTestCaseWithBLOBs> apiTestCases = oldCaseMap.get(definitionWithBLOBs.getId());
        //map List 结构是因为表里可能一个接口有多个同名case的可能
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseNameMap;
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            oldCaseNameMap = apiTestCases.stream().collect(Collectors.groupingBy(ApiTestCase::getName));
            caseNameMap.forEach((name, caseWithBLOBs1) -> {
                //如果导入的有重名，覆盖，接口ID替换成系统内的
                caseWithBLOBs1.setApiDefinitionId(definitionWithBLOBs.getId());
                List<ApiTestCaseWithBLOBs> caseWithBLOBs = oldCaseNameMap.get(name);
                if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
                    ApiTestCaseWithBLOBs apiTestCaseWithBLOBs1 = caseWithBLOBs.get(0);
                    if (apiTestCaseWithBLOBs1 != null) {
                        if (caseWithBLOBs.size() > 1) {
                            for (int i = 0; i < caseWithBLOBs.size(); i++) {
                                int version = 0;
                                if (caseWithBLOBs.get(i).getVersion() != null) {
                                    version = caseWithBLOBs.get(i).getVersion() + 1;
                                }
                                if (i == 0) {
                                    caseWithBLOBs1.setId(apiTestCaseWithBLOBs1.getId());
                                    caseWithBLOBs1.setNum(apiTestCaseWithBLOBs1.getNum());
                                    caseWithBLOBs1.setVersion(version);
                                } else {
                                    ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = new ApiTestCaseWithBLOBs();
                                    BeanUtils.copyBean(apiTestCaseWithBLOBs, caseWithBLOBs1);
                                    apiTestCaseWithBLOBs.setId(caseWithBLOBs.get(i).getId());
                                    apiTestCaseWithBLOBs.setNum(caseWithBLOBs.get(i).getNum());
                                    apiTestCaseWithBLOBs.setVersion(version);
                                    apiTestCaseWithBLOBs.setVersionId("create_repeat");
                                    optionDataCases.add(apiTestCaseWithBLOBs);
                                }
                            }
                        } else {
                            caseWithBLOBs1.setId(apiTestCaseWithBLOBs1.getId());
                            caseWithBLOBs1.setNum(apiTestCaseWithBLOBs1.getNum());
                            caseWithBLOBs1.setVersion(apiTestCaseWithBLOBs1.getVersion() == null ? 0 : apiTestCaseWithBLOBs1.getVersion() + 1);
                        }
                        oldCaseNameMap.remove(name);
                    } else {
                        caseWithBLOBs1.setVersion(0);
                    }
                }
            });
        } else {
            //否则直接给新增接口赋值新的接口ID
            caseNameMap.forEach((name, caseWithBLOBs1) -> {
                //如果导入的有重名，覆盖，接口ID替换成系统内的
                caseWithBLOBs1.setApiDefinitionId(definitionWithBLOBs.getId());
            });
        }
    }


    private Map<String, ApiTestCaseWithBLOBs> getDistinctCaseNameMap(Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        if (MapUtils.isEmpty(definitionIdCaseMAp)) {
            return null;
        }
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
        if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
            return caseWithBLOBs.stream().filter(t -> !StringUtils.equalsIgnoreCase("create_repeat", t.getVersionId())).collect(Collectors.toMap(ApiTestCase::getName, testCase -> testCase));
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
                    api.setNum(definitionWithBLOBs.getNum());
                    api.setStatus(definitionWithBLOBs.getStatus());
                    api.setVersionId(updateVersionId);
                    api.setModuleId(definitionWithBLOBs.getModuleId());
                    api.setModulePath(definitionWithBLOBs.getModulePath());
                    api.setOrder(definitionWithBLOBs.getOrder());
                    api.setRefId(apiDefinitionWithBLOBs.getRefId());
                    api.setLatest(apiDefinitionWithBLOBs.getLatest());
                    api.setCreateTime(definitionWithBLOBs.getCreateTime());
                    api.setUpdateTime(definitionWithBLOBs.getUpdateTime());
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
        List<String> intersection = repeatKeyList.stream().filter(item -> importKeysList.contains(item)).collect(Collectors.toList());
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

    private List<ApiDefinitionWithBLOBs> setModule(Map<String, ApiModule> moduleMap, Map<String, List<ApiModule>> pidChildrenMap,
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
        return data;
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
                ApiModule minModule = getMinModule(pathTree, moduleList, null, pidChildrenMap, moduleMap, idPathMap, idModuleMap);
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
        String chooseModuleParentId = chooseModule.getParentId();
        return chooseModuleParentId;
    }

    public String[] getPathTree(String modulePath) {
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

    private ApiModule getMinModule(String[] tagTree, List<ApiModule> parentModuleList, ApiModule parentModule, Map<String, List<ApiModule>> pidChildrenMap, Map<String, ApiModule> moduleMap
            , Map<String, String> idPathMap, Map<String, ApiModuleDTO> idModuleMap) {
        //如果parentModule==null 则证明需要创建根目录同级的模块
        ApiModule returnModule = null;
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
                returnModule = collect.get(0);
                parentModule = collect.get(0);
                parentModuleList = pidChildrenMap.get(collect.get(0).getId());
            }
        }
        return returnModule;
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
