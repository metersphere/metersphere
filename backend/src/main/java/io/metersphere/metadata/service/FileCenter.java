package io.metersphere.metadata.service;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.metadata.repository.FileRepository;
import io.metersphere.metadata.repository.GitFileRepository;
import io.metersphere.metadata.repository.LocalFileRepository;
import org.apache.commons.lang3.StringUtils;

public class FileCenter {
    public static FileRepository getRepository(String storage) {
        if (StringUtils.equals(StorageConstants.MINIO.name(), storage)) {
            LogUtil.info("NAS文件处理");
            return null;
        } else if (StringUtils.equals(StorageConstants.GIT.name(), storage)) {
            LogUtil.info("Git文件处理");
            return new GitFileRepository();
        } else {
            LogUtil.info("LOCAL文件处理");
            return new LocalFileRepository();
        }
    }
}
