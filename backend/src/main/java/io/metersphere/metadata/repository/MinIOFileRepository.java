package io.metersphere.metadata.repository;

import io.metersphere.metadata.vo.FileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MinIOFileRepository implements FileRepository {

    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws IOException {
        return null;
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws IOException {
        return null;
    }

    @Override
    public void delete(FileRequest request) throws Exception {

    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        return new byte[0];
    }

    @Override
    public boolean reName(FileRequest request) throws Exception {
        return false;
    }
}
