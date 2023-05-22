
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "接口用例详情")
@TableName("api_test_case_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiTestCaseBlob extends ApiTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "请求内容", required = false, dataType = "byte[]")
    private byte[] request;

}