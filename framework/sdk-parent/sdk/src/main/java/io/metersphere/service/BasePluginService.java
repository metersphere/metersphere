package io.metersphere.service;

import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BasePluginService {
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    FileManagerService fileManagerService;
    public static final String DIR_PATH = "system/plugin";

    public List<PluginWithBLOBs> getPlugins(String scenario) {
        PluginExample example = new PluginExample();
        example.createCriteria().andScenarioEqualTo(scenario);
        return pluginMapper.selectByExampleWithBLOBs(example);
    }

    public PluginWithBLOBs get(String pluginId) {
        return pluginMapper.selectByPrimaryKey(pluginId);
    }

    public PluginWithBLOBs getByScripId(String scripId) {
        PluginExample example = new PluginExample();
        example.createCriteria().andScriptIdEqualTo(scripId);
        List<PluginWithBLOBs> plugins = pluginMapper.selectByExampleWithBLOBs(example);
        return CollectionUtils.isEmpty(plugins) ? null : plugins.get(0);
    }

    public InputStream getPluginResource(String pluginId, String resourceName) {
        FileRequest request = new FileRequest();
        request.setProjectId(DIR_PATH + "/" + pluginId);
        request.setFileName(resourceName);
        request.setStorage(StorageConstants.MINIO.name());
        return fileManagerService.downloadFileAsStream(request);
    }

    public InputStream getPluginJar(String pluginId) {
        PluginWithBLOBs plugin = pluginMapper.selectByPrimaryKey(pluginId);
        if (plugin == null) {
            return null;
        }
        return getPluginResource(pluginId, plugin.getSourceName());
    }
}
