package io.metersphere.api.parser.jmeter.body;


import io.metersphere.api.dto.ApiFile;
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
     * @param sampler
     * @param body
     * @param config
     */
    public abstract void parse(HTTPSamplerProxy sampler, T body, ParameterConfig config);

    /**
     * 解析文本类型的 kv 参数
     * @param textFromValues
     * @return
     */
    protected Arguments getArguments(List<? extends WWWFormKV> textFromValues) {
        Arguments arguments = new Arguments();
        textFromValues.forEach(formDataKV -> {
            // 处理 mock 函数
            String value = Mock.buildFunctionCallString(formDataKV.getValue());
            if (value == null) {
                value = StringUtils.EMPTY;
            }
            HTTPArgument httpArgument = new HTTPArgument(formDataKV.getKey(), value);
            httpArgument.setAlwaysEncoded(formDataKV.getEncode());
            if (StringUtils.isNotBlank(formDataKV.getContentType())) {
                httpArgument.setContentType(formDataKV.getContentType());
            }
            arguments.addArgument(httpArgument);
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
     * @param text
     * @return
     */
    protected String parseTextMock(String text) {
        String pattern = "@[a-zA-Z]*";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            text =  text.replace(matcher.group(), "${__Mock(" + matcher.group() + ")}");
        }
        return text;
    }

    /**
     * 处理raw格式参数
     * 包含了 json 等格式
     * @param sampler
     * @param raw
     */
    protected static void handleRowBody(HTTPSamplerProxy sampler, String raw) {
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
