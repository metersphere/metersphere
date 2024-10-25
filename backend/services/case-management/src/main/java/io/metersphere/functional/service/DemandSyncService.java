package io.metersphere.functional.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseDemandMapper;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.plugin.platform.dto.request.DemandRelateQueryRequest;
import io.metersphere.plugin.platform.dto.response.PlatformDemandDTO;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private FunctionalCaseDemandMapper demandMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public static int DEFAULT_BATCH_SIZE = 50;

    /**
     * 定时任务同步缺陷(存量-默认中文环境通知)
     *
     * @param projectId    项目ID
     * @param scheduleUser 任务触发用户
     */
    public void syncPlatformDemandBySchedule(String projectId, String scheduleUser) {
        String platformId = projectApplicationService.getDemandPlatformId(projectId);
        // 创建一个 List 来保存合并后的结果
        Platform platform = projectApplicationService.getPlatform(projectId, false);
        List<String> deleteIds = new ArrayList<>();
        // 批量处理需求更新
        processDemandUpdates(projectId, platformId, platform,deleteIds);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            deleteDemands(deleteIds);
        }
        LogUtils.info("End synchronizing demands");
    }

    private void deleteDemands(List<String> deleteIds) {
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        List<String> deleteIdDistinct = deleteIds.stream().distinct().toList();
        functionalCaseDemandExample.createCriteria().andDemandIdIn(deleteIdDistinct);
        demandMapper.deleteByExample(functionalCaseDemandExample);
    }

    private void processDemandUpdates(String projectId, String platformId, Platform platform, List<String> deleteIds) {
        int pageNumber = 1;
        boolean count = true;
        Page<Object> page = PageHelper.startPage(pageNumber, DEFAULT_BATCH_SIZE, count);
        Pager<List<FunctionalCaseDemand>> listPager = PageUtils.setPageInfo(page, extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId, platformId));
        long total = listPager.getTotal();
        List<FunctionalCaseDemand> list = listPager.getList();
        Map<String, List<FunctionalCaseDemand>> demandFirstMap = list.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        Set<String> demandFirstIds = demandFirstMap.keySet();
        buildUpdateMap(projectId, demandFirstIds, platform, demandFirstMap, platformId,deleteIds);
        count = false;
        for (int i = 1; i < ((int) Math.ceil((double) total / DEFAULT_BATCH_SIZE)); i++) {
            Page<Object> pageCycle = PageHelper.startPage(i + 1, DEFAULT_BATCH_SIZE, count);
            Pager<List<FunctionalCaseDemand>> listPagerCycle = PageUtils.setPageInfo(pageCycle, extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId, platformId));
            List<FunctionalCaseDemand> pageResults = listPagerCycle.getList();
            if (CollectionUtils.isEmpty(pageResults)) {
                break; // 如果列表为空，退出循环
            }
            Map<String, List<FunctionalCaseDemand>> demandsCycleMap = pageResults.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
            Set<String> demandCycleIds = demandsCycleMap.keySet();
            buildUpdateMap(projectId, demandCycleIds, platform, demandsCycleMap, platformId, deleteIds);
        }
    }

    private void batchUpdateDemands(List<FunctionalCaseDemand>updateList) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
            updateList.forEach(functionalCaseDemandMapper::updateByPrimaryKeySelective);
            sqlSession.flushStatements();
        } catch (Exception e) {
            LogUtils.info("Synchronizing demands error:" + e.getMessage());
        }
    }

    private void buildUpdateMap(String projectId, Set<String> demandIds, Platform platform, Map<String, List<FunctionalCaseDemand>> demandMap, String platformId, List<String> deleteIds) {
        DemandRelateQueryRequest demandRelateQueryRequest = new DemandRelateQueryRequest();
        demandRelateQueryRequest.setProjectConfig(projectApplicationService.getProjectDemandThirdPartConfig(projectId));
        demandRelateQueryRequest.setRelateDemandIds(new ArrayList<>(demandIds));
        PlatformDemandDTO demands = platform.getDemands(demandRelateQueryRequest);
        if (demands == null) {
            deleteIds.addAll(demandIds);
            return;
        }
        List<PlatformDemandDTO.Demand> demandList = demands.getList();
        if (CollectionUtils.isEmpty(demandList)) {
            deleteIds.addAll(demandIds);
            return;
        }
        List<String> platformIds = demandList.stream().map(PlatformDemandDTO.Demand::getDemandId).toList();
        if (demandIds.size() > platformIds.size()) {
            platformIds.forEach(demandIds::remove);
            deleteIds.addAll(demandIds);
        }
        List<FunctionalCaseDemand> updateList = new ArrayList<>();
        for (PlatformDemandDTO.Demand demand : demandList) {
            List<FunctionalCaseDemand> functionalCaseDemands = demandMap.get(demand.getDemandId());
            if (CollectionUtils.isNotEmpty(functionalCaseDemands)) {
                for (FunctionalCaseDemand functionalCaseDemand : functionalCaseDemands) {
                     buildFunctionalCaseDemand(functionalCaseDemand.getId(), platformId, demand, updateList);
                }
            }
        }
        batchUpdateDemands(updateList);
        demandMap.clear();
    }

    private void buildFunctionalCaseDemand(String id, String demandPlatform, PlatformDemandDTO.Demand demand, List<FunctionalCaseDemand> updateList) {
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setId(id);
        functionalCaseDemand.setDemandPlatform(demandPlatform);
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(demand.getParent())) {
            functionalCaseDemand.setParent("NONE");
        } else {
            functionalCaseDemand.setParent(demand.getParent());
        }
        if (StringUtils.isNotBlank(demand.getDemandName())) {
            if (demand.getDemandName().length() > 255) {
                demand.setDemandName(demand.getDemandName().substring(0, 255));
            } else {
                demand.setDemandName(demand.getDemandName());
            }
        }
        functionalCaseDemand.setDemandName(demand.getDemandName());
        functionalCaseDemand.setDemandUrl(demand.getDemandUrl());
        updateList.add(functionalCaseDemand);
    }

}
