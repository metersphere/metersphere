package io.metersphere.functional.service;


import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.CaseReviewAddRequest;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.sdk.constants.FunctionalCaseReviewStatus;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用例评审表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewService {

    public static final int ORDER_STEP = 5000;

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    /**
     * 添加用例评审
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void addCaseReview(CaseReviewAddRequest request, String userId) {
        String caseReviewId = IDGenerator.nextStr();
        addCaseReview(request, userId, caseReviewId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewUserMapper mapper = sqlSession.getMapper(CaseReviewUserMapper.class);
        CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper = sqlSession.getMapper(CaseReviewFunctionalCaseMapper.class);
        try {
            //保存和评审人的关系
            addCaseReviewUser(request, caseReviewId, mapper);
            //保存和用例的关系
            addCaseReviewFunctionalCase(request, userId, caseReviewId, caseReviewFunctionalCaseMapper);
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    /**
     * 保存用例评审和功能用例的关系
     * @param request request
     * @param userId 当前操作人
     * @param caseReviewId 用例评审id
     * @param caseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper
     */
    private void addCaseReviewFunctionalCase(CaseReviewAddRequest request, String userId, String caseReviewId, CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper) {
        if (CollectionUtils.isNotEmpty(request.getCaseIds())) {
            request.getReviewers().forEach(caseId -> {
                CaseReviewFunctionalCase caseReviewFunctionalCase = new CaseReviewFunctionalCase();
                caseReviewFunctionalCase.setReviewId(caseReviewId);
                caseReviewFunctionalCase.setCaseId(caseId);
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
                caseReviewFunctionalCase.setCreateUser(userId);
                caseReviewFunctionalCase.setCreateTime(System.currentTimeMillis());
                caseReviewFunctionalCase.setUpdateTime(System.currentTimeMillis());
                caseReviewFunctionalCase.setId(IDGenerator.nextStr());
                caseReviewFunctionalCase.setPos(getNextOrder(request.getProjectId()));
                caseReviewFunctionalCaseMapper.insert(caseReviewFunctionalCase);
            });
        }
    }

    /**
     * 保存用例评审和评审人的关系
     * @param request request
     * @param caseReviewId 用例评审
     * @param caseReviewUserMapper caseReviewUserMapper
     */
    private static void addCaseReviewUser(CaseReviewAddRequest request, String caseReviewId, CaseReviewUserMapper caseReviewUserMapper) {
        request.getReviewers().forEach(user -> {
            CaseReviewUser caseReviewUser = new CaseReviewUser();
            caseReviewUser.setReviewId(caseReviewId);
            caseReviewUser.setUserId(user);
            caseReviewUserMapper.insert(caseReviewUser);
        });
    }

    /**
     * 新增用例评审
     * @param request request
     * @param userId 当前操作人
     * @param caseReviewId 用例评审id
     */
    private void addCaseReview(CaseReviewAddRequest request, String userId, String caseReviewId) {
        CaseReview caseReview = new CaseReview();
        caseReview.setId(caseReviewId);
        caseReview.setProjectId(request.getProjectId());
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        caseReview.setStatus(CaseReviewStatus.PREPARED.toString());
        caseReview.setReviewPassRule(request.getReviewPassRule());
        caseReview.setPos(getNextOrder(request.getProjectId()));
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(JSON.toJSONString(request.getTags()));
        }
        caseReview.setStartTime(request.getStartTime());
        caseReview.setEndTime(request.getEndTime());
        caseReview.setCreateTime(System.currentTimeMillis());
        caseReview.setUpdateTime(System.currentTimeMillis());
        caseReview.setCreateUser(userId);
        caseReview.setUpdateUser(userId);
        caseReviewMapper.insert(caseReview);
    }

    /**
     * 关注/取消关注用例
     *
     * @param caseReviewId 用例评审id
     * @param userId       当前操作人
     */
    public void editFollower(String caseReviewId, String userId) {
        checkCaseReview(caseReviewId);
        CaseReviewFollowerExample example = new CaseReviewFollowerExample();
        example.createCriteria().andReviewIdEqualTo(caseReviewId).andUserIdEqualTo(userId);
        if (caseReviewFollowerMapper.countByExample(example) > 0) {
            caseReviewFollowerMapper.deleteByPrimaryKey(caseReviewId, userId);
        } else {
            CaseReviewFollower caseReviewFollower = new CaseReviewFollower();
            caseReviewFollower.setReviewId(caseReviewId);
            caseReviewFollower.setUserId(userId);
            caseReviewFollowerMapper.insert(caseReviewFollower);
        }
    }

    /**
     * 检查用例评审是否存在
     *
     * @param caseReviewId 用例评审id
     * @return CaseReview
     */
    private CaseReview checkCaseReview(String caseReviewId) {
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andIdEqualTo(caseReviewId);
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(caseReviewId);
        if (caseReview == null) {
            throw new MSException(CaseManagementResultCode.CASE_REVIEW_NOT_FOUND);
        }
        return caseReview;
    }

    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }


}