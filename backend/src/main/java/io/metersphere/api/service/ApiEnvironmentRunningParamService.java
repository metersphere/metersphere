package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.base.domain.ApiEnvironmentRunningParam;
import io.metersphere.base.domain.ApiEnvironmentRunningParamExample;
import io.metersphere.base.mapper.ApiEnvironmentRunningParamMapper;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author song.tianyang
 *  2021/5/13 6:24 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiEnvironmentRunningParamService {
    @Resource
    ApiEnvironmentRunningParamMapper apiEnvironmentRunningParamMapper;

    public synchronized void addParam(String enviromentId, String key, String value) {
        if(StringUtils.isEmpty(key)){
            return;
        }
        ApiEnvironmentRunningParamExample example = new ApiEnvironmentRunningParamExample();
        example.createCriteria().andApiEnviromentIdEqualTo(enviromentId).andKeyEqualTo(key);
        List<ApiEnvironmentRunningParam> list = apiEnvironmentRunningParamMapper.selectByExampleWithBLOBs(example);
        long timeStamp = System.currentTimeMillis();
        String userId = SessionUtils.getUserId();
        if(list.isEmpty()){
            ApiEnvironmentRunningParam model = new ApiEnvironmentRunningParam();
            model.setApiEnviromentId(enviromentId);
            model.setKey(key);
            model.setValue(value);
            model.setCreateUserId(userId);
            model.setId(UUID.randomUUID().toString());
            model.setCreateTime(timeStamp);
            model.setUpdateTime(timeStamp);
            model.setUpdateUserId(userId);
            apiEnvironmentRunningParamMapper.insert(model);
        }else {
            ApiEnvironmentRunningParam model = list.get(0);
            model.setValue(value);
            model.setUpdateTime(timeStamp);
            model.setUpdateUserId(userId);
            apiEnvironmentRunningParamMapper.updateByPrimaryKeySelective(model);
        }
    }

    public void deleteParam(String enviromentId, String key) {
        ApiEnvironmentRunningParamExample example = new ApiEnvironmentRunningParamExample();
        example.createCriteria().andApiEnviromentIdEqualTo(enviromentId).andKeyEqualTo(key);
        apiEnvironmentRunningParamMapper.deleteByExample(example);
    }

    public String getParam(String enviromentId, String key) {
        ApiEnvironmentRunningParamExample example = new ApiEnvironmentRunningParamExample();
        example.createCriteria().andApiEnviromentIdEqualTo(enviromentId).andKeyEqualTo(key);
        List<ApiEnvironmentRunningParam> list = apiEnvironmentRunningParamMapper.selectByExampleWithBLOBs(example);
        if(list.isEmpty()){
            return  "";
        }else {
            return list.get(0).getValue();
        }
    }

    public String showParams(String enviromentId) {
        Map<String,String> paramMap = new LinkedHashMap<>();

        ApiEnvironmentRunningParamExample example = new ApiEnvironmentRunningParamExample();
        example.createCriteria().andApiEnviromentIdEqualTo(enviromentId);
        List<ApiEnvironmentRunningParam> list = apiEnvironmentRunningParamMapper.selectByExampleWithBLOBs(example);

        for (ApiEnvironmentRunningParam model:
             list) {
            paramMap.put(model.getKey(),model.getValue());
        }

        return JSONArray.toJSONString(paramMap);
    }
}
