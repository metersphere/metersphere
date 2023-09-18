package io.metersphere.project.service;

import io.metersphere.project.domain.MessageTaskExample;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CleanupMessageTaskService implements CleanupProjectResourceService {

    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Override
    public void deleteResources(String projectId) {
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(projectId);
        messageTaskMapper.deleteByExample(messageTaskExample);
        LogUtils.info("删除当前项目[" + projectId + "]相关消息管理资源");
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关消息管理报告资源");
    }
}
