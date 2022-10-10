package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttachmentModuleRelation implements Serializable {
    private String relationId;

    private String relationType;

    private String attachmentId;

    private String fileMetadataRefId;

    private static final long serialVersionUID = 1L;
}