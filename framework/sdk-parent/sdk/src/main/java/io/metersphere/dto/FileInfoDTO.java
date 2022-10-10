package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileInfoDTO {
    private String id;
    private String fileName;
    private String storage;
    private byte[] fileByte;
}
