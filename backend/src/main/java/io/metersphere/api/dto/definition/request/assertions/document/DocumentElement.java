package io.metersphere.api.dto.definition.request.assertions.document;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Data
public class DocumentElement {
    private String id;
    private String name;
    private boolean include;
    private String status;
    private boolean typeVerification;
    private String type;
    private String groupId;
    private int rowspan;
    private boolean arrayVerification;
    private String contentVerification;
    private Object expectedOutcome;

    private List<DocumentElement> children;

    // 候补两个属性，在执行时组装数据用
    private String jsonPath;
    List<String> conditions;

    public DocumentElement() {

    }

    public DocumentElement(String name, String type, Object expectedOutcome, List<DocumentElement> children) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.expectedOutcome = expectedOutcome;
        this.type = type;
        this.children = children == null ? this.children = new LinkedList<>() : children;
        this.rowspan = 1;
        this.contentVerification = "value_eq";
        if (StringUtils.equalsAny(type, "object", "array")) {
            this.contentVerification = "none";
        } else if (expectedOutcome == null || StringUtils.isEmpty(expectedOutcome.toString())) {
            this.contentVerification = "none";
        }
    }

    public DocumentElement(String name, String type, Object expectedOutcome, boolean include, List<DocumentElement> children) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.expectedOutcome = expectedOutcome;
        this.type = type;
        this.include = include;
        this.children = children == null ? this.children = new LinkedList<>() : children;
        this.rowspan = 1;
        this.contentVerification = "value_eq";
        if (StringUtils.equalsAny(type, "object", "array")) {
            this.contentVerification = "none";
        } else if (expectedOutcome == null || StringUtils.isEmpty(expectedOutcome.toString())) {
            this.contentVerification = "none";
        }
    }


    public DocumentElement(String name, String type, Object expectedOutcome, boolean typeVerification, boolean arrayVerification, List<DocumentElement> children) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.expectedOutcome = expectedOutcome;
        this.type = type;
        this.typeVerification = typeVerification;
        this.arrayVerification = arrayVerification;
        this.children = children == null ? this.children = new LinkedList<>() : children;
        this.rowspan = 1;
        this.contentVerification = "value_eq";
        if (StringUtils.equalsAny(type, "object", "array")) {
            this.contentVerification = "none";
        } else if (expectedOutcome == null || StringUtils.isEmpty(expectedOutcome.toString())) {
            this.contentVerification = "none";
        }
    }


    public DocumentElement(String id, String name, String type, Object expectedOutcome, List<DocumentElement> children) {
        this.id = id;
        this.name = name;
        this.expectedOutcome = expectedOutcome;
        this.type = type;
        this.children = children == null ? this.children = new LinkedList<>() : children;
        this.rowspan = 1;
        this.contentVerification = "value_eq";
        if (StringUtils.equalsAny(type, "object", "array")) {
            this.contentVerification = "none";
        } else if (expectedOutcome == null || StringUtils.isEmpty(expectedOutcome.toString())) {
            this.contentVerification = "none";
        }
    }

    public DocumentElement newRoot(String type, List<DocumentElement> children) {
        return new DocumentElement("root", "root", type, "", children);
    }

    public String getLabel(String value) {
        String label = "";
        switch (value) {
            case "value_eq":
                label = "值-等于[value='%']";
                break;
            case "value_not_eq":
                label = "值-不等于[value!='%']";
                break;
            case "value_in":
                label = "值-包含[include='%']";
                break;
            case "length_eq":
                label = "长度-等于[length='%']";
                break;
            case "length_not_eq":
                label = "长度-不等于[length!='%']";
                break;
            case "length_gt":
                label = "长度-大于[length>'%']";
                break;
            case "length_lt":
                label = "长度-小于[length<'%']";
                break;
            case "regular":
                label = "正则匹配";
                break;
            default:
                label = "不校验[]";
                break;
        }
        return label;
    }
}
