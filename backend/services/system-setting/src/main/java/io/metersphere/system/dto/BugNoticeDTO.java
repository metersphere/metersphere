package io.metersphere.system.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugNoticeDTO {

    @Schema(description ="message.domain.bug_num")
    private String id;

    @Schema(description ="message.domain.bug_title")
    private String title;

    @Schema(description ="message.domain.bug_handleUser")
    private String handleUser;

    @Schema(description ="message.domain.bug_status")
    private String status;

    @Schema(description = "message.domain.bug_createUser")
    private String createUser;

    @Schema(description = "message.domain.bug_updateUser")
    private String updateUser;

    @Schema(description = "message.domain.bug_deleteUser")
    private String deleteUser;

    @Schema(description = "message.domain.bug_createTime")
    private Long createTime;

    @Schema(description = "message.domain.bug_updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.bug_deleteTime")
    private Long deleteTime;

    @Schema(description = "自定义字段内容")
    private List<OptionDTO> customFields;
}
