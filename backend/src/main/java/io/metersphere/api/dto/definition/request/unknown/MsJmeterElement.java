package io.metersphere.api.dto.definition.request.unknown;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 暂时存放所有未知的Jmeter Element对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JmeterElement")
public class MsJmeterElement extends MsTestElement {
    private String type = "JmeterElement";

    private Object jmeterElement;
    
}
