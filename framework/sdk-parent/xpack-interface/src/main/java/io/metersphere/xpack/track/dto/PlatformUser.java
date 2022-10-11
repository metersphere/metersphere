package io.metersphere.xpack.track.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlatformUser {
    private List<String> roleId;
    private String name;
    private String user;
}
