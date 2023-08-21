package io.metersphere.service;

import io.metersphere.base.domain.FileAssociation;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.metadata.service.FileAssociationService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.request.MdImageSaveRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 处理富文本中的图片
 * 以功能用例为例，步骤如下：
 * <p>
 * 1. 服务框创建图片时调用 resource/md/temp/upload 接口，将图片上传到临时目录
 * 2. 用例保存成功后，解析用例所有富文本框中的图片，调用 resource/md/save/image 接口，将图片从临时目录移入，正式目录，并创建用例和图片的关联关系
 * 3. 编辑时，resource/md/save/image 接口会对比图片，删除无用并且没有被其他资源占用的图片，将新增图片建立关联关系，并移入正式目录
 * 4. CleanMdTempImageJob 会定时删除临时目录中的图片，删除时会对比图片修改时间，避免删除正在使用的图片
 * 5. 删除用例时，会调用本类中 deleteBySourceId 方法，查询项目下的图片，删除
 * 6. 删除项目时，会调用本类中 deleteByProjectId 方法，查询项目下的图片，删除
 * <p>
 */
@Service
public class MdFileService {

    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileAssociationService fileAssociationService;
    public static final String FILE_ASSOCIATION_TYPE = "MD_IMAGE";

    public void saveFiles(MdImageSaveRequest request) {
        List<String> fileNames = request.getFileNames();
        String resourceId = request.getResourceId();
        String projectId = request.getProjectId();
        if (fileNames == null) {
            return;
        }

        fileNames.forEach(FileUtils::validateFileName);

        // 该资源关联的markdown图片
        List<String> originFileNames = getResourceOriginFiles(resourceId);

        List<String> deleteFileNames = ListUtils.subtract(originFileNames, fileNames);
        List<String> uploadFileNames = ListUtils.subtract(fileNames, originFileNames);

        // 本次编辑中不包含的图片，校验时候有其他资源应用后，删除
        deleteUselessFile(resourceId, deleteFileNames);

        // 保存本次编辑中新增的图片
        saveNewFile(resourceId, projectId, uploadFileNames);
    }

    private void saveNewFile(String resourceId, String projectId, List<String> uploadFileNames) {
        uploadFileNames.forEach(saveFile -> {
            // 从临时目录移入正式目录
            FileUtils.moveFileToDir(StringUtils.join(FileUtils.MD_IMAGE_TEMP_DIR, "/", saveFile), FileUtils.MD_IMAGE_DIR);
            FileMetadata fileMetadata = saveFileMetadata(saveFile, projectId);
            // 保存文件关联信息
            saveFileAssociation(resourceId, fileMetadata);
        });
    }

    private List<String> getResourceOriginFiles(String resourceId) {
        List<FileAssociation> fileAssociations = fileAssociationService.getByResourceIdAndType(resourceId, FILE_ASSOCIATION_TYPE);
        List<String> fileIds = fileAssociations.stream().map(FileAssociation::getFileMetadataId).toList();
        List<FileMetadataWithBLOBs> mdFiles = fileMetadataService.getFileMetadataByIdList(fileIds);
        List<String> originFileNames = mdFiles.stream().map(FileMetadataWithBLOBs::getName).collect(Collectors.toList());
        return originFileNames;
    }

    /**
     * 保存资源与文件的关联关系
     *
     * @param resourceId
     * @param file
     */
    private void saveFileAssociation(String resourceId, FileMetadata file) {
        FileAssociation fileAssociation = new FileAssociation();
        fileAssociation.setId(UUID.randomUUID().toString());
        fileAssociation.setFileMetadataId(file.getId());
        fileAssociation.setFileType(file.getType());
        fileAssociation.setType(FILE_ASSOCIATION_TYPE);
        fileAssociation.setProjectId(file.getProjectId());
        fileAssociation.setSourceItemId(file.getId());
        fileAssociation.setSourceId(resourceId);
        fileAssociationService.save(fileAssociation);
    }


    /**
     * 保存文件元数据
     *
     * @param fileName
     * @param projectId
     * @return
     */
    private FileMetadata saveFileMetadata(String fileName, String projectId) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataService.getFileMetadataById(fileName);
        if (fileMetadata != null) {
            return fileMetadata;
        }
        String filePath = StringUtils.join(FileUtils.MD_IMAGE_DIR, "/", fileName);
        File file = new File(filePath);

        fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setProjectId(projectId);
        fileMetadata.setStorage(StorageConstants.LOCAL.name());
        fileMetadataService.initBase(fileMetadata);
        fileMetadata.setName(fileName);
        fileMetadata.setId(fileName);
        fileMetadata.setSize(file.getTotalSpace());
        String fileType = MetadataUtils.getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType);
        // latest 为false，项目列表页面不展示
        fileMetadata.setLatest(false);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadata.setPath(filePath);
        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata;
    }

    /**
     * 查询是否还有资源引用，没有则删除
     *
     * @param deleteFileNames
     */
    private void deleteUselessFile(String resourceId, List<String> deleteFileNames) {
        deleteUselessFile(Arrays.asList(resourceId), deleteFileNames);
    }

    /**
     * 查询是否还有资源引用，没有则删除
     *
     * @param deleteFileNames
     */
    private void deleteUselessFile(List<String> resourceIds, List<String> deleteFileNames) {
        if (CollectionUtils.isEmpty(deleteFileNames)) {
            return;
        }

        // 先删除图片与资源的关联关系
        fileAssociationService.deleteByFileIdsAndResourceIds(resourceIds, deleteFileNames);

        // 查询文件是否被其他资源引用
        List<String> retainFileIds = fileAssociationService.getByFileIds(deleteFileNames)
                .stream()
                .map(FileAssociation::getFileMetadataId)
                .toList();

        // 排除被其他资源引用的文件，删除
        deleteFileNames = ListUtils.subtract(deleteFileNames, retainFileIds);
        fileMetadataService.deleteBatch(deleteFileNames);

        deleteFileNames.forEach(deleteFileName -> {
            // 删除文件
            FileUtils.deleteFile(StringUtils.join(FileUtils.MD_IMAGE_DIR, "/", deleteFileName));
        });
    }

    /**
     * 查询是否被其他资源引用，没有则删除
     *
     * @param resourceId
     */
    public void deleteBySourceId(String resourceId) {
        // 查询文件是否被其他资源引用
        List<FileAssociation> deleteFileAssociations = fileAssociationService.getByResourceId(resourceId);
        List<String> fileNames = deleteFileAssociations.stream().map(FileAssociation::getFileMetadataId).toList();
        deleteUselessFile(resourceId, fileNames);
    }

    /**
     * 查询是否被其他资源引用，没有则删除
     *
     * @param resourceIds
     */
    public void deleteBySourceIds(List<String> resourceIds) {
        // 查询文件是否被其他资源引用
        List<FileAssociation> deleteFileAssociations = fileAssociationService.getByResourceIds(resourceIds);
        List<String> fileNames = deleteFileAssociations.stream().map(FileAssociation::getFileMetadataId).toList();
        deleteUselessFile(resourceIds, fileNames);
    }

    public void deleteByProjectId(String projectId) {
        List<String> fileIds = fileAssociationService.getFileIdsByProjectIdAndType(projectId, FILE_ASSOCIATION_TYPE);
        fileIds.forEach(fileId -> {
            // 删除文件
            FileUtils.deleteFile(StringUtils.join(FileUtils.MD_IMAGE_DIR, "/", fileId));
        });

        fileAssociationService.deleteByProjectIdAndType(projectId, FILE_ASSOCIATION_TYPE);
        fileMetadataService.deleteMetadataByIds(fileIds);
    }

    public void deleteTempImages() {
        LogUtil.info("clean temp images===========start");
        File tempDir = new File(FileUtils.MD_IMAGE_TEMP_DIR);
        // 过期时间
        int expirationTime= 1000 * 60 * 60 * 24 * 7;
        for (File file : tempDir.listFiles()) {
            if (System.currentTimeMillis() - file.lastModified() > expirationTime) {
                LogUtil.info("clean temp image:" + file.getName());
                // 如果文件的超过了过期时间，删除临时文件
                FileUtils.deleteFile(file.getAbsolutePath());
            }
        }
        LogUtil.info("clean temp images===========end");
    }
}
