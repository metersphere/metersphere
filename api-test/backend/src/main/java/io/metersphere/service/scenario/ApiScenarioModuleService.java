package io.metersphere.service.scenario;


import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.*;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioModuleExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioModuleMapper;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.service.NodeTreeService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioModuleService extends NodeTreeService<ApiScenarioModuleDTO> {

    @Resource
    ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    ExtApiScenarioModuleMapper extApiScenarioModuleMapper;
    @Resource
    ApiScenarioService apiAutomationService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
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
        list.add(ApiTestDataStatus.PREPARE.getValue());
        list.add(ApiTestDataStatus.UNDERWAY.getValue());
        list.add(ApiTestDataStatus.COMPLETED.getValue());
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);
        List<String> allModuleIdList = new ArrayList<>();
        for (ApiScenarioModuleDTO node : nodes) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(nodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            for (String moduleId : moduleIds) {
                if (!allModuleIdList.contains(moduleId)) {
                    allModuleIdList.add(moduleId);
                }
            }
        }
        request.setModuleIds(allModuleIdList);
        List<Map<String, Object>> moduleCountList = extApiScenarioMapper.listModuleByCollection(request);
        Map<String, Integer> moduleCountMap = this.parseModuleCountList(moduleCountList);
        nodes.forEach(node -> {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = this.nodeList(nodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            int countNum = 0;
            for (String moduleId : moduleIds) {
                if (moduleCountMap.containsKey(moduleId)) {
                    countNum += moduleCountMap.get(moduleId).intValue();
                }
            }
            node.setCaseNum(countNum);
        });
        return getNodeTrees(nodes);
    }

    public List<ApiScenarioModuleDTO> getTrashNodeTreeByProjectId(String projectId) {
        //回收站数据初始化：被删除了的数据挂在默认模块上
        initTrashDataModule(projectId);
        //通过回收站里的接口模块进行反显
        Map<String, List<ApiScenario>> trashApiMap = apiAutomationService.selectApiBaseInfoGroupByModuleId(projectId,
                ApiTestDataStatus.TRASH.getValue());
        //查找回收站里的模块
        List<ApiScenarioModuleDTO> trashModuleList = this.selectTreeStructModuleById(trashApiMap.keySet());
        this.initApiCount(trashModuleList, trashApiMap);
        return getNodeTrees(trashModuleList);
    }

    private void initApiCount(List<ApiScenarioModuleDTO> moduleDTOList, Map<String, List<ApiScenario>> scenarioMap) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(moduleDTOList) && MapUtils.isNotEmpty(scenarioMap)) {
            moduleDTOList.forEach(node -> {
                List<String> moduleIds = new ArrayList<>();
                moduleIds = this.nodeList(moduleDTOList, node.getId(), moduleIds);
                moduleIds.add(node.getId());
                int countNum = 0;
                for (String moduleId : moduleIds) {
                    if (scenarioMap.containsKey(moduleId)) {
                        countNum += scenarioMap.get(moduleId).size();
                    }
                }
                node.setCaseNum(countNum);
            });
        }
    }

    private List<ApiScenarioModuleDTO> selectTreeStructModuleById(Collection<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(0);
        } else {
            List<String> parentIdList = new ArrayList<>();
            List<ApiScenarioModuleDTO> apiModuleList = extApiScenarioModuleMapper.selectByIds(ids);
            apiModuleList.forEach(apiModuleDTO -> {
                if (StringUtils.isNotBlank(apiModuleDTO.getParentId()) && !parentIdList.contains(apiModuleDTO.getParentId())) {
                    parentIdList.add(apiModuleDTO.getParentId());
                }
            });
            apiModuleList.addAll(0, this.selectTreeStructModuleById(parentIdList));
            List<ApiScenarioModuleDTO> returnList = new ArrayList<>(apiModuleList.stream().collect(Collectors.toMap(ApiScenarioModuleDTO::getId, Function.identity(), (t1, t2) -> t1)).values());
            return returnList;
        }
    }

    private void initTrashDataModule(String projectId) {
        ApiScenarioModule defaultModule = this.getDefaultNode(projectId);
        if (defaultModule != null) {
            apiAutomationService.updateNoModuleToDefaultModule(projectId, ApiTestDataStatus.TRASH.getValue(), defaultModule.getId());
        }
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

    public double getNextLevelPos(String projectId, int level, String parentId) {
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

    private void validateNode(ApiScenarioModule node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            MSException.throwException(Translator.get("test_case_node_level_tip")
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
                criteria.andLevelEqualTo(node.getLevel());
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
            StringBuilder path = new StringBuilder(modulePath == null ? StringUtils.EMPTY : modulePath);
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
        apiScenarios.forEach(apiScenarioMapper::updateByPrimaryKeySelective);
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
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
                .andProjectIdEqualTo(node.getProjectId())
                .andLevelEqualTo(node.getLevel());

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
        if (PropertyConstant.ROOT.equals(rootNode.getId())) {
            rootPath = StringUtils.EMPTY;
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
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
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
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo("未规划场景").andParentIdIsNull();
        List<ApiScenarioModule> list = apiScenarioModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            ApiScenarioModule record = new ApiScenarioModule();
            record.setId(UUID.randomUUID().toString());
            record.setName("未规划场景");
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            record.setCreateUser(SessionUtils.getUserId());
            apiScenarioModuleMapper.insert(record);
            return record;
        } else {
            return list.get(0);
        }
    }

    /**
     * 上传文件时对文件的模块进行检测
     *
     * @param data
     * @param fullCoverage         是否覆盖接口
     * @param fullCoverageScenario 是否更新当前接口所在模块
     * @return Return to the newly added module map
     */
    public UpdateScenarioModuleDTO checkScenarioModule(ApiTestImportRequest request, List<ApiScenarioWithBLOBs> data, Boolean fullCoverage, Boolean fullCoverageScenario) {
        //需要新增的模块，key 为模块路径
        Map<String, ApiScenarioModule> moduleMap = new HashMap<>();
        List<ApiScenarioWithBLOBs> toUpdateList = new ArrayList<>();

        //上传文件时选的模块ID
        String chooseModuleId = request.getModuleId();
        String projectId = request.getProjectId();

        if (fullCoverageScenario == null) {
            fullCoverageScenario = false;
        }

        //获取当前项目的当前协议下的所有模块的Tree
        List<ApiScenarioModuleDTO> scenarioModules = extApiScenarioModuleMapper.getNodeTreeByProjectId(projectId);
        List<ApiScenarioModuleDTO> nodeTreeByProjectId = this.getNodeTrees(scenarioModules);

        Map<String, ApiScenarioModuleDTO> idModuleMap = scenarioModules.stream().collect(Collectors.toMap(ApiScenarioModuleDTO::getId, scenarioModuleDTO -> scenarioModuleDTO));

        Map<String, List<ApiScenarioModule>> pidChildrenMap = new HashMap<>();
        Map<String, String> idPathMap = new HashMap<>();
        //构建以上两种数据
        String initParentModulePath = "/root";
        Map<String, String> initParentModulePathMap = new HashMap<>();
        initParentModulePathMap.put(PropertyConstant.ROOT, initParentModulePath);
        buildProcessData(nodeTreeByProjectId, pidChildrenMap, idPathMap, initParentModulePathMap);

        ApiScenarioModuleDTO chooseModule = null;
        if (chooseModuleId != null) {
            chooseModule = idModuleMap.get(chooseModuleId);
        }

        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);

        List<ApiScenarioWithBLOBs> optionData = new ArrayList<>();

        //覆盖模式留重复的最后一个，不覆盖留第一个
        LinkedHashMap<String, List<ApiScenarioWithBLOBs>> nameModuleMapList = data.stream().collect(Collectors.groupingBy(t -> t.getName() + (t.getModulePath() == null ? StringUtils.EMPTY : t.getModulePath()), LinkedHashMap::new, Collectors.toList()));
        removeRepeatOrigin(fullCoverage, optionData, nameModuleMapList);

        //处理模块
        setModule(optionData, moduleMap, pidChildrenMap, idPathMap, idModuleMap, chooseModule);
        List<String> names = optionData.stream().map(ApiScenario::getName).collect(Collectors.toList());
        //系统内重复的数据
        List<ApiScenarioWithBLOBs> repeatAllScenarioWithBLOBs = extApiScenarioMapper.selectRepeatByBLOBs(names, projectId);
        ArrayList<ApiScenarioWithBLOBs> repeatApiScenarioWithBLOBs = repeatAllScenarioWithBLOBs.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(t -> t.getName() + t.getModulePath()))), ArrayList::new)
        );

        Map<String, ApiScenarioWithBLOBs> nameModuleMap = null;
        Map<String, ApiScenarioWithBLOBs> repeatDataMap = null;
        if (chooseModule != null) {
            if (!CollectionUtils.isEmpty(repeatApiScenarioWithBLOBs)) {
                String chooseModuleParentId = getChooseModuleParentId(chooseModule);
                String chooseModulePath = getChooseModulePath(idPathMap, chooseModule, chooseModuleParentId);
                nameModuleMap = optionData.stream().collect(Collectors.toMap(t -> t.getName() + chooseModulePath, scenario -> scenario));
                repeatDataMap = repeatApiScenarioWithBLOBs.stream().filter(t -> t.getApiScenarioModuleId().equals(chooseModuleId)).collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), scenario -> scenario));
            }
        } else {
            nameModuleMap = optionData.stream().collect(Collectors.toMap(t -> t.getName() + (t.getModulePath() == null ? StringUtils.EMPTY : t.getModulePath()), scenario -> scenario));
            repeatDataMap = repeatApiScenarioWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), scenario -> scenario));
        }
        //处理数据
        if (fullCoverage) {
            if (fullCoverageScenario) {
                startCoverModule(toUpdateList, nameModuleMap, repeatDataMap, updateVersionId);
            } else {
                //覆盖但不覆盖模块
                if (nameModuleMap != null) {
                    //导入文件没有新增场景无需创建接口模块
                    moduleMap = judgeModuleMap(moduleMap, nameModuleMap, repeatDataMap);
                    startCover(toUpdateList, nameModuleMap, repeatDataMap, updateVersionId);
                }
            }
        } else {
            //不覆盖
            removeRepeat(optionData, nameModuleMap, repeatDataMap, moduleMap, versionId);
        }

        if (!CollectionUtils.isEmpty(repeatApiScenarioWithBLOBs)) {
            Map<String, ApiScenarioWithBLOBs> repeatMap = repeatApiScenarioWithBLOBs.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), scenario -> scenario));
            Map<String, ApiScenarioWithBLOBs> optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName() + t.getModulePath(), scenario -> scenario));
            if (fullCoverage) {
                startCover(toUpdateList, optionMap, repeatMap, updateVersionId);
            } else {
                //不覆盖,同一接口不做更新
                removeRepeat(optionData, optionMap, repeatMap, moduleMap, versionId);
            }
        }

        if (optionData.isEmpty()) {
            moduleMap = new HashMap<>();
        }
        UpdateScenarioModuleDTO updateScenarioModuleDTO = new UpdateScenarioModuleDTO();
        updateScenarioModuleDTO.setModuleList(new ArrayList<>(moduleMap.values()));
        updateScenarioModuleDTO.setNeedUpdateList(toUpdateList);
        updateScenarioModuleDTO.setApiScenarioWithBLOBsList(optionData);
        return updateScenarioModuleDTO;
    }

    private void removeRepeat(List<ApiScenarioWithBLOBs> optionData, Map<String, ApiScenarioWithBLOBs> nameModuleMap,
                              Map<String, ApiScenarioWithBLOBs> repeatDataMap, Map<String, ApiScenarioModule> moduleMap,
                              String versionId) {
        if (repeatDataMap != null) {
            Map<String, List<ApiScenarioWithBLOBs>> moduleOptionData = optionData.stream().collect(Collectors.groupingBy(ApiScenario::getModulePath));
            repeatDataMap.forEach((k, v) -> {
                ApiScenarioWithBLOBs apiScenarioWithBLOBs = nameModuleMap.get(k);
                if (apiScenarioWithBLOBs != null) {
                    String modulePath = apiScenarioWithBLOBs.getModulePath();
                    List<ApiScenarioWithBLOBs> moduleDatas = moduleOptionData.get(modulePath);
                    if (moduleDatas != null && moduleDatas.size() <= 1) {
                        moduleMap.remove(modulePath);
                        removeModulePath(moduleMap, moduleOptionData, modulePath);
                        moduleDatas.remove(apiScenarioWithBLOBs);
                    }
                    //不覆盖选择版本，如果被选版本有同接口，不导入，否则创建新版本接口
                    if (v.getVersionId().equals(versionId)) {
                        optionData.remove(apiScenarioWithBLOBs);
                    } else {
                        addNewVersionScenario(apiScenarioWithBLOBs, v, "new");
                    }
                }
            });
        }
    }

    private void addNewVersionScenario(ApiScenarioWithBLOBs apiScenarioWithBLOBs, ApiScenarioWithBLOBs v, String version) {
        apiScenarioWithBLOBs.setVersionId(version);
        apiScenarioWithBLOBs.setNum(v.getNum());
        apiScenarioWithBLOBs.setStatus(v.getStatus());
        apiScenarioWithBLOBs.setCreateTime(v.getCreateTime());
        apiScenarioWithBLOBs.setRefId(v.getRefId());
        apiScenarioWithBLOBs.setOrder(v.getOrder());
        apiScenarioWithBLOBs.setLatest(v.getLatest());
    }

    private void removeModulePath(Map<String, ApiScenarioModule> moduleMap, Map<String, List<ApiScenarioWithBLOBs>> moduleOptionData, String modulePath) {
        if (StringUtils.isBlank(modulePath)) {
            return;
        }
        String[] pathTree = getTagTree(modulePath);
        String lastPath = pathTree[pathTree.length - 1];
        String substring = modulePath.substring(0, modulePath.indexOf("/" + lastPath));
        if (moduleOptionData.get(substring) == null || moduleOptionData.get(substring).size() == 0) {
            moduleMap.remove(substring);
            removeModulePath(moduleMap, moduleOptionData, substring);
        }

    }

    private void startCover(List<ApiScenarioWithBLOBs> toUpdateList, Map<String, ApiScenarioWithBLOBs> nameModuleMap, Map<String, ApiScenarioWithBLOBs> repeatDataMap, String updateVersionId) {
        repeatDataMap.forEach((k, v) -> {
            ApiScenarioWithBLOBs apiScenarioWithBLOBs = nameModuleMap.get(k);
            if (apiScenarioWithBLOBs != null) {
                if (!v.getVersionId().equals(updateVersionId)) {
                    addNewVersionScenario(apiScenarioWithBLOBs, v, "update");
                    return;
                }
                apiScenarioWithBLOBs.setId(v.getId());
                apiScenarioWithBLOBs.setVersionId(updateVersionId);
                apiScenarioWithBLOBs.setApiScenarioModuleId(v.getApiScenarioModuleId());
                apiScenarioWithBLOBs.setModulePath(v.getModulePath());
                apiScenarioWithBLOBs.setNum(v.getNum());
                apiScenarioWithBLOBs.setStatus(v.getStatus());
                apiScenarioWithBLOBs.setCreateTime(v.getCreateTime());
                apiScenarioWithBLOBs.setRefId(v.getRefId());
                apiScenarioWithBLOBs.setOrder(v.getOrder());
                apiScenarioWithBLOBs.setLatest(v.getLatest());
                apiScenarioWithBLOBs.setCreateTime(v.getCreateTime());
                toUpdateList.add(apiScenarioWithBLOBs);
            }
        });
    }

    private Map<String, ApiScenarioModule> judgeModuleMap(Map<String, ApiScenarioModule> moduleMap, Map<String, ApiScenarioWithBLOBs> nameModuleMap, Map<String, ApiScenarioWithBLOBs> repeatDataMap) {
        AtomicBoolean remove = new AtomicBoolean(true);

        if (repeatDataMap.size() >= nameModuleMap.size()) {
            repeatDataMap.forEach((k, v) -> {
                ApiScenarioWithBLOBs scenario = nameModuleMap.get(k);
                if (scenario == null) {
                    remove.set(false);
                }
            });
            if (remove.get()) {
                moduleMap = new HashMap<>();
            }
        }
        return moduleMap;
    }

    private void startCoverModule(List<ApiScenarioWithBLOBs> toUpdateList, Map<String, ApiScenarioWithBLOBs> nameModuleMap,
                                  Map<String, ApiScenarioWithBLOBs> repeatDataMap, String updateVersionId) {
        if (repeatDataMap != null) {
            repeatDataMap.forEach((k, v) -> {
                ApiScenarioWithBLOBs apiScenarioWithBLOBs = nameModuleMap.get(k);
                if (apiScenarioWithBLOBs != null) {
                    if (!v.getVersionId().equals(updateVersionId)) {
                        addNewVersionScenario(apiScenarioWithBLOBs, v, "update");
                        return;
                    }
                    apiScenarioWithBLOBs.setId(v.getId());
                    apiScenarioWithBLOBs.setVersionId(updateVersionId);
                    apiScenarioWithBLOBs.setNum(v.getNum());
                    apiScenarioWithBLOBs.setStatus(v.getStatus());
                    apiScenarioWithBLOBs.setCreateTime(v.getCreateTime());
                    apiScenarioWithBLOBs.setRefId(v.getRefId());
                    apiScenarioWithBLOBs.setOrder(v.getOrder());
                    apiScenarioWithBLOBs.setLatest(v.getLatest());
                    apiScenarioWithBLOBs.setCreateTime(v.getCreateTime());
                    toUpdateList.add(apiScenarioWithBLOBs);
                }
            });
        }
    }

    private void removeRepeatOrigin(Boolean fullCoverage, List<ApiScenarioWithBLOBs> optionData, LinkedHashMap<String, List<ApiScenarioWithBLOBs>> nameModuleMapList) {
        if (fullCoverage) {
            nameModuleMapList.forEach((k, v) -> {
                optionData.add(v.get(v.size() - 1));
            });
        } else {
            nameModuleMapList.forEach((k, v) -> {
                optionData.add(v.get(0));
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
        if (request.getUpdateVersionId() == null) {
            updateVersionId = request.getDefaultVersion();
        } else {
            updateVersionId = request.getUpdateVersionId();
        }
        return updateVersionId;
    }

    private void setModule(List<ApiScenarioWithBLOBs> data, Map<String, ApiScenarioModule> map, Map<String, List<ApiScenarioModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, ApiScenarioModuleDTO> idModuleMap, ApiScenarioModuleDTO chooseModule) {
        for (ApiScenarioWithBLOBs datum : data) {
            StringBuilder path = new StringBuilder();
            path.append("/");
            String[] tagTree;
            String modulePath = datum.getModulePath();
            ApiScenarioModule scenarioModule = map.get(modulePath);
            if (chooseModule != null) {
                String chooseModuleParentId = getChooseModuleParentId(chooseModule);
                //导入时选了模块，且接口有模块的
                if (StringUtils.isNotBlank(modulePath)) {
                    //场景的全部路径的集合
                    tagTree = getTagTree(modulePath);
                    ApiScenarioModule chooseModuleOne = JSON.parseObject(JSON.toJSONString(chooseModule), ApiScenarioModule.class);
                    ApiScenarioModule minModule = getChooseMinModule(tagTree, chooseModuleOne, pidChildrenMap, map, idPathMap);
                    String id = minModule.getId();
                    datum.setApiScenarioModuleId(id);
                    datum.setModulePath(idPathMap.get(id));
                } else {
                    //导入时选了模块，且接口没有模块的
                    datum.setApiScenarioModuleId(chooseModule.getId());
                    datum.setModulePath(idPathMap.get(chooseModule.getId()));
                }
            } else {
                if (StringUtils.isNotBlank(modulePath)) {
                    //导入时没选模块但接口有模块的，根据modulePath，和当前协议查询当前项目里是否有同名称模块，如果有，就在该模块下建立接口，否则新建模块
                    tagTree = getTagTree(modulePath);
                    if (scenarioModule != null) {
                        datum.setApiScenarioModuleId(scenarioModule.getId());
                        datum.setModulePath(modulePath);
                    } else {
                        //父级同级的模块list
                        ApiScenarioModule minModule = getMinModule(tagTree, pidChildrenMap.get(PropertyConstant.ROOT), null, pidChildrenMap, map, idPathMap, idModuleMap);
                        String id = minModule.getId();
                        datum.setApiScenarioModuleId(id);
                        datum.setModulePath(idPathMap.get(id));
                    }
                } else {
                    //导入时即没选中模块，接口自身也没模块的，直接返会当前项目，当前协议下的默认模块
                    List<ApiScenarioModule> moduleList = pidChildrenMap.get(PropertyConstant.ROOT);
                    for (ApiScenarioModule module : moduleList) {
                        if (module.getName().equals("未规划场景")) {
                            datum.setApiScenarioModuleId(module.getId());
                            datum.setModulePath("/" + module.getName());
                        }
                    }
                }
            }
        }
    }

    private String getChooseModuleParentId(ApiScenarioModuleDTO chooseModule) {
        if (chooseModule.getParentId() == null) {
            chooseModule.setParentId(PropertyConstant.ROOT);
        }
        return chooseModule.getParentId();
    }

    private String getChooseModulePath(Map<String, String> idPathMap, ApiScenarioModuleDTO chooseModule, String chooseModuleParentId) {
        String s;
        if (chooseModuleParentId.equals(PropertyConstant.ROOT)) {
            s = "/" + chooseModule.getName();
        } else {
            s = idPathMap.get(chooseModuleParentId);
        }
        return s;
    }

    private String[] getTagTree(String modulePath) {
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

    private ApiScenarioModule getMinModule(String[] tagTree, List<ApiScenarioModule> parentModuleList, ApiScenarioModule parentModule, Map<String, List<ApiScenarioModule>> pidChildrenMap, Map<String, ApiScenarioModule> map, Map<String, String> idPathMap, Map<String, ApiScenarioModuleDTO> idModuleMap) {
        //如果parentModule==null 则证明需要创建根目录同级的模块
        ApiScenarioModule returnModule = null;

        for (int i = 0; i < tagTree.length; i++) {
            int finalI = i;
            //查找上一级里面是否有当前全路径的第一级，没有则需要创建
            List<ApiScenarioModule> collect = null;
            if (!CollectionUtils.isEmpty(parentModuleList)) {
                collect = parentModuleList.stream().filter(t -> t.getName().equals(tagTree[finalI])).collect(Collectors.toList());
            }

            if (CollectionUtils.isEmpty(collect)) {
                if (i == 0) {
                    //证明需要在根目录创建，
                    parentModule = new ApiScenarioModule();
                    parentModule.setProjectId(pidChildrenMap.get(PropertyConstant.ROOT).get(0).getProjectId());
                    parentModule.setId(PropertyConstant.ROOT);
                    parentModule.setLevel(0);
                } else {
                    if (!CollectionUtils.isEmpty(parentModuleList) && parentModule == null) {
                        String parentId = parentModuleList.get(0).getParentId();
                        ApiScenarioModuleDTO apiScenarioModuleDTO = idModuleMap.get(parentId);
                        parentModule = JSON.parseObject(JSON.toJSONString(apiScenarioModuleDTO), ApiScenarioModule.class);
                    }
                }
                return createModule(tagTree, i, parentModule, map, pidChildrenMap, idPathMap);
            } else {
                returnModule = collect.get(0);
                parentModule = collect.get(0);
                parentModuleList = pidChildrenMap.get(collect.get(0).getId());
            }
        }
        return returnModule;
    }


    private ApiScenarioModule getChooseMinModule(String[] tagTree, ApiScenarioModule parentModule, Map<String, List<ApiScenarioModule>> pidChildrenMap, Map<String, ApiScenarioModule> moduleMap
            , Map<String, String> idPathMap) {
        //如果parentModule==null 则证明需要创建根目录同级的模块
        ApiScenarioModule returnModule = null;
        for (int i = 0; i < tagTree.length; i++) {
            int finalI = i;
            //在选择的模块下建模块，查看选择的模块下有没有同名的模块
            List<ApiScenarioModule> moduleList = pidChildrenMap.get(parentModule.getId());
            List<ApiScenarioModule> filterModuleList = null;
            if (!CollectionUtils.isEmpty(moduleList)) {
                filterModuleList = moduleList.stream().filter(t -> t.getName().equals(tagTree[finalI])).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(filterModuleList)) {
                return createModule(tagTree, i, parentModule, moduleMap, pidChildrenMap, idPathMap);
            } else {
                returnModule = filterModuleList.get(0);
                parentModule = filterModuleList.get(0);
            }
        }
        return returnModule;
    }

    private ApiScenarioModule createModule(String[] tagTree, int i, ApiScenarioModule parentModule, Map<String, ApiScenarioModule> map, Map<String, List<ApiScenarioModule>> pidChildrenMap, Map<String, String> idPathMap) {

        ApiScenarioModule returnModule = null;
        for (int i1 = i; i1 < tagTree.length; i1++) {
            String pathName = tagTree[i1];

            //创建模块
            ApiScenarioModule newModule = this.getNewModule(pathName, parentModule.getProjectId(), parentModule.getLevel() + 1);
            String parentId;
            if (parentModule.getId().equals(PropertyConstant.ROOT)) {
                parentId = null;
            } else {
                parentId = parentModule.getId();
            }
            double pos = this.getNextLevelPos(parentModule.getProjectId(), parentModule.getLevel() + 1, parentId);
            newModule.setPos(pos);
            newModule.setParentId(parentId);
            List<ApiScenarioModule> moduleList = pidChildrenMap.get(parentModule.getId());
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
            map.putIfAbsent(path, newModule);
            parentModule = newModule;
            returnModule = newModule;
        }
        return returnModule;
    }

    private void buildProcessData(List<ApiScenarioModuleDTO> nodeTreeByProjectId, Map<String, List<ApiScenarioModule>> pidChildrenMap, Map<String, String> idPathMap, Map<String, String> parentModulePathMap) {
        Map<String, List<ApiScenarioModuleDTO>> idChildrenMap = new HashMap<>();
        int i = 0;
        Map<String, List<ApiScenarioModule>> idModuleMap = new HashMap<>();
        for (ApiScenarioModuleDTO scenarioModuleDTO : nodeTreeByProjectId) {
            if (StringUtils.isBlank(scenarioModuleDTO.getParentId()) || StringUtils.equals(scenarioModuleDTO.getParentId(), "0")) {
                scenarioModuleDTO.setParentId(PropertyConstant.ROOT);
            }
            String parentModulePath = parentModulePathMap.get(scenarioModuleDTO.getParentId());
            if (parentModulePath != null) {
                if (parentModulePath.equals("/root")) {
                    scenarioModuleDTO.setPath("/" + scenarioModuleDTO.getName());
                } else {
                    scenarioModuleDTO.setPath(parentModulePath + "/" + scenarioModuleDTO.getName());
                }
            } else {
                scenarioModuleDTO.setPath("/" + scenarioModuleDTO.getName());
            }
            idPathMap.put(scenarioModuleDTO.getId(), scenarioModuleDTO.getPath());

            ApiScenarioModule scenarioModule = buildModule(idModuleMap, scenarioModuleDTO);
            if (pidChildrenMap.get(scenarioModuleDTO.getParentId()) != null) {
                pidChildrenMap.get(scenarioModuleDTO.getParentId()).add(scenarioModule);
            } else {
                pidChildrenMap.put(scenarioModuleDTO.getParentId(), idModuleMap.get(scenarioModuleDTO.getId()));
            }
            i = i + 1;
            List<ApiScenarioModuleDTO> childrenList = idChildrenMap.get(scenarioModuleDTO.getId());
            if (scenarioModuleDTO.getChildren() != null) {
                if (childrenList != null) {
                    childrenList.addAll(scenarioModuleDTO.getChildren());
                } else {
                    idChildrenMap.put(scenarioModuleDTO.getId(), scenarioModuleDTO.getChildren());
                }
            } else {
                if (childrenList == null) {
                    pidChildrenMap.put(scenarioModuleDTO.getId(), new ArrayList<>());
                }
            }
            parentModulePathMap.put(scenarioModuleDTO.getId(), scenarioModuleDTO.getPath());
        }
        if (i == nodeTreeByProjectId.size() && nodeTreeByProjectId.size() > 0) {
            Collection<List<ApiScenarioModuleDTO>> values = idChildrenMap.values();
            List<ApiScenarioModuleDTO> childrenList = new ArrayList<>();
            for (List<ApiScenarioModuleDTO> value : values) {
                childrenList.addAll(value);
            }
            buildProcessData(childrenList, pidChildrenMap, idPathMap, parentModulePathMap);
        }
    }

    private ApiScenarioModule buildModule(Map<String, List<ApiScenarioModule>> IdModuleMap, ApiScenarioModuleDTO scenarioModuleDTO) {
        ApiScenarioModule scenarioModule = new ApiScenarioModule();
        scenarioModule.setId(scenarioModuleDTO.getId());
        scenarioModule.setName(scenarioModuleDTO.getName());
        scenarioModule.setParentId(scenarioModuleDTO.getParentId());
        scenarioModule.setProjectId(scenarioModuleDTO.getProjectId());
        scenarioModule.setLevel(scenarioModuleDTO.getLevel());
        List<ApiScenarioModule> moduleList = IdModuleMap.get(scenarioModuleDTO.getId());
        if (moduleList != null) {
            moduleList.add(scenarioModule);
        } else {
            moduleList = new ArrayList<>();
            moduleList.add(scenarioModule);
            IdModuleMap.put(scenarioModuleDTO.getId(), moduleList);
        }
        return scenarioModule;
    }
}
