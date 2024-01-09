package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApiTestCaseUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用例Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.project_id.length_range}")
    private String id;

    @Schema(description = "用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}")
    private String name;

    @Schema(description = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}")
    private String priority;

    @Schema(description = "用例状态  Underway Prepare Completed", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}")
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}")
    private String status;

    @Schema(description = "标签")
    private List<
            @NotBlank
                    String> tags;

    @Schema(description = "环境fk")
    private String environmentId;

    @Schema(description = "请求内容")
    @NotBlank
    private String request;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

    /**
     * 新关联文件管理的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;

    /**
     * 删除本地上传的文件ID
     */
    @Schema(description = "删除的文件ID")
    private List<String> deleteFileIds;

    /**
     * 删除关联的文件ID
     */
    @Schema(description = "取消关联文件ID")
    private List<String> unLinkRefIds;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
