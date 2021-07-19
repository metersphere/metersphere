package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ApiScenarioReferenceId;
import io.metersphere.base.domain.ApiScenarioReferenceIdExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioReferenceIdMapper;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author song.tianyang
 * @Date 2021/7/19 11:33 上午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReferenceIdService {
    @Resource
    private ApiScenarioReferenceIdMapper apiScenarioReferenceIdMapper;

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

    public void saveByApiScenario(ApiScenarioWithBLOBs scenario) {
        this.deleteByScenarioId(scenario.getId());

        long createTime = System.currentTimeMillis();
        String createUser = SessionUtils.getUserId();

        Map<String, ApiScenarioReferenceId> refreceIdDic = new HashMap<>();
        try {
            if (scenario.getScenarioDefinition() != null) {
                JSONObject jsonObject = JSONObject.parseObject(scenario.getScenarioDefinition());
                if (jsonObject.containsKey("hashTree")) {
                    JSONArray testElementList = jsonObject.getJSONArray("hashTree");
                    for (int index = 0; index < testElementList.size(); index++) {
                        JSONObject item = testElementList.getJSONObject(index);
                        String refId = "";
                        String refrenced = "";
                        String dataType = "";
                        if(item.containsKey("id")){
                            refId = item.getString("id");
                        }
                        if(item.containsKey("referenced")){
                            refrenced = item.getString("referenced");
                        }
                        if(item.containsKey("refType")){
                            dataType = item.getString("refType");
                        }

                        if (StringUtils.isNotEmpty(refId) && StringUtils.isNotEmpty(refrenced)) {
                            ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
                            saveItem.setId(UUID.randomUUID().toString());
                            saveItem.setApiScenarioId(scenario.getId());
                            saveItem.setCreateTime(createTime);
                            saveItem.setCreateUserId(createUser);
                            saveItem.setReferenceId(refId);
                            saveItem.setReferenceType(refrenced);
                            saveItem.setDataType(dataType);
                            refreceIdDic.put(refId,saveItem);
                        }

                        if(item.containsKey("hashTree")){
                            refreceIdDic.putAll(this.deepParseTestElement(createTime,createUser,scenario.getId(),item.getJSONArray("hashTree")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(MapUtils.isNotEmpty(refreceIdDic)){
            for (ApiScenarioReferenceId model:refreceIdDic.values()) {
                apiScenarioReferenceIdMapper.insert(model);
            }
        }else {
            ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
            saveItem.setId(UUID.randomUUID().toString());
            saveItem.setApiScenarioId(scenario.getId());
            saveItem.setCreateTime(createTime);
            saveItem.setCreateUserId(createUser);
            try{
                apiScenarioReferenceIdMapper.insert(saveItem);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public Map<String,ApiScenarioReferenceId> deepParseTestElement(long createTime,String createUser,String scenarioId,JSONArray testElementList){
        Map<String,ApiScenarioReferenceId> returnMap = new HashMap<>();

        if(CollectionUtils.isNotEmpty(testElementList)){
            for (int index = 0; index < testElementList.size(); index++) {
                JSONObject item = testElementList.getJSONObject(index);
                String refId = "";
                String refrenced = "";
                String dataType = "";
                if(item.containsKey("id")){
                    refId = item.getString("id");
                }
                if(item.containsKey("referenced")){
                    refrenced = item.getString("referenced");
                }
                if(item.containsKey("refType")){
                    dataType = item.getString("refType");
                }

                if (StringUtils.isNotEmpty(refId) && StringUtils.isNotEmpty(refrenced)) {
                    ApiScenarioReferenceId saveItem = new ApiScenarioReferenceId();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setApiScenarioId(scenarioId);
                    saveItem.setCreateTime(createTime);
                    saveItem.setCreateUserId(createUser);
                    saveItem.setReferenceId(refId);
                    saveItem.setReferenceType(refrenced);
                    saveItem.setDataType(dataType);
                    returnMap.put(refId,saveItem);
                }
                if(item.containsKey("hashTree")){
                    returnMap.putAll(this.deepParseTestElement(createTime,createUser,scenarioId,item.getJSONArray("hashTree")));
                }
            }
        }

        return returnMap;
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
}
