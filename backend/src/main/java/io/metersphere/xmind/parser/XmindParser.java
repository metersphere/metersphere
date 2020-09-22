package io.metersphere.xmind.parser;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.xmind.parser.pojo.JsonRootBean;
import io.metersphere.xmind.utils.FileUtil;
import org.apache.commons.compress.archivers.ArchiveException;
import org.dom4j.DocumentException;
import org.springframework.util.StringUtils;
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
    public static final String xmindZenJson = "content.json";
    public static final String xmindLegacyContent = "content.xml";
    public static final String xmindLegacyComments = "comments.xml";

    /**
     * 解析脑图文件，返回content整合后的内容
     *
     * @param multipartFile
     * @return
     * @throws IOException
     * @throws ArchiveException
     * @throws DocumentException
     */
    public static String parseJson(MultipartFile multipartFile) throws IOException, ArchiveException, DocumentException {

        File file = FileUtil.multipartFileToFile(multipartFile);
        if (file == null || !file.exists())
            MSException.throwException(Translator.get("incorrect_format"));

        String res = ZipUtils.extract(file);
        String content = null;
        if (isXmindZen(res, file)) {
            content = getXmindZenContent(file, res);
        } else {
            content = getXmindLegacyContent(file, res);
        }

        // 删除生成的文件夹
        File dir = new File(res);
        FileUtil.deleteDir(dir);
        JsonRootBean jsonRootBean = JSON.parseObject(content, JsonRootBean.class);
        // 删除零时文件
        if (file != null)
            file.delete();
        String json = (JSON.toJSONString(jsonRootBean, false));

        if (StringUtils.isEmpty(content) || content.split("(?:tc:|tc：|TC:|TC：|tc|TC)").length == 1) {
            MSException.throwException(Translator.get("import_xmind_not_found"));
        }
        if (!StringUtils.isEmpty(content) && content.split("(?:tc:|tc：|TC:|TC：|tc|TC)").length > 500) {
            MSException.throwException(Translator.get("import_xmind_count_error"));
        }
        return json;
    }

    public static JsonRootBean parseObject(MultipartFile multipartFile) throws DocumentException, ArchiveException, IOException {
        String content = parseJson(multipartFile);
        JsonRootBean jsonRootBean = JSON.parseObject(content, JsonRootBean.class);
        return jsonRootBean;
    }

    /**
     * @return
     */
    public static String getXmindZenContent(File file, String extractFileDir)
            throws IOException, ArchiveException {
        List<String> keys = new ArrayList<>();
        keys.add(xmindZenJson);
        Map<String, String> map = ZipUtils.getContents(keys, file, extractFileDir);
        String content = map.get(xmindZenJson);
        content = XmindZen.getContent(content);
        return content;
    }

    /**
     * @return
     */
    public static String getXmindLegacyContent(File file, String extractFileDir)
            throws IOException, ArchiveException, DocumentException {
        List<String> keys = new ArrayList<>();
        keys.add(xmindLegacyContent);
        keys.add(xmindLegacyComments);
        Map<String, String> map = ZipUtils.getContents(keys, file, extractFileDir);

        String contentXml = map.get(xmindLegacyContent);
        String commentsXml = map.get(xmindLegacyComments);
        String xmlContent = XmindLegacy.getContent(contentXml, commentsXml);

        return xmlContent;
    }

    private static boolean isXmindZen(String res, File file) throws IOException, ArchiveException {
        // 解压
        File parent = new File(res);
        if (parent.isDirectory()) {
            String[] files = parent.list(new ZipUtils.FileFilter());
            for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
                if (files[i].equals(xmindZenJson)) {
                    return true;
                }
            }
        }
        return false;
    }
}
