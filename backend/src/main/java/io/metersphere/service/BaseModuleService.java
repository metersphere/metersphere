package io.metersphere.service;


import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.track.dto.EditModuleDateDTO;
import io.metersphere.track.dto.ModuleNodeDTO;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseModuleService extends NodeTreeService<ModuleNodeDTO> {

    private String tableName;

    @Resource
    ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    ProjectMapper projectMapper;


    public BaseModuleService() {
        super(ModuleNodeDTO.class);
    }

    public BaseModuleService(String tableName) {
        this();
        this.tableName = tableName;
    }

    public String addNode(ModuleNode module) {
        validateNode(module);
        module.setCreateTime(System.currentTimeMillis());
        module.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(module.getId())) {
            module.setId(UUID.randomUUID().toString());
        }
        module.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(module.getProjectId(), module.getLevel(), module.getParentId());
        module.setPos(pos);
        module.setModulePath(getModulePath(module));
        extModuleNodeMapper.insertSelective(tableName, module);
        return module.getId();
    }

    public String getModulePath(ModuleNode module) {
        Integer level = module.getLevel();
        if (level == null || level <= 1) {
            return String.format("/%s", module.getName());
        }
        //获取父级信息
        if (StringUtils.isNotBlank(module.getParentId())) {
            ModuleNode parent = extModuleNodeMapper.selectByPrimaryKey(tableName, module.getParentId());
            return parent.getModulePath() + "/" + module.getName();
        }
        return null;
    }

    public List<String> getNodes(String nodeId) {
        return extModuleNodeMapper.getNodeIdsByPid(tableName, nodeId);
    }

    public ModuleNode get(String id) {
        return extModuleNodeMapper.selectByPrimaryKey(tableName, id);
    }

    private void validateNode(ModuleNode node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkTestCaseNodeExist(node);
    }

    private void checkTestCaseNodeExist(ModuleNode node) {
        if (node.getName() != null) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            TestCaseNodeExample.Criteria criteria = example.createCriteria();
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

            if(StringUtils.isNotBlank(node.getScenarioType())){
                criteria.andScenarioTypeEqualTo(node.getScenarioType());
            }

            if (extModuleNodeMapper.selectByExample(tableName, example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    public ModuleNode getDefaultNode(String projectId, String defaultName) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(Optional.ofNullable(defaultName).orElse("未规划用例")).andParentIdIsNull();
        List<ModuleNode> list = extModuleNodeMapper.selectByExample(tableName, example);
        if (CollectionUtils.isEmpty(list)) {
            ModuleNode record = new ModuleNode();
            record.setId(UUID.randomUUID().toString());
            record.setCreateUser(SessionUtils.getUserId());
            record.setName(Optional.ofNullable(defaultName).orElse("未规划用例"));
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            extModuleNodeMapper.insert(tableName, record);
            record.setCaseNum(0);
            return record;
        } else {
            return list.get(0);
        }
    }

    public ModuleNode getDefaultNodeWithType(String projectId, String type,  String defaultName) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andScenarioTypeEqualTo(type).andNameEqualTo(Optional.ofNullable(defaultName).orElse("未规划用例")).andParentIdIsNull();
        List<ModuleNode> list = extModuleNodeMapper.selectByExample(tableName, example);
        if (CollectionUtils.isEmpty(list)) {
            ModuleNode record = new ModuleNode();
            record.setId(UUID.randomUUID().toString());
            record.setCreateUser(SessionUtils.getUserId());
            record.setName(Optional.ofNullable(defaultName).orElse("未规划用例"));
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            record.setScenarioType(type);
            extModuleNodeMapper.insertWithModulePathAndType(tableName, record);
            record.setCaseNum(0);
            return record;
        } else {
            return list.get(0);
        }
    }

    public List<ModuleNodeDTO> getNodeTreeByProjectIdWithCount(String projectId, Function<QueryNodeRequest, List<Map<String, Object>>> getModuleCountFunc, String defaultName) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNode(projectId, defaultName);

        List<ModuleNodeDTO> moduleNodes = extModuleNodeMapper.getNodeTreeByProjectId(tableName, projectId);
        if (getModuleCountFunc != null) {
            buildNodeCount(projectId, moduleNodes, getModuleCountFunc);
        }
        return getNodeTrees(moduleNodes);
    }

    public List<ModuleNodeDTO> getNodeTreeByProjectId(String projectId, String defaultName) {
        return getNodeTreeByProjectIdWithCount(projectId, null, defaultName);
    }

    protected void buildNodeCount(String projectId, List<ModuleNodeDTO> moduleNodes, Function<QueryNodeRequest, List<Map<String, Object>>> getModuleCountFunc) {
        this.buildNodeCount(projectId, moduleNodes, getModuleCountFunc, null);
    }

    protected void buildNodeCount(String projectId, List<ModuleNodeDTO> moduleNodes, Function<QueryNodeRequest, List<Map<String, Object>>> getModuleCountFunc,
                                  QueryNodeRequest request) {
        if (request == null) {
            request = new QueryNodeRequest();
        }
        request.setProjectId(projectId);

        //优化：将for循环内的SQL抽出来，只查一次
        List<String> allModuleIdList = new ArrayList<>();
        for (ModuleNodeDTO node : moduleNodes) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = nodeList(moduleNodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            for (String moduleId : moduleIds) {
                if (!allModuleIdList.contains(moduleId)) {
                    allModuleIdList.add(moduleId);
                }
            }
        }
        request.setModuleIds(allModuleIdList);

        List<Map<String, Object>> moduleCountList = getModuleCountFunc.apply(request);
//        List<Map<String,Object>> moduleCountList = extTestCaseMapper.moduleCountByCollection(request);

        Map<String, Integer> moduleCountMap = this.parseModuleCountList(moduleCountList);
        moduleNodes.forEach(node -> {
            List<String> moduleIds = new ArrayList<>();
            moduleIds = nodeList(moduleNodes, node.getId(), moduleIds);
            moduleIds.add(node.getId());
            int countNum = 0;
            for (String moduleId : moduleIds) {
                if (moduleCountMap.containsKey(moduleId)) {
                    countNum += moduleCountMap.get(moduleId).intValue();
                }
            }
            node.setCaseNum(countNum);
        });
    }

    private Map<String, Integer> parseModuleCountList(List<Map<String, Object>> moduleCountList) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (Map<String, Object> map : moduleCountList) {
            Object moduleIdObj = map.get("moduleId");
            Object countNumObj = map.get("countNum");
            if (moduleIdObj != null && countNumObj != null) {
                String moduleId = String.valueOf(moduleIdObj);
                try {
                    Integer countNumInteger = Integer.valueOf(String.valueOf(countNumObj));
                    returnMap.put(moduleId, countNumInteger);
                } catch (Exception e) {
                }
            }
        }
        return returnMap;
    }

    public List<String> nodeList(List<ModuleNodeDTO> testCaseNodes, String pid, List<String> list) {
        for (ModuleNodeDTO node : testCaseNodes) {
            //遍历出父id等于参数的id，add进子节点集合
            if (StringUtils.equals(node.getParentId(), pid)) {
                list.add(node.getId());
                //递归遍历下一级
                nodeList(testCaseNodes, node.getId(), list);
            }
        }
        return list;
    }

    public int editNode(DragNodeRequest request) {
        return editNodeAndNodePath(request, null);
    }

    protected int editNodeAndNodePath(DragNodeRequest request, Consumer<List<String>> editNodePathFunc) {
        request.setUpdateTime(System.currentTimeMillis());
        checkTestCaseNodeExist(request);
        if (!CollectionUtils.isEmpty(request.getNodeIds()) && editNodePathFunc != null) {
            editNodePathFunc.accept(request.getNodeIds());
        }
        return extModuleNodeMapper.updateByPrimaryKeySelective(tableName, request);
    }

    /**
     * nodeIds 包含了删除节点ID及其所有子节点ID
     *
     * @param nodeIds
     * @param deleteNodeDataFunc
     * @return
     */
    protected int deleteNode(List<String> nodeIds, Consumer<List<String>> deleteNodeDataFunc) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return 1;
        }

        if (deleteNodeDataFunc != null) {
            // 删除node下的数据
            deleteNodeDataFunc.accept(nodeIds);
        }

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return extModuleNodeMapper.deleteByExample(tableName, testCaseNodeExample);
    }

    public List<ModuleNodeDTO> getNodeTreeWithPruningTree(Map<String, List<String>> projectNodeMap) {
        List<ModuleNodeDTO> list = new ArrayList<>();
        projectNodeMap.forEach((k, v) -> {
            Project project = projectMapper.selectByPrimaryKey(k);
            if (project != null) {
                String name = project.getName();
                List<ModuleNodeDTO> moduleNodes = getNodeTreeWithPruningTree(k, v);
                ModuleNodeDTO moduleNodeDTO = new ModuleNodeDTO();
                moduleNodeDTO.setId(project.getId());
                moduleNodeDTO.setName(name);
                moduleNodeDTO.setLabel(name);
                moduleNodeDTO.setChildren(moduleNodes);
                if (!CollectionUtils.isEmpty(moduleNodes)) {
                    list.add(moduleNodeDTO);
                }
            }
        });
        return list;
    }

    /**
     * 获取当前项目下的
     *
     * @param projectId
     * @param pruningTreeIds
     * @return
     */
    public List<ModuleNodeDTO> getNodeTreeWithPruningTree(String projectId, List<String> pruningTreeIds) {
        List<ModuleNodeDTO> testCaseNodes = extModuleNodeMapper.getNodeTreeByProjectId(tableName, projectId);
        List<ModuleNodeDTO> nodeTrees = getNodeTrees(testCaseNodes);
        Iterator<ModuleNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ModuleNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, pruningTreeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }

    public Map<String, String> createNodeByTestCases(List<TestCaseWithBLOBs> testCases, String projectId, String defaultName) {
        List<String> nodePaths = testCases.stream()
                .map(TestCase::getNodePath)
                .collect(Collectors.toList());
        return this.createNodes(nodePaths, projectId, defaultName);
    }

    public Map<String, String> createNodes(List<String> nodePaths, String projectId, String defaultName) {
        List<ModuleNodeDTO> nodeTrees = getNodeTreeByProjectId(projectId, defaultName);
        Map<String, String> pathMap = new HashMap<>();
        for (String item : nodePaths) {
            if (item == null) {
                throw new ExcelException(Translator.get("test_case_module_not_null"));
            }
            List<String> nodeNameList = new ArrayList<>(Arrays.asList(item.split("/")));
            Iterator<String> itemIterator = nodeNameList.iterator();
            Boolean hasNode = false;
            String rootNodeName;

            if (nodeNameList.size() <= 1) {
                throw new ExcelException(Translator.get("test_case_create_module_fail") + ":" + item);
            } else {
                itemIterator.next();
                itemIterator.remove();
                rootNodeName = itemIterator.next().trim();
                //原来没有，新建的树nodeTrees也不包含
                for (ModuleNodeDTO nodeTree : nodeTrees) {
                    if (StringUtils.equals(rootNodeName, nodeTree.getName())) {
                        hasNode = true;
                        createNodeByPathIterator(itemIterator, "/" + rootNodeName, nodeTree,
                                pathMap, projectId, 2);
                    }
                    ;
                }
            }
            if (!hasNode) {
                createNodeByPath(itemIterator, rootNodeName, null, projectId, 1, "", pathMap);
            }
        }
        return pathMap;

    }

    @Override
    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        ModuleNode moduleNode = new ModuleNode();
        moduleNode.setName(nodeName.trim());
        moduleNode.setModulePath(path);
        moduleNode.setParentId(pId);
        moduleNode.setProjectId(projectId);
        moduleNode.setCreateTime(System.currentTimeMillis());
        moduleNode.setUpdateTime(System.currentTimeMillis());
        moduleNode.setLevel(level);
        moduleNode.setCreateUser(SessionUtils.getUserId());
        moduleNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        moduleNode.setPos(pos);
        extModuleNodeMapper.insertWithModulePath(tableName, moduleNode);
        return moduleNode.getId();
    }

    public void dragNode(DragNodeRequest request) {
        dragNodeAndDataEdit(request, null, null);
    }

    /**
     * 拖拽批量修改模块，以及模块下数据的 modulePath 字段
     *
     * @param request
     * @param getNodeDataFunc  通过 nodeIds 获取需要修改的数据
     * @param editNodeDataFunc 修改 modulePath 的方法
     */
    protected void dragNodeAndDataEdit(DragNodeRequest request,
                                       Function<List<String>, List<EditModuleDateDTO>> getNodeDataFunc,
                                       Consumer<List<EditModuleDateDTO>> editNodeDataFunc) {

        if (request.getNodeTree() == null) {
            return;
        }

        checkTestCaseNodeExist(request);

        List<String> nodeIds = request.getNodeIds();
        TestCaseNodeDTO nodeTree = request.getNodeTree();
        List<ModuleNode> updateNodes = new ArrayList<>();

        if (getNodeDataFunc != null && editNodeDataFunc != null) {
            List<EditModuleDateDTO> nodeData = getNodeDataFunc.apply(nodeIds);
            buildUpdateTestCase(nodeTree, nodeData, updateNodes, "/", "0", 1);
            editNodeDataFunc.accept(nodeData);
        } else {
            buildUpdateModule(nodeTree, updateNodes, "0", 1, "");
        }

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateTestCaseNode(updateNodes);
    }

    private void batchUpdateTestCaseNode(List<ModuleNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtModuleNodeMapper extModuleNodeMapper = sqlSession.getMapper(ExtModuleNodeMapper.class);
        updateNodes.forEach((value) -> {
            extModuleNodeMapper.updateByPrimaryKeySelective(tableName, value);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void buildUpdateModule(TestCaseNodeDTO rootNode,
                                   List<ModuleNode> updateNodes, String pId, int level, String parentPath) {
        checkoutNodeLimit(level);

        ModuleNode moduleNode = new ModuleNode();
        moduleNode.setId(rootNode.getId());
        moduleNode.setLevel(level);
        moduleNode.setParentId(pId);
        if (level <= 1 && StringUtils.isBlank(parentPath)) {
            moduleNode.setModulePath("/" + rootNode.getName());
        } else {
            moduleNode.setModulePath(parentPath + "/" + rootNode.getName());
        }
        updateNodes.add(moduleNode);

        List<TestCaseNodeDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateModule(children.get(i), updateNodes, rootNode.getId(), level + 1, moduleNode.getModulePath());
            }
        }
    }

    public void checkoutNodeLimit(int level) {
        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }
    }

    private void buildUpdateTestCase(TestCaseNodeDTO rootNode, List<EditModuleDateDTO> nodeData,
                                     List<ModuleNode> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        checkoutNodeLimit(level);

        ModuleNode moduleNode = new ModuleNode();
        moduleNode.setId(rootNode.getId());
        moduleNode.setLevel(level);
        moduleNode.setParentId(pId);
        updateNodes.add(moduleNode);

        for (EditModuleDateDTO item : nodeData) {
            if (StringUtils.equals(item.getModuleId(), rootNode.getId())) {
                item.setModulePath(rootPath);
            }
        }

        List<TestCaseNodeDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateTestCase(children.get(i), nodeData, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    @Override
    public ModuleNodeDTO getNode(String id) {
        return extModuleNodeMapper.get(tableName, id);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extModuleNodeMapper.updatePos(tableName, id, pos);
    }

    /**
     * 按照指定排序方式获取同级模块的列表
     *
     * @param projectId 所属项目 id
     * @param level     node level
     * @param parentId  node parent id
     * @param order     pos 排序方式
     * @return 按照指定排序方式排序的同级模块列表
     */
    private List<ModuleNode> getPos(String projectId, int level, String parentId, String order) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        TestCaseNodeExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return extModuleNodeMapper.selectByExample(tableName, example);
    }

    /**
     * 刷新同级模块的 pos 值
     *
     * @param projectId project id
     * @param level     node level
     * @param parentId  node parent id
     */
    @Override
    protected void refreshPos(String projectId, int level, String parentId) {
        List<ModuleNode> nodes = getPos(projectId, level, parentId, "pos asc");
        if (!CollectionUtils.isEmpty(nodes)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ExtModuleNodeMapper extModuleNodeMapper = sqlSession.getMapper(ExtModuleNodeMapper.class);
            AtomicDouble pos = new AtomicDouble(DEFAULT_POS);
            nodes.forEach((node) -> {
                node.setPos(pos.getAndAdd(DEFAULT_POS));
                extModuleNodeMapper.updateByPrimaryKey(tableName, node);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }


    /**
     * 获得同级模块下一个 pos 值
     *
     * @param projectId project id
     * @param level     node level
     * @param parentId  node parent id
     * @return 同级模块下一个 pos 值
     */
    private double getNextLevelPos(String projectId, int level, String parentId) {
        List<ModuleNode> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    public String getLogDetails(List<String> ids) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andIdIn(ids);
        List<ModuleNode> nodes = extModuleNodeMapper.selectByExample(tableName, example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(TestCaseNode::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ModuleNode node) {
        ModuleNode module = null;
        if (StringUtils.isNotEmpty(node.getId())) {
            module = extModuleNodeMapper.selectByPrimaryKey(tableName, node.getId());
        }
        if (module == null && StringUtils.isNotEmpty(node.getName())) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            TestCaseNodeExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotEmpty(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotEmpty(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            List<ModuleNode> list = extModuleNodeMapper.selectByExample(tableName, example);
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

    public long countById(String nodeId) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andIdEqualTo(nodeId);
        return extModuleNodeMapper.countByExample(tableName, example);
    }

    public List<ModuleNode> selectSameModule(ModuleNode node) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        TestCaseNodeExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(node.getName())
                .andProjectIdEqualTo(node.getProjectId())
                .andLevelEqualTo(node.getLevel());

        if (StringUtils.isNotBlank(node.getId())) {
            criteria.andIdNotEqualTo(node.getId());
        }
        return extModuleNodeMapper.selectByExample(tableName, example);
    }

    public List<ModuleNode> selectByModulePath(ModuleNode node) {
        return extModuleNodeMapper.selectByModulePath(tableName, node.getModulePath(), node.getProjectId());
    }
}
