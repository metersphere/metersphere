package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiReportSendNoticeService {

    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ProjectMapper projectMapper;

    public void sendNotice(ApiNoticeDTO noticeDTO) {
        String noticeType;
        String reportUrl;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        assert systemParameterService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        BeanMap beanMap;
        switch (noticeDTO.getResourceType()) {
            case ApiExecuteResourceType.API_SCENARIO:
                ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(noticeDTO.getResourceId());
                beanMap = new BeanMap(scenario);
                noticeType = NoticeConstants.TaskType.API_SCENARIO_TASK;
                reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + noticeDTO.getReportId();
                break;
            case ApiExecuteResourceType.API:
                ApiDefinition definition = apiDefinitionMapper.selectByPrimaryKey(noticeDTO.getResourceId());
                beanMap = new BeanMap(definition);
                noticeType = NoticeConstants.TaskType.API_DEFINITION_TASK;

                // TODO: 缺少生成分享链接
                reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + noticeDTO.getReportId();
                break;
            default:
                ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(noticeDTO.getResourceId());
                beanMap = new BeanMap(testCase);

                // TODO 是否需要区分场景和用例
                noticeType = NoticeConstants.TaskType.API_DEFINITION_TASK;
                reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + noticeDTO.getReportId();
                break;
        }

        String event;
        String status;
        if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ApiReportStatus.SUCCESS.name())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
            status = "成功";
        } else {
            event = NoticeConstants.Event.EXECUTE_FAILED;
            status = "失败";
        }
        String userId = noticeDTO.getUserId();
        User user = userMapper.selectByPrimaryKey(userId);

        Map<String, Object> paramMap = new HashMap(beanMap);
        paramMap.put("operator", user != null ? user.getName() : "");
        paramMap.put("status", noticeDTO.getReportStatus());

        // TODO 暂时取一个环境处理
        if (CollectionUtils.isNotEmpty(noticeDTO.getEnvironmentIds())) {
            Environment environment = environmentMapper.selectByPrimaryKey(noticeDTO.getEnvironmentIds().getFirst());
            if (environment != null) {
                paramMap.put("environment", environment.getName());
            }
        } else {
            paramMap.put("environment", "未配置");
        }
        paramMap.put("reportUrl", reportUrl);


        // TODO: 缺少生成分享链接
        String shareUrl = null;
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/api/share-api-report" + shareUrl);
        String context = "${operator}执行接口测试" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder().operator(userId)
                .context(context).subject("执行通知").paramMap(paramMap).event(event).build();

        Project project = projectMapper.selectByPrimaryKey(noticeDTO.getProjectId());

        noticeSendService.send(project, noticeType, noticeModel);
    }

}
