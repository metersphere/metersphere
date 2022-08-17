package io.metersphere.metadata.service;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.metadata.vo.FileRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileManagerService {
    public String upload(MultipartFile file, FileRequest request) {
        try {
            this.initStorage(request);
            return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
        } catch (IOException e) {
            LogUtil.error(e);
            return null;
        }
    }

    public String upload(byte[] file, FileRequest request) {
        try {
            this.initStorage(request);
            return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
        } catch (IOException e) {
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
        } catch (IOException e) {
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
            request.setStorage(StorageConstants.LOCAL.name());
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
}
