package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtFileMetadataMapper;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.performance.request.QueryProjectFileRequest;
import org.apache.commons.collections.CollectionUtils;
import io.metersphere.xmind.utils.FileUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileAttachmentMetadataMapper fileAttachmentMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private TestCaseFileMapper testCaseFileMapper;
    @Resource
    private IssueFileMapper issueFileMapper;
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public byte[] getAttachmentBytes(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        return FileUtils.fileToByte(attachmentFile);
    }

    public ResponseEntity<byte[]> downloadLocalAttachment(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        byte[] bytes = FileUtils.fileToByte(attachmentFile);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MetadataUtils.getFileName(fileAttachmentMetadata.getName(), fileAttachmentMetadata.getType()) + "\"")
                .contentLength(bytes.length)
                .body(bytes);
    }

    public MultipartFile getAttachmentMultipartFile(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        return FileUtil.fileToMultipartFile(attachmentFile);
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void setFileContent(String fileId, byte[] content) {
        FileContent record = new FileContent();
        record.setFile(content);
        record.setFileId(fileId);
        fileContentMapper.updateByPrimaryKeySelective(record);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);

        List<String> refIdList = extFileMetadataMapper.selectRefIdsByIds(ids);
        if (CollectionUtils.isNotEmpty(refIdList)) {
            //删除其余版本的文件
            example.clear();
            example.createCriteria().andRefIdIn(refIdList);
            fileMetadataMapper.deleteByExample(example);
        }

        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public void deleteFileRelatedByIds(List<String> ids) {
        deleteFileByIds(ids);
    }

    public void deleteFileAttachmentByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileAttachmentMetadataMapper.deleteByExample(example);
    }

    public List<FileMetadata> getAllFileMeta() {
        return fileMetadataMapper.selectByExample(new FileMetadataExample());
    }

    public List<FileContent> getAllFileContent() {
        return fileContentMapper.selectByExampleWithBLOBs(new FileContentExample());
    }

    public FileMetadata saveFile(MultipartFile file, String projectId, String fileId) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        if (StringUtils.isEmpty(fileId)) {
            fileMetadata.setId(UUID.randomUUID().toString());
        } else {
            fileMetadata.setId(fileId);
        }
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setProjectId(projectId);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        try {
            fileContent.setFile(file.getBytes());
        } catch (IOException e) {
            MSException.throwException(e);
        }
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileAttachmentMetadata saveAttachment(MultipartFile file, String attachmentType, String belongId) {
        String uploadPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileUtils.uploadFile(file, uploadPath);
        final FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
        fileAttachmentMetadata.setId(UUID.randomUUID().toString());
        fileAttachmentMetadata.setName(file.getOriginalFilename());
        fileAttachmentMetadata.setType(getFileTypeWithoutEnum(fileAttachmentMetadata.getName()));
        fileAttachmentMetadata.setSize(file.getSize());
        fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
        fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
        fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
        fileAttachmentMetadata.setFilePath(uploadPath);
        fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
        return fileAttachmentMetadata;
    }

    public FileAttachmentMetadata saveAttachmentByBytes(byte[] bytes, String attachmentType, String belongId, String attachmentName) {
        String uploadPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        File parentFile = new File(uploadPath);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try (OutputStream os = new FileOutputStream(uploadPath + "/" + attachmentName)) {
            InputStream in = new ByteArrayInputStream(bytes);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();

            final FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
            fileAttachmentMetadata.setId(UUID.randomUUID().toString());
            fileAttachmentMetadata.setName(attachmentName);
            fileAttachmentMetadata.setType(getFileTypeWithoutEnum(attachmentName));
            fileAttachmentMetadata.setSize(Integer.valueOf(bytes.length).longValue());
            fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
            fileAttachmentMetadata.setFilePath(uploadPath);
            fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
            return fileAttachmentMetadata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAttachment(List<String> ids) {
        for (String id : ids) {
            FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
            FileUtils.deleteFile(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        }
    }

    public void deleteAttachment(String attachmentType, String belongId) {
        String deletePath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileUtils.deleteDir(deletePath);
    }

    public FileMetadata saveFile(MultipartFile file, String projectId) {
        return saveFile(file, projectId, null);
    }

    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file, null);
    }

    public FileMetadata saveFile(File file, byte[] fileByte) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getName());
        fileMetadata.setSize(file.length());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata insertFileByFileName(File file, byte[] fileByte, String projectId) {
        if (StringUtils.isEmpty(file.getName())) {
            return null;
        } else {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(file.getName());
            List<FileMetadataWithBLOBs> fileMetadatasInDataBase = fileMetadataMapper.selectByExampleWithBLOBs(example);
            if (CollectionUtils.isEmpty(fileMetadatasInDataBase)) {
                final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
                fileMetadata.setId(UUID.randomUUID().toString());
                fileMetadata.setName(file.getName());
                fileMetadata.setSize(file.length());
                fileMetadata.setProjectId(projectId);
                fileMetadata.setCreateTime(System.currentTimeMillis());
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                FileType fileType = getFileType(fileMetadata.getName());
                fileMetadata.setType(fileType.name());
                fileMetadata.setLatest(true);
                fileMetadata.setRefId(fileMetadata.getId());
                fileMetadataMapper.insert(fileMetadata);

                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(fileByte);
                fileContentMapper.insert(fileContent);
                return fileMetadata;
            } else {
                FileMetadataWithBLOBs fileMetadata = fileMetadatasInDataBase.get(0);
                fileMetadata.setName(file.getName());
                fileMetadata.setSize(file.length());
                fileMetadata.setProjectId(projectId);
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                FileType fileType = getFileType(fileMetadata.getName());
                fileMetadata.setType(fileType.name());
                fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);

                fileContentMapper.deleteByPrimaryKey(fileMetadata.getId());
                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(fileByte);
                fileContentMapper.insert(fileContent);
                return fileMetadata;
            }
        }

    }

    public FileMetadata saveFile(File file, byte[] fileByte, String projectId) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getName());
        fileMetadata.setSize(file.length());
        fileMetadata.setProjectId(projectId);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(fileSize);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata copyFile(String fileId) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileContent fileContent = getFileContent(fileId);
        if (fileMetadata != null && fileContent != null) {
            fileMetadata.setId(UUID.randomUUID().toString());
            fileMetadata.setCreateTime(System.currentTimeMillis());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadata.setLatest(true);
            fileMetadata.setRefId(fileMetadata.getId());
            fileMetadataMapper.insert(fileMetadata);

            fileContent.setFileId(fileMetadata.getId());
            fileContentMapper.insert(fileContent);
        }
        return fileMetadata;
    }


    public FileAttachmentMetadata copyAttachment(String fileId, String attachmentType, String belongId) {
        String copyPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(fileId);
        if (fileAttachmentMetadata != null) {
            File copyFile = new File(copyPath);
            if (!copyFile.exists()) {
                FileUtils.copyFolder(fileAttachmentMetadata.getFilePath(), copyPath);
            }
            fileAttachmentMetadata.setId(UUID.randomUUID().toString());
            fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
            fileAttachmentMetadata.setFilePath(copyPath);
            fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
        }
        return fileAttachmentMetadata;
    }

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
    }

    private String getFileTypeWithoutEnum(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return type.toUpperCase();
    }

    public List<FileMetadata> getFileMetadataByCaseId(String caseId) {
        TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
        testCaseFileExample.createCriteria().andCaseIdEqualTo(caseId);
        final List<TestCaseFile> testCaseFiles = testCaseFileMapper.selectByExample(testCaseFileExample);

        if (CollectionUtils.isEmpty(testCaseFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = testCaseFiles.stream().map(TestCaseFile::getFileId).collect(Collectors.toList());
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    public List<FileAttachmentMetadata> getFileAttachmentMetadataByCaseId(String caseId) {
        TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
        testCaseFileExample.createCriteria().andCaseIdEqualTo(caseId);
        final List<TestCaseFile> testCaseFiles = testCaseFileMapper.selectByExample(testCaseFileExample);

        if (CollectionUtils.isEmpty(testCaseFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = testCaseFiles.stream().map(TestCaseFile::getFileId).collect(Collectors.toList());
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileAttachmentMetadataMapper.selectByExample(example);
    }

    public List<FileAttachmentMetadata> getFileAttachmentMetadataByIssueId(String issueId) {
        IssueFileExample issueFileExample = new IssueFileExample();
        issueFileExample.createCriteria().andIssueIdEqualTo(issueId);
        final List<IssueFile> issueFiles = issueFileMapper.selectByExample(issueFileExample);

        if (CollectionUtils.isEmpty(issueFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = issueFiles.stream().map(IssueFile::getFileId).collect(Collectors.toList());
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileAttachmentMetadataMapper.selectByExample(example);
    }

    public FileAttachmentMetadata getFileAttachmentMetadataByFileId(String fileId) {
        return fileAttachmentMetadataMapper.selectByPrimaryKey(fileId);
    }

    public void deleteFileById(String fileId) {
        deleteFileByIds(Collections.singletonList(fileId));
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public void updateFileMetadata(FileMetadataWithBLOBs fileMetadata) {
        fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
    }

    public boolean isFileExsits(String fileId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId);
        long fileCount = fileMetadataMapper.countByExample(example);
        if (fileCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<FileMetadata> getProjectFiles(String projectId, QueryProjectFileRequest request) {
        FileMetadataExample example = new FileMetadataExample();
        FileMetadataExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        if (!StringUtils.isEmpty(request.getName())) {
            criteria.andNameEqualTo(request.getName());
        }
        return fileMetadataMapper.selectByExample(example);
    }

    public List<FileMetadata> getFileMetadataByIds(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return new ArrayList<>();
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    public RsaKey checkRsaKey() {
        String key = "ms.login.rsa.key";
        FileContent value = getFileContent(key);
        if (value == null) {
            try {
                RsaKey rsaKey = RsaUtil.getRsaKey();
                byte[] bytes = SerializationUtils.serialize(rsaKey);
                final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
                fileMetadata.setId(key);
                fileMetadata.setName(key);
                fileMetadata.setSize((long) bytes.length);
                fileMetadata.setCreateTime(System.currentTimeMillis());
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                fileMetadata.setType("RSA_KEY");
                fileMetadata.setLatest(true);
                fileMetadata.setRefId(fileMetadata.getId());
                fileMetadataMapper.insert(fileMetadata);

                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(bytes);
                fileContentMapper.insert(fileContent);
                return rsaKey;
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return SerializationUtils.deserialize(value.getFile());
    }
}
