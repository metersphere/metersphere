package io.metersphere.track.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JiraTransitionsResponse {

    private String expand;
    private List<Transitions> transitions;

    @Getter
    @Setter
    public static class Transitions {
        private String id;
        private String name;
        private To to;
        private Boolean hasScreen;
        private Boolean isGlobal;
        private Boolean isInitial;
        private Boolean isAvailable;
        private Boolean isConditional;
        private Boolean isLooped;

    }

    @Getter
    @Setter
    public static class To {
        private String self;
        private String description;
        private String iconUrl;
        private String name;
        private String id;
        private StatusCategory statusCategory;
    }

    @Getter
    @Setter
    public static class StatusCategory {
        private String self;
        private int id;
        private String key;
        private String colorName;
        private String name;
    }
}
