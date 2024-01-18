package io.metersphere.system.mock;

import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.service.LicenseService;
import org.springframework.stereotype.Service;
@Service
public class LicenseServiceMockImpl implements LicenseService {


    @Override
    public LicenseDTO refreshLicense() {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("valid");
        return licenseDTO;
    }

    @Override
    public LicenseDTO validate() {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("valid");
        return licenseDTO;
    }

    @Override
    public LicenseDTO addLicense(String licenseCode, String userId) {
        LicenseDTO licenseDTO = new LicenseDTO();
        licenseDTO.setStatus("valid");
        return licenseDTO;
    }

    @Override
    public String getCode(String encrypt) {
        return encrypt;
    }
}
