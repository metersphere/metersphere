package io.metersphere.api.dto.request;

import io.metersphere.system.dto.sdk.request.PosRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiEditPosRequest extends PosRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id  模块树列表拖拽的时候 这个字段必传 ,其他情况不传")
    private String moduleId;

}
