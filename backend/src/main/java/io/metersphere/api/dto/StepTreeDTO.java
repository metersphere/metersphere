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
    /**
     * 该步骤的状态（RequestResult为空时，通过子步骤的结果来判断）
     * 当有多个子步骤结果时，如果当前步骤不是场景，则：失败>误报>未执行>成功>未执行； 如果是场景：误报>失败>成功>未执行
     */
    private String totalStatus;

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
