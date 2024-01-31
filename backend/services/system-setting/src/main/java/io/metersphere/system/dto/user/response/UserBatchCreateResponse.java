package io.metersphere.system.dto.user.response;

import io.metersphere.system.dto.user.UserCreateInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserBatchCreateResponse {
    @Schema(description = "成功创建的数据")
    List<UserCreateInfo> successList;
    @Schema(description = "邮箱异常数据")
    Map<String, String> errorEmails;
}
