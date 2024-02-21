package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;

@Data
public class ResponseBinaryBody extends ApiFile {
    private boolean sendAsBody;
    private String description;
}
