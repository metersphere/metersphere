package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class FileAssociation implements Serializable {
    private String id;

    private String type;

    private String sourceId;

    private String sourceItemId;

    private String fileMetadataId;

    private String fileType;

    private String projectId;

    private static final long serialVersionUID = 1L;
}