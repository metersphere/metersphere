package io.metersphere.project.dto.environment.ssl;


import io.metersphere.project.dto.environment.BodyFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KeyStoreFile {
    @Schema(description = "证书id")
    private String id;
    @Schema(description = "证书名称")
    private String name;
    @Schema(description = "证书类型")
    private String type;
    @Schema(description = "修改时间")
    private String updateTime;
    @Schema(description = "证书密码")
    private String password;
    @Schema(description = "证书文件")
    private BodyFile file;

}
