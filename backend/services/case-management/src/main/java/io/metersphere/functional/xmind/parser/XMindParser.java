package io.metersphere.functional.xmind.parser;

import io.metersphere.functional.xmind.pojo.JsonRootBean;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 解析主体
 */
public class XMindParser {
    public static final String CONTENT_JSON = "content.json";
    public static final String CONTENT_XML = "content.xml";
    public static final String COMMENTS_XML = "comments.xml";

    /**
     * 解析脑图文件，返回content整合后的内容
     *
     */
    public static List<String> parseJson(MultipartFile multipartFile) throws IOException {

        File file = MsFileUtils.multipartFileToFile(multipartFile);
        List<String> contents;
        String res = null;
        if (file == null || !file.exists()) {
            throw new MSException (Translator.get("incorrect_format"));
        }
        try {
            res = ZipUtils.extract(file);
            if (isXMindZen(res)) {
                contents = (getXMindZenContent(res));
            } else {
                contents = getXMindLegacyContent(res);
            }
        } catch (Exception e) {
            throw new MSException (e.getMessage());
        } finally {
            // 删除生成的文件夹
            if (res != null) {
                File dir = new File(res);
                FileUtils.deleteDirectory(dir);
            }
            // 删除临时文件
            if (file != null) {
                file.delete();
            }
        }
        return contents;
    }

    public static List<JsonRootBean> parseObject(MultipartFile multipartFile) throws DocumentException, ArchiveException, IOException {
        List<String> contents = parseJson(multipartFile);
        int caseCount = 0;
        List<JsonRootBean> jsonRootBeans = new ArrayList<>();
        if (contents != null) {
            for (String content : contents) {
                caseCount += content.split("((?i)case)").length;
                JsonRootBean jsonRootBean = JSON.parseObject(content, JsonRootBean.class);
                jsonRootBeans.add(jsonRootBean);
            }
            if (caseCount > 800) {
                throw new MSException (Translator.get("import_xmind_count_error"));
            }
        }
        return jsonRootBeans;

    }


    /**
     * 解析xmind zen 格式的文件
     * @param extractFileDir 解压后的文件夹名字
     */
    public static List<String> getXMindZenContent(String extractFileDir)
            throws IOException {
        List<String> keys = new ArrayList<>();
        keys.add(CONTENT_JSON);
        Map<String, String> map = ZipUtils.getContents(keys, extractFileDir);
        String content = map.get(CONTENT_JSON);
        return XMindZen.getContent(content);
    }

    /**
     * 解析正常xmind 格式的文件
     * @param extractFileDir 解压后的文件夹名字
     */
    public static List<String> getXMindLegacyContent(String extractFileDir)
            throws IOException, DocumentException {
        List<String> keys = new ArrayList<>();
        keys.add(CONTENT_XML);
        keys.add(COMMENTS_XML);
        Map<String, String> map = ZipUtils.getContents(keys, extractFileDir);

        String contentXml = map.get(CONTENT_XML);
        String commentsXml = map.get(COMMENTS_XML);

        return XMindLegacy.getContent(contentXml, commentsXml);
    }

    private static boolean isXMindZen(String res){
        // 解压
        File parent = new File(res);
        if (parent.isDirectory()) {
            String[] files = parent.list(new ZipUtils.FileFilter());
            for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
                if (files[i].equals(CONTENT_JSON)) {
                    return true;
                }
            }
        }
        return false;
    }
}
