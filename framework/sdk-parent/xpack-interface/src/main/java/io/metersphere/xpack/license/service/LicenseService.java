package io.metersphere.xpack.license.service;

import io.metersphere.xpack.license.dto.LicenseDTO;

public interface LicenseService {

    LicenseDTO refreshLicense();

    LicenseDTO validate();

}
