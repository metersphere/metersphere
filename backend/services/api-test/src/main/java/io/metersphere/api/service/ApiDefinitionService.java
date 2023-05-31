package io.metersphere.api.service;

import io.metersphere.api.dto.ApiDefinitionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ApiDefinitionService {

    public ApiDefinitionDTO create(ApiDefinitionDTO request, List<MultipartFile> bodyFiles) {
        return request;
    }
}
