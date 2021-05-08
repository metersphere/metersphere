package io.metersphere.api.dto.ssl;

import lombok.Data;

@Data
public class KeyStoreEntry {
    private String id;
    private String originalAsName;
    private String newAsName;
    private String type;
    private String password;
    private String sourceName;
    private String sourceId;
    private boolean isDefault;
}
