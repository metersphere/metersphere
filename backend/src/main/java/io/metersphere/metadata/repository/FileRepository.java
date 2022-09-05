package io.metersphere.metadata.repository;

import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.repository.FileInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileRepository {
    String saveFile(MultipartFile file, FileRequest request) throws IOException;

    String saveFile(byte[] bytes, FileRequest request) throws IOException;

    void delete(FileRequest request) throws Exception;

    byte[] getFile(FileRequest request) throws Exception;

    List<FileInfoDTO> getFileBatch(List<FileRequest> requestList) throws Exception;

    boolean reName(FileRequest request) throws Exception;

}
