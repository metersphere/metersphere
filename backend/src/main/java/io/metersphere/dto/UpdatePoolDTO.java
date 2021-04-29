package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePoolDTO {

    /**
     * 禁用资源池时，"；" 连接多个使用该资源池的性能测试名称
     */
    private String testName;
    private Boolean haveTestUsePool = false;
}
