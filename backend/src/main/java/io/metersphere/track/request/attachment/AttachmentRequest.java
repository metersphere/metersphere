package io.metersphere.track.request.attachment;

import lombok.Data;

import java.util.List;


/**
 * @author songcc
 */
@Data
public class AttachmentRequest {

    private String belongType;

    private String belongId;

    private String copyBelongId;

    private List<String> metadataRefIds;
}
