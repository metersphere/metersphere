package io.metersphere.api.dto.scenario;

import lombok.Data;

@Data
public class DatabaseConfig {

    private String id;
    private String name;
    private long poolMax;
    private long timeout;
    private String driver;
    private String dbUrl;
    private String username;
    private String password;
}
