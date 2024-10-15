package io.metersphere.system.service;

import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class FileService {
    public String upload(MultipartFile file, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
    }

    public void upload(File file, FileRequest request) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            this.upload(inputStream, request);
        }
    }

    public String upload(InputStream inputStream, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(inputStream, request);
    }

    public String upload(byte[] file, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
    }

    public byte[] download(FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).getFile(request);
    }

    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).getFileAsStream(request);
    }

    public void deleteFile(FileRequest request) throws Exception {
        FileCenter.getRepository(request.getStorage()).delete(request);
    }
}
