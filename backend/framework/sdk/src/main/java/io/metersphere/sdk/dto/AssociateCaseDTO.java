package io.metersphere.sdk.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class AssociateCaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //排除的ids
    private List<String> excludeIds;

    //选中的ids
    private List<String> selectIds;

    //全选的模块
    public List<String> moduleIds;

    public AssociateCaseDTO(List<String> excludeIds, List<String> selectIds, List<String> moduleIds) {
        this.excludeIds = excludeIds;
        this.selectIds = selectIds;
        this.moduleIds = moduleIds;
    }
}
