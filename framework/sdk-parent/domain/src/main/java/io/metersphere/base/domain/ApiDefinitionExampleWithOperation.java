package io.metersphere.base.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/7/5 5:43 下午
 */
@Getter
@Setter
public class ApiDefinitionExampleWithOperation extends ApiDefinitionExample {
    private String operator;
    private Long operationTime;
}
