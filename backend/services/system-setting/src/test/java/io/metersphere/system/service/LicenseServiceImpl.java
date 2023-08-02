package io.metersphere.system.service;

import io.metersphere.sdk.dto.LicenseDTO;
import io.metersphere.sdk.service.LicenseService;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceImpl implements LicenseService {

    public synchronized LicenseDTO refreshLicense() {
        LicenseDTO licenseDTO = new LicenseDTO();
        return licenseDTO;
    }

    @Override
    public LicenseDTO validate() {
        LicenseDTO licenseDTO = new LicenseDTO();
        return licenseDTO;
    }

    @Override
    public LicenseDTO addLicense(String licenseCode) {
        LicenseDTO licenseDTO = new LicenseDTO();
        return licenseDTO;
    }


}
