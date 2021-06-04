package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.api.dto.automation.SaveApiScenarioRequest;
import io.metersphere.api.dto.automation.parse.EsbDataParser;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.EsbApiParamsExample;
import io.metersphere.base.domain.EsbApiParamsWithBLOBs;
import io.metersphere.base.mapper.EsbApiParamsMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author song.tianyang
 * @Date 2021/3/16 4:57 下午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EsbApiParamService {
    @Resource
    private EsbApiParamsMapper esbApiParamsMapper;

    public EsbApiParamsWithBLOBs createEsbApiParam(String resourceId, String esbDataStruct, String backedEsbDataStrcut, String backedScript) {
        EsbApiParamsWithBLOBs model = null;
        EsbApiParamsExample example = new EsbApiParamsExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);
        List<EsbApiParamsWithBLOBs> list = esbApiParamsMapper.selectByExampleWithBLOBs(example);
        if (list.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            model = new EsbApiParamsWithBLOBs();
            model.setId(uuid);
            model.setResourceId(resourceId);
            model.setDataStruct(esbDataStruct);
            model.setResponseDataStruct(backedEsbDataStrcut);
            model.setBackedScript(backedScript);
            esbApiParamsMapper.insert(model);
        } else {
            model = list.get(0);
            model.setDataStruct(esbDataStruct);
            model.setResponseDataStruct(backedEsbDataStrcut);
            model.setBackedScript(backedScript);
            esbApiParamsMapper.updateByPrimaryKeyWithBLOBs(model);
        }
        return model;
    }

    public List<KeyValue> genKeyValueByEsbDataStruct(List<EsbDataStruct> dataStructRequestList, String prefix) {
        List<KeyValue> returnList = new ArrayList<>();
        for (EsbDataStruct request : dataStructRequestList) {
            String itemName = request.getName();
            if (StringUtils.isNotEmpty(prefix)) {
                itemName = prefix + "." + itemName;
            }
            KeyValue kv = new KeyValue();
            kv.setName(itemName);
            kv.setValue(request.getValue());
            kv.setType(request.getType());
            kv.setDescription(request.getDescription());
            kv.setContentType(request.getContentType());
            kv.setRequired(request.isRequired());
            returnList.add(kv);
            if (request.getChildren() != null) {
                List<KeyValue> childValueList = this.genKeyValueByEsbDataStruct(request.getChildren(), itemName);
                if (!childValueList.isEmpty()) {
                    returnList.addAll(childValueList);
                }
            }
        }
        return returnList;
    }

    public EsbApiParamsWithBLOBs getEsbParamBLOBsByResourceID(String resourceId) {
        EsbApiParamsExample example = new EsbApiParamsExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);

        String returnStr = null;
        List<EsbApiParamsWithBLOBs> list = esbApiParamsMapper.selectByExampleWithBLOBs(example);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public void deleteByResourceId(String resourceId) {
        EsbApiParamsExample example = new EsbApiParamsExample();
        example.createCriteria().andResourceIdEqualTo(resourceId);
        esbApiParamsMapper.deleteByExample(example);
    }

    public void handleApiEsbParams(ApiDefinitionResult res) {
        if(res == null){
            return;
        }
        EsbApiParamsWithBLOBs esbParamBlobs = this.getEsbParamBLOBsByResourceID(res.getId());
        if (esbParamBlobs == null) {
            return;
        }
        if (StringUtils.isNotEmpty(res.getRequest())) {
            JSONObject jsonObj = this.addEsbInfoToJsonString(esbParamBlobs, res.getRequest());
            if (jsonObj != null) {
                res.setRequest(jsonObj.toJSONString());
            }

        }

//        try {
//            if (StringUtils.isNotEmpty(res.getRequest())) {
//                JSONObject jsonObj = JSONObject.parseObject(res.getRequest());
//
//                JSONArray esbDataArray = JSONArray.parseArray(esbParamBlobs.getDataStruct());
//                jsonObj.put("esbDataStruct", esbDataArray);
//
//                JSONArray responseDataArray = JSONArray.parseArray(esbParamBlobs.getResponseDataStruct());
//                jsonObj.put("backEsbDataStruct", responseDataArray);
//
//                JSONObject backedScriptObj = JSONObject.parseObject(esbParamBlobs.getBackedScript());
//                jsonObj.put("backScript", backedScriptObj);
//
//                jsonObj.put("esbFrontedScript", esbParamBlobs.getFrontedScript());
//
//                res.setRequest(jsonObj.toJSONString());
//            }
//        } catch (Exception e) {
//        }
    }

    public void handleApiEsbParams(ApiTestCaseWithBLOBs res) {
        if(res==null){
            return;
        }
        EsbApiParamsWithBLOBs esbParamBlobs = this.getEsbParamBLOBsByResourceID(res.getId());
        if (esbParamBlobs == null) {
            return;
        }
        if (StringUtils.isNotEmpty(res.getRequest())) {
            JSONObject jsonObj = this.addEsbInfoToJsonString(esbParamBlobs, res.getRequest());
            if (jsonObj != null) {
                res.setRequest(jsonObj.toJSONString());
            }

        }
    }

    private JSONObject addEsbInfoToJsonString(EsbApiParamsWithBLOBs esbParamBlobs, String requestString) {
        JSONObject returnObj = null;
        try {
            returnObj = JSONObject.parseObject(requestString);

            JSONArray esbDataArray = JSONArray.parseArray(esbParamBlobs.getDataStruct());
            if (esbDataArray == null) {
                returnObj.put("esbDataStruct", "");
            } else {
                returnObj.put("esbDataStruct", esbDataArray);
            }

            JSONArray responseDataArray = JSONArray.parseArray(esbParamBlobs.getResponseDataStruct());
            if (responseDataArray == null) {
                returnObj.put("backEsbDataStruct", "");
            } else {
                returnObj.put("backEsbDataStruct", responseDataArray);
            }

            returnObj.put("esbFrontedScript", esbParamBlobs.getFrontedScript());

            JSONObject backedScriptObj = JSONObject.parseObject(esbParamBlobs.getBackedScript());
            returnObj.put("backScript", backedScriptObj);
        } catch (Exception e) {

        }
        return returnObj;
    }

    public void handleApiEsbParams(ApiTestCaseResult res) {
        if(res == null){
            return;
        }
        EsbApiParamsWithBLOBs esbParamBlobs = this.getEsbParamBLOBsByResourceID(res.getId());
        if (esbParamBlobs == null) {
            return;
        }
        if (StringUtils.isNotEmpty(res.getRequest())) {
            JSONObject jsonObj = this.addEsbInfoToJsonString(esbParamBlobs, res.getRequest());
            if (jsonObj != null) {
                res.setRequest(jsonObj.toJSONString());
            }

        }
    }

    public SaveApiDefinitionRequest updateEsbRequest(SaveApiDefinitionRequest request) {
        try {
            //修改reqeust.parameters
            //用户交互感受：ESB的发送数据以报文模板为主框架，同时前端不再有key-value的表格数据填充。
            //业务逻辑：   发送ESB接口数据时，使用报文模板中的数据，同时报文模板中的${取值}目的是为了拼接数据结构(比如xml的子节点)
            //代码实现:    此处打算解析前端传来的EsbDataStruct数据结构，将数据结构按照报文模板中的${取值}为最高优先级组装keyValue对象。这样Jmeter会自动拼装为合适的xml
            if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
                MsTCPSampler tcpSampler = (MsTCPSampler) request.getRequest();
                List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, request.getEsbDataStruct());
                tcpSampler.setParameters(keyValueList);
                request.setRequest(tcpSampler);
            }
            //更新EsbApiParams类
//            EsbApiParamsWithBLOBs esbApiParams = this.createEsbApiParam(request.getId(), request.getEsbDataStruct(), request.getBackEsbDataStruct(), request.getBackScript());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }
    public SaveApiDefinitionRequest handleEsbRequest(SaveApiDefinitionRequest request) {
        try {
            //修改reqeust.parameters
            //用户交互感受：ESB的发送数据以报文模板为主框架，同时前端不再有key-value的表格数据填充。
            //业务逻辑：   发送ESB接口数据时，使用报文模板中的数据，同时报文模板中的${取值}目的是为了拼接数据结构(比如xml的子节点)
            //代码实现:    此处打算解析前端传来的EsbDataStruct数据结构，将数据结构按照报文模板中的${取值}为最高优先级组装keyValue对象。这样Jmeter会自动拼装为合适的xml
            if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
                MsTCPSampler tcpSampler = (MsTCPSampler) request.getRequest();
                tcpSampler.setProtocol("ESB");
                List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, request.getEsbDataStruct());
                tcpSampler.setParameters(keyValueList);
            }
            //更新EsbApiParams类
            EsbApiParamsWithBLOBs esbApiParams = this.createEsbApiParam(request.getId(), request.getEsbDataStruct(), request.getBackEsbDataStruct(), request.getBackScript());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

//    public RunDefinitionRequest handleEsbRequest(RunDefinitionRequest request) {
//        try {
//            //修改reqeust.parameters
//            //用户交互感受：ESB的发送数据以报文模板为主框架，同时前端不再有key-value的表格数据填充。
//            //业务逻辑：   发送ESB接口数据时，使用报文模板中的数据，同时报文模板中的${取值}目的是为了拼接数据结构(比如xml的子节点)
//            //代码实现:    此处打算解析前端传来的EsbDataStruct数据结构，将数据结构按照报文模板中的${取值}为最高优先级组装keyValue对象。这样Jmeter会自动拼装为合适的xml
//            if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
//                if(request.getTestElement() instanceof MsTestPlan){
//                    MsTestPlan testPlan = (MsTestPlan)request.getTestElement();
//                    for (MsTestElement testElement: testPlan.getHashTree()) {
//                        if(testElement instanceof MsThreadGroup){
//                            MsThreadGroup group = (MsThreadGroup)testElement;
//                            for (MsTestElement groupElement: testPlan.getHashTree()) {
//                                if(groupElement instanceof MsScenario){
//                                    MsScenario scenario = (MsScenario)groupElement;
//                                    for (MsTestElement scenarioElement: scenario.getHashTree()) {
//                                        if(scenarioElement instanceof MsTCPSampler){
//                                            MsTCPSampler tcpSampler = (MsTCPSampler) scenarioElement;
//                                            List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, request.getEsbDataStruct());
//                                            tcpSampler.setParameters(keyValueList);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            //更新EsbApiParams类
//            EsbApiParamsWithBLOBs esbApiParams = this.createEsbApiParam(request.getId(), request.getEsbDataStruct(), request.getBackEsbDataStruct(), request.getBackScript());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return request;
//    }

    //通过esb数据结构生成keyValue集合，以及发送参数
    public List<KeyValue> genKeyValueListByDataStruct(MsTCPSampler tcpSampler, String esbDataStruct) {
        List<KeyValue> keyValueList = new ArrayList<>();
        String sendRequest = tcpSampler.getRequest();
        String paramRegexStr = "\\$\\{([^}]*)\\}";
        try {
            if (StringUtils.isNotEmpty(sendRequest)) {
                List<String> paramList = new ArrayList<>();
                List<EsbDataStruct> dataStructRequestList = JSONArray.parseArray(esbDataStruct, EsbDataStruct.class);
                Pattern regex = Pattern.compile(paramRegexStr);
                Matcher matcher = regex.matcher(sendRequest);
                while (matcher.find()) {
                    paramList.add(matcher.group(1));
                }
                for (String param : paramList) {
                    String value = this.genValueFromEsbDataStructByParam(dataStructRequestList, param);
                    if (StringUtils.isNotEmpty(value)) {
                        KeyValue kv = new KeyValue();
                        kv.setName(param);
                        kv.setValue(value);
                        kv.setRequired(true);
                        keyValueList.add(kv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyValueList;
    }

    private List<KeyValue> genKeyValueListByDataStruct(MsTCPSampler tcpSampler, List<EsbDataStruct> dataStructRequestList) {
        List<KeyValue> keyValueList = new ArrayList<>();
        String sendRequest = tcpSampler.getRequest();
          String paramRegexStr = "\\$\\{([^}]*)\\}";
        try {
            if (StringUtils.isNotEmpty(sendRequest)) {
                List<String> paramList = new ArrayList<>();
                Pattern regex = Pattern.compile(paramRegexStr);
                Matcher matcher = regex.matcher(sendRequest);
                while (matcher.find()) {
                    paramList.add(matcher.group(1));
                }
                for (String param : paramList) {
                    String value = this.genValueFromEsbDataStructByParam(dataStructRequestList, param);
                    if (StringUtils.isNotEmpty(value)) {
                        KeyValue kv = new KeyValue();
                        kv.setName(param);
                        kv.setValue(value);
                        kv.setRequired(true);
                        keyValueList.add(kv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyValueList;
    }

    //通过报文模版中的变量参数，解析报文数据结构，生成对应的xml数据
    private String genValueFromEsbDataStructByParam(List<EsbDataStruct> dataStructRequestList, String param) {
        String returnValue = "";
        if (StringUtils.isNotEmpty(param)) {
            //多层结构使用"."来表示。aaa.bb.cc 代表的是dataStructRequestList中，aaa节点下，bb节点下的cc节点数据
            String[] paramArr = param.split("\\.");
            returnValue = EsbDataParser.esbData2XmlByParamStruct(dataStructRequestList, paramArr);
        }

        return returnValue;
    }

    public SaveApiTestCaseRequest handleEsbRequest(SaveApiTestCaseRequest request) {
        try {
            //修改reqeust.parameters, 将树结构类型数据转化为表格类型数据，供执行时参数的提取
            if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
                MsTCPSampler tcpSampler = (MsTCPSampler) request.getRequest();
                List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, request.getEsbDataStruct());
                tcpSampler.setParameters(keyValueList);
            }
            //更新EsbApiParams类
            EsbApiParamsWithBLOBs esbApiParams = this.createEsbApiParam(request.getId(), request.getEsbDataStruct(), request.getBackEsbDataStruct(), request.getBackEsbDataStruct());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    public void handleEsbRequest(MsTCPSampler tcpSampler) {
        try {
            //修改reqeust.parameters, 将树结构类型数据转化为表格类型数据，供执行时参数的提取
            if (tcpSampler.getEsbDataStruct() != null ) {
                List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, tcpSampler.getEsbDataStruct());
                tcpSampler.setParameters(keyValueList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteByResourceIdIn(List<String> apiIds) {
        EsbApiParamsExample example = new EsbApiParamsExample();
        example.createCriteria().andResourceIdIn(apiIds);
        esbApiParamsMapper.deleteByExample(example);
    }

    public void checkScenarioRequests(SaveApiScenarioRequest request) {
        if(request.getScenarioDefinition() != null ){
            List<MsTestElement> hashTreeList = request.getScenarioDefinition().getHashTree();
            for (MsTestElement testElement :hashTreeList) {
                if(testElement instanceof MsTCPSampler){
                    this.handleEsbRequest((MsTCPSampler)testElement);
                }
            }
        }
    }

    public RunDefinitionRequest checkIsEsbRequest(RunDefinitionRequest request) {
        try {
            //修改reqeust.parameters
            //用户交互感受：ESB的发送数据以报文模板为主框架，同时前端不再有key-value的表格数据填充。
            //业务逻辑：   发送ESB接口数据时，使用报文模板中的数据，同时报文模板中的${取值}目的是为了拼接数据结构(比如xml的子节点)
            //代码实现:    此处打算解析前端传来的EsbDataStruct数据结构，将数据结构按照报文模板中的${取值}为最高优先级组装keyValue对象。这样Jmeter会自动拼装为合适的xml
            if(request.getTestElement() != null ){
                MsTestPlan testPlan = (MsTestPlan) request.getTestElement();
                if(CollectionUtils.isNotEmpty(testPlan.getHashTree())){
                    for (MsTestElement testElement: testPlan.getHashTree()) {
                        if(CollectionUtils.isNotEmpty(testElement.getHashTree())){
                            for (MsTestElement apiElement:testElement.getHashTree()) {
                                if(apiElement instanceof  MsTCPSampler){
                                    MsTCPSampler tcpSampler = (MsTCPSampler) apiElement;
                                    tcpSampler.setProtocol("ESB");
                                    List<KeyValue> keyValueList = this.genKeyValueListByDataStruct(tcpSampler, tcpSampler.getEsbDataStruct());
                                    tcpSampler.setParameters(keyValueList);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }
}
