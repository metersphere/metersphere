package io.metersphere.metadata.vo;

import lombok.Data;

@Data
public class AttachmentDumpRequest extends DumpFileRequest{
    private String attachmentId;
}
