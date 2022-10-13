package io.metersphere.commons.utils;

import io.metersphere.dto.FileInfoDTO;
import io.metersphere.request.BodyFile;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

public class ApiFileUtil extends FileUtils {
    public static String getFilePath(BodyFile file) {
        String type = StringUtils.isNotEmpty(file.getFileType()) ? file.getFileType().toLowerCase() : null;
        String name = file.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtil.BODY_FILE_DIR, File.separator, file.getProjectId(), File.separator, name);
    }

    public static void createFiles(List<FileInfoDTO> infoDTOS) {
        infoDTOS.forEach(item -> {
            createFile(item.getPath(), item.getFileByte());
        });
    }
}
