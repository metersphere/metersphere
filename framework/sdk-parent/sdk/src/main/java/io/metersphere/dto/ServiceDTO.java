package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceDTO {
    private String serviceId;
    private int port;
}
