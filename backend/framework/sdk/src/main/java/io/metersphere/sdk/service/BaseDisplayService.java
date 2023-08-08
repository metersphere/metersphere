package io.metersphere.sdk.service;


import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface BaseDisplayService {
    ResponseEntity<byte[]> getFile(String fileName) throws IOException;
}
