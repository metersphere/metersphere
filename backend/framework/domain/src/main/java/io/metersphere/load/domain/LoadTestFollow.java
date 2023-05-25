package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestFollow implements Serializable {
    @Schema(title = "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_follow.test_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_follow.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(title = "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_follow.follow_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    private String followId;

    private static final long serialVersionUID = 1L;
}