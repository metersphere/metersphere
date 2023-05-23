package io.metersphere.load.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@ApiModel(value = "关联场景测试和性能测试")
@Table("load_test_api")
@Data
public class LoadTestApi implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{load_test_api.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 255, message = "{load_test_api.api_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_api.api_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口场景或用例ID", required = true, allowableValues="range[1, 255]")
    private String apiId;
    
    @Size(min = 1, max = 50, message = "{load_test_api.load_test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_api.load_test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "性能测试ID", required = true, allowableValues="range[1, 50]")
    private String loadTestId;
    
    
    @ApiModelProperty(name = "环境ID", required = false, allowableValues="range[1, 50]")
    private String envId;
    
    @Size(min = 1, max = 20, message = "{load_test_api.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_api.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "类型: SCENARIO, CASE", required = true, allowableValues="range[1, 20]")
    private String type;
    
    
    @ApiModelProperty(name = "关联版本", required = false, dataType = "Integer")
    private Integer apiVersion;
    

}