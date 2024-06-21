package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtOrganizationCustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseNoticeService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private FunctionalCaseFollowerMapper functionalCaseFollowerMapper;

    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    @Resource
    private CustomFieldMapper customFieldMapper;

    @Resource
    private FunctionalCaseCustomFieldService functionalCaseCustomFieldService;
    @Resource
    private ExtOrganizationCustomFieldMapper extOrganizationCustomFieldMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;

    @Resource
    private CommonNoticeSendService commonNoticeSendService;


    public FunctionalCaseDTO getFunctionalCaseDTO(FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        functionalCaseDTO.setTriggerMode(NoticeConstants.TriggerMode.MANUAL_EXECUTION);
        BeanUtils.copyBean(functionalCaseDTO, functionalCase);
        setNotifier(functionalCaseCommentRequest, functionalCaseDTO);
        List<OptionDTO> customFields = getCustomFields(functionalCaseCommentRequest.getCaseId());
        functionalCaseDTO.setFields(customFields);
        setPlanName(functionalCaseCommentRequest.getCaseId(), functionalCaseDTO);
        setReviewName(functionalCaseCommentRequest.getCaseId(), functionalCaseDTO);
        return functionalCaseDTO;
    }

    /**
     * 如果是REPLAY事件，需要判断有无@的人，如果有@的人且当前被回复的人不是同一人，这里只要被回复的人,如果是同一人，这里通知人为空，走AT事件
     * 如果不是REPLAY事件，需要判断有无被回复的人，如果被回复的人不在被@人里，则用页面参数传递的通知人，如果在，则排除这个人,如果没有被回复的人，用页面数据
     *
     * @param functionalCaseCommentRequest 页面参数
     * @param functionalCaseDTO            发通知需要解析字段集合
     */
    private void setNotifier(FunctionalCaseCommentRequest functionalCaseCommentRequest, FunctionalCaseDTO functionalCaseDTO) {
        String notifier = functionalCaseCommentRequest.getNotifier();
        String replyUser = functionalCaseCommentRequest.getReplyUser();
        if (StringUtils.equals(functionalCaseCommentRequest.getEvent(), NoticeConstants.Event.REPLY)) {
            if (StringUtils.isNotBlank(notifier)) {
                List<String> notifierList = Arrays.asList(notifier.split(";"));
                if (!notifierList.contains(replyUser)) {
                    functionalCaseDTO.setRelatedUsers(replyUser);
                }
            } else {
                functionalCaseDTO.setRelatedUsers(replyUser);
            }
        } else {
            if (StringUtils.isNotBlank(replyUser) && StringUtils.isNotBlank(notifier)) {
                List<String> notifierList = Arrays.asList(notifier.split(";"));
                StringBuilder notifierStr = new StringBuilder();
                if (notifierList.contains(replyUser)) {
                    notifierStr.append(replyUser).append(";");
                    for (String notifierId : notifierList) {
                        if (!StringUtils.equals(notifierId, replyUser)) {
                            notifierStr.append(notifierId).append(";");
                        }
                    }
                } else {
                    notifierStr = new StringBuilder(notifier);
                }
                functionalCaseDTO.setRelatedUsers(notifierStr.toString());
            } else {
                functionalCaseDTO.setRelatedUsers(notifier);
            }
        }
    }

    /**
     * 根据用例id获取当前用例在使用的自定义的字段及其值
     *
     * @param caseId 用例Id
     * @return 返回 字段以及字段值的组合
     */
    private List<OptionDTO> getCustomFields(String caseId) {
        FunctionalCaseCustomFieldExample functionalCaseCustomFieldExample = new FunctionalCaseCustomFieldExample();
        functionalCaseCustomFieldExample.createCriteria().andCaseIdEqualTo(caseId);
        List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(functionalCaseCustomFieldExample);
        List<OptionDTO> optionDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(functionalCaseCustomFields)) {
            Map<String, String> fieldValueMap = functionalCaseCustomFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, FunctionalCaseCustomField::getValue));
            List<String> fieldIds = functionalCaseCustomFields.stream().map(FunctionalCaseCustomField::getFieldId).distinct().toList();
            CustomFieldExample customFieldExample = new CustomFieldExample();
            customFieldExample.createCriteria().andIdIn(fieldIds);
            List<CustomField> customFields = customFieldMapper.selectByExample(customFieldExample);
            customFields.forEach(t -> {
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(t.getName());
                optionDTO.setName(fieldValueMap.get(t.getId()));
                optionDTOList.add(optionDTO);
            });
        }
        return optionDTOList;
    }

    public FunctionalCaseDTO getAddMainFunctionalCaseDTO(FunctionalCaseAddRequest request, List<CaseCustomFieldDTO> customFields) {
        FunctionalCaseEditRequest editRequest = new FunctionalCaseEditRequest();
        BeanUtils.copyBean(editRequest, request);
        return getMainFunctionalCaseDTO(editRequest, customFields);
    }

    public FunctionalCaseDTO getMainFunctionalCaseDTO(FunctionalCaseEditRequest request, List<CaseCustomFieldDTO> customFields) {
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        if (StringUtils.isNotBlank(request.getId())) {
            FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getId());
            BeanUtils.copyBean(functionalCaseDTO, functionalCase);
            setReviewName(request.getId(), functionalCaseDTO);
            setPlanName(request.getId(), functionalCaseDTO);
        } else {
            BeanUtils.copyBean(functionalCaseDTO, request);
            functionalCaseDTO.setCreateUser(null);
        }
        functionalCaseDTO.setTriggerMode(NoticeConstants.TriggerMode.MANUAL_EXECUTION);
        List<OptionDTO> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            for (CaseCustomFieldDTO customFieldDTO : customFields) {
                OptionDTO optionDTO = new OptionDTO();
                CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldDTO.getFieldId());
                if (customField == null) {
                    continue;
                }
                optionDTO.setId(customField.getName());
                optionDTO.setName(customFieldDTO.getValue());
                fields.add(optionDTO);
            }
        }
        functionalCaseDTO.setFields(fields);
        return functionalCaseDTO;
    }

    private void setReviewName(String caseId, FunctionalCaseDTO functionalCaseDTO) {
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andCaseIdEqualTo(caseId);
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        List<String> reviewIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getReviewId).distinct().toList();
        if (CollectionUtils.isNotEmpty(reviewIds)) {
            CaseReviewExample caseReviewExample = new CaseReviewExample();
            caseReviewExample.createCriteria().andIdIn(reviewIds);
            List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
            List<String> reviewName = caseReviews.stream().map(CaseReview::getName).toList();
            String string = reviewName.toString();
            functionalCaseDTO.setReviewName(string);
        }
    }

    private void setPlanName(String caseId, FunctionalCaseDTO functionalCaseDTO) {
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andFunctionalCaseIdEqualTo(caseId);
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        List<String> planIds = testPlanFunctionalCases.stream().map(TestPlanFunctionalCase::getTestPlanId).distinct().toList();
        if (CollectionUtils.isNotEmpty(planIds)) {
            TestPlanExample testPlanExample = new TestPlanExample();
            testPlanExample.createCriteria().andIdIn(planIds);
            List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);
            List<String> planName = testPlans.stream().map(TestPlan::getName).toList();
            String string = planName.toString();
            functionalCaseDTO.setTestPlanName(string);
        }
    }


    public FunctionalCaseDTO getDeleteFunctionalCaseDTO(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        Optional.ofNullable(functionalCase).ifPresent(functional -> {
            BeanUtils.copyBean(functionalCaseDTO, functionalCase);
            List<OptionDTO> customFields = getCustomFields(id);
            functionalCaseDTO.setFields(customFields);

        });
        setPlanName(id, functionalCaseDTO);
        setReviewName(id, functionalCaseDTO);
        return functionalCaseDTO;
    }

    public Map<String, FunctionalCase> copyBaseCaseInfo(String projectId, List<String> ids) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andIdIn(ids);
        List<FunctionalCase> functionalCaseLists = functionalCaseMapper.selectByExample(example);
        return functionalCaseLists.stream().collect(Collectors.toMap(FunctionalCase::getId, functionalCase -> functionalCase));
    }

    public List<FunctionalCaseDTO> handleBatchNotice(String projectId, List<String> ids) {
        List<FunctionalCaseDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            Map<String, FunctionalCase> functionalCaseMap = copyBaseCaseInfo(projectId, ids);
            Map<String, List<FunctionalCaseCustomField>> customFieldMap = functionalCaseCustomFieldService.getCustomFieldMapByCaseIds(ids);
            AtomicReference<List<OptionDTO>> optionDTOS = new AtomicReference<>(new ArrayList<>());
            CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
            caseReviewFunctionalCaseExample.createCriteria().andCaseIdIn(ids);
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
            List<String> reviewIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getReviewId).distinct().toList();
            TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
            testPlanFunctionalCaseExample.createCriteria().andFunctionalCaseIdIn(ids);
            List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
            List<String> planIds = testPlanFunctionalCases.stream().map(TestPlanFunctionalCase::getTestPlanId).distinct().toList();
            Map<String, String> reviewMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(reviewIds)) {
                CaseReviewExample caseReviewExample = new CaseReviewExample();
                caseReviewExample.createCriteria().andIdIn(reviewIds);
                List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
                reviewMap = caseReviews.stream().collect(Collectors.toMap(CaseReview::getId, CaseReview::getName));
            }
            Map<String, String> planMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(planIds)) {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(planIds);
                List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);
                planMap = testPlans.stream().collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));
            }
            Map<String, List<CaseReviewFunctionalCase>> caseReviewMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getCaseId));
            Map<String, List<TestPlanFunctionalCase>> casePlanMap = testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getFunctionalCaseId));
            Map<String, String> finalReviewMap = reviewMap;
            Map<String, String> finalPlanMap = planMap;

            FunctionalCaseFollowerExample example = new FunctionalCaseFollowerExample();
            example.createCriteria().andCaseIdIn(ids);
            List<FunctionalCaseFollower> functionalCaseFollowers = functionalCaseFollowerMapper.selectByExample(example);
            Map<String, List<FunctionalCaseFollower>> followMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(functionalCaseFollowers)) {
               followMap = functionalCaseFollowers.stream().collect(Collectors.groupingBy(FunctionalCaseFollower::getCaseId));
            }
            Map<String, List<FunctionalCaseFollower>> finalFollowMap = followMap;
            ids.forEach(id -> {
                FunctionalCase functionalCase = functionalCaseMap.get(id);
                if (functionalCase != null) {
                    List<FunctionalCaseCustomField> customFields = customFieldMap.get(id);
                    if (CollectionUtils.isNotEmpty(customFields)) {
                        List<String> fields = customFields.stream().map(FunctionalCaseCustomField::getFieldId).toList();
                        optionDTOS.set(extOrganizationCustomFieldMapper.getCustomFieldOptions(fields));
                    }
                    FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
                    functionalCaseDTO.setName(functionalCase.getName());
                    functionalCaseDTO.setProjectId(functionalCase.getProjectId());
                    functionalCaseDTO.setCaseEditType(functionalCase.getCaseEditType());
                    functionalCaseDTO.setCreateUser(StringUtils.isBlank(functionalCase.getCreateUser()) ? null : functionalCase.getCreateUser());
                    functionalCaseDTO.setFields(optionDTOS.get());
                    List<FunctionalCaseFollower> caseFollowers = finalFollowMap.get(id);
                    if (CollectionUtils.isNotEmpty(caseFollowers)) {
                        List<String> followUsers = caseFollowers.stream().map(FunctionalCaseFollower::getUserId).toList();
                        functionalCaseDTO.setFollowUsers(followUsers);
                    }
                    List<CaseReviewFunctionalCase> caseReviewFunctionalCases1 = caseReviewMap.get(id);
                    List<String> reviewName = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(caseReviewFunctionalCases1)) {
                        for (CaseReviewFunctionalCase caseReviewFunctionalCase : caseReviewFunctionalCases1) {
                            String s = finalReviewMap.get(caseReviewFunctionalCase.getReviewId());
                            reviewName.add(s);
                        }
                    }
                    List<TestPlanFunctionalCase> planFunctionalCases = casePlanMap.get(id);
                    List<String> planName = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(planFunctionalCases)) {
                        for (TestPlanFunctionalCase planFunctionalCase : planFunctionalCases) {
                            String s = finalPlanMap.get(planFunctionalCase.getTestPlanId());
                            planName.add(s);
                        }
                    }

                    List<String> list = reviewName.stream().distinct().toList();
                    functionalCaseDTO.setReviewName(list.toString());
                    List<String> planNameList = planName.stream().distinct().toList();
                    functionalCaseDTO.setTestPlanName(planNameList.toString());
                    dtoList.add(functionalCaseDTO);
                }
            });

        }
        return dtoList;
    }


    public void batchSendNotice(String projectId, List<String> ids, User user, String event) {
        int amount = 100;//每次读取的条数
        long roundTimes = Double.valueOf(Math.ceil((double) ids.size() / amount)).longValue();//循环的次数
        for (int i = 0; i < (int) roundTimes; i++) {
            int fromIndex = (i * amount);
            int toIndex = ((i + 1) * amount);
            if (i == roundTimes - 1 || toIndex > ids.size()) {//最后一次遍历
                toIndex = ids.size();
            }
            List<String> subList = ids.subList(fromIndex, toIndex);
            List<FunctionalCaseDTO> functionalCaseDTOS = handleBatchNotice(projectId, subList);
            List<Map> resources = new ArrayList<>();
            resources.addAll(JSON.parseArray(JSON.toJSONString(functionalCaseDTOS), Map.class));
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event, resources, user, projectId);
        }
    }

}
