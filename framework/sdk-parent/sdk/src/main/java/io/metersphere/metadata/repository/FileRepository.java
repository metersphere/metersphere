package io.metersphere.metadata.repository;

import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.vo.FileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileRepository {
    String saveFile(MultipartFile file, FileRequest request) throws Exception;

    String saveFile(byte[] bytes, FileRequest request) throws Exception;

    void delete(FileRequest request) throws Exception;

    byte[] getFile(FileRequest request) throws Exception;

    InputStream getFileAsStream(FileRequest request) throws Exception;

    List<FileInfoDTO> getFileBatch(List<FileRequest> requestList) throws Exception;

    boolean reName(FileRequest request) throws Exception;

}
