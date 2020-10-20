package io.metersphere.xmind.parser;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.xmind.parser.pojo.JsonRootBean;
import io.metersphere.xmind.utils.FileUtil;
import org.apache.commons.compress.archivers.ArchiveException;
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
public class XmindParser {
    public static final String CONTENT_JSON = "content.json";
    public static final String CONTENT_XML = "content.xml";
    public static final String COMMENTS_XML = "comments.xml";

    /**
     * 解析脑图文件，返回content整合后的内容
     *
     * @param multipartFile
     * @return
     * @throws IOException
     * @throws ArchiveException
     * @throws DocumentException
     */
    public static List<String> parseJson(MultipartFile multipartFile) throws IOException, ArchiveException, DocumentException {
        File file = FileUtil.multipartFileToFile(multipartFile);
        List<String> contents = null;
        String res = null;
        if (file == null || !file.exists()) {
            MSException.throwException(Translator.get("incorrect_format"));
        }
        try {
            res = ZipUtils.extract(file);
            if (isXmindZen(res, file)) {
                contents = (getXmindZenContent(file, res));
            } else {
                contents = getXmindLegacyContent(file, res);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
        } finally {
            // 删除生成的文件夹
            if (res != null) {
                File dir = new File(res);
                FileUtil.deleteDir(dir);
            }
            // 删除零时文件
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
                caseCount += content.split("(?:tc:|tc：|TC:|TC：|tc|TC)").length;
                JsonRootBean jsonRootBean = JSON.parseObject(content, JsonRootBean.class);
                jsonRootBeans.add(jsonRootBean);
            }
            if (caseCount > 800) {
                MSException.throwException(Translator.get("import_xmind_count_error"));
            }
        }
        return jsonRootBeans;

    }

    /**
     * @return
     */
    public static List<String> getXmindZenContent(File file, String extractFileDir)
            throws IOException, ArchiveException {
        List<String> keys = new ArrayList<>();
        keys.add(CONTENT_JSON);
        Map<String, String> map = ZipUtils.getContents(keys, file, extractFileDir);
        String content = map.get(CONTENT_JSON);
        return XmindZen.getContent(content);
    }

    /**
     * @return
     */
    public static List<String> getXmindLegacyContent(File file, String extractFileDir)
            throws IOException, ArchiveException, DocumentException {
        List<String> keys = new ArrayList<>();
        keys.add(CONTENT_XML);
        keys.add(COMMENTS_XML);
        Map<String, String> map = ZipUtils.getContents(keys, file, extractFileDir);

        String contentXml = map.get(CONTENT_XML);
        String commentsXml = map.get(COMMENTS_XML);
        List<String> xmlContent = XmindLegacy.getContent(contentXml, commentsXml);

        return xmlContent;
    }

    private static boolean isXmindZen(String res, File file) throws IOException, ArchiveException {
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
