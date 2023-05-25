package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserKey implements Serializable {
    @Schema(title = "user_key ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{user_key.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_key.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "access_key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.access_key.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_key.access_key.length_range}", groups = {Created.class, Updated.class})
    private String accessKey;

    @Schema(title = "secret key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_key.secret_key.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_key.secret_key.length_range}", groups = {Created.class, Updated.class})
    private String secretKey;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "状态")
    private String status;

    private static final long serialVersionUID = 1L;
}