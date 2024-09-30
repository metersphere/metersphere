package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
public class ModuleSelectDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否选择当前模块下的全部数据")
    private boolean selectAll;

    @Schema(description = "不处理的ID")
    List<String> excludeIds = new ArrayList<>();

    @Schema(description = "选中的ID")
    List<String> selectIds = new ArrayList<>();
}
