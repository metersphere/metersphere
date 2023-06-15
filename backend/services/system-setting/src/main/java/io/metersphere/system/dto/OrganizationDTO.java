package io.metersphere.system.dto;

import io.metersphere.system.domain.Organization;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationDTO extends Organization {

    /**
     * 成员数量
     */
    @Schema(title = "成员数量")
    private Integer memberCount;

    /**
     * 项目数量
     */
    @Schema(title = "项目数量" )
    private Integer projectCount;

    /**
     * 成员ID集合
     */
    @Schema(title = "成员ID集合")
    @NotEmpty(groups = {Created.class}, message = "{member.id.not_empty}")
    private List<String> memberIds;
}
