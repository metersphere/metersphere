package io.metersphere.api.dto.ssl;

import io.metersphere.api.dto.scenario.request.BodyFile;
import lombok.Data;

@Data
public class KeyStoreFile {
    private String id;
    private String name;
    private String type;
    private String updateTime;
    private String password;
    private BodyFile file;

}
