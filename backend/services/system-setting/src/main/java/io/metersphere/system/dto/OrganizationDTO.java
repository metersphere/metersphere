package io.metersphere.system.dto;

import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.User;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
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
    @Schema(description =  "成员数量")
    private Integer memberCount;

    /**
     * 项目数量
     */
    @Schema(description =  "项目数量" )
    private Integer projectCount;

    /**
     * 列表组织管理员集合
     */
    @Schema(description =  "列表组织管理员集合")
    private List<User> orgAdmins;

    /**
     * 组织管理员ID集合(新增, 编辑), 必填
     */
    @Schema(description =  "组织管理员ID集合")
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{member.id.not_empty}")
    private List<String> userIds;

    /**
     * 创建人是否是管理员
     */
    @Schema(description =  "创建人是否是管理员")
    private Boolean orgCreateUserIsAdmin;

    /**
     * 剩余删除保留天数
     */
    @Schema(description =  "剩余删除保留天数")
    private Integer remainDayCount;

    /**
     * 是否可以进入组织
     */
    @Schema(description = "是否切换并进入")
    private Boolean switchAndEnter;
}
