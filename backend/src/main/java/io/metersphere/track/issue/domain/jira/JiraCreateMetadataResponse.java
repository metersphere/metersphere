package io.metersphere.track.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class JiraCreateMetadataResponse {
    private List<Projects> projects;

    @Setter
    @Getter
    public static class Projects {
        private List<Issuetypes> issuetypes;
    }

    @Setter
    @Getter
    public static class Issuetypes {
        private Map<String, Field> fields;
    }

    @Setter
    @Getter
    public static class Field {
        private boolean required;
        private Schema schema;
        private String name;
        private String key;
        private String autoCompleteUrl;
        private boolean hasDefaultValue;
        private Object defaultValue;
        private List<AllowedValues> allowedValues;
    }

    @Setter
    @Getter
    public static class Schema {
        private String type;
        private String items;
        private String custom;
        private int customId;
    }

    @Setter
    @Getter
    public static class AllowedValues {
        private String self;
        private String id;
        private String description;
        private String name;
        private String value;
        private boolean subtask;
        private int avatarId;
        private int hierarchyLevel;
        private List<AllowedValues> children;
    }
}
