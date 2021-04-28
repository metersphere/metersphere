package io.metersphere.service;

import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.TestCaseFileMapper;
import io.metersphere.base.mapper.ext.ExtFileMetadataMapper;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.request.QueryProjectFileRequest;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private TestCaseFileMapper testCaseFileMapper;

    private static final String FILE_FILE_DIR = "/opt/metersphere/data/file";
    private static final String JAR_FILE_DIR = "/opt/metersphere/data/jar";

    public List<FileMetadata> selectFileByWorkSpace(QueryCustomFieldRequest request, List<String> ids) {//   查找 projectId 在 ids 中的文件，和 projectId 为空的 jar 包
        return extFileMetadataMapper.selectFileOrJar(request, ids);
    }

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void setFileContent(String fileId, byte[] content) {
        FileContent record = new FileContent();
        record  .setFile(content);
        record.setFileId(fileId);
        fileContentMapper.updateByPrimaryKeySelective(record);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        List<FileMetadata> fileList = fileMetadataMapper.selectByExample(example);
        fileMetadataMapper.deleteByExample(example);

        for(FileMetadata file : fileList) {
            if(file.getMethod().equals("PATH")) {   //  删除以路径方式存储的文件
                String path = file.getPath();
                if (existsFile(path)) {
                    deleteIfExists(path);
                }
            }
        }
        FileContentExample example2 = new FileContentExample(); //  删除以二进制方式存在数据库的文件
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public static boolean existsFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    public static void deleteIfExists(String path) {
        try {
            deleteIfExists(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            if (file.isFile()) {
                if (!file.delete()) {
                    throw new IOException("Delete file failure,path:" + file.getAbsolutePath());
                }
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File temp : files) {
                        deleteIfExists(temp);
                    }
                }
                if (!file.delete()) {
                    throw new IOException("Delete file failure,path:" + file.getAbsolutePath());
                }
            }
        }
    }

    public FileMetadata get(String id) {
        return fileMetadataMapper.selectByPrimaryKey(id);
    }

    public List<FileMetadata> list() {  //  jar 包管理
        FileMetadataExample example = new FileMetadataExample();
        List<FileMetadata> fileList = fileMetadataMapper.selectByExample(example);
        fileList.stream().filter(f -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(f.getType(), FileType.JAR.name())).collect(Collectors.toList());
        return fileList;
    }

    public List<FileMetadata> list(FileMetadata jarConfig) {  //  jar 包管理
        FileMetadataExample example = new FileMetadataExample();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(jarConfig.getName())) {
            example.createCriteria().andNameLike("%" + jarConfig.getName() + "%").andTypeEqualTo(FileType.JAR.name());
        }
        example.setOrderByClause("update_time desc");
        List<FileMetadata> fileList = fileMetadataMapper.selectByExample(example);
        fileList.stream().filter(f -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(f.getType(), FileType.JAR.name())).collect(Collectors.toList());
        //  只显示 projectId = null 或 等于当前 projectId 的 jar 包
        fileList = fileList.stream().filter(f -> (f.getProjectId() == null || f.getProjectId().equals(SessionUtils.getCurrentProjectId()))).collect(Collectors.toList());
        return fileList;
    }

    public List<FileMetadata> searchList(FileMetadata fileMetadata, boolean isJar) {  //  jar 包管理、文件管理中调用
        FileMetadataExample example = new FileMetadataExample();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(fileMetadata.getName())) {
            example.createCriteria().andNameLike("%" + fileMetadata.getName() + "%");
            example.or(example.createCriteria().andFileNameLike("%" + fileMetadata.getName() + "%"));
        }   //  根据文件名和自定义名称查找
        example.setOrderByClause("update_time desc");
        //  合并两个查找结果并去重，按时间降序
        List<FileMetadata> fileList = fileMetadataMapper.selectByExample(example);
        if(isJar == true) {
            fileList = fileList.stream().filter(f -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(f.getType(), FileType.JAR.name())).distinct().collect(Collectors.toList());
            //  只显示 projectId = null 或 等于当前 projectId 的 jar 包
            fileList = fileList.stream().filter(f -> (f.getProjectId() == null || f.getProjectId().equals(SessionUtils.getCurrentProjectId()))).collect(Collectors.toList());
        } else {
            fileList = fileList.stream().distinct().collect(Collectors.toList());
        }

        Collections.sort(fileList, Comparator.comparingLong(FileMetadata::getUpdateTime).reversed());
        return fileList;
    }

    public void deleteFileRelatedByIds(List<String> ids) {
        deleteFileByIds(ids);
    }

    public void updateFile(FileMetadata fileMetadata, MultipartFile file) {  //  jar 包管理、文件管理中调用
        checkExist(fileMetadata);
        fileMetadata.setModifier(SessionUtils.getUser().getId());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        String path = fileMetadata.getPath();
        String file_dir = fileMetadata.getType().equals(FileType.JAR.name())? JAR_FILE_DIR : FILE_FILE_DIR; //  判断是jar包还是其它文件
        if (file != null) {
            fileMetadata.setFileName(file.getOriginalFilename());
            if (fileMetadata.getProjectId() != null) {
                fileMetadata.setPath(file_dir + "/" + fileMetadata.getProjectId() + "/" + file.getOriginalFilename());
            } else {
                fileMetadata.setPath(file_dir + "/" + file.getOriginalFilename());
            }
        }
        fileMetadataMapper.updateByPrimaryKey(fileMetadata);
        if (file != null) {
            if (existsFile(path)) { //  先删除，再保存
                deleteIfExists(path);
            }
            createFile(fileMetadata.getPath(), file);
            fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
        }
        if(fileMetadata.getType().equals(FileType.JAR.name())) {
            NewDriverManager.loadJar(fileMetadata.getPath());
        }
    }

    public String saveFile(FileMetadata jarFile, MultipartFile file) {  //  jar 包保存的调用
        jarFile.setMethod("PATH");
        jarFile.setPath(JAR_FILE_DIR + "/" + SessionUtils.getCurrentProjectId() + "/" + file.getOriginalFilename());
        jarFile.setProjectId(SessionUtils.getCurrentProjectId());   //  设置 projectId 为当前项目的
        checkExist(jarFile);
        jarFile.setType("JAR");
        if(jarFile.getId() == null) {   //  若是第一次创建
            jarFile.setId(UUID.randomUUID().toString());
            jarFile.setCreator(SessionUtils.getUser().getId());
            jarFile.setCreateTime(System.currentTimeMillis());
        }
        jarFile.setModifier(SessionUtils.getUser().getId());
        jarFile.setUpdateTime(System.currentTimeMillis());
        jarFile.setSize(file.getSize());
        jarFile.setFileName(file.getOriginalFilename());
        File folder = new File(JAR_FILE_DIR + "/" + SessionUtils.getCurrentProjectId());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        createFile(jarFile.getPath(), file);
        fileMetadataMapper.insert(jarFile);
        NewDriverManager.loadJar(jarFile.getPath());
        return jarFile.getId();
    }

    private void createFile(String path, MultipartFile file) {
        File newfile = new File(path);
        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(newfile)) {
            newfile.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("upload_fail"));
        }
    }

    private void checkExist(FileMetadata file) {
        if (file.getName() != null) {
            FileMetadataExample example = new FileMetadataExample();
            FileMetadataExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(file.getName());
            if (org.apache.commons.lang3.StringUtils.isNotBlank(file.getId())) {
                criteria.andIdNotEqualTo(file.getId());
            }
            if (fileMetadataMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("already_exists"));
            }
        }
    }

    public FileMetadata saveFile(MultipartFile file, String projectId, String fileId) {
        final FileMetadata fileMetadata = new FileMetadata();
        if (StringUtils.isEmpty(fileId)) {
            fileMetadata.setId(UUID.randomUUID().toString());
        } else {
            fileMetadata.setId(fileId);
        }
        FileType type = getFileType(file.getOriginalFilename());
        File testDir;
        if(type.toString().equals("JAR")) { //  jar 文件放在专门放 jar 的文件夹下
            if(projectId != null && !projectId.isEmpty()) {
                testDir = new File(JAR_FILE_DIR + "/" + projectId);
            } else {
                testDir = new File(JAR_FILE_DIR);
            }   //  有所属项目的 jar 包放在 JAR_FILE_DIR 路径下的名称为 projectId 的目录下；无所属的 jar 包直接放在 JAR_FILE_DIR 下
        } else {
            testDir = new File(FILE_FILE_DIR + "/" + projectId);
        }
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        String filePath = testDir + "/" + file.getOriginalFilename();
        if(existsFile(filePath)) {
            MSException.throwException(Translator.get("already_exists"));
            return null;
        }
        fileMetadata.setPath(filePath);
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setFileName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setProjectId(projectId);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setCreator(SessionUtils.getUser().getId());
        fileMetadata.setModifier(SessionUtils.getUser().getId());
        fileMetadata.setMethod("PATH");
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());

        createFile(filePath, file);
        fileMetadataMapper.insert(fileMetadata);

        return fileMetadata;
    }

    public FileMetadata saveFile(MultipartFile file, String projectId) {
        return saveFile(file, projectId, null);
    }

    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file, null);
    }

    public FileMetadata saveFile(File file, byte[] fileByte, String projectId) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getName());
        fileMetadata.setFileName(file.getName());
        fileMetadata.setSize(file.length());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setCreator(SessionUtils.getUser().getId());
        fileMetadata.setModifier(SessionUtils.getUser().getId());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setMethod("PATH");
        saveBinaryFile(fileByte, file.getName(), projectId);

        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata;
    }

    private void saveBinaryFile(byte[] fileByte, String fileName, String projectId) {
        File copyFile;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(FILE_FILE_DIR);
            if (!dir.exists()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            copyFile = new File(FILE_FILE_DIR + "/" + projectId + "/" + fileName);
            fos = new FileOutputStream(copyFile);
            bos = new BufferedOutputStream(fos);
            bos.write(fileByte);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] getBinaryFile(String path) throws IOException {
        return FileUtil.readAsByteArray(new File(path));
    }

    public FileMetadata saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(fileSize);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setCreator(SessionUtils.getUser().getId());
        fileMetadata.setModifier(SessionUtils.getUser().getId());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata copyFile(String fileId, String projectId) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileContent fileContent = getFileContent(fileId);
        if (fileMetadata != null) {
            fileMetadata.setId(UUID.randomUUID().toString());
            fileMetadata.setCreateTime(System.currentTimeMillis());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadata.setCreator(SessionUtils.getUser().getId());
            fileMetadata.setModifier(SessionUtils.getUser().getId());
            fileMetadata.setMethod("PATH");
            saveBinaryFile(fileContent.getFile(), fileMetadata.getFileName(), projectId);  //  上传文件到服务器而不是以二进制文件存于数据库

            fileMetadataMapper.insert(fileMetadata);

            fileContent.setFileId(fileMetadata.getId());
            fileContentMapper.insert(fileContent);
        }
        return fileMetadata;
    }

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
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

    public void deleteFileById(String fileId) {
        try {
            deleteFileByIds(Collections.singletonList(fileId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public void updateFileMetadata(byte[] bytes, FileMetadata fileMetadata) {
        if(fileMetadata.getMethod().equals("PATH")) {
            String path = fileMetadata.getPath();
            File file = new File(path);
            String fatherPath1 = file.getParent();
            String fatherPath2 = new File(fatherPath1).getParent();
            String projectId = fatherPath1.substring(fatherPath2.length() + 1);
            if (existsFile(path)) { //  先删除，再保存
                deleteIfExists(path);
            }
            saveBinaryFile(bytes, fileMetadata.getFileName(), projectId);
        }
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
            criteria.andFileNameEqualTo(request.getName());   //  根据文件名判断是否重复
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
}