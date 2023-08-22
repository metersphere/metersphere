package io.metersphere.sdk.service;


import io.metersphere.sdk.dto.LicenseDTO;

public interface LicenseService {

    LicenseDTO refreshLicense();

    LicenseDTO validate();

    LicenseDTO addLicense(String licenseCode, String userId);

}
