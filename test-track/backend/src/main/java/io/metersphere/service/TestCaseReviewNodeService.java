package io.metersphere.service;

import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.base.domain.TestCaseReviewExample;
import io.metersphere.base.domain.TestCaseReviewNode;
import io.metersphere.base.domain.TestCaseReviewNodeExample;
import io.metersphere.base.mapper.TestCaseReviewMapper;
import io.metersphere.base.mapper.TestCaseReviewNodeMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewNodeMapper;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestCaseReviewNodeDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.request.testreview.DragReviewNodeRequest;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
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
public class TestCaseReviewNodeService extends NodeTreeService<TestCaseReviewNodeDTO>{

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TestCaseReviewNodeMapper testCaseReviewNodeMapper;
    @Resource
    private ExtTestCaseReviewNodeMapper extTestCaseReviewNodeMapper;
    @Resource
    private TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private static final String TEST_CASE_REVIEW_DEFAULT_NODE_CREATE_KEY = "TEST_CASE_REVIEW:DEFAULT_NODE:CREATE";

    public TestCaseReviewNodeService() {
        super(TestCaseReviewNodeDTO.class);
    }

    public List<TestCaseReviewNodeDTO> getNodeTreeByProjectId(String projectId, QueryCaseReviewRequest request) {
        checkDefaultNode(projectId);
        request.setNodeIds(null);
        List<TestCaseReviewNodeDTO> countNodes = extTestCaseReviewNodeMapper.getCountNodes(request);
        List<TestCaseReviewNodeDTO> testCaseNodes = extTestCaseReviewNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTrees(testCaseNodes, getCountMap(countNodes));
    }

    public String addNode(TestCaseReviewNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(node.getId())) {
            node.setId(UUID.randomUUID().toString());
        }
        node.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        testCaseReviewNodeMapper.insertSelective(node);
        return node.getId();
    }

    public int editNode(TestCaseReviewNode request) {
        request.setUpdateTime(System.currentTimeMillis());
        return testCaseReviewNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return 1;
        }

        // 删除所有节点下的评审
        TestCaseReviewExample example = new TestCaseReviewExample();
        example.createCriteria().andNodeIdIn(nodeIds);
        testCaseReviewMapper.deleteByExample(example);
        // 删除节点
        TestCaseReviewNodeExample testCaseNodeExample = new TestCaseReviewNodeExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return testCaseReviewNodeMapper.deleteByExample(testCaseNodeExample);
    }

    public void dragNode(DragReviewNodeRequest request) {
        checkTestCaseNodeExist(request);
        List<String> nodeIds = request.getNodeIds();
        TestCaseReviewNodeDTO nodeTree = request.getNodeTree();
        if (nodeTree == null) {
            return;
        }
        List<TestCaseReviewNode> updateNodes = new ArrayList<>();
        buildUpdateTestCase(nodeTree, updateNodes, "0", 1);
        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());
        batchUpdateTestCaseNode(updateNodes);
    }

    @Override
    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        TestCaseReviewNode reviewNode = new TestCaseReviewNode();
        reviewNode.setName(nodeName.trim());
        reviewNode.setParentId(pId);
        reviewNode.setProjectId(projectId);
        reviewNode.setCreateTime(System.currentTimeMillis());
        reviewNode.setUpdateTime(System.currentTimeMillis());
        reviewNode.setLevel(level);
        reviewNode.setCreateUser(SessionUtils.getUserId());
        reviewNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        reviewNode.setPos(pos);
        testCaseReviewNodeMapper.insert(reviewNode);
        return reviewNode.getId();
    }

    @Override
    public TestCaseReviewNodeDTO getNode(String id) {
        return extTestCaseReviewNodeMapper.getNode(id);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extTestCaseReviewNodeMapper.updatePos(id, pos);
    }

    @Override
    protected void refreshPos(String projectId, int level, String parentId) {
        List<TestCaseReviewNode> nodes = getPos(projectId, level, parentId, "pos asc");
        if (!CollectionUtils.isEmpty(nodes)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            TestCaseReviewNodeMapper batchMapper = sqlSession.getMapper(TestCaseReviewNodeMapper.class);
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

    private void validateNode(TestCaseReviewNode node) {
        checkTestCaseReviewNodeExist(node);
    }

    private void checkTestCaseReviewNodeExist(TestCaseReviewNode node) {
        if (node.getName() != null) {
            TestCaseReviewNodeExample example = new TestCaseReviewNodeExample();
            TestCaseReviewNodeExample.Criteria criteria = example.createCriteria();
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
            if (!testCaseReviewNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private double getNextLevelPos(String projectId, int level, String parentId) {
        List<TestCaseReviewNode> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<TestCaseReviewNode> getPos(String projectId, int level, String parentId, String order) {
        TestCaseReviewNodeExample example = new TestCaseReviewNodeExample();
        TestCaseReviewNodeExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return testCaseReviewNodeMapper.selectByExample(example);
    }

    private void checkTestCaseNodeExist(TestCaseReviewNode node) {
        if (node.getName() != null) {
            TestCaseReviewNodeExample example = new TestCaseReviewNodeExample();
            TestCaseReviewNodeExample.Criteria criteria = example.createCriteria();
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
            if (!testCaseReviewNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private void batchUpdateTestCaseNode(List<TestCaseReviewNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseReviewNodeMapper batchMapper = sqlSession.getMapper(TestCaseReviewNodeMapper.class);
        updateNodes.forEach(batchMapper::updateByPrimaryKeySelective);
        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void buildUpdateTestCase(TestCaseReviewNodeDTO rootNode,
                                     List<TestCaseReviewNode> updateNodes, String pId, int level) {
        if (updateNodes != null) {
            TestCaseReviewNode testCaseReviewNode = new TestCaseReviewNode();
            testCaseReviewNode.setId(rootNode.getId());
            testCaseReviewNode.setLevel(level);
            testCaseReviewNode.setParentId(pId);
            updateNodes.add(testCaseReviewNode);
        }

        List<TestCaseReviewNodeDTO> children = rootNode.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (TestCaseReviewNodeDTO child : children) {
                buildUpdateTestCase(child, updateNodes, rootNode.getId(), level + 1);
            }
        }
    }

    public void checkDefaultNode(String projectId) {
        TestCaseReviewNode defaultNode = getDefaultNode(projectId);
        if (defaultNode == null) {
            createDefaultNode(projectId);
        }
    }

    public TestCaseReviewNode getDefaultNode(String projectId) {
        TestCaseReviewNodeExample example = new TestCaseReviewNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<TestCaseReviewNode> defaultNodes = testCaseReviewNodeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(defaultNodes)) {
            return null;
        } else {
            return defaultNodes.get(0);
        }
    }

    public void createDefaultNode(String projectId) {
        // 加锁, 防止并发创建
        RLock lock = redissonClient.getLock(TEST_CASE_REVIEW_DEFAULT_NODE_CREATE_KEY + ":" + projectId);
        if (lock.tryLock()) {
            try {
                // 双重检查, 判断是否已经存在默认节点
                if (getDefaultNode(projectId) != null) {
                    return;
                }

                // 创建默认节点, 只执行一次
                TestCaseReviewNode defaultNode = new TestCaseReviewNode();
                defaultNode.setId(UUID.randomUUID().toString());
                defaultNode.setCreateUser(SessionUtils.getUserId());
                defaultNode.setName(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName());
                defaultNode.setPos(1.0);
                defaultNode.setLevel(1);
                defaultNode.setCreateTime(System.currentTimeMillis());
                defaultNode.setUpdateTime(System.currentTimeMillis());
                defaultNode.setProjectId(projectId);
                testCaseReviewNodeMapper.insert(defaultNode);
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
