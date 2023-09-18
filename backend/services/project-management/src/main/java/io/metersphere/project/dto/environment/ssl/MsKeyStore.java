package io.metersphere.project.dto.environment.ssl;

import lombok.Data;

@Data
public class MsKeyStore {
    private String id;
    private String password;
    private String path;

    public MsKeyStore() {
    }

    public MsKeyStore(String path, String password) {
        this.password = password;
        this.path = path;
    }
}
