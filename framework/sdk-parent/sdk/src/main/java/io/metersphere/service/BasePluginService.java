package io.metersphere.service;

import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BasePluginService {
    @Resource
    private PluginMapper pluginMapper;

    public List<PluginWithBLOBs> getPlugins(String scenario) {
        PluginExample example = new PluginExample();
        example.createCriteria().andScenarioEqualTo(scenario);
        return pluginMapper.selectByExampleWithBLOBs(example);
    }
}
