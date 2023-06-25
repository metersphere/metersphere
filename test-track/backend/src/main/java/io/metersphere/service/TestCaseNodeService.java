package io.metersphere.service;


import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ProjectVersionMapper;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseNodeMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestReviewCaseMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.NodeNumDTO;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.dto.TestPlanCaseDTO;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.plan.request.function.QueryTestPlanCaseRequest;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.request.testcase.*;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseNodeService extends NodeTreeService<TestCaseNodeDTO> {

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;
    @Resource
    ExtTestCaseNodeMapper extTestCaseNodeMapper;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    ProjectVersionMapper projectVersionMapper;
    @Resource
    ExtTestReviewCaseMapper extTestReviewCaseMapper;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    TestPlanService testPlanService;

    public TestCaseNodeService() {
        super(TestCaseNodeDTO.class);
    }

    public String addNode(TestCaseNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(node.getId())) {
            node.setId(UUID.randomUUID().toString());
        }
        node.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        testCaseNodeMapper.insertSelective(node);
        return node.getId();
    }

    public List<String> getNodes(String nodeId) {
        return extTestCaseNodeMapper.getNodes(nodeId);
    }

    private void validateNode(TestCaseNode node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            MSException.throwException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        this.checkTestCaseNodeExist(node);
    }

    private void checkTestCaseNodeExist(TestCaseNode node) {
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
            if (testCaseNodeMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    public TestCaseNode getDefaultNode(String projectId) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo("未规划用例").andParentIdIsNull();
        List<TestCaseNode> list = testCaseNodeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            NodeNumDTO record = new NodeNumDTO();
            record.setId(UUID.randomUUID().toString());
            record.setCreateUser(SessionUtils.getUserId());
            record.setName("未规划用例");
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            testCaseNodeMapper.insert(record);
            record.setCaseNum(0);
            return record;
        } else {
            return list.get(0);
        }
    }

    public List<TestCaseNodeDTO> getNodeTreeByProjectId(String projectId) {
        return getNodeTreeByProjectId(projectId, new QueryTestCaseRequest());
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

    public int editNode(DragNodeRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        return testCaseNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return 1;
        }
        io.metersphere.service.TestCaseService testCaseService = CommonBeanFactory.getBean(io.metersphere.service.TestCaseService.class);
        List<String> testCaseIdList = this.selectCaseIdByNodeIds(nodeIds);
        TestCaseBatchRequest request = new TestCaseBatchRequest();
        request.setIds(testCaseIdList);
        testCaseService.deleteToGcBatch(request);

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return testCaseNodeMapper.deleteByExample(testCaseNodeExample);
    }

    private List<String> selectCaseIdByNodeIds(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return new ArrayList<>();
        } else {
            return extTestCaseMapper.selectIdsByNodeIds(nodeIds);
        }
    }

    /**
     * 获取当前计划下
     * 有关联数据的节点
     *
     * @param request
     * @return List<TestCaseNodeDTO>
     */
    public List<TestCaseNodeDTO> getNodeByQueryRequest(QueryTestPlanCaseRequest request) {
        List<TestCaseNodeDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(request.getPlanId());
        projectIds.forEach(id -> {
            Project project = projectMapper.selectByPrimaryKey(id);
            String name = project.getName();
            List<TestCaseNodeDTO> nodeList = getNodeDTO(id, request);
            TestCaseNodeDTO testCaseNodeDTO = new TestCaseNodeDTO();
            testCaseNodeDTO.setId(project.getId());
            testCaseNodeDTO.setName(name);
            testCaseNodeDTO.setLabel(name);
            testCaseNodeDTO.setChildren(nodeList);
            list.add(testCaseNodeDTO);
        });
        return list;
    }

    public List<TestCaseNodeDTO> getNodeTreeByProjectId(String projectId, QueryTestCaseRequest request) {
        this.setRequestWeekParam(request);
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNode(projectId);
        request.setProjectId(projectId);
        request.setUserId(SessionUtils.getUserId());
        request.setNodeIds(null);
        request.setOrders(null);
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<TestCaseNodeDTO> countNodes = extTestCaseMapper.getCountNodes(request);
        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTrees(testCaseNodes, getCountMap(countNodes));
    }



    public Map<String, Integer> getNodeCountMapByProjectId(String projectId, QueryTestCaseRequest request) {
        this.setRequestWeekParam(request);
        request.setProjectId(projectId);
        request.setUserId(SessionUtils.getUserId());
        request.setNodeIds(null);
        request.setOrders(null);
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<TestCaseNodeDTO> countNodes = extTestCaseMapper.getCountNodes(request);
        Map<String, Integer> countMap = getCountMap(countNodes);
        countMap.remove(null); // 脏数据，没有模块 ID 的会有 null 的清空
        return countMap;
    }

    /**
     * 获取当前计划下
     * 有关联数据的节点
     *
     * @param planId  plan id
     * @param request 根据查询条件过滤用例数量
     * @return List<TestCaseNodeDTO>
     */
    public List<TestCaseNodeDTO> getNodeByPlanId(String planId, QueryTestPlanCaseRequest request) {
        request.setPlanId(planId);
        request.setProjectId(null);
        request.setNodeIds(null);
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        List<TestCaseNodeDTO> countModules = extTestPlanTestCaseMapper.getTestPlanCountNodes(request);
        return getNodeTreeWithPruningTree(countModules);
    }

    public List<TestCaseNodeDTO> getPublicCaseNode(String workspaceId, QueryTestCaseRequest request) {
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        request.setWorkspaceId(workspaceId);
        request.setProjectId(null);
        request.setNodeIds(null);
        // 保留: 后续若需要根据列表版本筛选的话, version_id => version_name
        List<String> versionIds = request.getFilters().get("version_id");
        if (!CollectionUtils.isEmpty(versionIds)) {
            ProjectVersionExample versionExample = new ProjectVersionExample();
            versionExample.createCriteria().andIdIn(versionIds);
            List<ProjectVersion> versions = projectVersionMapper.selectByExample(versionExample);
            List<String> versionNames = versions.stream().map(ProjectVersion::getName).distinct().collect(Collectors.toList());
            request.getFilters().put("version_name", versionNames);
            request.getFilters().put("version_id", Collections.emptyList());
        }
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<TestCaseNodeDTO> countModules = extTestCaseMapper.getWorkspaceCountNodes(request);
        return getNodeTreeWithPruningTree(countModules);
    }

    public List<TestCaseNodeDTO> getTrashCaseNode(String projectId, QueryTestCaseRequest request) {
        // 初始化回收站中模块被删除的用例, 挂在默认未规划模块, 获取回收站模块节点数据
        TestCaseNode defaultNode = this.getDefaultNode(projectId);
        extTestCaseMapper.updateNoModuleTrashNodeToDefault(projectId, defaultNode.getId(), defaultNode.getName());
        request.setProjectId(projectId);
        request.setNodeIds(null);
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<TestCaseNodeDTO> countModules = extTestCaseMapper.getCountNodes(request);
        countModules.forEach(item -> item.setProjectId(projectId));
        return getNodeTreeWithPruningTree(countModules);
    }

    public List<TestCaseNodeDTO> getNodeByPlanId(String planId) {
        return this.getNodeByPlanId(planId, new QueryTestPlanCaseRequest());
    }

    public List<TestCaseNodeDTO> getNodeByReviewId(String reviewId) {
        return this.getNodeByReviewId(reviewId, new QueryCaseReviewRequest());
    }

    public List<TestCaseNodeDTO> getNodeByReviewId(String reviewId, QueryCaseReviewRequest request) {
        request.setReviewId(reviewId);
        request.setProjectId(null);
        request.setNodeIds(null);
        List<TestCaseNodeDTO> countModules = extTestReviewCaseMapper.getTestReviewCountNodes(request);
        return getNodeTreeWithPruningTree(countModules);
    }

    public List<TestCaseNodeDTO> getNodeTreeWithPruningTree(List<TestCaseNodeDTO> countModules) {
        return getNodeTreeWithPruningTree(countModules, extTestCaseNodeMapper::getNodeTreeByProjectIds);
    }

    /**
     * 生成模块树并剪枝
     *
     * @return
     */
    public List<TestCaseNodeDTO> getNodeTreeWithPruningTree(List<TestCaseNodeDTO> testCaseNodes, List<String> containIds) {
        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(testCaseNodes);
        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            TestCaseNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, containIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }

    private List<TestCaseNodeDTO> getNodeDTO(String projectId, QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> testPlanTestCases = extTestPlanTestCaseMapper.listByPlanId(request);
        if (testPlanTestCases.isEmpty()) {
            return null;
        }

        List<String> caseIds = testPlanTestCases.stream()
                .map(TestPlanCaseDTO::getCaseId)
                .collect(Collectors.toList());

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<String> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTreeWithPruningTree(testCaseNodes, dataNodeIds);
    }

    public List<TestCaseNodeDTO> getRelatePlanNodes(QueryTestCaseRequest request) {
        request.setNodeIds(null);
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setRepeatCase(true);
        }
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<TestCaseNodeDTO> countMNodes = extTestCaseMapper.getTestPlanRelateCountNodes(request);
        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(request.getProjectId());
        return getNodeTreeWithPruningTreeByCaseCount(testCaseNodes, getCountMap(countMNodes));
    }

    public List<TestCaseNodeDTO> getRelateReviewNodes(QueryTestCaseRequest request) {
        request.setNodeIds(null);
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        List<TestCaseNodeDTO> countMNodes = extTestCaseMapper.getTestReviewRelateCountNodes(request);
        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(request.getProjectId());
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        return getNodeTreeWithPruningTreeByCaseCount(testCaseNodes, getCountMap(countMNodes));
    }

    public List<TestCaseNodeDTO> getAllNodeByProjectId(QueryNodeRequest request) {
        return getNodeTreeByProjectId(request.getProjectId());
    }

    public Map<String, String> createNodeByTestCases(List<TestCaseWithBLOBs> testCases, String projectId) {
        List<String> nodePaths = testCases.stream()
                .map(TestCase::getNodePath)
                .collect(Collectors.toList());

        return this.createNodes(nodePaths, projectId);
    }

    public Map<String, String> getNodePathMap(String projectId) {
        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(getNodeTreeByProjectId(projectId));
        Map<String, String> pathMap = new HashMap<>();
        buildPathMap(nodeTrees, pathMap, "");
        return pathMap;
    }

    private void buildPathMap(List<TestCaseNodeDTO> nodeTrees, Map<String, String> pathMap, String rootPath) {
        for (TestCaseNodeDTO nodeTree : nodeTrees) {
            String currentPath = rootPath + "/" + nodeTree.getName();
            pathMap.put(nodeTree.getId(), currentPath);
            List<TestCaseNodeDTO> children = nodeTree.getChildren();
            if (!CollectionUtils.isEmpty(children)) {
                buildPathMap(children, pathMap, currentPath);
            }
        }
    }

    public Map<String, String> createNodes(List<String> nodePaths, String projectId) {
        List<TestCaseNodeDTO> nodeTrees = getNodeTreeByProjectId(projectId);
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
                for (TestCaseNodeDTO nodeTree : nodeTrees) {
                    if (StringUtils.equals(rootNodeName, nodeTree.getName())) {
                        hasNode = true;
                        createNodeByPathIterator(itemIterator, "/" + rootNodeName, nodeTree,
                                pathMap, projectId, 2);
                    }
                }
            }
            if (!hasNode) {
                createNodeByPath(itemIterator, rootNodeName, null, projectId, 1, StringUtils.EMPTY, pathMap);
            }
        }
        return pathMap;

    }

    public void createNodeByNodePath(String nodePath, String projectId, List<TestCaseNodeDTO> nodeTrees, Map<String, String> pathMap) {
        if (nodePath == null) {
            throw new ExcelException(Translator.get("test_case_module_not_null"));
        }
        List<String> nodeNameList = new ArrayList<>(Arrays.asList(nodePath.split("/")));
        Iterator<String> itemIterator = nodeNameList.iterator();
        Boolean hasNode = false;
        String rootNodeName;

        if (nodeNameList.size() <= 1) {
            throw new ExcelException(Translator.get("test_case_create_module_fail") + ":" + nodePath);
        } else {
            itemIterator.next();
            itemIterator.remove();
            rootNodeName = itemIterator.next().trim();
            //原来没有，新建的树nodeTrees也不包含
            for (TestCaseNodeDTO nodeTree : nodeTrees) {
                if (StringUtils.equals(rootNodeName, nodeTree.getName())) {
                    hasNode = true;
                    createNodeByPathIterator(itemIterator, "/" + rootNodeName, nodeTree,
                            pathMap, projectId, 2);
                }
            }
        }
        if (!hasNode) {
            createNodeByPath(itemIterator, rootNodeName, null, projectId, 1, StringUtils.EMPTY, pathMap);
        }
    }

    @Override
    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setName(nodeName.trim());
        testCaseNode.setParentId(pId);
        testCaseNode.setProjectId(projectId);
        testCaseNode.setCreateTime(System.currentTimeMillis());
        testCaseNode.setUpdateTime(System.currentTimeMillis());
        testCaseNode.setLevel(level);
        testCaseNode.setCreateUser(SessionUtils.getUserId());
        testCaseNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        testCaseNode.setPos(pos);
        testCaseNodeMapper.insert(testCaseNode);
        return testCaseNode.getId();
    }

    public void dragNode(DragNodeRequest request) {

        checkTestCaseNodeExist(request);

        List<String> nodeIds = request.getNodeIds();

        TestCaseNodeDTO nodeTree = request.getNodeTree();

        if (nodeTree == null) {
            return;
        }

        List<TestCaseNode> updateNodes = new ArrayList<>();

        buildUpdateTestCase(nodeTree, updateNodes,  "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateTestCaseNode(updateNodes);
    }

    private void batchUpdateTestCaseNode(List<TestCaseNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseNodeMapper testCaseNodeMapper = sqlSession.getMapper(TestCaseNodeMapper.class);
        updateNodes.forEach((value) -> {
            testCaseNodeMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void buildUpdateTestCase(TestCaseNodeDTO rootNode,
                                     List<TestCaseNode> updateNodes, String pId, int level) {

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }

        if (updateNodes != null) {
            TestCaseNode testCaseNode = new TestCaseNode();
            testCaseNode.setId(rootNode.getId());
            testCaseNode.setLevel(level);
            testCaseNode.setParentId(pId);
            updateNodes.add(testCaseNode);
        }

        List<TestCaseNodeDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateTestCase(children.get(i), updateNodes, rootNode.getId(), level + 1);
            }
        }
    }

    public Project getProjectByNode(String nodeId) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andIdEqualTo(nodeId);
        List<TestCaseNode> testCaseNodes = testCaseNodeMapper.selectByExample(example);
        String projectId = testCaseNodes.get(0).getProjectId();
        return projectMapper.selectByPrimaryKey(projectId);
    }

    @Override
    public TestCaseNodeDTO getNode(String id) {
        return extTestCaseNodeMapper.get(id);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extTestCaseNodeMapper.updatePos(id, pos);
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
    private List<TestCaseNode> getPos(String projectId, int level, String parentId, String order) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        TestCaseNodeExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return testCaseNodeMapper.selectByExample(example);
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
        List<TestCaseNode> nodes = getPos(projectId, level, parentId, "pos asc");
        if (!CollectionUtils.isEmpty(nodes)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            TestCaseNodeMapper testCaseNodeMapper = sqlSession.getMapper(TestCaseNodeMapper.class);
            AtomicDouble pos = new AtomicDouble(DEFAULT_POS);
            nodes.forEach((node) -> {
                node.setPos(pos.getAndAdd(DEFAULT_POS));
                testCaseNodeMapper.updateByPrimaryKey(node);
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
        List<TestCaseNode> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }


    public String getLogDetails(List<String> ids) {
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria().andIdIn(ids);
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(TestCaseNode::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(TestCaseNode node) {
        TestCaseNode module = null;
        if (StringUtils.isNotEmpty(node.getId())) {
            module = testCaseNodeMapper.selectByPrimaryKey(node.getId());
        }
        if (module == null && StringUtils.isNotEmpty(node.getName())) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            TestCaseNodeExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).
                    andProjectIdEqualTo(node.getProjectId())
                    .andLevelEqualTo(node.getLevel());

            if (StringUtils.isNotEmpty(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            List<TestCaseNode> list = testCaseNodeMapper.selectByExample(example);
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
        return testCaseNodeMapper.countByExample(example);
    }

    public LinkedList<TestCaseNode> getPathNodeById(String moduleId) {
        TestCaseNode testCaseNode = testCaseNodeMapper.selectByPrimaryKey(moduleId);
        LinkedList<TestCaseNode> returnList = new LinkedList<>();

        while (testCaseNode != null) {
            returnList.addFirst(testCaseNode);
            if (testCaseNode.getParentId() == null) {
                testCaseNode = null;
            } else {
                testCaseNode = testCaseNodeMapper.selectByPrimaryKey(testCaseNode.getParentId());
            }
        }
        return returnList;
    }

    public long trashCount(String projectId) {
        return extTestCaseMapper.trashCount(projectId);
    }

    public void minderEdit(TestCaseMinderEditRequest request) {
        deleteNode(request.getIds());

        List<TestCaseMinderEditRequest.TestCaseNodeMinderEditItem> testCaseNodes = request.getTestCaseNodes();
        List<String> editNodeIds = new ArrayList<>();

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(testCaseNodes)) {

            for (TestCaseMinderEditRequest.TestCaseNodeMinderEditItem item : testCaseNodes) {
                if (StringUtils.isBlank(item.getParentId()) || item.getParentId().equals("root")) {
                    item.setParentId(null);
                }
                item.setProjectId(request.getProjectId());
                if (item.getIsEdit()) {
                    DragNodeRequest editNode = new DragNodeRequest();
                    BeanUtils.copyBean(editNode, item);
                    checkTestCaseNodeExist(editNode);
                    editNodeIds.add(editNode.getId());
                    testCaseNodeMapper.updateByPrimaryKeySelective(editNode);
                } else {
                    TestCaseNode testCaseNode = new TestCaseNode();
                    BeanUtils.copyBean(testCaseNode, item);
                    testCaseNode.setProjectId(request.getProjectId());
                    addNode(testCaseNode);
                }
            }
        }
    }

    public long publicCount(String workSpaceId) {
        return extTestCaseMapper.countByWorkSpaceId(workSpaceId);
    }

    /**
     * 统计某些节点下的临时节点的数量
     *
     * @param nodeIds
     * @return
     */
    public Map<String, Integer> getMinderTreeExtraNodeCount(List<String> nodeIds) {
        if (nodeIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Map<String, Object>> moduleCountList = extTestCaseMapper.moduleExtraNodeCount(nodeIds);
        return this.parseModuleCountList(moduleCountList);
    }

    /**
     * 设置请求参数中本周区间参数
     * @param request 页面请求参数
     * @return
     */
    private void setRequestWeekParam(QueryTestCaseRequest request) {
        Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
        Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
        if (request.isSelectThisWeedData()) {
            if (weekFirstTime != null) {
                request.setCreateTime(weekFirstTime.getTime());
            }
        }
        if (request.isSelectThisWeedRelevanceData()) {
            if (weekFirstTime != null) {
                request.setRelevanceCreateTime(weekFirstTime.getTime());
            }
        }
    }
}
