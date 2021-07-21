package io.metersphere.api.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.definition.*;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiModuleMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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
    SqlSessionFactory sqlSessionFactory;

    public ApiModuleService() {
        super(ApiModuleDTO.class);
    }

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNode(projectId,protocol);
        List<ApiModuleDTO> apiModules = extApiModuleMapper.getNodeTreeByProjectId(projectId, protocol);
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setProjectId(projectId);
        request.setProtocol(protocol);
        List<String> list = new ArrayList<>();
        list.add("Prepare");
        list.add("Underway");
        list.add("Completed");
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);
//        apiModules.forEach(node -> {
//            List<String> moduleIds = new ArrayList<>();
//            moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
//            moduleIds.add(node.getId());
//            request.setModuleIds(moduleIds);
//            node.setCaseNum(extApiDefinitionMapper.moduleCount(request));
//        });

        //优化： 所有统计SQL一次查询出来
        List<String> allModuleIdList = new ArrayList<>();
        for (ApiModuleDTO node : apiModules) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            for (String moduleId : moduleIds) {
                if(!allModuleIdList.contains(moduleId)){
                    allModuleIdList.add(moduleId);
                }
            }
        }
        request.setModuleIds(allModuleIdList);
        List<Map<String,Object>> moduleCountList = extApiDefinitionMapper.moduleCountByCollection(request);
        Map<String,Integer> moduleCountMap = this.parseModuleCountList(moduleCountList);
        apiModules.forEach(node -> {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(apiModules, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            int countNum = 0;
            for (String moduleId : moduleIds) {
                if(moduleCountMap.containsKey(moduleId)){
                    countNum += moduleCountMap.get(moduleId).intValue();
                }
            }
            node.setCaseNum(countNum);
        });
        return getNodeTrees(apiModules);
    }

    private Map<String, Integer> parseModuleCountList(List<Map<String, Object>> moduleCountList) {
        Map<String,Integer> returnMap = new HashMap<>();
        for (Map<String, Object> map: moduleCountList){
            Object moduleIdObj = map.get("moduleId");
            Object countNumObj = map.get("countNum");
            if(moduleIdObj!= null && countNumObj != null){
                String moduleId = String.valueOf(moduleIdObj);
                try {
                    Integer countNumInteger = new Integer(String.valueOf(countNumObj));
                    returnMap.put(moduleId,countNumInteger);
                }catch (Exception e){
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

    private double getNextLevelPos(String projectId, int level, String parentId) {
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
        List<ApiModuleDTO> testCaseNodes = extApiModuleMapper.getNodeTreeByProjectId(projectId, protocol);

        List<String> caseIds = apiCases.stream()
                .map(TestPlanApiCase::getApiCaseId)
                .collect(Collectors.toList());

        List<String> definitionIds = apiTestCaseService.selectCasesBydIds(caseIds).stream()
                .map(ApiTestCase::getApiDefinitionId)
                .collect(Collectors.toList());

        List<String> dataNodeIds = apiDefinitionService.selectApiDefinitionBydIds(definitionIds).stream()
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
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
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

            if (StringUtils.isNotBlank(node.getProtocol())) {
                criteria.andProtocolEqualTo(node.getProtocol());
            }

            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
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
                .andProjectIdEqualTo(node.getProjectId());
        if (StringUtils.isNotBlank(node.getParentId())) {
            criteria.andParentIdEqualTo(node.getParentId());
        } else {
            criteria.andParentIdIsNull();
        }
        if (StringUtils.isNotBlank(node.getId())) {
            criteria.andIdNotEqualTo(node.getId());
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
            apiDefinitionMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
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
    }

    public ApiModule getModuleByName(String projectId, String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        ApiModuleExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo("bug")
                .andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol);
        List<ApiModule> modules = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(modules)) {
            return modules.get(0);
        } else {
            ApiModule node = new ApiModule();
            node.setName("bug");
            node.setLevel(1);
            node.setPos(0.0);
            node.setParentId(null);
            node.setProjectId(projectId);
            node.setProtocol(protocol);
            node.setCreateTime(System.currentTimeMillis());
            node.setUpdateTime(System.currentTimeMillis());
            node.setId(UUID.randomUUID().toString());
            apiModuleMapper.insertSelective(node);
            return node;
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
        return  apiModuleMapper.countByExample(example);
    }

    public ApiModule getDefaultNode(String projectId,String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo("默认模块").andParentIdIsNull();;
        List<ApiModule> list = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            ApiModule record = new ApiModule();
            record.setId(UUID.randomUUID().toString());
            record.setName("默认模块");
            record.setProtocol(protocol);
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            apiModuleMapper.insert(record);
            return record;
        }else {
            return list.get(0);
        }
    }
}
