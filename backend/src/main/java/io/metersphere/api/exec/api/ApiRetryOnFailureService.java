package io.metersphere.api.exec.api;

public interface ApiRetryOnFailureService {
    public String retry(String data, long retryNum);
}
