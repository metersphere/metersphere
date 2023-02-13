package io.metersphere.jmeter;

import groovy.lang.GroovyClassLoader;
import io.metersphere.utils.JarConfigUtils;
import io.metersphere.utils.JsonUtils;
import io.metersphere.utils.LocalPathUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClassLoader {
    // 记录以项目为单位的自定义JAR的类加载器
    private final static Map<String, GroovyClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    public static void initClassLoader(List<String> projectIds) {
        // 读取所有JAR路径
        for (String projectId : projectIds) {
            List<String> jarPaths = JarConfigUtils.walk(LocalPathUtil.JAR_PATH + File.separator + projectId);
            if (CollectionUtils.isNotEmpty(jarPaths)) {
                LoggerUtil.info("加载JAR-PATH:" + JsonUtils.toJSONString(jarPaths), projectId);
                // 初始化类加载器
                GroovyClassLoader loader = MsClassLoader.getDynamic(jarPaths);
                classLoaderMap.put(projectId, loader);
            }
        }
    }

    public static ClassLoader getClassLoader(List<String> projectIds) {
        GroovyClassLoader classLoader = new GroovyClassLoader();
        List<GroovyClassLoader> classLoaders = new ArrayList<>();
        for (String projectId : projectIds) {
            if (classLoaderMap.containsKey(projectId)) {
                classLoaders.add(classLoaderMap.get(projectId));
            }
        }
        // 单项目加载器
        if (classLoaders.size() == 1) {
            return classLoaders.get(0);
        }
        // 跨项目加载器
        return new JoinClassLoader(classLoader, classLoaders.toArray(new ClassLoader[classLoaders.size()]));
    }

    public static void initClassLoader() {
        // 读取所有JAR路径
        List<String> projectIds = JarConfigUtils.getFileNames(LocalPathUtil.JAR_PATH + File.separator);
        LoggerUtil.info("初始化所有JAR：" + JsonUtils.toJSONString(projectIds));
        if (CollectionUtils.isNotEmpty(projectIds)) {
            initClassLoader(projectIds);
        }
    }
}
