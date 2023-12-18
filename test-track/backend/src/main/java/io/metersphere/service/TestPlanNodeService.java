package io.metersphere.service;

import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.base.domain.TestPlanExample;
import io.metersphere.base.domain.TestPlanNode;
import io.metersphere.base.domain.TestPlanNodeExample;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanNodeMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanNodeMapper;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestPlanNodeDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.request.testplan.DragPlanNodeRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanNodeService extends NodeTreeService<TestPlanNodeDTO>{

    @Resource
    private TestPlanNodeMapper testPlanNodeMapper;
    @Resource
    private ExtTestPlanNodeMapper extTestPlanNodeMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private RedissonClient redissonClient;

    private static final String TEST_PLAN_NODE_CREATE_KEY = "TEST_PLAN:DEFAULT_NODE:CREATE";

    public TestPlanNodeService() {
        super(TestPlanNodeDTO.class);
    }

    public List<TestPlanNodeDTO> getNodeTreeByProjectId(String projectId, QueryTestPlanRequest request) {
        checkDefaultNode(projectId);
        request.setNodeIds(null);
        List<TestPlanNodeDTO> countNodes = extTestPlanNodeMapper.getCountNodes(request);
        List<TestPlanNodeDTO> testCaseNodes = extTestPlanNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTrees(testCaseNodes, getCountMap(countNodes));
    }

    public String addNode(TestPlanNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(node.getId())) {
            node.setId(UUID.randomUUID().toString());
        }
        node.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        testPlanNodeMapper.insertSelective(node);
        return node.getId();
    }

    public int editNode(TestPlanNode request) {
        request.setUpdateTime(System.currentTimeMillis());
        return testPlanNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return 1;
        }

        // 删除所有节点下的计划
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andNodeIdIn(nodeIds);
        testPlanMapper.deleteByExample(example);
        // 删除节点
        TestPlanNodeExample planNodeExample = new TestPlanNodeExample();
        planNodeExample.createCriteria().andIdIn(nodeIds);
        return testPlanNodeMapper.deleteByExample(planNodeExample);
    }

    public void dragNode(DragPlanNodeRequest request) {
        checkTestCaseNodeExist(request);
        List<String> nodeIds = request.getNodeIds();
        TestPlanNodeDTO nodeTree = request.getNodeTree();
        if (nodeTree == null) {
            return;
        }
        List<TestPlanNode> updateNodes = new ArrayList<>();
        buildUpdateTestCase(nodeTree, updateNodes, "0", 1);
        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());
        batchUpdateTestCaseNode(updateNodes);
    }

    @Override
    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        TestPlanNode planNode = new TestPlanNode();
        planNode.setName(nodeName.trim());
        planNode.setParentId(pId);
        planNode.setProjectId(projectId);
        planNode.setCreateTime(System.currentTimeMillis());
        planNode.setUpdateTime(System.currentTimeMillis());
        planNode.setLevel(level);
        planNode.setCreateUser(SessionUtils.getUserId());
        planNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        planNode.setPos(pos);
        testPlanNodeMapper.insert(planNode);
        return planNode.getId();
    }

    @Override
    public TestPlanNodeDTO getNode(String id) {
        return extTestPlanNodeMapper.getNode(id);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extTestPlanNodeMapper.updatePos(id, pos);
    }

    @Override
    protected void refreshPos(String projectId, int level, String parentId) {
        List<TestPlanNode> nodes = getPos(projectId, level, parentId, "pos asc");
        if (!CollectionUtils.isEmpty(nodes)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            TestPlanNodeMapper batchMapper = sqlSession.getMapper(TestPlanNodeMapper.class);
            AtomicDouble pos = new AtomicDouble(DEFAULT_POS);
            nodes.forEach((node) -> {
                node.setPos(pos.getAndAdd(DEFAULT_POS));
                batchMapper.updateByPrimaryKey(node);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private void validateNode(TestPlanNode node) {
        checkTestPlanNodeExist(node);
    }

    private void checkTestPlanNodeExist(TestPlanNode node) {
        if (node.getName() != null) {
            TestPlanNodeExample example = new TestPlanNodeExample();
            TestPlanNodeExample.Criteria criteria = example.createCriteria();
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
            if (!testPlanNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private double getNextLevelPos(String projectId, int level, String parentId) {
        List<TestPlanNode> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<TestPlanNode> getPos(String projectId, int level, String parentId, String order) {
        TestPlanNodeExample example = new TestPlanNodeExample();
        TestPlanNodeExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return testPlanNodeMapper.selectByExample(example);
    }

    private void checkTestCaseNodeExist(TestPlanNode node) {
        if (node.getName() != null) {
            TestPlanNodeExample example = new TestPlanNodeExample();
            TestPlanNodeExample.Criteria criteria = example.createCriteria();
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
            if (!testPlanNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private void batchUpdateTestCaseNode(List<TestPlanNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanNodeMapper batchMapper = sqlSession.getMapper(TestPlanNodeMapper.class);
        updateNodes.forEach(batchMapper::updateByPrimaryKeySelective);
        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void buildUpdateTestCase(TestPlanNodeDTO rootNode,
                                     List<TestPlanNode> updateNodes, String pId, int level) {
        if (updateNodes != null) {
            TestPlanNode testPlanNode = new TestPlanNode();
            testPlanNode.setId(rootNode.getId());
            testPlanNode.setLevel(level);
            testPlanNode.setParentId(pId);
            updateNodes.add(testPlanNode);
        }

        List<TestPlanNodeDTO> children = rootNode.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (TestPlanNodeDTO child : children) {
                buildUpdateTestCase(child, updateNodes, rootNode.getId(), level + 1);
            }
        }
    }

    public void checkDefaultNode(String projectId) {
        TestPlanNode defaultNode = getDefaultNode(projectId);
        if (defaultNode == null) {
            createDefaultNode(projectId);
        }
    }

    public TestPlanNode getDefaultNode(String projectId) {
        TestPlanNodeExample example = new TestPlanNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<TestPlanNode> defaultNodes = testPlanNodeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(defaultNodes)) {
            return null;
        } else {
            return defaultNodes.get(0);
        }
    }

    public void createDefaultNode(String projectId) {
        // 加锁, 防止并发创建
        RLock lock = redissonClient.getLock(TEST_PLAN_NODE_CREATE_KEY + ":" + projectId);
        if (lock.tryLock()) {
            try {
                // 双重检查, 判断是否已经存在默认节点
                if (getDefaultNode(projectId) != null) {
                    return;
                }

                // 创建默认节点, 只执行一次
                TestPlanNode defaultNode = new TestPlanNode();
                defaultNode.setId(UUID.randomUUID().toString());
                defaultNode.setCreateUser(SessionUtils.getUserId());
                defaultNode.setName(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName());
                defaultNode.setPos(1.0);
                defaultNode.setLevel(1);
                defaultNode.setCreateTime(System.currentTimeMillis());
                defaultNode.setUpdateTime(System.currentTimeMillis());
                defaultNode.setProjectId(projectId);
                testPlanNodeMapper.insert(defaultNode);
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
