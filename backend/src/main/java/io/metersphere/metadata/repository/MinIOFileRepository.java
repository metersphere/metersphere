package io.metersphere.metadata.repository;

import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.repository.FileInfoDTO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public List<FileInfoDTO> getFileBatch(List<FileRequest> requestList) throws Exception {
        List<FileInfoDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(requestList)) {
            for (FileRequest fileRequest : requestList) {
                FileInfoDTO fileInfoDTO = new FileInfoDTO(fileRequest.getResourceId(), fileRequest.getFileName(), fileRequest.getStorage(), this.getFile(fileRequest));
                list.add(fileInfoDTO);
            }
        }
        return list;
    }

    @Override
    public boolean reName(FileRequest request) throws Exception {
        return false;
    }
}
