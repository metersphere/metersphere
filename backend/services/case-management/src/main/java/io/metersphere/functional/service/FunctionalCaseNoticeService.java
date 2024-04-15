package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtOrganizationCustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
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
    private CaseReviewMapper caseReviewMapper;


    public FunctionalCaseDTO getFunctionalCaseDTO(FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        functionalCaseDTO.setTriggerMode(NoticeConstants.TriggerMode.MANUAL_EXECUTION);
        BeanUtils.copyBean(functionalCaseDTO, functionalCase);
        setNotifier(functionalCaseCommentRequest, functionalCaseDTO);
        List<OptionDTO> customFields = getCustomFields(functionalCaseCommentRequest.getCaseId());
        functionalCaseDTO.setFields(customFields);
        //TODO:设置测试计划名称
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

    public FunctionalCaseDTO getMainFunctionalCaseDTO(FunctionalCaseEditRequest request, List<CaseCustomFieldDTO> customFields) {
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        if (StringUtils.isNotBlank(request.getId())) {
            FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getId());
            BeanUtils.copyBean(functionalCaseDTO, functionalCase);
            setReviewName(request.getId(), functionalCaseDTO);
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
                optionDTO.setId(customField.getId());
                optionDTO.setName(customField.getName());
                fields.add(optionDTO);
            }
        }
        functionalCaseDTO.setFields(fields);
        //TODO:设置测试计划名称
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


    public FunctionalCaseDTO getDeleteFunctionalCaseDTO(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        Optional.ofNullable(functionalCase).ifPresent(functional -> {
            BeanUtils.copyBean(functionalCaseDTO, functionalCase);
            List<OptionDTO> customFields = getCustomFields(id);
            functionalCaseDTO.setFields(customFields);

        });
        //TODO:设置测试计划名称
        setReviewName(id, functionalCaseDTO);
        return functionalCaseDTO;
    }
    public Map<String, FunctionalCase> copyBaseCaseInfo(String projectId, List<String> ids) {
        FunctionalCaseExample example = new FunctionalCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andDeletedEqualTo(false).andIdIn(ids);
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
            Map<String, String> reviewMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(reviewIds)) {
                CaseReviewExample caseReviewExample = new CaseReviewExample();
                caseReviewExample.createCriteria().andIdIn(reviewIds);
                List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
                reviewMap = caseReviews.stream().collect(Collectors.toMap(CaseReview::getId, CaseReview::getName));
            }
            Map<String, List<CaseReviewFunctionalCase>> caseReviewMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getCaseId));
            Map<String, String> finalReviewMap = reviewMap;
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
                    functionalCaseDTO.setCreateUser(null);
                    functionalCaseDTO.setFields(optionDTOS.get());
                    List<CaseReviewFunctionalCase> caseReviewFunctionalCases1 = caseReviewMap.get(id);
                    List<String>reviewName = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(caseReviewFunctionalCases1)) {
                        for (CaseReviewFunctionalCase caseReviewFunctionalCase : caseReviewFunctionalCases1) {
                            String s = finalReviewMap.get(caseReviewFunctionalCase.getReviewId());
                            reviewName.add(s);
                        }
                    }
                    List<String> list = reviewName.stream().distinct().toList();
                    functionalCaseDTO.setReviewName(list.toString());
                    dtoList.add(functionalCaseDTO);
                }
            });
            //TODO:设置测试计划名称
        }
        return dtoList;
    }

}
