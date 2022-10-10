package io.metersphere.service.scenario;

import io.metersphere.service.MsHashTreeService;
import io.metersphere.base.domain.ApiScenarioReferenceId;
import io.metersphere.base.domain.ApiScenarioReferenceIdExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioReferenceIdMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReferenceIdMapper;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author song.tianyang
 * @Date 2021/7/19 11:33 上午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReferenceIdService {
    @Resource
    private ApiScenarioReferenceIdMapper apiScenarioReferenceIdMapper;
    @Resource
    private ExtApiScenarioReferenceIdMapper extApiScenarioReferenceIdMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public List<ApiScenarioReferenceId> findByReferenceIds(List<String> deleteIds) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return new ArrayList<>(0);
        } else {
            ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
            example.createCriteria().andReferenceIdIn(deleteIds);
            return apiScenarioReferenceIdMapper.selectByExample(example);
        }
    }

    public void deleteByScenarioId(String scenarioId) {
        ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
        example.createCriteria().andApiScenarioIdEqualTo(scenarioId);
        apiScenarioReferenceIdMapper.deleteByExample(example);
    }

    public void deleteByScenarioIds(List<String> scenarioIds) {
        ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
        example.createCriteria().andApiScenarioIdIn(scenarioIds);
        apiScenarioReferenceIdMapper.deleteByExample(example);
    }

    public void saveApiAndScenarioRelation(ApiScenarioWithBLOBs scenario) {
        if (scenario.getId() == null) {
            return;
        }
        this.deleteByScenarioId(scenario.getId());
        List<ApiScenarioReferenceId> savedList = this.getApiAndScenarioRelation(scenario);
        this.insertApiScenarioReferenceIds(savedList);
    }

    public void saveApiAndScenarioRelation(List<ApiScenarioWithBLOBs> scenarios) {
        if (CollectionUtils.isNotEmpty(scenarios)) {
            List<String> idList = new ArrayList<>(scenarios.size());
            LinkedList<ApiScenarioReferenceId> savedList = new LinkedList<>();
            scenarios.forEach(scenario -> {
                if (StringUtils.isNotEmpty(scenario.getId())) {
                    idList.add(scenario.getId());
                    savedList.addAll(this.getApiAndScenarioRelation(scenario));
                }
            });
            if (CollectionUtils.isNotEmpty(idList)) {
                ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
                example.createCriteria().andApiScenarioIdIn(idList);
                apiScenarioReferenceIdMapper.deleteByExample(example);
            }
            this.insertApiScenarioReferenceIds(savedList);
        }
    }

    public void insertApiScenarioReferenceIds(List<ApiScenarioReferenceId> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReferenceIdMapper referenceIdMapper = sqlSession.getMapper(ApiScenarioReferenceIdMapper.class);
            for (ApiScenarioReferenceId apiScenarioReferenceId : list) {
                referenceIdMapper.insert(apiScenarioReferenceId);
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<ApiScenarioReferenceId> getApiAndScenarioRelation(ApiScenarioWithBLOBs scenario) {
        List<ApiScenarioReferenceId> returnList = new ArrayList<>();
        if (StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
            JSONObject jsonObject = JSONUtil.parseObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeService.HASH_TREE)) {
                return returnList;
            }
            JSONArray hashTree = jsonObject.getJSONArray(MsHashTreeService.HASH_TREE);
            for (int index = 0; index < hashTree.length(); index++) {
                JSONObject item = hashTree.getJSONObject(index);
                if (item == null) {
                    continue;
                }

                if (item.has(MsHashTreeService.ID) && item.has(MsHashTreeService.REFERENCED)) {
                    String url = null;
                    String method = null;
                    if (item.has(MsHashTreeService.PATH) && StringUtils.isNotEmpty(MsHashTreeService.PATH)) {
                        url = item.optString(MsHashTreeService.PATH);
                    } else if (item.has(MsHashTreeService.URL)) {
                        url = item.optString(MsHashTreeService.URL);
                    }
                    method = this.getMethodFromSample(item);
                    ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setApiScenarioId(scenario.getId());
                    saveItem.setReferenceId(item.optString(MsHashTreeService.ID));
                    saveItem.setReferenceType(item.optString(MsHashTreeService.REFERENCED));
                    saveItem.setDataType(item.optString(MsHashTreeService.REF_TYPE));
                    saveItem.setCreateTime(System.currentTimeMillis());
                    saveItem.setCreateUserId(SessionUtils.getUserId());
                    saveItem.setUrl(url);
                    saveItem.setMethod(method);
                    returnList.add(saveItem);
                }
                if (item.has(MsHashTreeService.HASH_TREE)) {
                    returnList.addAll(this.deepElementRelation(scenario.getId(), item.getJSONArray(MsHashTreeService.HASH_TREE)));
                }
            }
        }
        if (CollectionUtils.isEmpty(returnList)) {
            ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
            saveItem.setId(UUID.randomUUID().toString());
            saveItem.setApiScenarioId(scenario.getId());
            saveItem.setCreateTime(System.currentTimeMillis());
            saveItem.setCreateUserId(SessionUtils.getUserId());
            returnList.add(saveItem);
        }
        return returnList;
    }

    private String getMethodFromSample(JSONObject item) {
        String method = null;
        if (item.has(MsHashTreeService.TYPE) && item.has(MsHashTreeService.METHOD) && StringUtils.equalsIgnoreCase(item.optString(MsHashTreeService.TYPE), ElementConstants.HTTP_SAMPLER))
            method = item.optString(MsHashTreeService.METHOD);
        return method;
    }

    public List<ApiScenarioReferenceId> deepElementRelation(String scenarioId, JSONArray hashTree) {
        List<ApiScenarioReferenceId> deepRelations = new LinkedList<>();
        if (hashTree != null) {
            for (int index = 0; index < hashTree.length(); index++) {
                JSONObject item = hashTree.getJSONObject(index);
                if (item.has(MsHashTreeService.ID) && item.has(MsHashTreeService.REFERENCED)) {
                    String method = null;
                    String url = null;
                    if (item.has(MsHashTreeService.PATH) && StringUtils.isNotEmpty(MsHashTreeService.PATH)) {
                        url = item.optString(MsHashTreeService.PATH);
                    } else if (item.has(MsHashTreeService.URL)) {
                        url = item.optString(MsHashTreeService.URL);
                    }
                    if (item.has(MsHashTreeService.METHOD)) {
                        method = item.optString(MsHashTreeService.METHOD);
                    }
                    ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setApiScenarioId(scenarioId);
                    saveItem.setReferenceId(item.optString(MsHashTreeService.ID));
                    saveItem.setReferenceType(item.optString(MsHashTreeService.REFERENCED));
                    saveItem.setDataType(item.optString(MsHashTreeService.REF_TYPE));
                    saveItem.setCreateTime(System.currentTimeMillis());
                    saveItem.setCreateUserId(SessionUtils.getUserId());
                    saveItem.setMethod(method);
                    saveItem.setUrl(url);
                    deepRelations.add(saveItem);
                }
                if (item.has(MsHashTreeService.HASH_TREE)) {
                    deepRelations.addAll(this.deepElementRelation(scenarioId, item.getJSONArray(MsHashTreeService.HASH_TREE)));
                }
            }
        }
        return deepRelations;
    }

    public List<ApiScenarioReferenceId> findByReferenceIdsAndRefType(List<String> deleteIds, String referenceType) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return new ArrayList<>(0);
        } else {
            ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
            example.createCriteria().andReferenceIdIn(deleteIds).andReferenceTypeEqualTo(referenceType);
            return apiScenarioReferenceIdMapper.selectByExample(example);
        }
    }

    public List<ApiScenarioReferenceId> selectUrlByProjectId(String projectId) {
        return extApiScenarioReferenceIdMapper.selectUrlByProjectId(projectId);
    }
}
