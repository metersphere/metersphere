package io.metersphere.track.service;

import io.metersphere.base.domain.TestPlanReportResource;
import io.metersphere.base.domain.TestPlanReportResourceExample;
import io.metersphere.base.mapper.TestPlanReportResourceMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanReportResourceMapper;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/2 1:57 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportResourceService {
    Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");
    @Resource
    private TestPlanReportResourceMapper testPlanReportResourceMapper;
    @Resource
    private ExtTestPlanReportResourceMapper extTestPlanReportResourceMapper;

    public int updateExecuteResultByReportIdAndResourceIds(String executeResult, String planReportId, List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return 0;
        }else {
            TestPlanReportResourceExample example = new TestPlanReportResourceExample();
            example.createCriteria().andTestPlanReportIdEqualTo(planReportId).andResourceIdIn(ids);

            TestPlanReportResource updateModel = new TestPlanReportResource();
            updateModel.setExecuteResult(executeResult);
            return testPlanReportResourceMapper.updateByExampleSelective(updateModel,example);
        }
    }

    public long countByReportIdAndResourceTypeAndExecuteResultNotEquals(String planReportId, String resourceType, String executeResult) {
        TestPlanReportResourceExample example = new TestPlanReportResourceExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId).andResourceTypeEqualTo(resourceType).andExecuteResultNotEqualTo(executeResult);
        return this.testPlanReportResourceMapper.countByExample(example);
    }

    public long countByReportIdAndResourceTypeAndExecuteResultEquals(String planReportId, String resourceType, String executeResult) {
        TestPlanReportResourceExample example = new TestPlanReportResourceExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId).andResourceTypeEqualTo(resourceType).andExecuteResultEqualTo(executeResult);
        return this.testPlanReportResourceMapper.countByExample(example);
    }

    public Map<String, Map<String, String>> selectExecuteResultByTestPlanReportId(String testPlanReportId) {
        Map<String,Map<String,String>> resourceTypeMap = new HashMap<>();
        List<TestPlanReportResource> testPlanReportResourceList = extTestPlanReportResourceMapper.selectResourceIdAndResourceTypeAndExecuteResultByTestPlanReportId(testPlanReportId);
        for (TestPlanReportResource model : testPlanReportResourceList) {
            String resourceType = model.getResourceType();
            String resourceId = model.getResourceId();
            String executeResult = model.getExecuteResult();

            if(resourceTypeMap.containsKey(resourceType)){
                resourceTypeMap.get(resourceType).put(resourceId,executeResult);
            }else {
                Map<String ,String> map = new HashMap<>();
                map.put(resourceId,executeResult);
                resourceTypeMap.put(resourceType,map);
            }
        }
        return  resourceTypeMap;
    }

    public void deleteByExample(TestPlanReportResourceExample resourceExample) {
        testPlanReportResourceMapper.deleteByExample(resourceExample);
    }

    public void clearExecuteStatus(String planReportId) {
        TestPlanReportResourceExample example = new TestPlanReportResourceExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReportId).andExecuteResultEqualTo("RUNNING");
        long dataCount = testPlanReportResourceMapper.countByExample(example);
        if(dataCount > 0){
            testPlanLog.info("TestPlanReport Execute Check error: ["+planReportId+"],error data count:"+dataCount);
            TestPlanReportResource updateModel = new TestPlanReportResource();
            updateModel.setExecuteResult(TestPlanApiExecuteStatus.FAILD.name());
            testPlanReportResourceMapper.updateByExampleSelective(updateModel,example);
        }
    }
}
