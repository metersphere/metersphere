package io.metersphere.track.issue.domain.Jira;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class JiraIssueDescriptionV3 {
    private String type;
    private int version;
    private List<Content> content;

    public JiraIssueDescriptionV3(String text) {
        List<Content> list = new ArrayList<>();
        Content content = new Content(text);
        list.add(content);
        this.type = "doc";
        this.version = 1;
        this.content = list;
    }

    @Data
    @NoArgsConstructor
    public class Content {
        private String type;
        private List<ContentInfo> content;

        public Content(String text) {
            List<ContentInfo> list = new ArrayList<>();
            ContentInfo content = new ContentInfo(text);
            list.add(content);
            this.type = "paragraph";
            this.content = list;
        }
    }

    @Data
    @NoArgsConstructor
    public class ContentInfo {
        private String text;
        private String type;

        public ContentInfo(String text) {
            this.text = text;
            this.type = "text";
        }

    }
}
