package io.metersphere.system.service;

import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-07-30  10:07
 */
@Service
public class CommonFileService {

    @Value("50MB")
    private DataSize maxFileSize;

    @Resource
    private FileService fileService;

    /**
     * 将图片文件上传到临时目录
     * @param file 文件
     * @return 文件ID
     */
    public String uploadTempImgFile(MultipartFile file) {
        String fileName = StringUtils.trim(file.getOriginalFilename());
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        String fileId = IDGenerator.nextStr();
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        try {
            FileCenter.getDefaultRepository().saveFile(file, fileRequest);
            uploadTempReviewImg(file, fileId);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file_upload_fail"), e);
        }
        return fileId;
    }

    private void uploadTempReviewImg(MultipartFile file, String fileId) throws Exception {
        uploadReviewImg(file, fileId, DefaultRepositoryDir.getSystemTempCompressDir());
    }

    /**
     * 上传预览的图片
     * @param file
     * @param fileId
     * @param folder
     * @throws Exception
     */
    public void uploadReviewImg(MultipartFile file, String fileId, String folder) throws Exception {
        String fileName = StringUtils.trim(file.getOriginalFilename());

        String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
        if (TempFileUtils.isImage(fileType)) {
            // 图片文件自动生成预览图
            byte[] previewImg = TempFileUtils.compressPic(file.getBytes());
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(fileName);
            fileRequest.setFolder(folder + "/" + fileId);
            fileRequest.setStorage(StorageType.MINIO.toString());
            fileService.upload(previewImg, fileRequest);
        }
    }

    /**
     * 从临时文件夹中保存文件到指定文件夹
     * 并删除临时文件
     *
     * @param folder     文件夹
     * @param fileIds 临时文件ID列表
     */
    public void saveReviewImgFromTempFile(String folder, String reviewFolder, List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        Map<String, String> fileMap = new HashMap<>();
        fileIds.forEach(fileId -> {
            String fileName = getTempFileNameByFileId(fileId);
            fileMap.put(fileId, fileName);
        });
        saveReviewImgFromTempFile(folder, reviewFolder, fileMap);
    }

    /**
     * 从临时文件夹中保存文件到指定文件夹
     * 并删除临时文件
     *
     * @param folder     文件夹
     * @param fileMap key:fileId value:fileName
     */
    public void saveReviewImgFromTempFile(String folder, String reviewFolder, Map<String, String> fileMap) {
        if (MapUtils.isEmpty(fileMap)) {
            return;
        }
        for (String fileId : fileMap.keySet()) {
            try {
                String fileName = fileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 将临时文件移动到指定文件夹
                moveTempFileToFolder(fileId, fileName, folder);
                // 将文件从临时目录移动到指定的图片预览目录
                moveTempFileToImgReviewFolder(reviewFolder, fileId, fileName);
                // 删除临时文件
                deleteTempFile(fileId, fileName);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"), e);
            }
        }
    }

    /**
     * 从临时文件夹中保存文件到指定文件夹
     * 并删除临时文件
     *
     * @param folder     文件夹
     * @param fileMap key:fileId value:fileName
     */
    public void saveFileFromTempFile(String folder, Map<String, String> fileMap) {
        if (MapUtils.isEmpty(fileMap)) {
            return;
        }
        for (String fileId : fileMap.keySet()) {
            try {
                String fileName = fileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 将临时文件移动到指定文件夹
                moveTempFileToFolder(fileId, fileName, folder);
                // 删除临时文件
                deleteTempFile(fileId, fileName);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"), e);
            }
        }
    }

    /**
     * 将文件从临时目录移动到指定的图片预览目录
     * @param reviewFolder
     * @param fileId
     * @param fileName
     * @throws Exception
     */
    public void moveTempFileToImgReviewFolder(String reviewFolder, String fileId, String fileName) throws Exception {
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        // 按ID建文件夹，避免文件名重复
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
        if (TempFileUtils.isImage(fileType)) {
            // 图片文件自动生成预览图
            byte[] file = defaultRepository.getFile(fileRequest);
            byte[] previewImg = TempFileUtils.compressPic(file);
            fileRequest.setFolder(reviewFolder + "/" + fileId);
            fileRequest.setStorage(StorageType.MINIO.toString());
            fileService.upload(previewImg, fileRequest);
        }
    }

    /**
     * 将文件从临时目录移动到指定目录
     * @param fileId
     * @param fileName
     * @param folder
     * @throws Exception
     */
    public void moveTempFileToFolder(String fileId, String fileName, String folder) throws Exception {
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        // 按ID建文件夹，避免文件名重复
        FileCopyRequest fileCopyRequest = new FileCopyRequest();
        fileCopyRequest.setCopyFolder(systemTempDir + "/" + fileId);
        fileCopyRequest.setCopyfileName(fileName);
        fileCopyRequest.setFileName(fileName);
        fileCopyRequest.setFolder(folder + "/" + fileId);
        // 将文件从临时目录复制到资源目录
        defaultRepository.copyFile(fileCopyRequest);
    }

    /**
     * 删除临时文件
     * @param fileId
     * @param fileName
     * @throws Exception
     */
    public void deleteTempFile(String fileId, String fileName) throws Exception {
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        FileCopyRequest fileRequest = new FileCopyRequest();
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        fileRequest.setFileName(fileName);
        defaultRepository.delete(fileRequest);
    }

    /**
     * 根据文件ID，查询临时文件的文件名称
     */
    public String getTempFileNameByFileId(String fileId) {
        return getFileNameByFileId(fileId, DefaultRepositoryDir.getSystemTempDir());
    }

    /**
     * 根据文件ID，查询minio中对应目录下的文件名称
     */
    public String getFileNameByFileId(String fileId, String folder) {
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        try {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(folder + "/" + fileId);
            List<String> folderFileNames = defaultRepository.getFolderFileNames(fileRequest);
            if (CollectionUtils.isEmpty(folderFileNames)) {
                return null;
            }
            String[] pathSplit = folderFileNames.getFirst().split("/");
            return pathSplit[pathSplit.length - 1];
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 从临时文件夹中下载图片
     * @param fileId
     * @param isCompressed
     * @return
     */
    public byte[] downloadTempImg(String fileId, String fileName, boolean isCompressed) {
        String systemTempDir;
        if (isCompressed) {
            systemTempDir = DefaultRepositoryDir.getSystemTempCompressDir();
        } else {
            systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        }
        FileRequest previewRequest = new FileRequest();
        previewRequest.setFileName(fileName);
        previewRequest.setStorage(StorageType.MINIO.name());
        previewRequest.setFolder(systemTempDir + "/" + fileId);
        byte[] previewImg = null;
        try {
            previewImg = fileService.download(previewRequest);
        } catch (Exception e) {
            LogUtils.error("获取预览图失败：{}", e);
        }

        if (previewImg == null || previewImg.length == 0) {
            try {
                if (isCompressed) {
                    // 如果压缩文件夹没有图片，则重新复制一份
                    moveTempFileToImgReviewFolder(systemTempDir, fileId, fileName);
                    previewImg = fileService.download(previewRequest);
                }
                return previewImg;
            } catch (Exception e) {
                LogUtils.error("获取预览图失败：{}", e);
            }
        }
        return previewImg;
    }
}
