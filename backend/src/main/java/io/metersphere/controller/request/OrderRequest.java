package io.metersphere.controller.request;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
public class OrderRequest {
    private String name;
    private String type;
    // 表前缀
    private String prefix;


    public String getName() {
        if (checkSqlInjection(name)) {
            return "1";
        }
        return name;
    }

    public String getType() {
        if (StringUtils.equalsIgnoreCase(type, "asc")) {
            return "ASC";
        } else {
            return "DESC";
        }
    }

    public String getPrefix() {
        if (checkSqlInjection(prefix)) {
            return "";
        }
        return prefix;
    }


    public static boolean checkSqlInjection(String script) {
        if (StringUtils.isEmpty(script)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+$");
        Matcher matcher = pattern.matcher(script.toLowerCase());
        return !matcher.find();
    }
}
