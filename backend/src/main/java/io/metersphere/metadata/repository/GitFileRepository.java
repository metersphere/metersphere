package io.metersphere.metadata.repository;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.metadata.utils.GitRepositoryUtils;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.repository.FileInfoDTO;
import io.metersphere.metadata.vo.repository.GitFileAttachInfo;
import io.metersphere.metadata.vo.repository.RepositoryRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        if (request.getFileAttachInfo() != null && request.getFileAttachInfo() instanceof GitFileAttachInfo) {
            GitFileAttachInfo gitFileInfo = (GitFileAttachInfo) request.getFileAttachInfo();
            GitRepositoryUtils repositoryUtils = new GitRepositoryUtils(
                    gitFileInfo.getRepositoryPath(), gitFileInfo.getUserName(), gitFileInfo.getToken());
            buffer = repositoryUtils.getSingleFile(gitFileInfo.getFilePath(), gitFileInfo.getCommitId());
        }
        return buffer;
    }

    @Override
    public List<FileInfoDTO> getFileBatch(List<FileRequest> allRequests) throws Exception {
        List<FileInfoDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allRequests)) {
            Map<String, List<FileRequest>> requestGroupByRepository = new HashMap<>();
            for (FileRequest request : allRequests) {
                if (request.getFileAttachInfo() != null && request.getFileAttachInfo() instanceof GitFileAttachInfo) {
                    GitFileAttachInfo gitFileInfo = (GitFileAttachInfo) request.getFileAttachInfo();
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
                GitFileAttachInfo baseGitFileInfo = null;

                List<RepositoryRequest> repositoryRequestList = new ArrayList<>();
                for (FileRequest fileRequest : requestList) {
                    GitFileAttachInfo gitFileInfo = (GitFileAttachInfo) fileRequest.getFileAttachInfo();
                    if (baseGitFileInfo == null) {
                        baseGitFileInfo = gitFileInfo;
                    }
                    repositoryRequestList.add(new RepositoryRequest() {{
                        this.setCommitId(gitFileInfo.getCommitId());
                        this.setFilePath(gitFileInfo.getFilePath());
                        this.setFileMetadataId(fileRequest.getResourceId());
                    }});
                }

                GitRepositoryUtils repositoryUtils = new GitRepositoryUtils(
                        baseGitFileInfo.getRepositoryPath(), baseGitFileInfo.getUserName(), baseGitFileInfo.getToken());


                Map<String, byte[]> fileByteMap = repositoryUtils.getFiles(repositoryRequestList);
                repositoryRequestList.forEach(repositoryFile -> {
                    if (fileByteMap.get(repositoryFile.getFileMetadataId()) != null) {
                        FileInfoDTO repositoryFileDTO = new FileInfoDTO(
                                repositoryFile.getFileMetadataId(), MetadataUtils.getFileNameByRemotePath(repositoryFile.getFilePath()), StorageConstants.GIT.name(), fileByteMap.get(repositoryFile.getFileMetadataId()));
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
