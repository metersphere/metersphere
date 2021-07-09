package io.metersphere.track.issue.domain.zentao;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class RequestUrl {
    private String login;
    private String sessionGet;
    private String bugCreate;
    private String bugGet;
    private String storyGet;
    private String userGet;
    private String buildsGet;
    private String fileUpload;
    private String replaceImgUrl;
    private Pattern imgPattern;
}
