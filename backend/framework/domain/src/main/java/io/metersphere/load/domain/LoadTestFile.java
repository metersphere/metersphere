package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestFile implements Serializable {
    @Schema(title = "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_file.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_file.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_file.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_file.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(title = "文件排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_file.sort.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{load_test_file.sort.length_range}", groups = {Created.class, Updated.class})
    private Integer sort;

    private static final long serialVersionUID = 1L;
}