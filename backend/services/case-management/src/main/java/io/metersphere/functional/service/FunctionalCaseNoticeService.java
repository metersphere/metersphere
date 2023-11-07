package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FunctionalCaseNoticeService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    @Resource
    private CustomFieldMapper customFieldMapper;

    public FunctionalCaseDTO getFunctionalCaseDTO(FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        BeanUtils.copyBean(functionalCaseDTO, functionalCase);
        setNotifier(functionalCaseCommentRequest, functionalCaseDTO);
        List<OptionDTO> customFields = getCustomFields(functionalCaseCommentRequest.getCaseId());
        functionalCaseDTO.setFields(customFields);
        //TODO:设置测试计划名称
        //TODO：设置用例评审名称
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
            if (StringUtils.isNotBlank(replyUser)) {
                if (StringUtils.isNotBlank(notifier)) {
                    List<String> notifierList = Arrays.asList(notifier.split(";"));
                    if (!notifierList.contains(replyUser)) {
                        functionalCaseDTO.setRelatedUsers(replyUser);
                    }
                } else {
                    functionalCaseDTO.setRelatedUsers(replyUser);
                }
            }
        }
        else {
            if (StringUtils.isNotBlank(replyUser) && StringUtils.isNotBlank(notifier)) {
                List<String> notifierList = Arrays.asList(notifier.split(";"));
                if (notifierList.contains(replyUser)) {
                    notifierList.remove(replyUser);
                    functionalCaseDTO.setRelatedUsers(String.join(";", notifierList));
                } else {
                    functionalCaseDTO.setRelatedUsers(notifier);
                }
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

    public FunctionalCaseDTO getMainFunctionalCaseDTO(String name, String caseEditType,String projectId, List<CaseCustomsFieldDTO> customsFields) {
        String userId = SessionUtils.getUserId();
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        functionalCaseDTO.setName(name);
        functionalCaseDTO.setProjectId(projectId);
        functionalCaseDTO.setCaseEditType(caseEditType);
        functionalCaseDTO.setCreateUser(userId);
        List<OptionDTO> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customsFields)) {
            for (CaseCustomsFieldDTO customsFieldDTO : customsFields) {
                OptionDTO optionDTO = new OptionDTO();
                CustomField customField = customFieldMapper.selectByPrimaryKey(customsFieldDTO.getFieldId());
                if (customField == null) {
                    continue;
                }
                optionDTO.setId(customField.getName());
                optionDTO.setName(customsFieldDTO.getValue());
                fields.add(optionDTO);
            }
        }
        functionalCaseDTO.setFields(fields);
        //TODO:设置测试计划名称
        //TODO：设置用例评审名称
        return functionalCaseDTO;
    }


    public FunctionalCaseDTO getDeleteFunctionalCaseDTO(String id){
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        Optional.ofNullable(functionalCase).ifPresent(functional -> {
            BeanUtils.copyBean(functionalCaseDTO, functionalCase);
            List<OptionDTO> customFields = getCustomFields(id);
            functionalCaseDTO.setFields(customFields);

        });
        //TODO:设置测试计划名称
        //TODO：设置用例评审名称
        return functionalCaseDTO;
    }

}
