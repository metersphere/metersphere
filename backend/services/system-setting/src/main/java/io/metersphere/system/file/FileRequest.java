package io.metersphere.system.file;

import io.metersphere.project.domain.FileMetadataRepository;
import io.metersphere.project.domain.FileModuleRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class FileRequest {
    public static final String MAIN_FOLDER_SYSTEM = "system";
    public static final String MAIN_FOLDER_PROJECT = "project";
    public static final String MAIN_FOLDER_ORGANIZATION = "organization";

    public static final String APP_NAME_FILE_MANAGEMENT = "fileManagement";

    //主文件夹  取值：system、project、organization
    private String mainFolder;

    /*
    资源所属组目录。
        当mainFolder为system时，这里可以是plugin（存放插件）
        当mainFolder为project时，这里可以是项目ID
        可为空
     */
    private String sourceGroupFolder;

    /*
        可以为空
        取值参照：fileManagement、api
     */
    private String appName;

    //项目ID
    private String projectId;

    // 存储类型
    private String storage;

    // 资源id为空时存储在项目目录下
    private String resourceId;

    // 文件名称
    private String fileName;

    //Git文件信息
    private GitFileRequest gitFileRequest;

    public void setGitFileRequest(FileModuleRepository repository, FileMetadataRepository file) {
        gitFileRequest = new GitFileRequest(repository.getUrl(), repository.getToken(), repository.getUserName(), file.getBranch(), file.getCommitId());
    }
}

@Data
@AllArgsConstructor
class GitFileRequest {
    private String url;
    private String token;
    private String userName;
    private String branch;
    private String commitId;
}
