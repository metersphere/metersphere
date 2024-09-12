package io.metersphere.system.dto.sdk;

import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  17:31
 * 封装 mapper 查询需要的条件
 * 搭配 BaseConditionFilter 和 BaseConditionFilterAspect 使用
 */
@Data
public class DBCombineSearch extends CombineSearch {
    /**
     * 视图的查询条件
     */
    private List<CombineCondition> userViewConditions;
    /**
     * 系统字段筛选条件
     */
    private List<CombineCondition> systemFieldConditions;
    /**
     * 自定义字段为空的筛选条件
     */
    private List<CombineCondition> customFiledEmptyConditions;
    /**
     * 自定义字段中 NOT_IN NOT_EQUALS NOT_CONTAINS COUNT_LT 的条件
     */
    private List<CombineCondition> customFiledNoneConditions;
    /**
     * 其他的自定义字段筛选条件
     */
    private List<CombineCondition> customFiledConditions;
}
