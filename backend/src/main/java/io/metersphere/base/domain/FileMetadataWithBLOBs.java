package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileMetadataWithBLOBs extends FileMetadata implements Serializable {
    private String description;

    private String attachInfo;

    private static final long serialVersionUID = 1L;
}