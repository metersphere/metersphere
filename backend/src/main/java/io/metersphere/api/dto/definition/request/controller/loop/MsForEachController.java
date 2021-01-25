package io.metersphere.api.dto.definition.request.controller.loop;

import lombok.Data;

@Data
public class MsForEachController {
    private String inputVal;
    private String returnVal;
    private String interval;
    private Object requestResult;

}
