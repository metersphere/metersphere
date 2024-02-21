package io.metersphere.api.service;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-20  21:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCommonService {
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private FileMetadataService fileMetadataService;
    /**
     * 根据 fileId 查找 MsHTTPElement 中的 ApiFile
     * @param fileId
     * @param msTestElement
     * @return
     */
    public List<ApiFile> getApiFilesByFileId(String fileId, AbstractMsTestElement msTestElement) {
        if (msTestElement instanceof MsHTTPElement httpElement) {
            List<ApiFile> apiFiles = getApiBodyFiles(httpElement.getBody());
            return apiFiles.stream()
                    .filter(file -> StringUtils.equals(fileId, file.getFileId()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<ApiFile> getApiFiles(AbstractMsTestElement msTestElement) {
        if (msTestElement instanceof MsHTTPElement httpElement) {
            return getApiBodyFiles(httpElement.getBody());
        } else {
            return List.of();
        }
    }

    /**
     * 设置关联的文件的最新信息
     * 包括文件别名和是否被删除
     * @param resourceId
     * @param msTestElement
     */
    public void updateLinkFileInfo(String resourceId, AbstractMsTestElement msTestElement) {
        updateLinkFileInfo(resourceId, getApiFiles(msTestElement));
    }

    /**
     * 设置关联的文件的最新信息
     * 包括文件别名和是否被删除
     * @param resourceId
     * @param body
     */
    public void updateLinkFileInfo(String resourceId, Body body) {
        updateLinkFileInfo(resourceId, getApiBodyFiles(body));
    }

    private void updateLinkFileInfo(String resourceId, List<ApiFile> apiFiles) {
        List<ApiFile> linkFiles = apiFiles.stream()
                .filter(file -> !file.getLocal() && !file.getDelete())
                .toList();
        List<String> linkFileIds = linkFiles.stream()
                .map(ApiFile::getFileId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(linkFileIds)) {
            return;
        }

        Map<String, String> fileNameMap = fileMetadataService.selectByList(linkFileIds)
                .stream()
                .collect(Collectors.toMap(FileMetadata::getId, FileMetadata::getName));

        for (ApiFile linkFile : linkFiles) {
            String fileName = fileNameMap.get(linkFile.getFileId());
            if (StringUtils.isBlank(fileName)) {
                // fileName 为空，则文件被删除，设置为已删除，并且设置文件名
                linkFile.setDelete(true);
                List<FileAssociation> fileAssociations = fileAssociationService.getByFileIdAndSourceId(resourceId, linkFile.getFileId());
                if (CollectionUtils.isNotEmpty(fileAssociations)) {
                    linkFile.setFileAlias(fileAssociations.get(0).getDeletedFileName());
                }
            } else {
                linkFile.setFileAlias(fileName);
            }
        }
    }


    /**
     *
     * @param body
     * @return
     */
    public List<ApiFile> getApiBodyFiles(Body body) {
        List<ApiFile> updateFiles = new ArrayList<>(0);
        if (body != null) {
            FormDataBody formDataBody = body.getFormDataBody();
            if (formDataBody != null) {
                List<FormDataKV> formValues = formDataBody.getFormValues();
                if (CollectionUtils.isNotEmpty(formValues)) {
                    formValues.forEach(formDataKV -> {
                        List<ApiFile> files = formDataKV.getFiles();
                        if (CollectionUtils.isNotEmpty(files)) {
                            updateFiles.addAll(files);
                        }
                    });
                }
            }
            BinaryBody binaryBody = body.getBinaryBody();
            if (binaryBody != null && binaryBody.getFile() != null) {
                updateFiles.add(binaryBody.getFile());
            }
        }
        return updateFiles;
    }

    public void replaceApiFileInfo(List<ApiFile> updateFiles, FileMetadata newFileMetadata) {
        for (ApiFile updateFile : updateFiles) {
            updateFile.setFileId(newFileMetadata.getId());
            // todo 重新设置文件名
            // updateFile.setFileName();
        }
    }
}
