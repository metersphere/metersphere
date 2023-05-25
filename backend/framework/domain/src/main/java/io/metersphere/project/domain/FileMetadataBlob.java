package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FileMetadataBlob implements Serializable {
    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata_blob.file_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{file_metadata_blob.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(title = "储存库")
    private byte[] gitInfo;

    private static final long serialVersionUID = 1L;
}