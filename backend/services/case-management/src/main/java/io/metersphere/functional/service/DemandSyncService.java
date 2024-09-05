package io.metersphere.functional.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.domain.FunctionalCaseDemand;
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
import org.mybatis.spring.SqlSessionUtils;
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
    private ProjectApplicationService projectApplicationService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public static int DEFAULT_BATCH_SIZE = 50;

    /**
     * 定时任务同步缺陷(存量-默认中文环境通知)
     * @param projectId 项目ID
     * @param scheduleUser 任务触发用户
     */
    public void syncPlatformDemandBySchedule(String projectId, String scheduleUser) {
        String platformId = projectApplicationService.getDemandPlatformId(projectId);
        // 创建一个 List 来保存合并后的结果
        Platform platform = projectApplicationService.getPlatform(projectId, false);
        Map<String, List<FunctionalCaseDemand>> updateMap = new HashMap<>();
        int pageNumber = 1;
        boolean count = true;
        Page<Object> page = PageHelper.startPage(pageNumber, DEFAULT_BATCH_SIZE, count);
        Pager<List<FunctionalCaseDemand>> listPager = PageUtils.setPageInfo(page,  extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId, platformId));
        long total = listPager.getTotal();
        List<FunctionalCaseDemand> list = listPager.getList();
        Map<String, List<FunctionalCaseDemand>> demandMap = list.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        Set<String> demandIds = demandMap.keySet();
        buildUpdateMap(projectId, demandIds, platform, demandMap, platformId, updateMap);
        count = false;
        for (int i = 1; i < ((int)Math.ceil((double) total/DEFAULT_BATCH_SIZE)); i ++) {
            Page<Object> pageCycle = PageHelper.startPage(i+1, DEFAULT_BATCH_SIZE, count);
            Pager<List<FunctionalCaseDemand>> listPagerCycle = PageUtils.setPageInfo(pageCycle, extFunctionalCaseDemandMapper.selectDemandByProjectId(projectId,platformId));
            List<FunctionalCaseDemand> pageResults = listPagerCycle.getList();
            Map<String, List<FunctionalCaseDemand>> demandsMap = pageResults.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
            Set<String> demandIdSet = demandsMap.keySet();
            buildUpdateMap(projectId, demandIdSet, platform, demandsMap, platformId, updateMap);
        }
        updateMap.forEach((k,v)->{
            for (FunctionalCaseDemand functionalCaseDemand : v) {
                try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                    FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
                    functionalCaseDemandMapper.updateByPrimaryKeySelective(functionalCaseDemand);
                    sqlSession.flushStatements();
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        });

        LogUtils.info("End synchronizing demands");

    }

    private void buildUpdateMap(String projectId, Set<String> demandIds, Platform platform, Map<String, List<FunctionalCaseDemand>> demandMap, String platformId, Map<String, List<FunctionalCaseDemand>> updateMap) {
        DemandRelateQueryRequest demandRelateQueryRequest = new DemandRelateQueryRequest();
        demandRelateQueryRequest.setProjectConfig(projectApplicationService.getProjectDemandThirdPartConfig(projectId));
        demandRelateQueryRequest.setRelateDemandIds(new ArrayList<>(demandIds));
        PlatformDemandDTO demands = platform.getDemands(demandRelateQueryRequest);

        List<PlatformDemandDTO.Demand> demandList = demands.getList();
        for (PlatformDemandDTO.Demand demand : demandList) {
            List<FunctionalCaseDemand> functionalCaseDemands = demandMap.get(demand.getDemandId());
            List<FunctionalCaseDemand>updateList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(functionalCaseDemands)) {
                for (FunctionalCaseDemand functionalCaseDemand : functionalCaseDemands) {
                    FunctionalCaseDemand update = buildFunctionalCaseDemand(functionalCaseDemand.getId(), platformId, demand);
                    updateList.add(update);
                }
            }
            updateMap.put(demand.getDemandId(), updateList);
        }
    }

    private FunctionalCaseDemand buildFunctionalCaseDemand(String id, String demandPlatform, PlatformDemandDTO.Demand demand) {
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setId(id);
        functionalCaseDemand.setDemandPlatform(demandPlatform);
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        dealWithDemand(demand, functionalCaseDemand, demandPlatform);
        return functionalCaseDemand;
    }

    private void dealWithDemand(PlatformDemandDTO.Demand demand, FunctionalCaseDemand functionalCaseDemand, String demandPlatform) {
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
    }


}
