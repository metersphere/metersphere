package io.metersphere.project.invoker;

import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-20  18:55
 */
@Component
public class FileAssociationUpdateServiceInvoker implements FileAssociationUpdateService {

    private final List<FileAssociationUpdateService> fileAssociationUpdateServices;

    @Autowired
    public FileAssociationUpdateServiceInvoker(List<FileAssociationUpdateService> services) {
        this.fileAssociationUpdateServices = services;
    }

    @Override
    public void handleUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        this.fileAssociationUpdateServices.forEach(service -> service.handleUpgrade(originFileAssociation, newFileMetadata));
    }
}
