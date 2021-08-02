package io.metersphere.track.service;

import io.metersphere.base.domain.TestPlanReportResource;
import io.metersphere.base.domain.TestPlanReportResourceExample;
import io.metersphere.base.mapper.TestPlanReportResourceMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanReportResourceMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/2 1:57 下午
 */
@Service
public class TestPlanReportResourceService {
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
}
