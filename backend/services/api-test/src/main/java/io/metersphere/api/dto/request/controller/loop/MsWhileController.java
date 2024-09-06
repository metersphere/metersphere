package io.metersphere.api.dto.request.controller.loop;

import io.metersphere.api.dto.request.controller.WhileConditionType;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

@Data
public class MsWhileController {
    /**
     * 变量对象
     */
    private MsWhileVariable msWhileVariable;
    /**
     * 脚本对象
     */
    private MsWhileScript msWhileScript;
    /**
     * 超时时间
     */
    private long timeout = 3000;
    /**
     * 条件类型
     *
     * @see io.metersphere.api.dto.request.controller.WhileConditionType
     */
    @EnumValue(enumClass = WhileConditionType.class)
    private String conditionType = WhileConditionType.CONDITION.name();

}

