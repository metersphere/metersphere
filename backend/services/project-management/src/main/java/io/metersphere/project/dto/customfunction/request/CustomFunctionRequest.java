package io.metersphere.project.dto.customfunction.request;

import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
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
 * @author: LAN
 * @date: 2024/1/9 19:51
 * @version: 1.0
 */
@Data
public class CustomFunctionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID")
    @NotBlank(message = "{custom_function.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{custom_function.project_id.length_range}")
    private String projectId;

    @Schema(description =  "函数名")
    @NotBlank(message = "{custom_function.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_function.name.length_range}")
    private String name;

    @Schema(description = "脚本语言类型")
    @EnumValue(enumClass = ScriptLanguageType.class)
    private String type;

    @Schema(description = "脚本状态（草稿/测试通过）")
    private String status;

    @Schema(description =  "标签")
    private LinkedHashSet<@NotBlank String> tags;

    @Schema(description =  "函数描述")
    private String description;

    @Schema(description =  "参数列表")
    private String params;

    @Schema(description =  "函数体")
    private String script;

    @Schema(description =  "执行结果")
    private String result;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }
}
