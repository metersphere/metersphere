package io.metersphere.metadata.service;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.vo.FileRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileManagerService {
    public String upload(MultipartFile file, FileRequest request) {
        try {
            this.initStorage(request);
            return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public String upload(byte[] file, FileRequest request) {
        try {
            this.initStorage(request);
            return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public boolean reName(String beforeName, String name, String projectId) {
        try {
            FileRequest request = new FileRequest();
            request.setBeforeName(beforeName);
            request.setFileName(name);
            request.setProjectId(projectId);
            this.initStorage(request);
            return FileCenter.getRepository(request.getStorage()).reName(request);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    public String coverFile(MultipartFile file, FileRequest request) {
        try {
            this.initStorage(request);
            this.delete(request);
            return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public void delete(FileRequest request) {
        try {
            this.initStorage(request);
            FileCenter.getRepository(request.getStorage()).delete(request);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void initStorage(FileRequest request) {
        if (StringUtils.isEmpty(request.getStorage())) {
            request.setStorage(StorageConstants.MINIO.name());
        }
    }

    public byte[] downloadFile(FileRequest request) {
        try {
            return FileCenter.getRepository(request.getStorage()).getFile(request);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public InputStream downloadFileAsStream(FileRequest request) {
        try {
            return FileCenter.getRepository(request.getStorage()).getFileAsStream(request);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public List<FileInfoDTO> downloadFileBatch(List<FileRequest> requestList) {
        List<FileInfoDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(requestList)) {
            Map<String, List<FileRequest>> requestByStorage = requestList.stream().filter((e) -> StringUtils.isNotBlank(e.getStorage()) && !StringUtils.equalsIgnoreCase(e.getStorage(), StorageConstants.LOCAL.name())).collect(Collectors.groupingBy(FileRequest::getStorage));
            for (Map.Entry<String, List<FileRequest>> requestByStorageEntry : requestByStorage.entrySet()) {
                try {
                    list.addAll(FileCenter.getRepository(requestByStorageEntry.getKey()).getFileBatch(requestByStorageEntry.getValue()));
                } catch (Exception e) {
                    LogUtil.error("下载文件失败!", e);
                }
            }
        }
        return list;
    }
}
