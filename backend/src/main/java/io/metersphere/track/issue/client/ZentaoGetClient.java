package io.metersphere.track.issue.client;

import io.metersphere.track.issue.domain.zentao.RequestUrl;

import java.util.regex.Pattern;

public class ZentaoGetClient extends ZentaoClient {

    private static final String LOGIN = "/?m=user&f=login&t=json&zentaosid=";
    private static final String SESSION_GET="/?m=api&f=getSessionID&t=json";
    private static final String BUG_CREATE="&module=bug&methodName=create&t=json&zentaosid=";
    private static final String BUG_UPDATE = "&module=bug&methodName=update&params=bugID={0}&t=json&zentaosid={1}";
    private static final String BUG_DELETE = "/?m=bug&f=delete&bugID={0}&confirm=yes&t=json&zentaosid={1}";
    private static final String BUG_GET="&module=bug&methodName=getById&params=bugID={1}&t=json&zentaosid={2}";
    private static final String STORY_GET="&module=story&methodName=getProductStories&params=productID={key}&t=json&zentaosid=";
    private static final String USER_GET="&module=user&methodName=getList&t=json&zentaosid=";
    private static final String BUILDS_GET="&module=build&methodName=getProductBuildPairs&productID={0}&zentaosid={1}";
    private static final String FILE_UPLOAD="&module=file&methodName=saveUpload&t=json&zentaosid=";
    private static final String CREATE_META_DATA="?m=bug&f=create&productID={0}&t=json&zentaosid={1}";
    private static final String REPLACE_IMG_URL="<img src=\"%s/index.php?m=file&f=read&fileID=$1\"/>";
    private static final Pattern IMG_PATTERN = Pattern.compile("m=file&f=read&fileID=(.*?)\"/>");
    private static final String PRODUCT_GET = "&module=product&methodName=getById&params=productID={0}&zentaosid={1}";
    // 注意 recTotal={1}&recPerPage={2}&pageID={3} 顺序不能调换，有点恶心
    private static final String BUG_LIST_URL = "/?m=bug&f=browse&productID={0}&branch=&browseType=&param=0&orderBy=&recTotal={1}&recPerPage={2}&pageID={3}&t=json&zentaosid={4}";

    RequestUrl request = new RequestUrl();

    public ZentaoGetClient(String url) {
        super(url);
    }

    {
        request.setLogin(getNotSuperModelUrl(LOGIN));
        request.setSessionGet(getNotSuperModelUrl(SESSION_GET));
        request.setBugCreate(getUrl(BUG_CREATE));
        request.setBugGet(getUrl(BUG_GET));
        request.setStoryGet(getUrl(STORY_GET));
        request.setUserGet(getUrl(USER_GET));
        request.setBuildsGet(getUrl(BUILDS_GET));
        request.setFileUpload(getUrl(FILE_UPLOAD));
        request.setReplaceImgUrl(getReplaceImgUrl(REPLACE_IMG_URL));
        request.setImgPattern(IMG_PATTERN);
        request.setBugUpdate(getUrl(BUG_UPDATE));
        request.setBugDelete(getNotSuperModelUrl(BUG_DELETE));
        request.setBugList(getNotSuperModelUrl(BUG_LIST_URL));
        request.setCreateMetaData(getNotSuperModelUrl(CREATE_META_DATA));
        request.setProductGet(getUrl(PRODUCT_GET));
        requestUrl = request;
    }

    private String getUrl(String url) {
        return getBaseUrl() + "/?m=api&f=getModel" + url;
    }

    private String getNotSuperModelUrl(String url) {
        return getBaseUrl() + url;
    }
}
