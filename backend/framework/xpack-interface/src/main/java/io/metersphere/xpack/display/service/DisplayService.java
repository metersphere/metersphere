package io.metersphere.xpack.display.service;

import io.metersphere.xpack.display.dto.DisplayDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DisplayService {

    void save(List<DisplayDTO> requests, List<MultipartFile> files);

    List<DisplayDTO> uiInfo(String type);
}
