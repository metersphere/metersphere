package io.metersphere.api.dto;

import io.metersphere.dto.RequestResult;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

@Data
public class StepTreeDTO {
    private String type;
    private int index;
    private String resourceId;
    private String label;
    private RequestResult value;
    private String allIndex;

    //误报库编码
    private String errorCode;

    private List<StepTreeDTO> children;

    public StepTreeDTO() {

    }

    public StepTreeDTO(String name, String resourceId, String type, int index) {
        this.label = StringUtils.isNotEmpty(name) ? name : type;
        this.resourceId = resourceId;
        this.type = type;
        this.index = index;
        this.children = new LinkedList<>();
    }
}
