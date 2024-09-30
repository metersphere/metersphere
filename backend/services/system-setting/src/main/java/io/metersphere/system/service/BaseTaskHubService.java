package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.mapper.ExtExecTaskMapper;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTaskHubService {

    @Resource
    private ExtExecTaskMapper extExecTaskMapper;


    /**
     * 系统-获取执行任务列表
     *
     * @param request
     * @param orgId
     * @param projectId
     * @return
     */
    public Pager<List<TaskHubDTO>> getTaskList(BasePageRequest request, String orgId, String projectId) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "start_time desc");
        return PageUtils.setPageInfo(page, getPage(request, orgId, projectId));
    }

    private List<TaskHubDTO> getPage(BasePageRequest request, String orgId, String projectId) {
        return extExecTaskMapper.selectList(request, orgId, projectId);
    }


}
