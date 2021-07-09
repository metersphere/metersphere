package io.metersphere.track.issue.client;

import io.metersphere.track.issue.domain.zentao.RequestUrl;

import java.util.regex.Pattern;

public class ZentaoGetClient extends ZentaoClient {

    private static final String LOGIN = "/?m=user&f=login&t=json&zentaosid=";
    private static final String SESSION_GET="/?m=api&f=getSessionID&t=json";
    private static final String BUG_CREATE="&module=bug&methodName=create&t=json&zentaosid=";
    private static final String BUG_GET="&module=bug&methodName=getById&params=bugID={1}&t=json&zentaosid={2}";
    private static final String STORY_GET="&module=story&methodName=getProductStories&params=productID={key}&t=json&zentaosid=";
    private static final String USER_GET="&module=user&methodName=getList&t=json&zentaosid=";
    private static final String BUILDS_GET="&module=build&methodName=getProductBuildPairs&productID={0}&zentaosid=";
    private static final String FILE_UPLOAD="&module=file&methodName=saveUpload&t=json&zentaosid=";
    private static final String REPLACE_IMG_URL="<img src=\"/zentao/index.php?m=file&f=read&fileID=$1\"/>";
    private static final Pattern IMG_PATTERN = Pattern.compile("m=file&f=read&fileID=(.*?)\"/>");

    {
        RequestUrl request = new RequestUrl();
        request.setLogin(getBaseUrl() + LOGIN);
        request.setSessionGet(getBaseUrl() + SESSION_GET);
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

    public ZentaoGetClient(String url) {
        super(url);
    }

    private String getUrl(String url) {
        return getBaseUrl() + "/?m=api&f=getModel" + url;
    }
}
