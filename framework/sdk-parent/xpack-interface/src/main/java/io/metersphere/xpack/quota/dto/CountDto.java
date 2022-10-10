package io.metersphere.xpack.quota.dto;

import lombok.Data;

@Data
public class CountDto {
    private Integer count = 0;
    private String sourceId;
}
