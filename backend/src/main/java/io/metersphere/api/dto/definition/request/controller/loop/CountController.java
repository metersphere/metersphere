package io.metersphere.api.dto.definition.request.controller.loop;

import lombok.Data;

@Data
public class CountController {
    private int loops;
    private int interval;
    private boolean proceed;
    private Object requestResult;

}
