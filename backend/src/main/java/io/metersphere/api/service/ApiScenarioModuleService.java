package io.metersphere.api.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.DragApiScenarioModuleRequest;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioModuleMapper;
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
import io.metersphere.track.service.TestPlanProjectService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioModuleService extends NodeTreeService<ApiScenarioModuleDTO> {

    @Resource
    ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    ExtApiScenarioModuleMapper extApiScenarioModuleMapper;
    @Resource
    ApiAutomationService apiAutomationService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    private ProjectService projectService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    public ApiScenarioModuleService() {
        super(ApiScenarioModuleDTO.class);
    }

    public List<ApiScenarioModuleDTO> getNodeTreeByProjectId(String projectId) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
       this.getDefaultNode(projectId);

        List<ApiScenarioModuleDTO> nodes = extApiScenarioModuleMapper.getNodeTreeByProjectId(projectId);
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setProjectId(projectId);
        List<String> list = new ArrayList<>();
        list.add("Prepare");
        list.add("Underway");
        list.add("Completed");
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);
        //优化：所有SQL统一查出来
//        nodes.forEach(node -> {
//            List<String> scenarioNodes = new ArrayList<>();
//            scenarioNodes = this.nodeList(nodes, node.getId(), scenarioNodes);
//            scenarioNodes.add(node.getId());
//            request.setModuleIds(scenarioNodes);
//            node.setCaseNum(extApiScenarioMapper.listModule(request));
//        });
        List<String> allModuleIdList = new ArrayList<>();
        for (ApiScenarioModuleDTO node : nodes) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(nodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            for (String moduleId : moduleIds) {
                if(!allModuleIdList.contains(moduleId)){
                    allModuleIdList.add(moduleId);
                }
            }
        }
        request.setModuleIds(allModuleIdList);
        List<Map<String,Object>> moduleCountList = extApiScenarioMapper.listModuleByCollection(request);
        Map<String,Integer> moduleCountMap = this.parseModuleCountList(moduleCountList);
        nodes.forEach(node -> {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(nodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            int countNum = 0;
            for (String moduleId : moduleIds) {
                if(moduleCountMap.containsKey(moduleId)){
                    countNum += moduleCountMap.get(moduleId).intValue();
                }
            }
            node.setCaseNum(countNum);
        });
        return getNodeTrees(nodes);
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

    public static List<String> nodeList(List<ApiScenarioModuleDTO> nodes, String pid, List<String> list) {
        for (ApiScenarioModuleDTO node : nodes) {
            //遍历出父id等于参数的id，add进子节点集合
            if (StringUtils.equals(node.getParentId(), pid)) {
                list.add(node.getId());
                //递归遍历下一级
                nodeList(nodes, node.getId(), list);
            }
        }

        return list;
    }

    private double getNextLevelPos(String projectId, int level, String parentId) {
        List<ApiScenarioModule> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<ApiScenarioModule> getPos(String projectId, int level, String parentId, String order) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        ApiScenarioModuleExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return apiScenarioModuleMapper.selectByExample(example);
    }

    public String addNode(ApiScenarioModule node) {
        validateNode(node);
        node.setId(UUID.randomUUID().toString());
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        apiScenarioModuleMapper.insertSelective(node);
        return node.getId();
    }

    public List<ApiScenarioModuleDTO> getNodeByPlanId(String planId) {
        List<ApiScenarioModuleDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        projectIds.forEach(id -> {
            Project project = projectService.getProjectById(id);
            String name = project.getName();
            List<ApiScenarioModuleDTO> nodeList = getNodeDTO(id, planId);
            ApiScenarioModuleDTO scenarioModuleDTO = new ApiScenarioModuleDTO();
            scenarioModuleDTO.setId(project.getId());
            scenarioModuleDTO.setName(name);
            scenarioModuleDTO.setLabel(name);
            scenarioModuleDTO.setChildren(nodeList);
            if (!org.springframework.util.CollectionUtils.isEmpty(nodeList)) {
                list.add(scenarioModuleDTO);
            }
        });
        return list;
    }

    private List<ApiScenarioModuleDTO> getNodeDTO(String projectId, String planId) {
        List<TestPlanApiScenario> apiCases = testPlanScenarioCaseService.getCasesByPlanId(planId);
        if (apiCases.isEmpty()) {
            return null;
        }
        List<ApiScenarioModuleDTO> testCaseNodes = extApiScenarioModuleMapper.getNodeTreeByProjectId(projectId);

        List<String> caseIds = apiCases.stream()
                .map(TestPlanApiScenario::getApiScenarioId)
                .collect(Collectors.toList());

        List<String> dataNodeIds = apiAutomationService.selectByIds(caseIds).stream()
                .map(ApiScenario::getApiScenarioModuleId)
                .collect(Collectors.toList());

        List<ApiScenarioModuleDTO> nodeTrees = getNodeTrees(testCaseNodes);

        Iterator<ApiScenarioModuleDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ApiScenarioModuleDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }


    private void validateNode(ApiScenarioModule node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkApiScenarioModuleExist(node);
    }

    private void checkApiScenarioModuleExist(ApiScenarioModule node) {
        if (node.getName() != null) {
            ApiScenarioModuleExample example = new ApiScenarioModuleExample();
            ApiScenarioModuleExample.Criteria criteria = example.createCriteria();
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
            if (apiScenarioModuleMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private List<ApiScenarioDTO> queryByModuleIds(DragApiScenarioModuleRequest request) {
        ApiScenarioRequest apiScenarioRequest = new ApiScenarioRequest();
        apiScenarioRequest.setProjectId(request.getProjectId());
        apiScenarioRequest.setModuleIds(request.getNodeIds());
        return apiAutomationService.list(apiScenarioRequest);
    }

    public int editNode(DragApiScenarioModuleRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkApiScenarioModuleExist(request);
        List<ApiScenarioDTO> apiScenarios = queryByModuleIds(request);
        apiScenarios.forEach(apiScenario -> {
            String modulePath = apiScenario.getModulePath();
            StringBuilder path = new StringBuilder(modulePath == null ? "" : modulePath);
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            if (pathLists.size() > request.getLevel()) {
                pathLists.set(request.getLevel(), request.getName());
                path.delete(0, path.length());
                for (int i = 1; i < pathLists.size(); i++) {
                    path.append("/").append(pathLists.get(i));
                }
                apiScenario.setModulePath(path.toString());
            }
        });
        batchUpdateApiScenario(apiScenarios);

        return apiScenarioModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        apiAutomationService.removeToGcByIds(nodeIds);

        ApiScenarioModuleExample apiScenarioModuleExample = new ApiScenarioModuleExample();
        apiScenarioModuleExample.createCriteria().andIdIn(nodeIds);
        return apiScenarioModuleMapper.deleteByExample(apiScenarioModuleExample);
    }

    private void batchUpdateApiScenario(List<ApiScenarioDTO> apiScenarios) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper apiScenarioMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        apiScenarios.forEach(apiScenarioMapper::updateByPrimaryKey);
        sqlSession.flushStatements();
    }

    @Override
    public ApiScenarioModuleDTO getNode(String id) {
        ApiScenarioModule module = apiScenarioModuleMapper.selectByPrimaryKey(id);
        ApiScenarioModuleDTO dto = JSON.parseObject(JSON.toJSONString(module), ApiScenarioModuleDTO.class);
        return dto;
    }

    public ApiScenarioModule getNewModule(String name, String projectId, int level) {
        ApiScenarioModule node = new ApiScenarioModule();
        buildNewModule(node);
        node.setLevel(level);
        node.setName(name);
        node.setProjectId(projectId);
        return node;
    }

    public void buildNewModule(ApiScenarioModule node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
    }

    public List<ApiScenarioModule> selectSameModule(ApiScenarioModule node) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        ApiScenarioModuleExample.Criteria criteria = example.createCriteria();
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
        return apiScenarioModuleMapper.selectByExample(example);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extApiScenarioModuleMapper.updatePos(id, pos);
    }

    public void dragNode(DragApiScenarioModuleRequest request) {

        checkApiScenarioModuleExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<ApiScenarioDTO> apiScenarios = queryByModuleIds(request);

        ApiScenarioModuleDTO nodeTree = request.getNodeTree();

        List<ApiScenarioModule> updateNodes = new ArrayList<>();
        if (nodeTree == null) {
            return;
        }
        buildUpdateDefinition(nodeTree, apiScenarios, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateModule(updateNodes);

        batchUpdateApiScenario(apiScenarios);
    }

    private void buildUpdateDefinition(ApiScenarioModuleDTO rootNode, List<ApiScenarioDTO> apiScenarios,
                                       List<ApiScenarioModule> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }
        if ("root".equals(rootNode.getId())) {
            rootPath = "";
        }
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId(rootNode.getId());
        apiScenarioModule.setLevel(level);
        apiScenarioModule.setParentId(pId);
        updateNodes.add(apiScenarioModule);

        for (ApiScenario item : apiScenarios) {
            if (StringUtils.equals(item.getApiScenarioModuleId(), rootNode.getId())) {
                item.setModulePath(rootPath);
            }
        }

        List<ApiScenarioModuleDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (ApiScenarioModuleDTO child : children) {
                buildUpdateDefinition(child, apiScenarios, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    private void batchUpdateModule(List<ApiScenarioModule> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioModuleMapper apiScenarioModuleMapper = sqlSession.getMapper(ApiScenarioModuleMapper.class);
        updateNodes.forEach(apiScenarioModuleMapper::updateByPrimaryKeySelective);
        sqlSession.flushStatements();
    }

    public String getLogDetails(List<String> ids) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioModule> nodes = apiScenarioModuleMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(ApiScenarioModule::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ApiScenarioModule node) {
        ApiScenarioModule module = null;
        if (StringUtils.isNotEmpty(node.getId())) {
            module = apiScenarioModuleMapper.selectByPrimaryKey(node.getId());
        }
        if (module == null && StringUtils.isNotEmpty(node.getName())) {
            ApiScenarioModuleExample example = new ApiScenarioModuleExample();
            ApiScenarioModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotEmpty(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotEmpty(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            List<ApiScenarioModule> list = apiScenarioModuleMapper.selectByExample(example);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
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

    public long countById(String id) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andIdEqualTo(id);
        return apiScenarioModuleMapper.countByExample(example);
    }

    public ApiScenarioModule getDefaultNode(String projectId) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo("默认模块").andParentIdIsNull();
        List<ApiScenarioModule> list = apiScenarioModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            ApiScenarioModule record = new ApiScenarioModule();
            record.setId(UUID.randomUUID().toString());
            record.setName("默认模块");
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            apiScenarioModuleMapper.insert(record);
            return  record;
        }else {
            return list.get(0);
        }
    }
}
