package io.metersphere.code.snippet.util;

import io.metersphere.commons.utils.FileUtils;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.utils.LocalPathUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

public class ProjectFileUtils extends FileUtils {

    public static void createFiles(List<FileInfoDTO> infoDTOS, String key, List<ProjectJarConfig> value) {
        infoDTOS.forEach(item -> {
            value.forEach(config -> {
                if (StringUtils.equals(item.getId(), config.getId())) {
                    createFile(StringUtils.join(LocalPathUtil.JAR_PATH,
                            File.separator,
                            key,
                            File.separator,
                            config.getId(),
                            File.separator,
                            String.valueOf(config.getUpdateTime()), ".jar"), item.getFileByte());
                }
            });
        });
    }
}
