package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseDemandMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.request.FunctionalCaseDemandBatchRequest;
import io.metersphere.functional.request.FunctionalCaseDemandRequest;
import io.metersphere.functional.request.FunctionalThirdDemandPageRequest;
import io.metersphere.functional.request.QueryDemandListRequest;
import io.metersphere.plugin.platform.dto.reponse.PlatformDemandDTO;
import io.metersphere.plugin.platform.dto.request.DemandPageRequest;
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

import java.util.*;
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

    /**
     * 获取需求列表
     *
     * @param request QueryDemandListRequest
     * @return List<FunctionalCaseDemand>
     */
    public List<FunctionalDemandDTO> listFunctionalCaseDemands(QueryDemandListRequest request) {
        List<FunctionalDemandDTO> parentDemands = extFunctionalCaseDemandMapper.selectParentDemandByKeyword(request.getKeyword(), request.getCaseId());
        Map<String, List<FunctionalDemandDTO>> functionalCaseDemandMap = parentDemands.stream().filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        List<String> ids = parentDemands.stream().map(FunctionalCaseDemand::getId).toList();
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        if (CollectionUtils.isNotEmpty(ids)) {
            functionalCaseDemandExample.createCriteria().andIdNotIn(ids).andCaseIdEqualTo(request.getCaseId());
        } else {
            functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(request.getCaseId());
        }
        List<FunctionalCaseDemand> functionalCaseDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        int lastSize = 0;
        while (CollectionUtils.isNotEmpty(functionalCaseDemands) && functionalCaseDemands.size() != lastSize) {
            lastSize = functionalCaseDemands.size();
            List<FunctionalCaseDemand> notMatchedList = new ArrayList<>();
            for (FunctionalCaseDemand demand : functionalCaseDemands) {
                if (functionalCaseDemandMap.containsKey(demand.getParent())) {
                    FunctionalDemandDTO functionalDemandDTO = new FunctionalDemandDTO();
                    BeanUtils.copyBean(functionalDemandDTO, demand);
                    functionalCaseDemandMap.get(demand.getParent()).stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandPlatform(), demand.getDemandPlatform())).toList().get(0).addChild(functionalDemandDTO);
                    List<FunctionalDemandDTO> functionalDemandDTOS = functionalCaseDemandMap.get(demand.getDemandId());
                    if (CollectionUtils.isEmpty(functionalDemandDTOS)) {
                        functionalDemandDTOS = new ArrayList<>();
                    }
                    if (!functionalDemandDTOS.contains(functionalDemandDTO)) {
                        functionalDemandDTOS.add(functionalDemandDTO);
                        functionalCaseDemandMap.put(demand.getDemandId(), functionalDemandDTOS);
                    }
                } else {
                    notMatchedList.add(demand);
                }
            }
            functionalCaseDemands = notMatchedList;
        }
        //处理剩下的没有跟节点的数据
        dealNotMatchedList(functionalCaseDemands, parentDemands);
        return parentDemands;
    }

    private static void dealNotMatchedList(List<FunctionalCaseDemand> functionalCaseDemands, List<FunctionalDemandDTO> parentDemands) {
        if (CollectionUtils.isEmpty(functionalCaseDemands)) {
            return;
        }
        List<FunctionalDemandDTO> functionalDemandDTOS = new ArrayList<>();
        List<FunctionalDemandDTO> copyFunctionalDemandDTOS = new ArrayList<>();
        for (FunctionalCaseDemand functionalCaseDemand : functionalCaseDemands) {
            FunctionalDemandDTO functionalDemandDTO = new FunctionalDemandDTO();
            BeanUtils.copyBean(functionalDemandDTO, functionalCaseDemand);
            functionalDemandDTOS.add(functionalDemandDTO);
            copyFunctionalDemandDTOS.add(functionalDemandDTO);
        }
        Map<String, List<FunctionalDemandDTO>> parentNotMatchedMap = functionalDemandDTOS.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getParent));

        int lastSize = 0;
        while (CollectionUtils.isNotEmpty(functionalDemandDTOS) && functionalDemandDTOS.size() != lastSize) {
            lastSize = functionalDemandDTOS.size();
            for (FunctionalDemandDTO functionalDemandDTO : functionalDemandDTOS) {
                String demandId = functionalDemandDTO.getDemandId();
                //当前数据例是否有父亲的这个数据，有，就把当前这个map数据放入父亲下(相同平台的)
                if (parentNotMatchedMap.get(demandId)!=null) {
                    List<FunctionalDemandDTO> list = parentNotMatchedMap.get(demandId).stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandPlatform(), functionalDemandDTO.getDemandPlatform())).toList();
                    if(CollectionUtils.isNotEmpty(list)) {
                        delChild(functionalDemandDTO, list, parentNotMatchedMap, copyFunctionalDemandDTOS);
                        functionalDemandDTO.setChildren(list);
                        List<FunctionalDemandDTO> otherPlatForm = parentNotMatchedMap.get(demandId).stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getDemandPlatform(), functionalDemandDTO.getDemandPlatform())).toList();
                        if (CollectionUtils.isNotEmpty(otherPlatForm)) {
                            parentNotMatchedMap.put(demandId,otherPlatForm);
                        } else {
                            parentNotMatchedMap.remove(demandId);
                        }
                    }
                    break;
                }
            }
            functionalDemandDTOS = new ArrayList<>(copyFunctionalDemandDTOS);
        }

        parentDemands.addAll(functionalDemandDTOS);
    }

    private static void delChild(FunctionalDemandDTO functionalDemandDTO, List<FunctionalDemandDTO> list, Map<String, List<FunctionalDemandDTO>> parentNotMatchedMap, List<FunctionalDemandDTO> copyFunctionalDemandDTOS) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (FunctionalDemandDTO demandDTO : list) {
            String demandId1 = demandDTO.getDemandId();
            if (parentNotMatchedMap.get(demandId1)!=null){
                List<FunctionalDemandDTO> list1 = parentNotMatchedMap.get(demandId1).stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandPlatform(), functionalDemandDTO.getDemandPlatform())).toList();
                demandDTO.setChildren(list1);
                delChild(demandDTO,list1, parentNotMatchedMap, copyFunctionalDemandDTOS);
            }
            copyFunctionalDemandDTOS.remove(demandDTO);
        }
    }

    /**
     * 新增本地需求
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void addDemand(FunctionalCaseDemandRequest request, String userId) {
        if (checkDemandList(request.getDemandList())) return;
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdEqualTo(request.getCaseId()).andDemandPlatformEqualTo(request.getDemandPlatform());
        List<FunctionalCaseDemand> existDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
        for (DemandDTO demandDTO : request.getDemandList()) {
            List<FunctionalCaseDemand> list = existDemands.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandId(), demandDTO.getDemandId()) && StringUtils.equalsIgnoreCase(t.getParent(), demandDTO.getParent())
                    && StringUtils.equalsIgnoreCase(t.getDemandName(), demandDTO.getDemandName()) && StringUtils.equalsIgnoreCase(t.getDemandUrl(), demandDTO.getDemandUrl())).toList();
            if (CollectionUtils.isNotEmpty(list)) {
                return;
            }
            FunctionalCaseDemand functionalCaseDemand = buildFunctionalCaseDemand(request.getCaseId(), request.getDemandPlatform(), userId, demandDTO);
            functionalCaseDemandMapper.insert(functionalCaseDemand);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
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
                functionalCaseDemand.setParent(demandDTO.getParent());
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
        dealWithDemand(request.getDemandList().get(0), functionalCaseDemand);
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
        functionalCaseDemandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量关联第三方需求 需要带有所属平台
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void batchRelevance(FunctionalCaseDemandBatchRequest request, String userId) {
        if (checkDemandList(request.getDemandList())) return;
        List<String> caseIds = doSelectIds(request, request.getProjectId());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
        List<String> demandIds = request.getDemandList().stream().map(DemandDTO::getDemandId).toList();
        String demandPlatform = request.getDemandPlatform();
        //查询当前平台所有已关联的需求
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andDemandPlatformEqualTo(demandPlatform);
        List<FunctionalCaseDemand> existPlatformDemands = functionalCaseDemandMapper.selectByExample(functionalCaseDemandExample);
        Map<String, List<FunctionalCaseDemand>> caseDemandMap = existPlatformDemands.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getCaseId));
        Map<String, DemandDTO> demandDTOMap = request.getDemandList().stream().collect(Collectors.toMap(DemandDTO::getDemandId, t -> t));
        caseIds.forEach(t -> {
            List<String> existDemandIds = new ArrayList<>();
            List<FunctionalCaseDemand> functionalCaseDemands = caseDemandMap.get(t);
            if (CollectionUtils.isNotEmpty(functionalCaseDemands)) {
                existDemandIds = functionalCaseDemands.stream().map(FunctionalCaseDemand::getDemandId).toList();
            }
            //过滤已存在的
            List<String> finalExistDemandIds = existDemandIds;
            List<String> notRepeatDemandIds = demandIds.stream().filter(demand -> !finalExistDemandIds.contains(demand)).toList();
            for (String notRepeatDemandId : notRepeatDemandIds) {
                DemandDTO demandDTO = demandDTOMap.get(notRepeatDemandId);
                FunctionalCaseDemand functionalCaseDemand = buildFunctionalCaseDemand(t, demandPlatform, userId, demandDTO);
                functionalCaseDemandMapper.insert(functionalCaseDemand);
            }
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
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
        DemandPageRequest demandPageRequest = new DemandPageRequest();
        demandPageRequest.setQuery(request.getKeyword());
        demandPageRequest.setFilter(request.getFilter());
        demandPageRequest.setStartPage(request.getCurrent());
        demandPageRequest.setPageSize(request.getPageSize());
        demandPageRequest.setProjectConfig(projectApplicationService.getProjectDemandThirdPartConfig(request.getProjectId()));
        Platform platform = projectApplicationService.getPlatform(request.getProjectId(), false);
        return platform.pageDemand(demandPageRequest);
    }
}
