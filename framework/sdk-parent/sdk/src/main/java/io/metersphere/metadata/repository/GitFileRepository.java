package io.metersphere.metadata.repository;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.utils.GitRepositoryUtil;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.RemoteFileAttachInfo;
import io.metersphere.metadata.vo.RepositoryRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitFileRepository implements FileRepository {
    @Override
    public String saveFile(MultipartFile multipartFile, FileRequest request) throws IOException {
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
        byte[] buffer = new byte[0];
        if (request.getFileAttachInfo() != null) {
            RemoteFileAttachInfo gitFileInfo = request.getFileAttachInfo();
            GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(
                    gitFileInfo.getRepositoryPath(), gitFileInfo.getUserName(), gitFileInfo.getToken());
            buffer = repositoryUtils.getSingleFile(gitFileInfo.getFilePath(), gitFileInfo.getCommitId());
        }
        return buffer;
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return new ByteArrayInputStream(getFile(request));
    }

    @Override
    public List<FileInfoDTO> getFileBatch(List<FileRequest> allRequests) throws Exception {
        List<FileInfoDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allRequests)) {
            Map<String, List<FileRequest>> requestGroupByRepository = new HashMap<>();
            for (FileRequest request : allRequests) {
                if (request.getFileAttachInfo() != null) {
                    RemoteFileAttachInfo gitFileInfo = request.getFileAttachInfo();
                    if (requestGroupByRepository.containsKey(gitFileInfo.getRepositoryInfo())) {
                        requestGroupByRepository.get(gitFileInfo.getRepositoryInfo()).add(request);
                    } else {
                        requestGroupByRepository.put(gitFileInfo.getRepositoryInfo(), new ArrayList<>() {{
                            this.add(request);
                        }});
                    }
                }
            }
            for (Map.Entry<String, List<FileRequest>> entry : requestGroupByRepository.entrySet()) {
                List<FileRequest> requestList = entry.getValue();
                RemoteFileAttachInfo baseGitFileInfo = null;

                List<RepositoryRequest> repositoryRequestList = new ArrayList<>();
                for (FileRequest fileRequest : requestList) {
                    RemoteFileAttachInfo gitFileInfo = fileRequest.getFileAttachInfo();
                    if (baseGitFileInfo == null) {
                        baseGitFileInfo = gitFileInfo;
                    }
                    repositoryRequestList.add(new RepositoryRequest() {{
                        this.setCommitId(gitFileInfo.getCommitId());
                        this.setFilePath(gitFileInfo.getFilePath());
                        this.setFileMetadataId(fileRequest.getResourceId());
                    }});
                }

                GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(
                        baseGitFileInfo.getRepositoryPath(),
                        baseGitFileInfo.getUserName(), baseGitFileInfo.getToken());

                Map<String, byte[]> fileByteMap = repositoryUtils.getFiles(repositoryRequestList);
                repositoryRequestList.forEach(repositoryFile -> {
                    if (fileByteMap.get(repositoryFile.getFileMetadataId()) != null) {
                        FileInfoDTO repositoryFileDTO = new FileInfoDTO(
                                repositoryFile.getFileMetadataId(),
                                MetadataUtils.getFileNameByRemotePath(repositoryFile.getFilePath()),
                                StorageConstants.GIT.name(),
                                repositoryFile.getFilePath(),
                                fileByteMap.get(repositoryFile.getFileMetadataId()));
                        list.add(repositoryFileDTO);
                    }
                });
            }
        }
        return list;
    }

    @Override
    public boolean reName(FileRequest request) throws Exception {
        return false;
    }
}
