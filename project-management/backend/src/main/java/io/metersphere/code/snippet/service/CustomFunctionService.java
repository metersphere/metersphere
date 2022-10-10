package io.metersphere.code.snippet.service;


import io.metersphere.base.domain.CustomFunction;
import io.metersphere.base.domain.CustomFunctionExample;
import io.metersphere.base.domain.CustomFunctionWithBLOBs;
import io.metersphere.base.mapper.CustomFunctionMapper;
import io.metersphere.base.mapper.ext.ExtCustomFunctionMapper;
import io.metersphere.code.snippet.listener.MsDebugListener;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.jmeter.LocalRunner;
import io.metersphere.request.CustomFunctionRequest;
import io.metersphere.service.MicroService;
import io.metersphere.code.snippet.util.FixedCapacityUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionService {


    @Resource
    private CustomFunctionMapper customFunctionMapper;
    @Resource
    private ExtCustomFunctionMapper extCustomFunctionMapper;
    @Resource
    private MicroService microService;

    public CustomFunctionWithBLOBs save(CustomFunctionRequest request) {
        request.setId(UUID.randomUUID().toString());
        request.setCreateUser(SessionUtils.getUserId());
        request.setProjectId(SessionUtils.getCurrentProjectId());

        checkFuncExist(request);

        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(request);
        return request;
    }

    private void checkFuncExist(CustomFunctionRequest request) {
        String id = request.getId();
        String name = request.getName();
        CustomFunctionExample example = new CustomFunctionExample();
        CustomFunctionExample.Criteria criteria = example
                .createCriteria()
                .andProjectIdEqualTo(request.getProjectId())
                .andNameEqualTo(name);
        if (StringUtils.isNotBlank(id)) {
            criteria.andIdNotEqualTo(id);
        }
        if (customFunctionMapper.countByExample(example) > 0) {
            MSException.throwException("自定义函数名称已存在！");
        }
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        customFunctionMapper.deleteByPrimaryKey(id);
    }

    public List<CustomFunction> query(CustomFunctionRequest request) {
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            projectId = SessionUtils.getCurrentProjectId();
            request.setProjectId(projectId);
        }
        return extCustomFunctionMapper.queryAll(request);
    }

    public void update(CustomFunctionRequest request) {
        checkFuncExist(request);
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.updateByPrimaryKeySelective(request);
    }

    public CustomFunctionWithBLOBs copy(String id) {
        CustomFunctionWithBLOBs blob = customFunctionMapper.selectByPrimaryKey(id);
        if (blob == null) {
            MSException.throwException("copy fail, source obj is null.");
        }
        CustomFunctionWithBLOBs copyBlob = new CustomFunctionWithBLOBs();
        BeanUtils.copyBean(copyBlob, blob);
        String copyId = UUID.randomUUID().toString();
        String copyNameId = copyId.substring(0, 3);
        String copyName = blob.getName() + "_" + copyNameId + "_COPY";
        copyBlob.setId(copyId);
        copyBlob.setName(copyName);
        copyBlob.setCreateUser(SessionUtils.getUserId());
        copyBlob.setCreateTime(System.currentTimeMillis());
        copyBlob.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(copyBlob);
        return copyBlob;
    }

    public CustomFunctionWithBLOBs get(String id) {
        if (StringUtils.isBlank(id)) {
            return new CustomFunctionWithBLOBs();
        }
        return customFunctionMapper.selectByPrimaryKey(id);
    }

    public MsExecResponseDTO run(String reportId, Object request) {
        HashTree hashTree = null;
        try {
            String jmx = microService.postForData(MicroServiceName.API_TEST, "/api/definition/get-hash-tree", request, String.class);
            Object scriptWrapper = SaveService.loadElement(new ByteArrayInputStream(jmx.getBytes(StandardCharsets.UTF_8)));
            hashTree = this.getHashTree(scriptWrapper);
        } catch (Exception e) {
            LogUtil.error("执行代码片段时获取API模块返回的hashTree失败");
            MSException.throwException("调用接口请求API模块获取 hash tree 失败!");
        }
        if (!FixedCapacityUtils.jmeterLogTask.containsKey(reportId)) {
            FixedCapacityUtils.jmeterLogTask.put(reportId, System.currentTimeMillis());
        }
        addDebugListener(reportId, hashTree);
        LocalRunner runner = new LocalRunner(hashTree);
        runner.run(reportId);
        return null;
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void addDebugListener(String testId, HashTree testPlan) {
        MsDebugListener resultCollector = new MsDebugListener();
        resultCollector.setName(testId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsDebugListener.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);

        // 添加DEBUG标示
        HashTree test = ArrayUtils.isNotEmpty(testPlan.getArray()) ? testPlan.getTree(testPlan.getArray()[0]) : null;
        if (test != null && ArrayUtils.isNotEmpty(test.getArray()) && test.getArray()[0] instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) test.getArray()[0];
            group.setProperty(BackendListenerConstants.MS_DEBUG.name(), true);
        }
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }
}
