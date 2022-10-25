package io.metersphere.service.issue.client;

import io.metersphere.service.issue.domain.zentao.RequestUrl;

import java.util.regex.Pattern;

public class ZentaoPathInfoClient extends ZentaoClient {

    private static final String LOGIN = "/user-login.json?zentaosid=";
    private static final String SESSION_GET = "/api-getsessionid.json";
    private static final String BUG_CREATE = "/api-getModel-bug-create.json?zentaosid=";
    private static final String BUG_UPDATE = "/api-getModel-bug-update-bugID={1}.json?zentaosid={2}";
    private static final String BUG_DELETE = "/bug-delete-{1}-yes.json?zentaosid={2}";
    private static final String BUG_GET = "/api-getModel-bug-getById-bugID={1}?zentaosid={2}";
    private static final String STORY_GET = "/api-getModel-story-getProductStories-productID={key}?zentaosid=";
    private static final String USER_GET = "/api-getModel-user-getList?zentaosid=";
    private static final String BUILDS_GET = "/api-getModel-build-getProductBuildPairs-productID={0}?zentaosid={1}";
    private static final String CREATE_META_DATA="/bug-create-{0}.json?zentaosid={1}";
    private static final String FILE_UPLOAD = "/api-getModel-file-saveUpload-objectType=bug,objectID={1}?zentaosid={2}";
    private static final String FILE_DELETE = "/file-delete-{1}-.yes.json?zentaosid={2}";
    private static final String FILE_DOWNLOAD="/file-download-{1}-.click.json?zentaosid={2}";
    private static final String REPLACE_IMG_URL = "<img src=\"%s/file-read-$1\"/>";
    private static final Pattern IMG_PATTERN = Pattern.compile("file-read-(.*?)\"/>");
    private static final String PRODUCT_GET = "/api-getModel-product-getById-productID={0}?zentaosid={1}";
    private static final String BUG_LIST_URL = "/bug-browse-{1}-0-all-0--{2}-{3}-{4}.json?&zentaosid={5}";

    public ZentaoPathInfoClient(String url) {
        super(url);
    }

    protected RequestUrl request = new RequestUrl();

    {
        request.setLogin(getUrl(LOGIN));
        request.setSessionGet(getUrl(SESSION_GET));
        request.setBugCreate(getUrl(BUG_CREATE));
        request.setBugGet(getUrl(BUG_GET));
        request.setStoryGet(getUrl(STORY_GET));
        request.setUserGet(getUrl(USER_GET));
        request.setBuildsGet(getUrl(BUILDS_GET));
        request.setFileUpload(getUrl(FILE_UPLOAD));
        request.setReplaceImgUrl(getReplaceImgUrl(REPLACE_IMG_URL));
        request.setImgPattern(IMG_PATTERN);
        request.setBugUpdate(getUrl(BUG_UPDATE));
        request.setBugDelete(getUrl(BUG_DELETE));
        request.setBugList(getUrl(BUG_LIST_URL));
        request.setCreateMetaData(getUrl(CREATE_META_DATA));
        request.setProductGet(getUrl(PRODUCT_GET));
        request.setFileDelete(getUrl(FILE_DELETE));
        request.setFileDownload(getUrl(FILE_DOWNLOAD));
        requestUrl = request;
    }

    protected String getUrl(String url) {
        return getBaseUrl() + url;
    }
}
