package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.exception.MSException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author song.tianyang
 *  2021/5/13 6:24 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiEnvironmentRunningParamService {
    @Resource
    ApiTestEnvironmentService apiTestEnvironmentService;

    public synchronized void addParam(String enviromentId, String key, String value) {
        if(StringUtils.isEmpty(key)){
            return;
        }
        ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs = apiTestEnvironmentService.get(enviromentId);
        if(apiTestEnvironmentWithBLOBs == null ){
            return;
        }

        JSONObject configObj = JSONObject.parseObject(apiTestEnvironmentWithBLOBs.getConfig());
        if(configObj.containsKey("commonConfig")){
            JSONObject commonConfig = configObj.getJSONObject("commonConfig");
            if(commonConfig.containsKey("variables")){
                JSONArray variables = commonConfig.getJSONArray("variables");
                boolean contains = false;
                for(int i = 0;i<variables.size();i++){
                    JSONObject jsonObj = variables.getJSONObject(i);
                    if(jsonObj.containsKey("name")&&StringUtils.equals(jsonObj.getString("name"), key)){
                        contains = true;
                        jsonObj.put("value",value);
                    }
                }
                if(!contains){
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("name",key);
                    itemObj.put("value",value);
                    itemObj.put("enable",true);

                    if(variables.size() == 0){
                        variables.add(itemObj);
                    }else {
                        variables.add(variables.size()-1,itemObj);
                    }
                    commonConfig.put("variables",variables);
                }
            }else {
                JSONArray variables = new JSONArray();
                JSONObject itemObj = new JSONObject();
                itemObj.put("name",key);
                itemObj.put("value",value);
                itemObj.put("enable",true);

                JSONObject emptyObj = new JSONObject();
                emptyObj.put("enable",true);

                variables.add(itemObj);
                variables.add(emptyObj);
                commonConfig.put("variables",variables);
            }
        }else {
            JSONObject commonConfig = new JSONObject();
            JSONArray variables = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("name",key);
            itemObj.put("value",value);
            itemObj.put("enable",true);

            JSONObject emptyObj = new JSONObject();
            emptyObj.put("enable",true);

            variables.add(itemObj);
            variables.add(emptyObj);
            commonConfig.put("variables",variables);
            configObj.put("commonConfig",commonConfig);
        }
        apiTestEnvironmentWithBLOBs.setConfig(configObj.toJSONString());
        apiTestEnvironmentService.update(apiTestEnvironmentWithBLOBs);
    }

    public void parseEvn(String envStr) {
        String [] envStringArr = envStr.split("\n");
        for (String env :envStringArr) {
            if(StringUtils.contains(env,"=")){
                String [] envItem = env.split("=");
                if(envItem.length > 1){
                    String jmeterVarKey = envItem[0];
                    if(this.checkValidity(jmeterVarKey,"MS.ENV.")){
                        String [] envAndKeyArr = jmeterVarKey.substring("MS.ENV.".length()).split("\\.");
                        String envId = envAndKeyArr[0];
                        String [] keyArr = ArrayUtils.remove(envAndKeyArr,0);
                        String key = StringUtils.join(keyArr,".");
//                        String [] valueArr = ArrayUtils.remove(envItem,0);
//                        String value = StringUtils.join(valueArr,"=");
                        String value = StringUtils.substring(env,jmeterVarKey.length()+1);
                        if(StringUtils.isNoneEmpty(envId,key,value)){
                            this.addParam(envId,key,value);
                        }
                    }
                }
            }
        }
    }
    public boolean checkValidity (String str, String regex) {
        if(str == null){
            return false;
        }
        if(regex == null){
            return  true;
        }

        if (str.startsWith(regex)) {
            return true;
        } else {
            return false;
        }
    }
}
