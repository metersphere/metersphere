package io.metersphere.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/1/5 5:48 下午
 * @Description
 */
@Getter
@Setter
@AllArgsConstructor
public class JmxInfoDTO {
    private String name;
    private String xml;
    private Map<String, String> attachFiles;
}
