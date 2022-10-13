package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileInfoDTO {
    private String id;
    private String fileName;
    private String storage;
    private String path;
    private byte[] fileByte;
}
