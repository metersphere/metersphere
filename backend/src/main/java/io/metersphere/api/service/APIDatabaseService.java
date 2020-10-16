package io.metersphere.api.service;

import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;

@Service
@Transactional(rollbackFor = Exception.class)
public class APIDatabaseService {

    public void validate(DatabaseConfig databaseConfig) {
        try {
            DriverManager.getConnection(databaseConfig.getDbUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }
}
