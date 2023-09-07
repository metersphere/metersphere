package io.metersphere.sdk.file;

import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.util.CommonBeanFactory;

import java.util.HashMap;
import java.util.Map;

public class FileCenter {

    public static FileRepository getRepository(StorageType storageType) {
        Map<StorageType, FileRepository> repositoryMap = new HashMap<>() {{
            put(StorageType.MINIO, CommonBeanFactory.getBean(MinioRepository.class));
            put(StorageType.LOCAL, CommonBeanFactory.getBean(LocalFileRepository.class));
        }};
        FileRepository fileRepository = repositoryMap.get(storageType);
        return fileRepository == null ? getDefaultRepository() : fileRepository;
    }

    public static FileRepository getRepository(String storage) {
        Map<String, StorageType> storageTypeMap = new HashMap<>() {{
            put(StorageType.MINIO.name(), StorageType.MINIO);
            put(StorageType.LOCAL.name(), StorageType.LOCAL);
            put(StorageType.GIT.name(), StorageType.GIT);
        }};

        return getRepository(storageTypeMap.get(storage.toUpperCase()));
    }

    public static FileRepository getDefaultRepository() {
        return CommonBeanFactory.getBean(MinioRepository.class);
    }
}
