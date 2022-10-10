package io.metersphere.request.attachment;

import io.metersphere.metadata.vo.DumpFileRequest;
import lombok.Data;

@Data
public class AttachmentDumpRequest extends DumpFileRequest {
    private String attachmentId;
}
