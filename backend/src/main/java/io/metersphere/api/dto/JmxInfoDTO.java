package io.metersphere.api.dto;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
    private Map<String, String> attachFiles;
    private List<FileMetadata> fileMetadataList;

    public JmxInfoDTO(String name,String xml,Map<String, String> attachFiles){
        this.name = name;
        this.xml = xml;
        this.attachFiles = attachFiles;
    }
}
