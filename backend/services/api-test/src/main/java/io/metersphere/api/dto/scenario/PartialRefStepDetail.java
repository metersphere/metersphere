package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-22  15:17
 */
@Data
public class PartialRefStepDetail {
    /**
     * 记录子步骤中启用的步骤ID
     * 不包含，部分引用的子步骤
     * 部分引用的子步骤也会自行保存子步骤的启用状态
     */
    private Set<String> enableStepIds = new HashSet<>();
    /**
     * 记录子步骤中未启用的步骤ID
     * 不包含，部分引用的子步骤
     * 部分引用的子步骤也会自行保存子步骤的启用状态
     */
    private Set<String> disableStepIds = new HashSet<>();
    // 预留保存其他信息
}
