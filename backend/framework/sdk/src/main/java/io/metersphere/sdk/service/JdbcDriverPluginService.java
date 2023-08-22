package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.mapper.PluginMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Driver;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class JdbcDriverPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PluginMapper pluginMapper;

    public boolean isJdbcDriver(String pluginId) {
        return pluginLoadService.getImplClass(pluginId, Driver.class) != null;
    }

    /**
     * 获取所有的 JDBC 驱动的实现类
     * @return
     */
    public List<String> getJdbcDriverClass() {
        PluginExample example = new PluginExample();
        example.createCriteria().andScenarioEqualTo(PluginScenarioType.JDBC_DRIVER.name());
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        List<String> pluginDriverClassNames = plugins.stream().map(plugin ->
                pluginLoadService.getImplClass(plugin.getId(), Driver.class)
        ).map(Class::getName).collect(Collectors.toList());
        // 已经内置了 mysql 依赖
        pluginDriverClassNames.add("com.mysql.jdbc.Driver");
        return pluginDriverClassNames;
    }
}
