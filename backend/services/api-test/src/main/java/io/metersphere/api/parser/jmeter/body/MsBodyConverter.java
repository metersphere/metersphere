package io.metersphere.api.parser.jmeter.body;


import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.dto.request.http.body.WWWFormKV;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.constants.LocalRepositoryDir;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * body 解析器
 */
public abstract class MsBodyConverter<T> {

    /**
     * 解析对应的请求体参数
     * 返回 Content-Type
     *
     * @param sampler
     * @param body
     * @param config
     */
    public abstract String parse(HTTPSamplerProxy sampler, T body, ParameterConfig config);

    /**
     * 解析文本类型的 kv 参数
     *
     * @param textFormValues
     * @return
     */
    protected Arguments getArguments(List<? extends WWWFormKV> textFormValues) {
        Arguments arguments = new Arguments();
        textFormValues.forEach(kv -> {
            // 处理 mock 函数
            String value = Mock.buildFunctionCallString(kv.getValue());
            if (value == null) {
                value = StringUtils.EMPTY;
            }
            HTTPArgument httpArgument = new HTTPArgument(kv.getKey(), value);
            arguments.addArgument(httpArgument);
            if (kv instanceof FormDataKV formDataKV && formDataKV.getContentType() != null) {
                httpArgument.setContentType(formDataKV.getContentType());
            }
        });
        return arguments;
    }

    /**
     * 将 form-data 和 binary 类型的文件转换为 jmeter 的 HTTPFileArg
     *
     * @param file
     * @return
     */
    protected HTTPFileArg getHttpFileArg(ApiFile file) {
        String fileId = file.getFileId();
        String fileName = file.getFileName();
        // 在对应目录下创建文件ID目录，将文件放入
        String path = LocalRepositoryDir.getSystemCacheDir() + '/' + fileId + '/' + fileName;
        if (!StringUtils.equals(File.separator, "/")) {
            // windows 系统下运行，将 / 转换为 \，否则jmeter报错
            path = path.replace("/", File.separator);
        }
        String mimetype = ContentType.APPLICATION_OCTET_STREAM.getMimeType();
        HTTPFileArg fileArg = new HTTPFileArg(path, StringUtils.EMPTY, mimetype);
        return fileArg;
    }

    /**
     * 将文本中的 @xxx 转换成 ${__Mock(@xxx)}
     *
     * @param text
     * @return
     */
    protected String parseTextMock(String text) {
        String pattern = "[\"\\s:]@[a-zA-Z\\\\(|,'-\\\\d ]*[a-zA-Z)-9),\\\\\"]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            //取出group的最后一个字符 主要是防止 @string|number 和 @string 这种情况
            //如果是 “ 或者, 结尾的  需要截取
            String group = matcher.group();
            if (group.endsWith(",") || group.endsWith("\"")) {
                group = group.substring(0, group.length() - 1);
            }
            // 去掉第一个字符，因为第一个字符是 " : 或者空格
            group = group.substring(1, group.length());
            text = text.replace(group, StringUtils.join("${__Mock(", group.replace(",", "\\,"), ")}"));
        }
        return text;
    }
    /**
     * 处理raw格式参数
     * 包含了 json 等格式
     *
     * @param sampler
     * @param raw
     */
    protected void handleRowBody(HTTPSamplerProxy sampler, String raw) {
        Arguments arguments = new Arguments();
        HTTPArgument httpArgument = new HTTPArgument();
        httpArgument.setValue(raw);
        httpArgument.setAlwaysEncoded(false);
        httpArgument.setEnabled(true);
        arguments.addArgument(httpArgument);
        sampler.setPostBodyRaw(true);
        sampler.setArguments(arguments);
    }
}
