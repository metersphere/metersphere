package io.metersphere.functional.service;


import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.dto.CaseReviewUserDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.ExtUserMapper;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用例评审表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewService {

    public static final int POS_STEP = 5000;

    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private CaseReviewUserMapper caseReviewUserMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private ExtCaseReviewUserMapper extCaseReviewUserMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private DeleteCaseReviewService deleteCaseReviewService;
    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewModuleService caseReviewModuleService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private BaseCaseProvider provider;
    @Resource
    private UserLoginService userLoginService;


    private static final String CASE_MODULE_COUNT_ALL = "all";

    /**
     * 获取用例评审列表
     *
     * @param request request
     * @return CaseReviewDTO
     */
    public List<CaseReviewDTO> getCaseReviewPage(CaseReviewPageRequest request) {
        List<CaseReviewDTO> list = extCaseReviewMapper.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> reviewIds = list.stream().map(CaseReview::getId).toList();
        Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap = getReviewCaseMap(reviewIds);
        List<CaseReviewUserDTO> reviewUsers = getReviewUsers(reviewIds);
        Set<String> userIds = extractUserIds(list);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        Map<String, List<CaseReviewUserDTO>> reviewUserMap = reviewUsers.stream().collect(Collectors.groupingBy(CaseReviewUserDTO::getReviewId));
        for (CaseReviewDTO caseReviewDTO : list) {
            buildCaseReviewDTO(caseReviewDTO, reviewCaseMap, reviewUserMap);
            caseReviewDTO.setCreateUserName(userMap.get(caseReviewDTO.getCreateUser()));
            caseReviewDTO.setUpdateUserName(userMap.get(caseReviewDTO.getUpdateUser()));
        }

        return list;
    }

    private Set<String> extractUserIds(List<CaseReviewDTO> list) {
        return list.stream()
                .flatMap(caseReviewDTO -> Stream.of(caseReviewDTO.getUpdateUser(), caseReviewDTO.getCreateUser()))
                .collect(Collectors.toSet());
    }

    /**
     * 通过 reviewCaseMap reviewUserMap 补充 用例评审的其他属性
     *
     * @param caseReviewDTO caseReviewDTO
     * @param reviewCaseMap 用例和评审的关系map
     */
    private static void buildCaseReviewDTO(CaseReviewDTO caseReviewDTO, Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap, Map<String, List<CaseReviewUserDTO>> reviewUserMap) {
        String caseReviewId = caseReviewDTO.getId();
        List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList = reviewCaseMap.get(caseReviewId);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCaseList)) {
            caseReviewDTO.setPassCount(0);
            caseReviewDTO.setUnPassCount(0);
            caseReviewDTO.setReReviewedCount(0);
            caseReviewDTO.setUnderReviewedCount(0);
            caseReviewDTO.setReviewedCount(0);
            caseReviewDTO.setUnReviewCount(0);
        } else {
            buildAboutCaseCount(caseReviewDTO, caseReviewFunctionalCaseList);
        }
        caseReviewDTO.setReviewers(reviewUserMap.get(caseReviewId));
    }


    /**
     * 构建用例相关的各种数量
     *
     * @param caseReviewDTO                用例评审
     * @param caseReviewFunctionalCaseList 用例和评审相关联的集合
     */
    private static void buildAboutCaseCount(CaseReviewDTO caseReviewDTO, List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList) {
        Map<String, List<CaseReviewFunctionalCase>> statusCaseMap = caseReviewFunctionalCaseList.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));

        List<CaseReviewFunctionalCase> passList = statusCaseMap.get(FunctionalCaseReviewStatus.PASS.toString());
        if (passList == null) {
            passList = new ArrayList<>();
        }
        caseReviewDTO.setPassCount(passList.size());

        BigDecimal passRate = BigDecimal.valueOf(caseReviewDTO.getPassCount()).divide(BigDecimal.valueOf(caseReviewDTO.getCaseCount()), 2, RoundingMode.HALF_UP);
        caseReviewDTO.setPassRate(passRate.multiply(BigDecimal.valueOf(100)));

        List<CaseReviewFunctionalCase> unPassList = statusCaseMap.get(FunctionalCaseReviewStatus.UN_PASS.toString());
        if (unPassList == null) {
            unPassList = new ArrayList<>();
        }
        caseReviewDTO.setUnPassCount(unPassList.size());

        List<CaseReviewFunctionalCase> reReviewedList = statusCaseMap.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        if (reReviewedList == null) {
            reReviewedList = new ArrayList<>();
        }
        caseReviewDTO.setReReviewedCount(reReviewedList.size());

        List<CaseReviewFunctionalCase> underReviewedList = statusCaseMap.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        if (underReviewedList == null) {
            underReviewedList = new ArrayList<>();
        }
        caseReviewDTO.setUnderReviewedCount(underReviewedList.size());

        caseReviewDTO.setReviewedCount(caseReviewDTO.getPassCount() + caseReviewDTO.getUnPassCount());

        List<CaseReviewFunctionalCase> unReviewList = statusCaseMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        if (unReviewList == null) {
            unReviewList = new ArrayList<>();
        }
        caseReviewDTO.setUnReviewCount(unReviewList.size());

    }

    /**
     * 通过评审ids获取评审和评审人的关系map
     *
     * @param reviewIds 评审ids
     * @return Map
     */
    private List<CaseReviewUserDTO> getReviewUsers(List<String> reviewIds) {
        return extCaseReviewUserMapper.getReviewUser(reviewIds);
    }


    /**
     * 通过评审ids获取用例和评审的关系map
     *
     * @param reviewIds 评审ids
     * @return Map
     */
    private Map<String, List<CaseReviewFunctionalCase>> getReviewCaseMap(List<String> reviewIds) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getList(null, reviewIds, false);
        return caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
    }


    /**
     * 添加用例评审
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public CaseReview addCaseReview(CaseReviewRequest request, String userId) {
        String caseReviewId = IDGenerator.nextStr();
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        CaseReview caseReview = addCaseReview(request, userId, caseReviewId, caseIds);
        addAssociate(request, userId, caseReviewId, caseIds, baseAssociateCaseRequest.getReviewers());
        return caseReview;
    }

    private void addAssociate(CaseReviewRequest request, String userId, String caseReviewId, List<String> caseIds, List<String> reviewers) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewUserMapper mapper = sqlSession.getMapper(CaseReviewUserMapper.class);
        CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper = sqlSession.getMapper(CaseReviewFunctionalCaseMapper.class);
        CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper = sqlSession.getMapper(CaseReviewFunctionalCaseUserMapper.class);
        try {
            //保存和评审人的关系
            addCaseReviewUser(request, caseReviewId, mapper);
            //保存和用例的关系
            addCaseReviewFunctionalCase(caseIds, userId, caseReviewId, caseReviewFunctionalCaseMapper);
            //保存用例和用例评审人的关系
            addCaseReviewFunctionalCaseUser(caseIds, reviewers, caseReviewId, caseReviewFunctionalCaseUserMapper);
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public CaseReview copyCaseReview(CaseReviewCopyRequest request, String userId) {
        String caseReviewId = IDGenerator.nextStr();
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(request.getCopyId());
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        List<String> caseIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).distinct().toList();
        CaseReview caseReview = addCaseReview(request, userId, caseReviewId, caseIds);
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        if (baseAssociateCaseRequest != null) {
            addAssociate(request, userId, caseReviewId, caseIds, baseAssociateCaseRequest.getReviewers());
        } else {
            addAssociate(request, userId, caseReviewId, caseIds, request.getReviewers());
        }
        return caseReview;
    }

    /**
     * 保存用例和用例评审人的关系
     *
     * @param caseIds                            caseIds
     * @param reviewers                          reviewers
     * @param caseReviewId                       当前用例评审id
     * @param caseReviewFunctionalCaseUserMapper mapper
     */
    private static void addCaseReviewFunctionalCaseUser(List<String> caseIds, List<String> reviewers, String caseReviewId, CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper) {
        if (CollectionUtils.isNotEmpty(caseIds)) {
            caseIds.forEach(caseId -> {
                reviewers.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseReviewFunctionalCaseUser = new CaseReviewFunctionalCaseUser();
                    caseReviewFunctionalCaseUser.setCaseId(caseId);
                    caseReviewFunctionalCaseUser.setUserId(reviewer);
                    caseReviewFunctionalCaseUser.setReviewId(caseReviewId);
                    caseReviewFunctionalCaseUserMapper.insert(caseReviewFunctionalCaseUser);
                });
            });
        }
    }

    /**
     * 编辑用例评审
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void editCaseReview(CaseReviewRequest request, String userId) {
        String reviewId = request.getId();
        checkCaseReview(reviewId);
        CaseReview caseReview = new CaseReview();
        caseReview.setId(reviewId);
        caseReview.setProjectId(request.getProjectId());
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(request.getTags());
        } else {
            caseReview.setTags(new ArrayList<>());
        }
        caseReview.setDescription(request.getDescription());
        checkAndSetStartAndEndTime(request, caseReview);
        caseReview.setUpdateTime(System.currentTimeMillis());
        caseReview.setUpdateUser(userId);
        caseReviewMapper.updateByPrimaryKeySelective(caseReview);
        //删除用例评审和评审人的关系
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdEqualTo(reviewId);
        caseReviewUserMapper.deleteByExample(caseReviewUserExample);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewUserMapper mapper = sqlSession.getMapper(CaseReviewUserMapper.class);
        try {
            //保存评审和评审人的关系
            addCaseReviewUser(request, reviewId, mapper);
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
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
     * 获取当前项目的所有用户
     *
     * @param projectId projectId
     * @param keyword   查询关键字，根据邮箱和用户名查询
     * @return List<User>
     */
    public List<UserDTO> getReviewUserList(String projectId, String keyword) {
        return extUserMapper.getUserByKeyword(projectId, keyword);

    }

    /**
     * 新增用例评审
     *
     * @param request      request
     * @param userId       当前操作人
     * @param caseReviewId 用例评审id
     */
    private CaseReview addCaseReview(CaseReviewRequest request, String userId, String caseReviewId, List<String> caseIds) {
        CaseReview caseReview = new CaseReview();
        caseReview.setId(caseReviewId);
        caseReview.setNum(getNextNum(request.getProjectId()));
        caseReview.setProjectId(request.getProjectId());
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        caseReview.setStatus(CaseReviewStatus.PREPARED.toString());
        caseReview.setReviewPassRule(request.getReviewPassRule());
        caseReview.setPos(getNextPos(request.getProjectId()));
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(request.getTags());
        }
        caseReview.setPassRate(BigDecimal.valueOf(0.00));
        if (CollectionUtils.isEmpty(caseIds)) {
            caseReview.setCaseCount(0);
        } else {
            caseReview.setCaseCount(caseIds.size());
        }
        caseReview.setDescription(request.getDescription());
        checkAndSetStartAndEndTime(request, caseReview);
        caseReview.setCreateTime(System.currentTimeMillis());
        caseReview.setUpdateTime(System.currentTimeMillis());
        caseReview.setCreateUser(userId);
        caseReview.setUpdateUser(userId);
        caseReviewMapper.insert(caseReview);
        return caseReview;
    }

    private void checkAndSetStartAndEndTime(CaseReviewRequest request, CaseReview caseReview) {
        long currentZeroTime = getCurrentZeroTime();
        if (request.getStartTime() != null && request.getStartTime() < currentZeroTime) {
            throw new MSException(Translator.get("permission.case_review.start_time"));
        } else {
            caseReview.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null && request.getEndTime() < currentZeroTime) {
            throw new MSException(Translator.get("permission.case_review.end_time"));
        } else {
            caseReview.setEndTime(request.getEndTime());
        }
    }

    public long getCurrentZeroTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }


    /**
     * 检查用例评审是否存在
     *
     * @param caseReviewId 用例评审id
     */
    public CaseReview checkCaseReview(String caseReviewId) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(caseReviewId);
        if (caseReview == null) {
            throw new MSException(CaseManagementResultCode.CASE_REVIEW_NOT_FOUND);
        }
        return caseReview;
    }

    /**
     * @param projectId 项目id
     * @return pos
     */
    public Long getNextPos(String projectId) {
        Long pos = extCaseReviewMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    /**
     * @param caseReviewId 用例评审id
     * @return pos
     */
    public Long getCaseFunctionalCaseNextPos(String caseReviewId) {
        Long pos = extCaseReviewFunctionalCaseMapper.getPos(caseReviewId);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    /**
     * @param projectId 项目id
     * @return num
     */
    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.REVIEW_CASE_MANAGEMENT);
    }

    /**
     * 保存用例评审和功能用例的关系
     *
     * @param caseIds                        功能用例Ids
     * @param userId                         当前操作人
     * @param caseReviewId                   用例评审id
     * @param caseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper
     */
    private void addCaseReviewFunctionalCase(List<String> caseIds, String userId, String caseReviewId, CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper) {
        if (CollectionUtils.isNotEmpty(caseIds)) {
            Long nextPos = getCaseFunctionalCaseNextPos(caseReviewId);
            caseIds.forEach(caseId -> {
                CaseReviewFunctionalCase caseReviewFunctionalCase = new CaseReviewFunctionalCase();
                caseReviewFunctionalCase.setReviewId(caseReviewId);
                caseReviewFunctionalCase.setCaseId(caseId);
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
                caseReviewFunctionalCase.setCreateUser(userId);
                caseReviewFunctionalCase.setCreateTime(System.currentTimeMillis());
                caseReviewFunctionalCase.setUpdateTime(System.currentTimeMillis());
                caseReviewFunctionalCase.setId(IDGenerator.nextStr());
                caseReviewFunctionalCase.setPos(nextPos + POS_STEP);
                caseReviewFunctionalCaseMapper.insert(caseReviewFunctionalCase);
            });
        }
    }

    /**
     * 保存用例评审和评审人的关系
     *
     * @param request              request
     * @param caseReviewId         用例评审
     * @param caseReviewUserMapper caseReviewUserMapper
     */
    private static void addCaseReviewUser(CaseReviewRequest request, String caseReviewId, CaseReviewUserMapper caseReviewUserMapper) {
        request.getReviewers().forEach(user -> {
            CaseReviewUser caseReviewUser = new CaseReviewUser();
            caseReviewUser.setReviewId(caseReviewId);
            caseReviewUser.setUserId(user);
            caseReviewUserMapper.insert(caseReviewUser);
        });
    }


    /**
     * 关联用例
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void associateCase(CaseReviewAssociateRequest request, String userId) {
        String caseReviewId = request.getReviewId();
        checkCaseReview(caseReviewId);
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        if (CollectionUtils.isEmpty(caseIds)) {
            return;
        }
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andIdIn(caseIds);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(functionalCases)) {
            return;
        }
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getList(caseReviewId, null, false);
        List<String> castIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        List<String> caseRealIds = caseIds.stream().filter(t -> !castIds.contains(t)).toList();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper = sqlSession.getMapper(CaseReviewFunctionalCaseMapper.class);
        CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper = sqlSession.getMapper(CaseReviewFunctionalCaseUserMapper.class);
        try {
            //保存和用例的关系
            addCaseReviewFunctionalCase(caseRealIds, userId, caseReviewId, caseReviewFunctionalCaseMapper);
            //保存用例和用例评审人的关系
            addCaseReviewFunctionalCaseUser(caseRealIds, request.getReviewers(), caseReviewId, caseReviewFunctionalCaseUserMapper);
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.REVIEW_ID, caseReviewId);
        param.put(CaseEvent.Param.CASE_IDS, caseRealIds);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.ASSOCIATE);
        provider.updateCaseReview(param);
    }

    public <T> List<String> doSelectIds(T dto, String projectId) {
        BaseFunctionalCaseBatchDTO request = (BaseFunctionalCaseBatchDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseMapper.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    /**
     * 用例评审列表拖拽排序
     *
     * @param request request
     */
    public void editPos(PosRequest request) {
        ServiceUtils.updatePosField(request,
                CaseReview.class,
                caseReviewMapper::selectByPrimaryKey,
                extCaseReviewMapper::getPrePos,
                extCaseReviewMapper::getLastPos,
                caseReviewMapper::updateByPrimaryKeySelective);
    }

    /**
     * 获取用例评审详情
     *
     * @param id     用例评审id
     * @param userId 当前操作人
     * @return CaseReviewDTO
     */
    public CaseReviewDTO getCaseReviewDetail(String id, String userId) {
        CaseReview caseReview = checkCaseReview(id);
        CaseReviewDTO caseReviewDTO = new CaseReviewDTO();
        BeanUtils.copyBean(caseReviewDTO, caseReview);
        Boolean isFollow = checkFollow(id, userId);
        caseReviewDTO.setFollowFlag(isFollow);
        Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap = getReviewCaseMap(List.of(id));
        List<CaseReviewUserDTO> reviewUsers = getReviewUsers(List.of(id));
        Map<String, List<CaseReviewUserDTO>> reviewUsersMap = new HashMap<>();
        reviewUsersMap.put(id, reviewUsers);
        buildCaseReviewDTO(caseReviewDTO, reviewCaseMap, reviewUsersMap);
        return caseReviewDTO;
    }

    /**
     * 检查当前操作人是否关注该用例评审
     *
     * @param id     评审人名称
     * @param userId 操作人
     * @return Boolean
     */
    private Boolean checkFollow(String id, String userId) {
        CaseReviewFollowerExample caseReviewFollowerExample = new CaseReviewFollowerExample();
        caseReviewFollowerExample.createCriteria().andReviewIdEqualTo(id).andUserIdEqualTo(userId);
        return caseReviewFollowerMapper.countByExample(caseReviewFollowerExample) > 0;
    }

    public void batchMoveCaseReview(CaseReviewBatchRequest request, String userId) {
        List<String> ids;
        if (StringUtils.isBlank(request.getMoveModuleId())) {
            throw new MSException(Translator.get("functional_case.module_id.not_blank"));
        }
        if (request.isSelectAll()) {
            ids = extCaseReviewMapper.getIds(request, request.getProjectId());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }

        } else {
            ids = request.getSelectIds();
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            extCaseReviewMapper.batchMoveModule(request, ids, userId);
        }
    }

    public void deleteCaseReview(String reviewId, String projectId) {
        deleteCaseReviewService.deleteCaseReviewResource(List.of(reviewId), projectId);
    }

    public void disassociate(String reviewId, String caseId, String userId) {
        checkCase(caseId);
        //1.刪除评审与功能用例关联关系
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo(caseId);
        caseReviewFunctionalCaseMapper.deleteByExample(caseReviewFunctionalCaseExample);

        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DISASSOCIATE);
        provider.updateCaseReview(param);
    }

    private void checkCase(String caseId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
        if (functionalCase == null) {
            throw new MSException(CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        }
    }

    public Map<String, Long> moduleCount(CaseReviewPageRequest request) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extCaseReviewMapper.countModuleIdByKeywordAndFileType(request);
        Map<String, Long> moduleCountMap = caseReviewModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        //查出全部用例数量
        long allCount = extCaseReviewMapper.caseCount(request);
        moduleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    public String getReviewPassRule(String id) {
        return extCaseReviewMapper.getReviewPassRule(id);
    }


}