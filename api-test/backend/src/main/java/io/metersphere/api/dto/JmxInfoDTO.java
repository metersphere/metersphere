package io.metersphere.api.dto;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author song.tianyang
 * @Date 2021/1/5 5:48 下午
 * @Description
 */
@Getter
@Setter
public class JmxInfoDTO {
    private String name;
    private String xml;
    private String id;
    private Integer version;
    private Map<String, String> attachFiles;
    private List<FileMetadata> fileMetadataList;

    public JmxInfoDTO(String name, String xml, Map<String, String> attachFiles) {
        this.name = StringUtils.replace(name, "/", "");
        this.xml = xml;
        this.attachFiles = attachFiles;
    }

    public void setName(String name) {
        this.name = StringUtils.replace(name, "/", "");
    }

    public void addFileMetadataLists(List<FileMetadata> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> fileMetadataIds = fileMetadataList.stream().map(FileMetadata::getId).collect(Collectors.toList());
            list.forEach(item -> {
                if (!fileMetadataIds.contains(item.getId())) {
                    fileMetadataList.add(item);
                }
            });
        }
    }
}
