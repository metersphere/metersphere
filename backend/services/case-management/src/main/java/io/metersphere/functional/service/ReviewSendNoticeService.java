package io.metersphere.functional.service;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReviewSendNoticeService {

    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Async
    public void sendNotice(List<String> relatedUsers, String userId, String reviewId, String task, String event) {
        User user = userMapper.selectByPrimaryKey(userId);
        setLanguage(user.getLanguage());
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(task + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(task + "_" + event);
        Map paramMap;
        BeanMap beanMap = new BeanMap(caseReview);
        paramMap = new HashMap<>(beanMap);
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

    public void sendNoticeCase(List<String> relatedUsers, String userId, String caseId, String task, String event,String reviewId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
        User user = userMapper.selectByPrimaryKey(userId);
        setLanguage(user.getLanguage());
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(task + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(task + "_" + event);
        Map paramMap;
        BeanMap beanMap = new BeanMap(functionalCase);
        paramMap = new HashMap<>(beanMap);
        paramMap.put("reviewName", caseReview.getName());
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
        if (StringUtils.containsIgnoreCase(language, "US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language, "TW")){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }

}
