package io.metersphere.sdk.file;

import io.metersphere.sdk.util.CommonBeanFactory;

public class FileCenter {

    // 多种实现时打开
    /*public static FileRepository getRepository(String storage) {
        if (StringUtils.equals(StorageConstants.GIT.name(), storage)) {
            LogUtils.info("扩展GIT存储方式");
            return null;
        } else {
            return getDefaultRepository();
        }
    }*/

    public static FileRepository getDefaultRepository() {
        return CommonBeanFactory.getBean(MinioRepository.class);
    }
}
