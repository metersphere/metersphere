package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestFollower implements Serializable {
    @Schema(description =  "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_follower.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_follower.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(description =  "关注人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_follower.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_follower.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}