package io.metersphere.track.request.attachment;

import lombok.Data;


/**
 * @author songcc
 */
@Data
public class AttachmentRequest {

    private String belongType;

    private String belongId;

    private String copyBelongId;
}
