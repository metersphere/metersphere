package io.metersphere.log.utils;

public interface ApiDefinitionDiffUtil {

    public String diffResponse(String newValue, String oldValue);

    public String diff(String newValue, String oldValue) ;

}
