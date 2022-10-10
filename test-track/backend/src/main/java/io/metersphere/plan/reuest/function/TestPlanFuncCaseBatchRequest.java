package io.metersphere.plan.reuest.function;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/7 10:30 上午
 * @Description
 */
@Getter
@Setter
public class TestPlanFuncCaseBatchRequest {
    private List<String> ids;
    private TestPlanFuncCaseConditions condition;
}
