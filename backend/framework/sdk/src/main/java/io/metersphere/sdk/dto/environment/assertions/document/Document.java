package io.metersphere.sdk.dto.environment.assertions.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class Document {
    @Schema(description = "JSON跟随API 这里的是id")
    private String jsonFollowAPI;
    @Schema(description = "XML跟随API")
    private String xmlFollowAPI;
    @Schema(description = "JSON内容")
    private List<DocumentElement> json;
    @Schema(description = "XML内容")
    private List<DocumentElement> xml;
    @Schema(description = "是否包含")
    private Boolean include = false;
    @Schema(description = "是否包含类型")
    private Boolean typeVerification = false;



}
