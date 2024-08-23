package io.metersphere.functional.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.mapper.ExtFunctionalCaseDemandMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DemandSyncService {
    @Resource
    private ExtFunctionalCaseDemandMapper extFunctionalCaseDemandMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;

    public static int DEFAULT_BATCH_SIZE = 50;

    /**
     * 定时任务同步缺陷(存量-默认中文环境通知)
     * @param projectId 项目ID
     * @param scheduleUser 任务触发用户
     */
    public void syncPlatformDemandBySchedule(String projectId, String scheduleUser) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        // 创建一个 List 来保存合并后的结果
        Map<String, List<FunctionalCaseDemand>> updateMap = new HashMap<>();
        int pageNumber = 1;
        boolean count = true;
        Page<Object> page = PageHelper.startPage(pageNumber, DEFAULT_BATCH_SIZE, count);
        Pager<List<FunctionalCaseDemand>> listPager = PageUtils.setPageInfo(page, extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId,platformName));
        long total = listPager.getTotal();
        List<FunctionalCaseDemand> list = listPager.getList();
        Map<String, List<FunctionalCaseDemand>> demandMap = list.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        Set<String> demandIds = demandMap.keySet();
        //TODO: 调用三方接口获取最新需求, updateMap 缓存数据
        count = false;
        for (int i = 1; i < ((int)Math.ceil((double) total/DEFAULT_BATCH_SIZE)); i ++) {
            Page<Object> pageCycle = PageHelper.startPage(i+1, DEFAULT_BATCH_SIZE, count);
            Pager<List<FunctionalCaseDemand>> listPagerCycle = PageUtils.setPageInfo(pageCycle, extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId,platformName));
            List<FunctionalCaseDemand> pageResults = listPagerCycle.getList();
            Map<String, List<FunctionalCaseDemand>> demandsMap = pageResults.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
            Set<String> demandIdSet = demandsMap.keySet();
            //TODO: 调用三方接口获取最新需求, updateMap 缓存数据
        }
        //TODO: 循环updateMap，更新需求

        LogUtils.info("End synchronizing demands");

    }



}
