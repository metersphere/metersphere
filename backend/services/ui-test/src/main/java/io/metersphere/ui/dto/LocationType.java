package io.metersphere.ui.dto;

public enum LocationType {

    ID("id"),
    NAME("name"),
    CLASS_NAME("className"),
    TAG_NAME("tagName"),
    LINK_TEXT("linkText"),
    PARTIAL_LINK_TEXT("partialLinkText"),
    CSS("css"),
    XPATH("xpath"),
    LABEL("label"),
    VALUE("value"),
    INDEX("index");

    private String name;

    public String getName() {
        return name;
    }

    LocationType(String name) {
        this.name = name;
    }

}
