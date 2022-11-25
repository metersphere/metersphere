package io.metersphere.service.definition;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.service.NodeTreeService;
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

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ProjectMapper projectMapper;

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
        Map<String, List<ApiDefinition>> trashApiMap =
                apiDefinitionService.selectApiBaseInfoGroupByModuleId(projectId, protocol, versionId,
                        ApiTestDataStatus.TRASH.getValue());
        //查找回收站里的模块
        List<ApiModuleDTO> trashModuleList = this.selectTreeStructModuleById(trashApiMap.keySet());
        this.initApiCount(trashModuleList, trashApiMap);
        return getNodeTrees(trashModuleList);
    }

    public List<ApiModuleDTO> getTrashNodeTreeByProtocolAndProjectId(String projectId, String protocol, String versionId, ApiDefinitionRequest request) {
        //回收站数据初始化：检查是否存在模块被删除的接口，则把接口挂再默认节点上
        initTrashDataModule(projectId, protocol, versionId);
        //通过回收站里的接口模块进行反显
        request.setModuleIds(null);
        Map<String, List<ApiDefinition>> trashApiMap =
                apiDefinitionService.selectApiBaseInfoGroupByModuleId(projectId, protocol, versionId,
                        ApiTestDataStatus.TRASH.getValue(), request);
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
            apiDefinitionService.updateNoModuleApiToDefaultModule(projectId, protocol, ApiTestDataStatus.TRASH.getValue(), versionId,
                    defaultModule.getId());
        }
    }

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol, String versionId) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        List<ApiModuleDTO> apiModules = getApiModulesByProjectAndPro(projectId, protocol);
        request.setProjectId(projectId);
        request.setProtocol(protocol);
        List<String> list = new ArrayList<>();
        list.add(ApiTestDataStatus.PREPARE.getValue());
        list.add(ApiTestDataStatus.UNDERWAY.getValue());
        list.add(ApiTestDataStatus.COMPLETED.getValue());
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

    public List<ApiModuleDTO> getNodeTreeByCondition(String projectId, String protocol, String versionId, ApiDefinitionRequest request) {
        List<ApiModuleDTO> apiModules = getApiModulesByProjectAndPro(projectId, protocol);
        request.setProjectId(projectId);
        request.setProtocol(protocol);
        List<String> list = new ArrayList<>();
        list.add(ApiTestDataStatus.PREPARE.getValue());
        list.add(ApiTestDataStatus.UNDERWAY.getValue());
        list.add(ApiTestDataStatus.COMPLETED.getValue());
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
        if (PropertyConstant.ROOT.equals(rootNode.getId())) {
            rootPath = StringUtils.EMPTY;
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

    public void initDefaultNode() {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andNameEqualTo("默认项目");
        List<Project> projects = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isNotEmpty(projects)) {
            String[] protocols = {RequestTypeConstants.HTTP, RequestTypeConstants.DUBBO, RequestTypeConstants.SQL, RequestTypeConstants.TCP};
            for (String protocol : protocols) {
                saveDefault(projects.get(0).getId(), protocol);
            }
        }
    }

    public ApiModule getDefaultNode(String projectId, String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<ApiModule> list = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    public ApiModule saveDefault(String projectId, String protocol) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<ApiModule> list = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            ApiModule module = new ApiModule();
            module.setId(UUID.randomUUID().toString());
            module.setName(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName());
            module.setProtocol(protocol);
            module.setPos(1.0);
            module.setLevel(1);
            module.setCreateTime(System.currentTimeMillis());
            module.setUpdateTime(System.currentTimeMillis());
            module.setProjectId(projectId);
            module.setCreateUser(SessionUtils.getUserId());
            apiModuleMapper.insert(module);
            return module;
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
        initParentModulePathMap.put(PropertyConstant.ROOT, initParentModulePath);
        buildProcessData(nodeTreeByProjectId, pidChildrenMap, idPathMap, initParentModulePathMap);

        //导入的case,导入的接口是有ID的，所以导入的case已经标记过一遍接口ID了,这里是处理覆盖时接口ID的变动
        List<ApiTestCaseWithBLOBs> importCases = apiImport.getCases();

        if (protocol.equals("HTTP")) {
            return dealHttp(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, urlRepeat, importCases);
        } else {
            return delOtherProtocol(data, pidChildrenMap, idPathMap, idModuleMap, request, fullCoverage, importCases);
        }

    }

    private UpdateApiModuleDTO delOtherProtocol(List<ApiDefinitionWithBLOBs> data,
                                                Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap,
                                                Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request,
                                                Boolean fullCoverage, List<ApiTestCaseWithBLOBs> importCases) {
        List<ApiDefinitionWithBLOBs> optionData = new ArrayList<>();
        //去重，TCP,SQL,DUBBO 模块下名称唯一
        removeRepeatOrigin(data, fullCoverage, optionData);
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
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionData, chooseModule);

        return getUpdateApiModuleDTO(chooseModule, idPathMap, optionData, fullCoverage, moduleMap, optionDataCases);
    }

    private UpdateApiModuleDTO dealHttp(List<ApiDefinitionWithBLOBs> data,
                                        Map<String, List<ApiModule>> pidChildrenMap, Map<String, String> idPathMap,
                                        Map<String, ApiModuleDTO> idModuleMap, ApiTestImportRequest request,
                                        Boolean fullCoverage, boolean urlRepeat, List<ApiTestCaseWithBLOBs> importCases) {

        List<ApiDefinitionWithBLOBs> optionData = new ArrayList<>();
        //去重 如果url可重复 则模块+名称+请求方式+路径 唯一，否则 请求方式+路径唯一，
        //覆盖模式留重复的最后一个，不覆盖留第一个
        removeHttpRepeat(data, fullCoverage, urlRepeat, optionData);

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
        setModule(moduleMap, pidChildrenMap, idPathMap, idModuleMap, optionData, chooseModule);


        return getUpdateApiModuleDTO(chooseModule, idPathMap, optionData, fullCoverage, moduleMap, optionDataCases);

    }

    @NotNull
    private Boolean getFullCoverage(ApiDefinitionImport apiImport, Boolean fullCoverage) {
        if (fullCoverage == null) {
            fullCoverage = false;
        }
        return fullCoverage;
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

    private UpdateApiModuleDTO getUpdateApiModuleDTO(ApiModuleDTO chooseModule, Map<String, String> idPathMap, List<ApiDefinitionWithBLOBs> optionData, Boolean fullCoverage, Map<String, ApiModule> moduleMap, List<ApiTestCaseWithBLOBs> optionDataCases) {
        UpdateApiModuleDTO updateApiModuleDTO = new UpdateApiModuleDTO();
        updateApiModuleDTO.setChooseModule(chooseModule);
        updateApiModuleDTO.setIdPathMap(idPathMap);
        updateApiModuleDTO.setFullCoverage(fullCoverage);
        updateApiModuleDTO.setModuleMap(moduleMap);
        updateApiModuleDTO.setDefinitionWithBLOBs(optionData);
        updateApiModuleDTO.setCaseWithBLOBs(optionDataCases);
        return updateApiModuleDTO;
    }

    private void removeRepeatOrigin(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, List<ApiDefinitionWithBLOBs> optionData) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + (t.getModulePath() == null ? StringUtils.EMPTY : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
        if (fullCoverage) {
            methodPathMap.forEach((k, v) -> optionData.add(v.get(v.size() - 1)));
        } else {
            methodPathMap.forEach((k, v) -> optionData.add(v.get(0)));
        }
    }

    private void removeHttpRepeat(List<ApiDefinitionWithBLOBs> data, Boolean fullCoverage, boolean urlRepeat, List<ApiDefinitionWithBLOBs> optionData) {
        if (urlRepeat) {
            LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getName() + t.getMethod() + t.getPath() + (t.getModulePath() == null ? StringUtils.EMPTY : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
            if (fullCoverage) {
                methodPathMap.forEach((k, v) -> optionData.add(v.get(v.size() - 1)));
            } else {
                methodPathMap.forEach((k, v) -> optionData.add(v.get(0)));
            }
        } else {
            LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = data.stream().collect(Collectors.groupingBy(t -> t.getMethod().concat(t.getPath()), LinkedHashMap::new, Collectors.toList()));
            if (fullCoverage) {
                methodPathMap.forEach((k, v) -> optionData.add(v.get(v.size() - 1)));
            } else {
                methodPathMap.forEach((k, v) -> optionData.add(v.get(0)));
            }
        }
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
                List<ApiModule> moduleList = pidChildrenMap.get(PropertyConstant.ROOT);
                ApiModule minModule = getMinModule(pathTree, moduleList, pidChildrenMap, moduleMap, idPathMap, idModuleMap);
                String id = minModule.getId();
                datum.setModuleId(id);
                datum.setModulePath(idPathMap.get(id));
            }
        } else {
            //导入时即没选中模块，接口自身也没模块的，直接返会当前项目，当前协议下的默认模块
            List<ApiModule> moduleList = pidChildrenMap.get(PropertyConstant.ROOT);
            for (ApiModule module : moduleList) {
                if (module.getName().equals(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName())) {
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
                    parentModule.setProjectId(pidChildrenMap.get(PropertyConstant.ROOT).get(0).getProjectId());
                    parentModule.setId(PropertyConstant.ROOT);
                    parentModule.setLevel(0);
                    parentModule.setProtocol(pidChildrenMap.get(PropertyConstant.ROOT).get(0).getProtocol());
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
            if (parentModule.getId().equals(PropertyConstant.ROOT)) {
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
            if (StringUtils.isBlank(apiModuleDTO.getParentId()) || StringUtils.equals(apiModuleDTO.getParentId(), "0")) {
                apiModuleDTO.setParentId(PropertyConstant.ROOT);
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
}
