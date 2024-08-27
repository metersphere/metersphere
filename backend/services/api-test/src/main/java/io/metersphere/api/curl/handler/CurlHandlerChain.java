package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;

/**
 * @author wx
 */
public abstract class CurlHandlerChain implements ICurlHandler<CurlEntity, String> {

    ICurlHandler<CurlEntity, String> next;

    @Override
    public ICurlHandler<CurlEntity, String> next(ICurlHandler<CurlEntity, String> handler) {
        this.next = handler;
        return this.next;
    }

    @Override
    public abstract void handle(CurlEntity entity, String curl);


    protected void nextHandle(CurlEntity curlEntity, String curl) {
        if (next != null) {
            next.handle(curlEntity, curl);
        }
    }

    protected void validate(String curl) {
        if (StringUtils.isBlank(curl)) {
            throw new MSException(Translator.get("curl_script_is_empty"));
        }

        Matcher matcher = CurlPatternConstants.CURL_STRUCTURE_PATTERN.matcher(curl);
        if (!matcher.find()) {
            throw new MSException(Translator.get("curl_script_is_invalid"));
        }
    }

    public static CurlHandlerChain init() {
        return new CurlHandlerChain() {
            @Override
            public void handle(CurlEntity entity, String curl) {
                this.validate(curl);

                // 替换掉可能存在的转译(字符串中的空白字符，包括空格、换行符和制表符...)
                curl = curl.replace("\\", "")
                        .replace("\n", "")
                        .replace("\t", "");

                Matcher matcher = CurlPatternConstants.PROXY_PATTERN.matcher(curl);
                if (matcher.find()) {
                    curl = matcher.replaceAll("").trim();
                }

                int compressedIndex = curl.indexOf("--compressed");
                if (compressedIndex != -1) {
                    String beforeCompressed = curl.substring(4, compressedIndex);
                    String afterCompressed = curl.substring(compressedIndex + "--compressed".length());
                    curl = "curl" + afterCompressed + beforeCompressed;
                }


                if (next != null) {
                    next.handle(entity, curl);
                }
            }
        };
    }

}