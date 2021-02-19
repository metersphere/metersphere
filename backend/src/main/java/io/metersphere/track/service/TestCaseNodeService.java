package io.metersphere.track.service;


import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseNodeMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.NodeTreeService;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
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
public class TestCaseNodeService extends NodeTreeService<TestCaseNodeDTO> {

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;
    @Resource
    ExtTestCaseNodeMapper extTestCaseNodeMapper;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    TestCaseReviewService testCaseReviewService;
    @Resource
    TestCaseReviewTestCaseMapper testCaseReviewTestCaseMapper;
    @Resource
    TestCaseReviewMapper testCaseReviewMapper;

    public TestCaseNodeService() {
        super(TestCaseNodeDTO.class);
    }

    public String addNode(TestCaseNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        testCaseNodeMapper.insertSelective(node);
        return node.getId();
    }

    private void validateNode(TestCaseNode node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkTestCaseNodeExist(node);
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
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (testCaseNodeMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    public List<TestCaseNodeDTO> getNodeTreeByProjectId(String projectId) {
        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTrees(testCaseNodes);
    }

    public int editNode(DragNodeRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkTestCaseNodeExist(request);
        List<TestCaseDTO> testCases = QueryTestCaseByNodeIds(request.getNodeIds());

        testCases.forEach(testCase -> {
            StringBuilder path = new StringBuilder(testCase.getNodePath());
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            pathLists.set(request.getLevel(), request.getName());
            path.delete(0, path.length());
            for (int i = 1; i < pathLists.size(); i++) {
                path = path.append("/").append(pathLists.get(i));
            }
            testCase.setNodePath(path.toString());
        });

        batchUpdateTestCase(testCases);

        return testCaseNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andNodeIdIn(nodeIds);
        testCaseMapper.deleteByExample(testCaseExample);

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return testCaseNodeMapper.deleteByExample(testCaseNodeExample);
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

    /**
     * 获取当前计划下
     * 有关联数据的节点
     *
     * @param planId plan id
     * @return List<TestCaseNodeDTO>
     */
    public List<TestCaseNodeDTO> getNodeByPlanId(String planId) {

        List<TestCaseNodeDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        projectIds.forEach(id -> {
            Project project = projectMapper.selectByPrimaryKey(id);
            if (project != null) {
                String name = project.getName();
                List<TestCaseNodeDTO> nodeList = getNodeDTO(id, planId);
                TestCaseNodeDTO testCaseNodeDTO = new TestCaseNodeDTO();
                testCaseNodeDTO.setId(project.getId());
                testCaseNodeDTO.setName(name);
                testCaseNodeDTO.setLabel(name);
                testCaseNodeDTO.setChildren(nodeList);
                list.add(testCaseNodeDTO);
            }
        });

        return list;
    }

    public List<TestCaseNodeDTO> getNodeByReviewId(String reviewId) {
        List<TestCaseNodeDTO> list = new ArrayList<>();
        ProjectExample example = new ProjectExample();
        example.createCriteria().andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId());
        List<Project> projects = projectMapper.selectByExample(example);
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        projectIds.forEach(id -> {
            String name = projectMapper.selectByPrimaryKey(id).getName();

            TestCaseReviewTestCaseExample testCaseReviewTestCaseExample = new TestCaseReviewTestCaseExample();
            testCaseReviewTestCaseExample.createCriteria().andReviewIdEqualTo(reviewId);
            List<TestCaseReviewTestCase> testCaseReviewTestCases = testCaseReviewTestCaseMapper.selectByExample(testCaseReviewTestCaseExample);
            List<String> caseIds = testCaseReviewTestCases.stream().map(TestCaseReviewTestCase::getCaseId).collect(Collectors.toList());

            List<TestCaseNodeDTO> nodeList = getReviewNodeDTO(id, caseIds);
            TestCaseNodeDTO testCaseNodeDTO = new TestCaseNodeDTO();
            testCaseNodeDTO.setName(name);
            testCaseNodeDTO.setLabel(name);
            testCaseNodeDTO.setChildren(nodeList);
            list.add(testCaseNodeDTO);
        });
        return list;

    }

    private List<TestCaseNodeDTO> getNodeDTO(String projectId, QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> testPlanTestCases = extTestPlanTestCaseMapper.listByPlanId(request);
        if (testPlanTestCases.isEmpty()) {
            return null;
        }

        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);

        List<String> caseIds = testPlanTestCases.stream()
                .map(TestPlanCaseDTO::getCaseId)
                .collect(Collectors.toList());

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<String> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(testCaseNodes);

        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            TestCaseNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }

        return nodeTrees;
    }

    private List<TestCaseNodeDTO> getNodeDTO(String projectId, String planId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(planId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        if (testPlanTestCases.isEmpty()) {
            return null;
        }

        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);

        List<String> caseIds = testPlanTestCases.stream()
                .map(TestPlanTestCase::getCaseId)
                .collect(Collectors.toList());

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<String> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(testCaseNodes);

        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            TestCaseNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }

        return nodeTrees;
    }

    private List<TestCaseNodeDTO> getReviewNodeDTO(String projectId, List<String> caseIds) {

        if (CollectionUtils.isEmpty(caseIds)) {
            return null;
        }

        List<TestCaseNodeDTO> testCaseNodes = extTestCaseNodeMapper.getNodeTreeByProjectId(projectId);


        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<String> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(testCaseNodes);

        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            TestCaseNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }

        return nodeTrees;
    }

    public List<TestCaseNodeDTO> getAllNodeByPlanId(QueryNodeRequest request) {
        String planId = request.getTestPlanId();
        String projectId = request.getProjectId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if (testPlan == null) {
            return Collections.emptyList();
        }

        return getNodeTreeByProjectId(projectId);
    }

    public List<TestCaseNodeDTO> getAllNodeByReviewId(QueryNodeRequest request) {
        String reviewId = request.getReviewId();
        String projectId = request.getProjectId();
        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(reviewId);
        if (testCaseReview == null) {
            return Collections.emptyList();
        }

        return getNodeTreeByProjectId(projectId);
    }

    public Map<String, String> createNodeByTestCases(List<TestCaseWithBLOBs> testCases, String projectId) {
        List<String> nodePaths = testCases.stream()
                .map(TestCase::getNodePath)
                .collect(Collectors.toList());

        return this.createNodes(nodePaths, projectId);
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
            String rootNodeName = null;

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
    public String insertNode(String nodeName, String pId, String projectId, Integer level) {
        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setName(nodeName.trim());
        testCaseNode.setParentId(pId);
        testCaseNode.setProjectId(projectId);
        testCaseNode.setCreateTime(System.currentTimeMillis());
        testCaseNode.setUpdateTime(System.currentTimeMillis());
        testCaseNode.setLevel(level);
        testCaseNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        testCaseNode.setPos(pos);
        testCaseNodeMapper.insert(testCaseNode);
        return testCaseNode.getId();
    }

    public void dragNode(DragNodeRequest request) {

        checkTestCaseNodeExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<TestCaseDTO> testCases = QueryTestCaseByNodeIds(nodeIds);

        TestCaseNodeDTO nodeTree = request.getNodeTree();

        if (nodeTree == null) {
            return;
        }

        List<TestCaseNode> updateNodes = new ArrayList<>();

        buildUpdateTestCase(nodeTree, testCases, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateTestCaseNode(updateNodes);

        batchUpdateTestCase(testCases);
    }

    private void batchUpdateTestCaseNode(List<TestCaseNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseNodeMapper testCaseNodeMapper = sqlSession.getMapper(TestCaseNodeMapper.class);
        updateNodes.forEach((value) -> {
            testCaseNodeMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
    }

    private void batchUpdateTestCase(List<TestCaseDTO> testCases) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper testCaseMapper = sqlSession.getMapper(TestCaseMapper.class);
        testCases.forEach((value) -> {
            testCaseMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
    }

    private List<TestCaseDTO> QueryTestCaseByNodeIds(List<String> nodeIds) {
        QueryTestCaseRequest testCaseRequest = new QueryTestCaseRequest();
        testCaseRequest.setNodeIds(nodeIds);
        return extTestCaseMapper.list(testCaseRequest);
    }

    private void buildUpdateTestCase(TestCaseNodeDTO rootNode, List<TestCaseDTO> testCases,
                                     List<TestCaseNode> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }

        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setId(rootNode.getId());
        testCaseNode.setLevel(level);
        testCaseNode.setParentId(pId);
        updateNodes.add(testCaseNode);

        for (TestCaseDTO item : testCases) {
            if (StringUtils.equals(item.getNodeId(), rootNode.getId())) {
                item.setNodePath(rootPath);
            }
        }

        List<TestCaseNodeDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateTestCase(children.get(i), testCases, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
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

    private TestCaseNode getCaseNode(String id) {
        return testCaseNodeMapper.selectByPrimaryKey(id);
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

}
