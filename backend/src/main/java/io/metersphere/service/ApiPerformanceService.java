package io.metersphere.service;

import io.metersphere.base.domain.ApiLoadTest;
import io.metersphere.base.domain.ApiLoadTestExample;
import io.metersphere.base.mapper.ApiLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtApiLoadTestMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiPerformanceService {

    @Resource
    private ApiLoadTestMapper apiLoadTestMapper;
    @Resource
    private ExtApiLoadTestMapper extApiLoadTestMapper;

    public boolean isNeedUpdate(String loadTestId) {
        int count = extApiLoadTestMapper.countNeedUpdateApiCase(loadTestId);
        if (count > 0) {
            return true;
        }
        count = extApiLoadTestMapper.countNeedUpdateApiScenario(loadTestId);
        return count > 0 ? true : false;
    }

    public  List<ApiLoadTest> getByLoadTestId(String loadTestId) {
        ApiLoadTestExample example = new ApiLoadTestExample();
        example.createCriteria().andLoadTestIdEqualTo(loadTestId);
        return apiLoadTestMapper.selectByExample(example);
    }

    public int UpdateVersion(String loadTestId, String apiId, int version) {
        ApiLoadTest apiLoadTest = new ApiLoadTest();
        apiLoadTest.setApiVersion(version);
        ApiLoadTestExample example = new ApiLoadTestExample();
        example.createCriteria()
                .andLoadTestIdEqualTo(loadTestId)
                .andApiIdEqualTo(apiId);
        return apiLoadTestMapper.updateByExampleSelective(apiLoadTest, example);
    }

    public void add(List<ApiLoadTest> apiList, String loadTestId) {
        if (CollectionUtils.isNotEmpty(apiList)) {
            apiList.forEach(item -> {
                item.setId(UUID.randomUUID().toString());
                item.setLoadTestId(loadTestId);
                item.setApiVersion(0);
                apiLoadTestMapper.insert(item);
            });
        }
    }
}
