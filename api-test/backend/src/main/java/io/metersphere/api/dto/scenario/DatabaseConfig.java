package io.metersphere.api.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
