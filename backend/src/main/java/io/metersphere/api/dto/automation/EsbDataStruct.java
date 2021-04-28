package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private String uuid;
    private String name;
    private String value;
    private String type;
    private String systemName;
    private String contentType;
    private boolean required;
    private String description;
    private List<EsbDataStruct> children;
    private String status = "";

    public void init(){
        this.uuid = UUID.randomUUID().toString();
        this.systemName = "";
        this.description = "";
        this.value="";
        this.required = true;
        this.contentType = "";
        this.type = "";
        this.children = new ArrayList<>();
    }

    public boolean initDefaultData(String name, String typeLength,String chineseName,String desc){
        this.init();
        if(StringUtils.isEmpty(name)){return  false; }
        if(typeLength == null){
            typeLength = "";
        }else{
            typeLength = typeLength.trim();
            typeLength = typeLength.toLowerCase();
        }

        this.name = name;

        if(typeLength.startsWith("string")){
            this.type = "string";
            String lengthStr = typeLength.substring(6);
            if(lengthStr.startsWith("(") && lengthStr.endsWith(")")){
                try {
                    int length = Integer.parseInt(lengthStr.substring(1,lengthStr.length()-1));
                    this.contentType = String.valueOf(length);
                }catch (Exception e){ }

            }
        }else if(typeLength.startsWith("array")){
            this.type = "array";
        }else{
            this.type = "object";
        }

        if(StringUtils.isEmpty(desc)){desc = "";}
        if(StringUtils.isNotEmpty(chineseName)){
            this.description = chineseName+":"+desc;
        }else{
            this.description =desc;
        }

        if(this.description.endsWith(":")){
            this.description = this.description.substring(0,this.description.length()-1);
        }

        return  true;
    }

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
                if(this.value == null ){
                    this.value = "";
                }
                element.addText(this.value);
            } else {
                for (EsbDataStruct child : children) {
                    child.genXmlElementByChildren(element);
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
