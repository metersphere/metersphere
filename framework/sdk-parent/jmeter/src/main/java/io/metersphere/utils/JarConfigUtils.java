package io.metersphere.utils;

import io.metersphere.dto.ProjectJarConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JarConfigUtils {

    public static Map<String, List<ProjectJarConfig>> getJarConfigs(List<String> projectIds, Map<String, List<ProjectJarConfig>> jarConfigMap) {
        String localPath = LocalPathUtil.JAR_PATH;
        Map<String, List<ProjectJarConfig>> jarConfigsMap = new HashMap<>();
        projectIds.forEach(item -> {
            List<ProjectJarConfig> jarConfigs = new ArrayList<>();
            //根据项目id获取node服务器当前项目下的所有文件
            List<String> nodeFiles = getFileNames(StringUtils.join(localPath, File.separator, item));
            //主服务的jar文件
            if (jarConfigMap.containsKey(item)) {
                List<ProjectJarConfig> projectJarConfigs = jarConfigMap.get(item);
                //本地文件多余的jar进行删除
                List<String> expiredJar = nodeFiles
                        .stream().filter(nodeFile -> !projectJarConfigs
                                .stream()
                                .map(ProjectJarConfig::getId)
                                .collect(Collectors.toList()).contains(nodeFile)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(expiredJar)) {
                    expiredJar.forEach(jar
                            -> deleteDir(StringUtils.join(
                            localPath,
                            File.separator,
                            item,
                            File.separator,
                            jar)));
                    jarConfigsMap.put(item, new ArrayList<>());
                }
                projectJarConfigs.forEach(projectJarConfig -> {
                    if (CollectionUtils.isNotEmpty(nodeFiles)) {
                        nodeFiles.forEach(refId -> {
                            if (!nodeFiles.contains(projectJarConfig.getId())) {
                                jarConfigs.add(projectJarConfig);
                            } else if (StringUtils.equals(refId, projectJarConfig.getId())) {
                                //资源id目录存在，需要判断文件的时间戳与数据记录的时间戳是不是同一个  不是同一个的需要删除jar然后重新下载
                                List<String> jarFiles = getFileNames(StringUtils.join(localPath, File.separator, item, File.separator, projectJarConfig.getId()));
                                if (CollectionUtils.isNotEmpty(jarFiles)) {
                                    jarFiles.forEach(jarFile -> {
                                        long updateTime = projectJarConfig.getUpdateTime();
                                        if (!StringUtils.equals(StringUtils.substringBefore(jarFile, ".jar"), String.valueOf(updateTime))) {
                                            deleteFile(StringUtils.join(localPath, File.separator, item, File.separator, refId, File.separator, jarFile));
                                            jarConfigs.add(projectJarConfig);
                                        }
                                    });
                                } else {
                                    //本地完全没有jar包  需要下载
                                    jarConfigs.add(projectJarConfig);
                                }
                            }
                        });
                    } else {
                        //项目级别的目录都不存在，需要从服务器下载
                        jarConfigs.add(projectJarConfig);
                    }
                    if (CollectionUtils.isNotEmpty(jarConfigs)) {
                        jarConfigsMap.put(item, jarConfigs);
                    }
                });
            }
        });
        return jarConfigsMap;
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static List<String> getFileNames(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        List<String> fileNames = new ArrayList<>();
        File fa[] = f.listFiles();
        for (int i = 0; i < fa.length; i++) {
            File fs = fa[i];
            if (fs.exists() && !StringUtils.equals(fs.getName(), ".DS_Store")) {
                fileNames.add(fs.getName());
            }
        }
        return fileNames;
    }

    public static void deleteDir(String path) {
        File file = new File(path);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public static List<String> findPathByProjectIds(List<String> projectIds) {
        List<String> jarPaths = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            projectIds.forEach(item -> {
                jarPaths.addAll(walk(LocalPathUtil.JAR_PATH + File.separator + item));
            });
        }
        return jarPaths;
    }


    public static List<String> walk(String dirName) {
        try (Stream<Path> paths = Files.walk(Paths.get(dirName), 2)) {
            return paths.map(path -> path.toString()).filter(f -> f.endsWith(".jar")).collect(Collectors.toList());
        } catch (IOException e) {
            LoggerUtil.error("读取文件路径异常：", e);
        }
        return new ArrayList<>();
    }
}
