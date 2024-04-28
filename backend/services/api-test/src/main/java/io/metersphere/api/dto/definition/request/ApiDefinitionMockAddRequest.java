package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author lan
 */
@Data
public class ApiDefinitionMockAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}")
    private String projectId;

    @Schema(description = "接口 mock 名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_definition_mock.name.length_range}")
    private String name;

    @Schema(description = "响应码")
    private int statusCode;

    @Schema(description = "标签")
    private LinkedHashSet<
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;

    @Schema(description = "请求内容")
    private MockMatchRule mockMatchRule;

    @Schema(description = "请求内容")
    private MockResponse response;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.api_definition_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}")
    private String apiDefinitionId;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
