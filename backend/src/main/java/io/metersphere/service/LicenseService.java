package io.metersphere.service;

import io.metersphere.dto.LicenseDTO;

public interface LicenseService {

    public LicenseDTO valid();

    public LicenseDTO addValidLicense(String reqLicenseCode);
}
