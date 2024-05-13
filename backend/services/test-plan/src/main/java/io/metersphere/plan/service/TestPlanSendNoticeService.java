package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanSendNoticeService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private NoticeSendService noticeSendService;

    public void sendNoticeCase(List<String> relatedUsers, String userId, String caseId, String task, String event, String testPlanId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
        User user = userMapper.selectByPrimaryKey(userId);
        setLanguage(user.getLanguage());
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(task + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(task + "_" + event);
        Map paramMap;
        BeanMap beanMap = new BeanMap(functionalCase);
        paramMap = new HashMap<>(beanMap);
        paramMap.put("testPlanName", testPlan.getName());
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(user.getId())
                .context(template)
                .subject(subject)
                .paramMap(paramMap)
                .event(event)
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(relatedUsers)
                .build();
        noticeSendService.send(task, noticeModel);
    }

    private static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase("US",language)) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase("TW",language)){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }
}
