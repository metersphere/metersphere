package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试和文件的关联表")
@Table("load_test_file")
@Data
public class LoadTestFile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(min = 1, max = 64, message = "{load_test_file.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_file.test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试ID", required = true, allowableValues = "range[1, 64]")
    private String testId;

    @Size(min = 1, max = 64, message = "{load_test_file.file_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_file.file_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "文件ID", required = true, allowableValues = "range[1, 64]")
    private String fileId;


    @ApiModelProperty(name = "文件排序", required = true, dataType = "Integer")
    private Integer sort;


}