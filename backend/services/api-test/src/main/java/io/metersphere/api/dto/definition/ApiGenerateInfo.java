package io.metersphere.api.dto.definition;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiGenerateInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否生成接口用例
     */
    private Boolean generateCase;
    /**
     * 生成用例的条数
     */
    private Integer generateCaseCount;
}
