package io.metersphere.api.exec.api;

import io.metersphere.plugin.core.MsTestElement;

public interface ApiRetryOnFailureService {
    public String retry(String data, long retryNum, boolean isCase);

    public MsTestElement retryParse(String retryCase);

}
