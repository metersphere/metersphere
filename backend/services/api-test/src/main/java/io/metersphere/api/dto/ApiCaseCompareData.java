package io.metersphere.api.dto;

import io.metersphere.plugin.api.spi.MsTestElement;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-02  17:49
 */
@Data
public class ApiCaseCompareData {
    private MsTestElement apiRequest;
    private MsTestElement caseRequest;
}
