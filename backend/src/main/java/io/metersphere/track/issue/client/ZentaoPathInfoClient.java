package io.metersphere.track.issue.client;


import io.metersphere.track.issue.domain.zentao.RequestUrl;

import java.util.regex.Pattern;

public class ZentaoPathInfoClient extends ZentaoClient {

    private static final String LOGIN = "/user-login.json?zentaosid=";
    private static final String SESSION_GET = "/api-getsessionid.json";
    private static final String BUG_CREATE = "/api-getModel-bug-create.json?zentaosid=";
    private static final String BUG_GET = "/api-getModel-bug-getById-bugID={1}?zentaosid={2}";
    private static final String STORY_GET = "/api-getModel-story-getProductStories-productID={key}?zentaosid=";
    private static final String USER_GET = "/api-getModel-user-getList?zentaosid=";
    private static final String BUILDS_GET = "/api-getModel-build-getProductBuildPairs-productID={projectId}?zentaosid=";
    private static final String FILE_UPLOAD = "/api-getModel-file-saveUpload.json?zentaosid=";
    private static final String REPLACE_IMG_URL = "<img src=\"/zentao/file-read-$1\"/>";
    private static final Pattern IMG_PATTERN = Pattern.compile("file-read-(.*?)\"/>");

    {
        RequestUrl request = new RequestUrl();
        request.setLogin(getUrl(LOGIN));
        request.setSessionGet(getUrl(SESSION_GET));
        request.setBugCreate(getUrl(BUG_CREATE));
        request.setBugGet(getUrl(BUG_GET));
        request.setStoryGet(getUrl(STORY_GET));
        request.setUserGet(getUrl(USER_GET));
        request.setBuildsGet(getUrl(BUILDS_GET));
        request.setFileUpload(getUrl(FILE_UPLOAD));
        request.setReplaceImgUrl(REPLACE_IMG_URL);
        request.setImgPattern(IMG_PATTERN);
        requestUrl = request;
    }

    public ZentaoPathInfoClient(String url) {
        super(url);
    }

    private String getUrl(String url) {
        return getBaseUrl() + url;
    }
}
