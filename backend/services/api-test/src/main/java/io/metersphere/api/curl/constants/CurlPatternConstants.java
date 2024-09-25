package io.metersphere.api.curl.constants;

import java.util.regex.Pattern;

/**
 * @author wx
 */
public interface CurlPatternConstants {

    /**
     * CURL结构校验
     */
    Pattern CURL_STRUCTURE_PATTERN = Pattern.compile("^curl");

    /**
     * URL路径
     */
    Pattern URL_PATH_PATTERN = Pattern.compile("(?:\\s|^)(?:'|\")?(https?://[^?\\s'\"]*)(?:\\?[^\\s'\"]*)?(?:'|\")?(?:\\s|$)");

    /**
     * URL_PARAMS请求参数
     */
    Pattern URL_PARAMS_PATTERN = Pattern.compile("(?:\\s|^)(?:'|\")?(https?://[^\\s'\"]+)(?:'|\")?(?:\\s|$)");

    /**
     * HTTP请求方法
     */
    Pattern HTTP_METHOD_PATTERN = Pattern.compile("curl\\s+(?:[^\\s]+\\s+)*(-X|--request|--head)\\s+'?(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD|CONNECT)'?");
    Pattern HTTP_HEAD_METHOD_PATTERN = Pattern.compile("curl\\s+(?:[^\\s]+\\s+)*(--head|-I)\\s");

    /**
     * 默认HTTP请求方法
     */
    Pattern DEFAULT_HTTP_METHOD_PATTERN = Pattern.compile(".*\\s(-d|--data|--data-binary|--data-raw|-F)\\s.*");

    /**
     * 请求头
     */
    Pattern CURL_HEADERS_PATTERN = Pattern.compile("(?:-H|--header)\\s+(?:\"([^\"]*)\"|'([^']*)')");

    /**
     * -u/--user 请求头
     */
    Pattern CURL_USER_HEAD_PATTERN = Pattern.compile("-(u|user)\\s+(\\S+:\\S+)");

    /**
     * -d/--data 请求体
     */
    Pattern DEFAULT_HTTP_BODY_PATTERN = Pattern.compile("(?:--data|-d)\\s+(?:'([^']*)'|\"([^\"]*)\"|(\\S+))", Pattern.DOTALL);
    Pattern DEFAULT_HTTP_BODY_PATTERN_KV = Pattern.compile("^([^=&]+=[^=&]*)(?:&[^=&]+=[^=&]*)*$", Pattern.DOTALL);

    /**
     * --data-raw 请求体
     */
    Pattern HTTP_XML_JSON_BODY_PATTERN = Pattern.compile("--data-raw '(.+?)'(?s)", Pattern.DOTALL);
    Pattern HTTP_ROW_BODY_PATTERN = Pattern.compile("--data-binary\\s+['\"](.+?)['\"](?s)", Pattern.DOTALL);

    /**
     * --form 请求体
     */
    Pattern HTTP_FROM_BODY_PATTERN = Pattern.compile("--form\\s+['\"](.*?)['\"]|-F\\s+['\"](.*?)['\"]");


    /**
     * --data-urlencode 请求体
     */
    Pattern HTTP_URLENCODE_BODY_PATTERN = Pattern.compile("--data-urlencode\\s+'(.*?)'");


    /**
     * -x/--proxy 代理配置
     */
    Pattern PROXY_PATTERN = Pattern.compile("(-x|--proxy)\\s+[\\S]+");

}