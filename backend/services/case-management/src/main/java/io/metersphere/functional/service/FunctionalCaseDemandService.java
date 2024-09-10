package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseDemandMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.*;
import io.metersphere.plugin.platform.dto.request.DemandPageRequest;
import io.metersphere.plugin.platform.dto.response.PlatformDemandDTO;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.platform.utils.PluginPager;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.mapper.SystemParameterMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseDemandService {

    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private ExtFunctionalCaseDemandMapper extFunctionalCaseDemandMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    /**
     * 获取需求列表
     *
     * @param request QueryDemandListRequest
     * @return List<FunctionalCaseDemand>
     */
    public List<FunctionalDemandDTO> listFunctionalCaseDemands(QueryDemandListRequest request) {
        List<FunctionalDemandDTO> parentDemands = extFunctionalCaseDemandMapper.selectParentDemandByKeyword(request.getKeyword(), request.getCaseId());
        if (CollectionUtils.isEmpty(parentDemands)) {
            return new ArrayList<>();
        }
        Map<String, List<FunctionalDemandDTO>> functionalCaseDemandMap = parentDemands.stream().filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        List<String> ids = parentDemands.stream().map(FunctionalCaseDemand::getId).toList();
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andIdNotIn(ids).andCaseIdEqualTo(request.getCaseId());
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        int lastSize = 0;
        while (CollectionUtils.isNotEmpty(functionalCaseDemands) && functionalCaseDemands.size() != lastSize) {
            lastSize = functionalCaseDemands.size();
            List<FunctionalCaseDemand> notMatchedList = new ArrayList<>();
            for (FunctionalCaseDemand demand : functionalCaseDemands) {
                if (functionalCaseDemandMap.containsKey(demand.getParent())) {
                    FunctionalDemandDTO functionalDemandDTO = new FunctionalDemandDTO();
                    BeanUtils.copyBean(functionalDemandDTO, demand);
                    functionalCaseDemandMap.get(demand.getParent()).stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandPlatform(), demand.getDemandPlatform())).toList().getFirst().addChild(functionalDemandDTO);
                    resetMap(demand, functionalCaseDemandMap, functionalDemandDTO);
                } else {
                    notMatchedList.add(demand);
                }
            }
            functionalCaseDemands = notMatchedList;
        }
        return parentDemands;
    }

    private static void resetMap(FunctionalCaseDemand demand, Map<String, List<FunctionalDemandDTO>> functionalCaseDemandMap, FunctionalDemandDTO functionalDemandDTO) {
        List<FunctionalDemandDTO> functionalDemandDTOS = functionalCaseDemandMap.get(demand.getDemandId());
        if (CollectionUtils.isEmpty(functionalDemandDTOS)) {
            functionalDemandDTOS = new ArrayList<>();
        }
        if (!functionalDemandDTOS.contains(functionalDemandDTO)) {
            functionalDemandDTOS.add(functionalDemandDTO);
            functionalCaseDemandMap.put(demand.getDemandId(), functionalDemandDTOS);
        }
    }

    /**
     * 新增本地需求
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void addDemand(FunctionalCaseDemandRequest request, String userId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getCaseId());
        List<DemandDTO> demandDTOList = doSelectDemandList(request.getFunctionalDemandBatchRequest(), request.getDemandList(), functionalCase.getProjectId());
        if (checkDemandList(demandDTOList)) return;
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(request.getCaseId()).andDemandPlatformEqualTo(request.getDemandPlatform());
        List<FunctionalCaseDemand> existDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
            insertDemand(demandDTOList, request, userId, existDemands, functionalCaseDemandMapper);
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private FunctionalCaseDemand buildFunctionalCaseDemand(String caseId, String demandPlatform, String userId, DemandDTO demandDTO) {
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setId(IDGenerator.nextStr());
        functionalCaseDemand.setCaseId(caseId);
        functionalCaseDemand.setDemandPlatform(demandPlatform);
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setCreateUser(userId);
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        functionalCaseDemand.setUpdateUser(userId);
        dealWithDemand(demandDTO, functionalCaseDemand);
        return functionalCaseDemand;
    }

    private static boolean checkDemandList(List<DemandDTO> demandList) {
        return CollectionUtils.isEmpty(demandList);
    }

    /**
     * 处理单个需求
     *
     * @param demandDTO            需求参数
     * @param functionalCaseDemand functionalCaseDemand
     */
    private void dealWithDemand(DemandDTO demandDTO, FunctionalCaseDemand functionalCaseDemand) {
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey("ui.platformName");
        String paramValue;
        if (systemParameter == null || StringUtils.isBlank(systemParameter.getParamValue())) {
            paramValue = "MeterSphere";
        } else {
            paramValue = systemParameter.getParamValue();
        }
        if (StringUtils.equalsIgnoreCase(functionalCaseDemand.getDemandPlatform(), paramValue)) {
            functionalCaseDemand.setDemandId(demandDTO.getDemandId());
            functionalCaseDemand.setParent("NONE");
        } else {
            if (StringUtils.isBlank(demandDTO.getDemandId())) {
                throw new MSException(Translator.get("case.demand.id.not.exist"));
            }
            functionalCaseDemand.setDemandId(demandDTO.getDemandId());
            if (StringUtils.isBlank(demandDTO.getParent())) {
                functionalCaseDemand.setParent("NONE");
            } else {
                if (StringUtils.isBlank(demandDTO.getParent())) {
                    functionalCaseDemand.setParent("NONE");
                } else {
                    functionalCaseDemand.setParent(demandDTO.getParent());
                }
            }
        }
        if (StringUtils.isBlank(demandDTO.getDemandName())) {
            throw new MSException(Translator.get("case.demand.name.not.exist"));
        }
        if (demandDTO.getDemandName().length() > 255) {
            demandDTO.setDemandName(demandDTO.getDemandName().substring(0, 255));
        }
        functionalCaseDemand.setDemandName(demandDTO.getDemandName());
        functionalCaseDemand.setDemandUrl(demandDTO.getDemandUrl());
    }

    /**
     * 更新本地需求
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void updateDemand(FunctionalCaseDemandRequest request, String userId) {
        if (checkDemandList(request.getDemandList())) return;
        FunctionalCaseDemand functionalCaseDemand = functionalCaseDemandMapper.selectByPrimaryKey(request.getId());
        if (functionalCaseDemand == null) {
            throw new MSException(Translator.get("case.demand.not.exist"));
        }
        dealWithDemand(request.getDemandList().getFirst(), functionalCaseDemand);
        functionalCaseDemand.setCreateTime(null);
        functionalCaseDemand.setCreateUser(null);
        functionalCaseDemand.setUpdateUser(userId);
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        functionalCaseDemandMapper.updateByPrimaryKeySelective(functionalCaseDemand);
    }

    /**
     * 取消关联需求 就是将该需求关系删除
     *
     * @param id 需求关系ID
     */
    public void deleteDemand(String id) {
        FunctionalCaseDemand functionalCaseDemandInDb = functionalCaseDemandMapper.selectByPrimaryKey(id);
        if (functionalCaseDemandInDb == null) {
            return;
        }
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andParentEqualTo(functionalCaseDemandInDb.getDemandId());
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        if (CollectionUtils.isNotEmpty(functionalCaseDemands)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FunctionalCaseDemandMapper updateMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
            for (FunctionalCaseDemand functionalCaseDemand : functionalCaseDemands) {
                functionalCaseDemand.setWithParent(false);
                updateMapper.updateByPrimaryKey(functionalCaseDemand);
            }
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        functionalCaseDemandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量关联第三方需求 需要带有所属平台
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void batchRelevance(FunctionalCaseDemandBatchRequest request, String userId) {
        List<DemandDTO> demandDTOList = doSelectDemandList(request.getFunctionalDemandBatchRequest(), request.getDemandList(), request.getProjectId());
        if (checkDemandList(demandDTOList)) return;
        List<String> caseIds = doSelectIds(request, request.getProjectId());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
        String demandPlatform = request.getDemandPlatform();
        //查询当前平台所有已关联的需求
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andDemandPlatformEqualTo(demandPlatform);
        List<FunctionalCaseDemand> existPlatformDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Map<String, List<FunctionalCaseDemand>> caseDemandMap = existPlatformDemands.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getCaseId));
        caseIds.forEach(t -> {
            List<FunctionalCaseDemand> existDemands = caseDemandMap.get(t);
            if (CollectionUtils.isEmpty(existDemands)) {
                existDemands = new ArrayList<>();
            }
            FunctionalCaseDemandRequest functionalCaseDemandRequest = new FunctionalCaseDemandRequest();
            functionalCaseDemandRequest.setCaseId(t);
            functionalCaseDemandRequest.setDemandPlatform(request.getDemandPlatform());
            //过滤已存在的
            insertDemand(demandDTOList, functionalCaseDemandRequest, userId, existDemands, functionalCaseDemandMapper);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private void insertDemand(List<DemandDTO> demandDTOList, FunctionalCaseDemandRequest request, String userId, List<FunctionalCaseDemand> existDemands, FunctionalCaseDemandMapper functionalCaseDemandMapper) {
        Map<String, List<FunctionalCaseDemand>> existMap = existDemands.stream().filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        Map<String, List<FunctionalCaseDemand>> existParentMap = existDemands.stream().filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(FunctionalCaseDemand::getParent));
        Map<String, List<DemandDTO>> insertMap = demandDTOList.stream().filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(DemandDTO::getDemandId));

        for (DemandDTO demandDTO : demandDTOList) {
            FunctionalCaseDemand functionalCaseDemand = buildFunctionalCaseDemand(request.getCaseId(), request.getDemandPlatform(), userId, demandDTO);
            //校验重复
            List<FunctionalCaseDemand> functionalCaseDemands = existMap.get(demandDTO.getDemandId());
            if (CollectionUtils.isNotEmpty(functionalCaseDemands)) {
                continue;
            }
            //校验当前关联的需求在系统或传入的数据结构中是否带有父节点，如果有置为true,用来做查询的root节点
            List<FunctionalCaseDemand> functionalCaseDemandParents = existMap.get(demandDTO.getParent());
            List<DemandDTO> demandDTOList1 = insertMap.get(demandDTO.getParent());
            if (CollectionUtils.isNotEmpty(functionalCaseDemandParents) || CollectionUtils.isNotEmpty(demandDTOList1)) {
                functionalCaseDemand.setWithParent(Boolean.TRUE);
            } else {
                functionalCaseDemand.setWithParent(Boolean.FALSE);
                //校验系统中有没有当前数据的子节点，如果有置为false
                List<FunctionalCaseDemand> existChildren = existParentMap.get(functionalCaseDemand.getDemandId());
                if (CollectionUtils.isNotEmpty(existChildren)) {
                    for (FunctionalCaseDemand existChild : existChildren) {
                        existChild.setWithParent(Boolean.TRUE);
                        functionalCaseDemandMapper.updateByPrimaryKey(existChild);
                    }
                }
            }
            functionalCaseDemandMapper.insert(functionalCaseDemand);
        }
    }

    private List<DemandDTO> doSelectDemandList(FunctionalDemandBatchRequest functionalDemandBatchRequest, List<DemandDTO> demandList, String projectId) {
        if (functionalDemandBatchRequest != null && functionalDemandBatchRequest.isSelectAll()) {
            DemandPageRequest demandPageRequest = new DemandPageRequest();
            demandPageRequest.setQuery(functionalDemandBatchRequest.getKeyword());
            demandPageRequest.setSelectAll(functionalDemandBatchRequest.isSelectAll());
            demandPageRequest.setExcludeIds(functionalDemandBatchRequest.getExcludeIds());
            demandPageRequest.setProjectConfig(projectApplicationService.getProjectDemandThirdPartConfig(projectId));
            Platform platform = projectApplicationService.getPlatform(projectId, false);
            PluginPager<PlatformDemandDTO> platformDemandDTOPluginPager = platform.pageDemand(demandPageRequest);
            return getDemandDTOS(platformDemandDTOPluginPager);
        } else {
            return demandList;
        }
    }

    public List<DemandDTO> getDemandDTOS(PluginPager<PlatformDemandDTO> platformDemandDTOPluginPager) {
        if (platformDemandDTOPluginPager.getData() != null) {
            List<PlatformDemandDTO.Demand> list = platformDemandDTOPluginPager.getData().getList();
            List<DemandDTO> demandDTOList = new ArrayList<>();
            setDemandDTOList(list, demandDTOList);
            return demandDTOList;
        } else {
            return new ArrayList<>();
        }
    }

    public void setDemandDTOList(List<PlatformDemandDTO.Demand> list, List<DemandDTO> demandDTOList) {
        for (PlatformDemandDTO.Demand demand : list) {
            DemandDTO demandDTO = new DemandDTO();
            demandDTO.setDemandId(demand.getDemandId());
            demandDTO.setDemandUrl(demand.getDemandUrl());
            demandDTO.setDemandName(demand.getDemandName());
            demandDTO.setParent(demand.getParent());
            if (CollectionUtils.isNotEmpty(demand.getChildren())) {
                setDemandDTOList(demand.getChildren(), demandDTOList);
            }
            demandDTOList.add(demandDTO);
        }
    }

    public <T> List<String> doSelectIds(T dto, String projectId) {
        FunctionalCaseDemandBatchRequest request = (FunctionalCaseDemandBatchRequest) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseMapper.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public PluginPager<PlatformDemandDTO> pageDemand(FunctionalThirdDemandPageRequest request) {
        String platformId = projectApplicationService.getDemandPlatformId(request.getProjectId());
        List<String> demandIds = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getCaseId())) {
            demandIds = extFunctionalCaseDemandMapper.selectDemandIdsByCaseId(request.getCaseId(), platformId);
        }
        DemandPageRequest demandPageRequest = new DemandPageRequest();
        demandPageRequest.setQuery(StringUtils.replace(request.getKeyword(), "\\", ""));
        demandPageRequest.setFilter(request.getFilter());
        demandPageRequest.setStartPage(request.getCurrent());
        demandPageRequest.setPageSize(request.getPageSize());
        demandPageRequest.setProjectConfig(projectApplicationService.getProjectDemandThirdPartConfig(request.getProjectId()));
        Platform platform = projectApplicationService.getPlatform(request.getProjectId(), false);
        PluginPager<PlatformDemandDTO> platformDemandDTOPluginPager = platform.pageDemand(demandPageRequest);
        if (CollectionUtils.isNotEmpty(demandIds)) {
            PlatformDemandDTO data = platformDemandDTOPluginPager.getData();
            List<PlatformDemandDTO.Demand> list = data.getList();
            setDisabled(list, demandIds);
            data.setList(list);
            platformDemandDTOPluginPager.setData(data);
        }
        return platformDemandDTOPluginPager;
    }

    private static void setDisabled(List<PlatformDemandDTO.Demand> list, List<String> demandIds) {
        for (PlatformDemandDTO.Demand demand : list) {
            if (demandIds.contains(demand.getDemandId())) {
                demand.setDisabled(true);
            }
            if (CollectionUtils.isNotEmpty(demand.getChildren())) {
                setDisabled(demand.getChildren(), demandIds);
            }
        }
    }
}
