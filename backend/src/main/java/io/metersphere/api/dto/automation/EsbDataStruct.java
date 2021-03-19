package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * //ESB数据格式
 *
 * @author song.tianyang
 * @Date 2021/3/15 4:37 下午
 * @Description
 */
@Getter
@Setter
public class EsbDataStruct {
    private String name;
    private String value;
    private String type;
    private String systemName;
    private String contentType;
    private String required;
    private String description;
    private List<EsbDataStruct> children;

    public EsbDataStruct copy(boolean copyChildren) {
        EsbDataStruct returnObj = new EsbDataStruct();
        returnObj.name = this.name;
        returnObj.value = this.value;
        returnObj.type = this.type;
        returnObj.systemName = this.systemName;
        returnObj.contentType = this.contentType;
        returnObj.required = this.required;
        returnObj.description = this.description;
        if (copyChildren) {
            returnObj.children = this.children;
        } else {
            returnObj.children = new ArrayList<>();
        }
        return returnObj;
    }

    public Element genXmlElementByChildren(Element document) {
        this.name = this.name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("©", "&apos;");
        if (StringUtils.isEmpty(this.name)) {
            return null;
        }

        Element element = null;
        try {
            element = document.addElement(this.name);
            if (StringUtils.equalsAnyIgnoreCase(type, "string", "array")) {
                long lengthNum = Long.parseLong(this.contentType);
                String attrString = "";
                if (StringUtils.equalsIgnoreCase(this.type, "string")) {
                    attrString = "s," + contentType;
                } else if (StringUtils.equalsIgnoreCase(this.type, "array")) {
                    attrString = "a," + contentType;
                }
                element.addAttribute("attr", attrString);
            }
        } catch (Exception e) {
            System.out.println(this.name);
            e.printStackTrace();
        }

        if (element != null) {
            if (this.children == null || this.children.isEmpty()) {
                element.addText(this.value);
            } else {
                for (EsbDataStruct child : children) {
                    child.genXmlElementByChildren(document);
                }
            }
        }
        return element;
    }

    public Element genXmlElementByDocument(Document document) {
        this.name = this.name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("©", "&apos;");
        if (StringUtils.isEmpty(this.name)) {
            return null;
        }

        Element element = null;
        try {
            element = document.addElement(this.name);
            if (StringUtils.equalsAnyIgnoreCase(type, "string", "array")) {
                long lengthNum = Long.parseLong(this.contentType);
                String attrString = "";
                if (StringUtils.equalsIgnoreCase(this.type, "string")) {
                    attrString = "s," + contentType;
                } else if (StringUtils.equalsIgnoreCase(this.type, "array")) {
                    attrString = "a," + contentType;
                }
                element.addAttribute("attr", attrString);
            }
        } catch (Exception e) {
            System.out.println(this.name);
            e.printStackTrace();
        }

        if (element != null) {
            if (this.children == null || this.children.isEmpty()) {
                element.addText(this.value);
            } else {
                for (EsbDataStruct child : children) {
                    child.genXmlElementByChildren(element);
                }
            }
        }
        return element;
    }
}
