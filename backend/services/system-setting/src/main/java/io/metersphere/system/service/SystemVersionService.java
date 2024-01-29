package io.metersphere.system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemVersionService {

    public String getVersion() {
        return System.getenv("MS_VERSION");
    }

    public String getPackageType() {
        return System.getenv("MS_PACKAGE_TYPE");
    }
}
