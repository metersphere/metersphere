package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.processor.extract.MsExtract;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 提取处理器配置
 * @Author: jianxing
 * @CreateTime: 2023-11-22  11:08
 */
@Data
@JsonTypeName("EXTRACT")
public class ExtractPostProcessor extends MsProcessor {
    /**
     * 提取器列表
     */
    private List<MsExtract> extractors = new ArrayList<>();
}
