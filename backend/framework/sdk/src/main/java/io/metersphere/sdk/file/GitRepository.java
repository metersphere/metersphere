package io.metersphere.sdk.file;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.GitRepositoryUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Component
public class GitRepository implements FileRepository {

    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws Exception {
        return null;
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws Exception {
        return null;
    }

    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        return null;
    }

    @Override
    public void delete(FileRequest request) throws Exception {

    }

    @Override
    public void deleteFolder(FileRequest request) throws Exception {

    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        byte[] fileBytes = new byte[0];
        if (request.getGitFileRequest() != null) {
            GitFileRequest gitFileInfo = request.getGitFileRequest();
            GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(
                    gitFileInfo.getUrl(), gitFileInfo.getUserName(), gitFileInfo.getToken());
            fileBytes = repositoryUtils.getFile(request.getFileName(), gitFileInfo.getCommitId());
        }
        return fileBytes;
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        GitFileRequest gitFileInfo = request.getGitFileRequest();
        GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(
                gitFileInfo.getUrl(), gitFileInfo.getUserName(), gitFileInfo.getToken());
        return repositoryUtils.getFileStream(gitFileInfo.getUrl(), gitFileInfo.getCommitId());
    }

    // 缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    @Override
    public void downloadFile(FileRequest request, String fullPath) throws Exception {
        // 下载对象到本地文件
        try (InputStream inputStream = getFileAsStream(request);
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullPath))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return null;
    }

    @Override
    public void copyFile(FileCopyRequest request) throws Exception {
        throw new MSException("Not support copy file");
    }

    @Override
    public long getFileSize(FileRequest request) throws Exception {
        return 0;
    }
}
