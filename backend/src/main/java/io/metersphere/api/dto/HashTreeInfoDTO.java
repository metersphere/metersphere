package io.metersphere.api.dto;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Getter
@Setter
public class HashTreeInfoDTO {
    private String jmx;
    private HashTree hashTree;
    private List<FileMetadata> repositoryFiles;
}
