package io.metersphere.system.base;

import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.service.PluginService;
import jakarta.annotation.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-20  11:32
 */
@Service
public class BaseApiPluginTestService {

    @Resource
    private PluginService pluginService;
    @Resource
    private PluginMapper pluginMapper;
    private static Plugin jdbcPlugin;


    /**
     * 添加插件，供测试使用
     *
     * @return
     * @throws Exception
     */
    public Plugin addJdbcPlugin() throws Exception {
        if (hasJdbcPlugin()) {
            return jdbcPlugin;
        }
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/jdbc-sampler-v3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试jdbc插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        jdbcPlugin = pluginService.add(request, mockMultipartFile);
        return jdbcPlugin;
    }
    

    public Plugin getJdbcPlugin() throws Exception {
        if (!hasJdbcPlugin()) {
            return this.addJdbcPlugin();
        }
        return jdbcPlugin;
    }

    public boolean hasJdbcPlugin() {
        if (jdbcPlugin != null) {
            return true;
        }
        jdbcPlugin = pluginMapper.selectByPrimaryKey("jdbc");
        return jdbcPlugin != null;
    }

    public void deleteJdbcPlugin() {
        if (jdbcPlugin != null) {
            pluginService.delete(jdbcPlugin.getId());
            jdbcPlugin = null;
        }
    }
}
