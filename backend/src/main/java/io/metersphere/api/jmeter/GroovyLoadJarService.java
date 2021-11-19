package io.metersphere.api.jmeter;

import groovy.lang.GroovyClassLoader;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.JarConfigService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class GroovyLoadJarService {
    /**
     * groovy 使用的是自己的类加载器，
     * 这里再执行脚本前，使用 groovy的加载器加载jar包，
     * 解决groovy脚本无法使用jar包的问题
     */
    public void loadGroovyJar(GroovyClassLoader classLoader) {
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        List<JarConfig> jars = jarConfigService.list();
        jars.forEach(jarConfig -> {
            try {
                String path = jarConfig.getPath();
                File file = new File(path);
                if (file.isDirectory() && !path.endsWith("/")) {
                    file = new File(path + "/");
                }
                classLoader.addURL(file.toURI().toURL());
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }
}
