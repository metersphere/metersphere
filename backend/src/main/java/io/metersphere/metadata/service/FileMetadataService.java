package io.metersphere.metadata.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtFileMetadataMapper;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.metadata.vo.DownloadRequest;
import io.metersphere.metadata.vo.DumpFileRequest;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.metadata.vo.MoveFIleMetadataRequest;
import io.metersphere.performance.request.QueryProjectFileRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileMetadataService {
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileManagerService fileManagerService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;

    public List<FileMetadata> create(FileMetadata fileMetadata, List<MultipartFile> files) {
        List<FileMetadata> result = new ArrayList<>();
        if (fileMetadata == null) {
            fileMetadata = new FileMetadata();
        }
        if (!CollectionUtils.isEmpty(files)) {
            for (MultipartFile file : files) {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                if (CollectionUtils.isEmpty(this.getProjectFiles(fileMetadata.getProjectId(), request))) {
                    result.add(this.saveFile(file, fileMetadata));
                } else {
                    MSException.throwException(Translator.get("project_file_already_exists"));
                }
            }
        }
        return result;
    }

    public FileMetadata saveFile(MultipartFile file, FileMetadata fileMetadata) {
        if (StringUtils.isEmpty(fileMetadata.getId())) {
            fileMetadata.setId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(fileMetadata.getName())) {
            fileMetadata.setName(file.getOriginalFilename());
        }
        if (StringUtils.isEmpty(fileMetadata.getStorage())) {
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
        }
        if (StringUtils.isEmpty(fileMetadata.getModuleId())) {
            fileMetadata.setModuleId(fileModuleService.getDefaultNode(fileMetadata.getProjectId()).getId());
        }
        fileMetadata.setSize(file.getSize());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        String fileType = MetadataUtils.getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType);
        if (StringUtils.isEmpty(fileMetadata.getCreateUser())) {
            fileMetadata.setCreateUser(SessionUtils.getUserId());
        }
        if (StringUtils.isEmpty(fileMetadata.getUpdateUser())) {
            fileMetadata.setUpdateUser(SessionUtils.getUserId());
        }

        // 上传文件
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        String path = fileManagerService.upload(file, request);
        fileMetadata.setPath(path);
        if (fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId()) == null) {
            fileMetadataMapper.insert(fileMetadata);
        } else {
            fileMetadataMapper.updateByPrimaryKeyWithBLOBs(fileMetadata);
        }

        return fileMetadata;
    }


    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file, null);
    }

    public List<FileMetadata> getProjectFiles(String projectId, QueryProjectFileRequest request) {
        if (CollectionUtils.isEmpty(request.getOrders())) {
            OrderRequest req = new OrderRequest();
            req.setName("update_time");
            req.setType("desc");
            request.setOrders(new ArrayList<>() {{
                this.add(req);
            }});
        }
        List<FileMetadata> list = extFileMetadataMapper.getProjectFiles(projectId, request);
        long size = list.stream().filter(t -> StringUtils.isEmpty(t.getModuleId())).count();
        if (size > 0) {
            // 历史数据处理
            String moduleId = fileModuleService.getDefaultNode(projectId).getId();
            list.forEach(item -> {
                if (StringUtils.isEmpty(item.getModuleId())) {
                    item.setModuleId(moduleId);
                }
            });
        }
        return list;
    }

    public void deleteFile(String fileId) {
        // 删除性能测试引用
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andFileIdEqualTo(fileId);
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(example);
        String errorMessage = "";
        if (loadTestFiles.size() > 0) {
            List<String> testIds = loadTestFiles.stream().map(LoadTestFile::getTestId).distinct().collect(Collectors.toList());
            LoadTestExample testExample = new LoadTestExample();
            testExample.createCriteria().andIdIn(testIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(testExample);
            errorMessage += Translator.get("load_test") + ": " + StringUtils.join(loadTests.stream().map(LoadTest::getName).toArray(), ",");
            errorMessage += "\n";
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            MSException.throwException(errorMessage + Translator.get("project_file_in_use"));
        }
        // 删除文件引用关系
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andFileMetadataIdEqualTo(fileId);
        fileAssociationMapper.deleteByExample(associationExample);

        // 删除文件,历史遗留数据保留附件只删除关系
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        fileMetadataMapper.deleteByPrimaryKey(fileId);
        if (StringUtils.isNotEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            fileManagerService.delete(request);
        }
    }

    public void deleteBatch(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            ids.forEach(item -> {
                deleteFile(item);
            });
        }
    }

    public List<String> getTypes() {
        return extFileMetadataMapper.getTypes();
    }

    public void move(MoveFIleMetadataRequest request) {
        if (!CollectionUtils.isEmpty(request.getMetadataIds()) && StringUtils.isNotEmpty(request.getModuleId())) {
            extFileMetadataMapper.move(request);
        }
    }

    public byte[] getContent(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);
        if (fileContent != null) {
            return fileContent.getFile();
        }
        return null;
    }

    public byte[] loadFileAsBytes(String id) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        if (fileMetadata == null) {
            return new byte[0];
        }
        return this.loadFileAsBytes(fileMetadata);
    }

    private byte[] loadFileAsBytes(FileMetadata fileMetadata) {
        byte[] bytes = new byte[0];
        // 兼容历史数据
        if (StringUtils.isEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            bytes = getContent(fileMetadata.getId());
        } else {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            request.setResourceType(fileMetadata.getResourceType());
            request.setPath(fileMetadata.getPath());
            bytes = fileManagerService.downloadFile(request);
        }
        return bytes;
    }

    public ResponseEntity<byte[]> getFile(String fileId) {
        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        if (fileMetadata == null) {
            return null;
        }
        byte[] bytes = loadFileAsBytes(fileMetadata);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MetadataUtils.getFileName(fileMetadata.getName(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    public byte[] exportZip(DownloadRequest request) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        request.getRequests().forEach(fileMetadata -> {
            byte[] bytes = loadFileAsBytes(fileMetadata);
            if (bytes != null) {
                files.put(MetadataUtils.getFileName(fileMetadata.getName(), fileMetadata.getType()), bytes);
            }
        });

        return FileUtils.listBytesToZip(files);
    }


    private void checkName(FileMetadata fileMetadata) {
        FileMetadataExample example = new FileMetadataExample();
        FileMetadataExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(fileMetadata.getName())
                .andProjectIdEqualTo(fileMetadata.getProjectId())
                .andIdNotEqualTo(fileMetadata.getId());

        if (fileMetadataMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("project_file_already_exists"));
        }
    }

    private String getBeforeName(FileMetadata fileMetadata) {
        return fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId()).getName();
    }

    public void update(FileMetadata fileMetadata) {
        this.checkName(fileMetadata);
        String beforeName = getBeforeName(fileMetadata);
        if (!StringUtils.equalsIgnoreCase(beforeName, fileMetadata.getName())) {
            boolean isReName = fileManagerService.reName(beforeName, fileMetadata.getName(), fileMetadata.getProjectId());
            if (!isReName) {
                MSException.throwException("重命名失败！");
            }
        }
        if (fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId()) != null) {
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadata.setUpdateUser(SessionUtils.getUserId());
            fileMetadata.setPath(FileUtils.getFilePath(fileMetadata));
            fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);

        }
    }

    public FileMetadata reLoad(FileMetadata fileMetadata, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return fileMetadata;
        }
        fileMetadata.setSize(files.get(0).getSize());
        String fileType = MetadataUtils.getFileType(files.get(0).getOriginalFilename());
        fileMetadata.setType(fileType);
        if (StringUtils.isEmpty(fileMetadata.getStorage())) {
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
        }
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        fileManagerService.coverFile(files.get(0), request);

        this.update(fileMetadata);
        return fileMetadata;
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public List<FileMetadata> uploadFiles(String projectId, List<MultipartFile> files) {
        List<FileMetadata> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files)) {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setProjectId(projectId);
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
            files.forEach(file -> {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                if (CollectionUtils.isEmpty(this.getProjectFiles(fileMetadata.getProjectId(), request))) {
                    result.add(this.saveFile(file, fileMetadata));
                } else {
                    MSException.throwException(Translator.get("project_file_already_exists"));
                }
            });
        }
        return result;
    }

    public FileMetadata updateFile(String fileId, MultipartFile file) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        reLoad(fileMetadata, new ArrayList<>() {{
            this.add(file);
        }});
        return fileMetadata;
    }

    public void dumpFile(DumpFileRequest request, List<MultipartFile> files) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setProjectId(request.getProjectId());
        fileMetadata.setModuleId(request.getModuleId());
        if (CollectionUtils.isEmpty(files)) {
            // 文件已经存储过了
            String path = FileUtils.BODY_FILE_DIR + '/' + request.getResourceId() + '/' + request.getFileName();
            File file = new File(path);
            if (!file.exists()) {
                MSException.throwException("文件不存在！");
            }
            MultipartFile multipartFile = fileManagerService.getMultipartFile(file);
            if (multipartFile != null) {
                this.create(fileMetadata, new ArrayList<>() {{
                    this.add(multipartFile);
                }});
            }
        } else {
            this.create(fileMetadata, files);
        }
    }

    public List<String> getJar(List<String> projectIds) {
        if (!CollectionUtils.isEmpty(projectIds)) {
            FileMetadataExample fileMetadata = new FileMetadataExample();
            fileMetadata.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
            List<FileMetadata> files = fileMetadataMapper.selectByExample(fileMetadata);
            return files.stream().map(FileMetadata::getPath).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    public long myFiles(String createUser, String projectId) {
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdEqualTo(projectId).andCreateUserEqualTo(createUser);
        return fileMetadataMapper.countByExample(fileMetadata);
    }

    public String getLogDetails(String id) {
        FileMetadata fileMetadata = this.getFileMetadataById(id);
        if (fileMetadata != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(fileMetadata, SystemReference.projectColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(fileMetadata.getId()), fileMetadata.getProjectId(), fileMetadata.getName(), null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
