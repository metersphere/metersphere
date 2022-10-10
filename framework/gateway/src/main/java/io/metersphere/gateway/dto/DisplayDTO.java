package io.metersphere.gateway.dto;

import io.metersphere.base.domain.SystemParameter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DisplayDTO extends SystemParameter {
    private MultipartFile file;
    private String fileName;
}
