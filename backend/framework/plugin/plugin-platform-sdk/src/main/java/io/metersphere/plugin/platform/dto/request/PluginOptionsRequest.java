package io.metersphere.plugin.platform.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wx
 */
@Setter
@Getter
public class PluginOptionsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectConfig;
    private String optionMethod;

}
