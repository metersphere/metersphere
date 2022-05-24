package io.metersphere.api.dto;

import lombok.Data;

@Data
public class UiExecutionQueueParam {
    private String browser;
    private Boolean headlessEnabled = false;
}
