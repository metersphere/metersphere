package io.metersphere.xpack.api.service;

import io.metersphere.plugin.core.MsTestElement;

public interface ApiRetryOnFailureService {
    public String retry(String data, long retryNum, boolean isCase);

    public MsTestElement retryParse(String retryCase);

}
