package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缺陷相关资源清理
 */
@Component
public class CleanupBugResourceServiceImpl implements CleanupProjectResourceService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private BugCommonService bugCommonService;

    @Async
    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关缺陷测试资源");
        List<String> deleteIds = getBugIds(projectId);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            // 清理缺陷
            deleteBug(deleteIds);
            // 清空关联关系
            bugCommonService.clearAssociateResource(projectId, deleteIds);
        }
    }

    private List<String> getBugIds(String projectId) {
        BugExample example = new BugExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<Bug> bugs = bugMapper.selectByExample(example);
        return bugs.stream().map(Bug::getId).toList();
    }

    private void deleteBug(List<String> bugIds) {
        BugExample example = new BugExample();
        example.createCriteria().andIdIn(bugIds);
        bugMapper.deleteByExample(example);
    }
}
