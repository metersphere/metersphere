package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
public class FileUpdateRequest {
    @Schema(description = "文件Id")
    @NotBlank(message = "{file_metadata.id.not_blank}")
    private String id;

    @Schema(description = "文件名称")
    @Size(min = 1, max = 255, message = "{file_metadata.name.length_range}")
    private String name;

    //注： tags内的数据确保不重复且有序。所以使用这个数据结构接受
    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
                    String> tags;

    @Schema(description = "文件描述")
    private String description;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "开启/关闭(目前用于jar文件)")
    private Boolean enable;

    public List<String> getTags() {
        if (tags == null) {
            return null;
        } else {
            return new ArrayList<>(tags);
        }
    }
}
