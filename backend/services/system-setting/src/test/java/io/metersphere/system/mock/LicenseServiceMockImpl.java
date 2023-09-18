package io.metersphere.system.mock;

import io.metersphere.sdk.dto.LicenseDTO;
import io.metersphere.system.service.LicenseService;
import org.springframework.stereotype.Service;
@Service
public class LicenseServiceMockImpl implements LicenseService {


    @Override
    public LicenseDTO refreshLicense() {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("OK");
        return licenseDTO;
    }

    @Override
    public LicenseDTO validate() {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("OK");
        return licenseDTO;
    }

    @Override
    public LicenseDTO addLicense(String licenseCode, String userId) {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("OK");
        return licenseDTO;
    }
}
