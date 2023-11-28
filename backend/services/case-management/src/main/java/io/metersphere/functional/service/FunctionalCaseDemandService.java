package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.dto.DemandDTO;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseDemandMapper;
import io.metersphere.functional.mapper.FunctionalCaseDemandMapper;
import io.metersphere.functional.request.FunctionalCaseDemandRequest;
import io.metersphere.functional.request.QueryDemandListRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
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

    /**
     * 获取需求列表
     * @param request QueryDemandListRequest
     * @return List<FunctionalCaseDemand>
     */
    public List<FunctionalDemandDTO> listFunctionalCaseDemands(QueryDemandListRequest request) {
        List<FunctionalCaseDemand> functionalCaseDemands = extFunctionalCaseDemandMapper.selectGroupByKeyword(request.getKeyword(), request.getCaseId());
        List<String> platforms = functionalCaseDemands.stream().map(FunctionalCaseDemand::getDemandPlatform).distinct().toList();
        List<String> ids = functionalCaseDemands.stream().map(FunctionalCaseDemand::getId).distinct().toList();
        List<FunctionalCaseDemand> functionalCaseDemandChildList = extFunctionalCaseDemandMapper.selectByKeyword(request.getKeyword(), request.getCaseId(), platforms, ids);
        Map<String, List<FunctionalCaseDemand>> platformDemandMap = functionalCaseDemandChildList.stream().collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandPlatform));
        List<FunctionalDemandDTO> list = new ArrayList<>();
        for (FunctionalCaseDemand functionalCaseDemand : functionalCaseDemands) {
            FunctionalDemandDTO functionalDemandDTO = new FunctionalDemandDTO();
            BeanUtils.copyBean(functionalDemandDTO,functionalCaseDemand);
            List<FunctionalCaseDemand> childrenDemands= platformDemandMap.get(functionalCaseDemand.getDemandPlatform());
            if (CollectionUtils.isNotEmpty(childrenDemands)) {
                functionalDemandDTO.setChildren(childrenDemands);
            } else {
                functionalDemandDTO.setChildren(new ArrayList<>());
            }
            list.add(functionalDemandDTO);
        }
        return list;
    }

    /**
     * 新增本地需求
     * @param request 页面参数
     * @param userId 当前操作人
     */
    public void addDemand(FunctionalCaseDemandRequest request, String userId) {
        if (checkDemandList(request)) return;
        FunctionalCaseDemand functionalCaseDemand = buildFunctionalCaseDemand(request, userId, request.getDemandList().get(0));
        functionalCaseDemandMapper.insert(functionalCaseDemand);
    }

    private static FunctionalCaseDemand buildFunctionalCaseDemand(FunctionalCaseDemandRequest request, String userId, DemandDTO demandDTO) {
        FunctionalCaseDemand functionalCaseDemand = new FunctionalCaseDemand();
        functionalCaseDemand.setId(IDGenerator.nextStr());
        functionalCaseDemand.setCaseId(request.getCaseId());
        functionalCaseDemand.setDemandPlatform(request.getDemandPlatform());
        functionalCaseDemand.setCreateTime(System.currentTimeMillis());
        functionalCaseDemand.setCreateUser(userId);
        functionalCaseDemand.setUpdateTime(System.currentTimeMillis());
        functionalCaseDemand.setUpdateUser(userId);
        dealWithDemand(demandDTO, functionalCaseDemand);
        return functionalCaseDemand;
    }

    private static boolean checkDemandList(FunctionalCaseDemandRequest request) {
        return CollectionUtils.isEmpty(request.getDemandList());
    }

    /**
     * 处理单个需求
     * @param demandDTO 需求参数
     * @param functionalCaseDemand functionalCaseDemand
     */
    private static void dealWithDemand(DemandDTO demandDTO, FunctionalCaseDemand functionalCaseDemand) {
        if (StringUtils.isNotBlank(demandDTO.getDemandId())) {
            functionalCaseDemand.setDemandId(demandDTO.getDemandId());
        }
        if (StringUtils.isBlank(demandDTO.getDemandName())) {
            throw new MSException(Translator.get("case.demand.name.not.exist"));
        }
        functionalCaseDemand.setDemandName(demandDTO.getDemandName());
        if (StringUtils.isNotBlank(demandDTO.getDemandUrl())) {
            functionalCaseDemand.setDemandUrl(demandDTO.getDemandUrl());
        }
    }

    /**
     * 更新本地需求
     * @param request 页面参数
     * @param userId 当前操作人
     */
    public void updateDemand(FunctionalCaseDemandRequest request, String userId) {
        if (checkDemandList(request)) return;
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
     * @param id 需求关系ID
     */
    public void deleteDemand(String id) {
        functionalCaseDemandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量关联第三方需求 需要带有所属平台
     * @param request 页面参数
     * @param userId 当前操作人
     */
    public void batchRelevance(FunctionalCaseDemandRequest request, String userId) {
        if (checkDemandList(request)) return;
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseDemandMapper functionalCaseDemandMapper = sqlSession.getMapper(FunctionalCaseDemandMapper.class);
        for (DemandDTO demandDTO : request.getDemandList()) {
            FunctionalCaseDemand functionalCaseDemand = buildFunctionalCaseDemand(request, userId, demandDTO);
            functionalCaseDemandMapper.insert(functionalCaseDemand);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }
}
