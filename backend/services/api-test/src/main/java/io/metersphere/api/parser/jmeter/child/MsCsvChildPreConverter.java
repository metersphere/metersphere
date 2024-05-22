package io.metersphere.api.parser.jmeter.child;

import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.scenario.CsvVariable;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.LocalRepositoryDir;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 解析 csv 文件
 * 在解析子步骤前，先添加 csv 配置
 * @Author: jianxing
 * @CreateTime: 2024-05-15  11:32
 */
public class MsCsvChildPreConverter extends AbstractJmeterElementConverter<AbstractMsTestElement> {

    @Override
    public void toHashTree(HashTree tree, AbstractMsTestElement element, ParameterConfig config) {
        List<String> csvIds = element.getCsvIds();
        ApiParamConfig apiParamConfig = (ApiParamConfig) config;
        if (CollectionUtils.isEmpty(csvIds)) {
            return;
        }
        csvIds.forEach(csvId -> {
            CsvVariable csvVariable = apiParamConfig.getCsvVariable(csvId);
            if (csvVariable != null) {
                // 场景级的线程共享，步骤级的私有
                String shareMode = StringUtils.equals(csvVariable.getScope(), CsvVariable.CsvVariableScope.SCENARIO.name()) ?
                        JmeterProperty.CSVDataSetProperty.SHARE_MODE_THREAD : UUID.randomUUID().toString();
                addCsvDataSet(tree, shareMode, csvVariable);
            }
        });
    }

    public static void addCsvDataSet(HashTree tree, String shareMode, List<CsvVariable> list) {
        list.forEach(item -> addCsvDataSet(tree, shareMode, item));
    }

    private static void addCsvDataSet(HashTree tree, String shareMode, CsvVariable csvVariable) {
        if (!csvVariable.isValid() || !csvVariable.isEnable()) {
            return;
        }
        // 执行机执行文件存放的缓存目录
        String path = LocalRepositoryDir.getSystemCacheDir() + "/" + csvVariable.getFile().getFileId() + "/" + csvVariable.getFile().getFileName();
        if (!StringUtils.equals(File.separator, "/")) {
            // windows 系统下运行，将 / 转换为 \，否则jmeter报错
            path = path.replace("/", File.separator);
        }
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setEnabled(true);
        csvDataSet.setIgnoreFirstLine(false);
        csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.TEST_BEAN_GUI));
        csvDataSet.setName(StringUtils.isEmpty(csvVariable.getName()) ? CSVDataSet.class.getSimpleName() : csvVariable.getName());
        csvDataSet.setProperty(JmeterProperty.FILE_ENCODING, StringUtils.isEmpty(csvVariable.getEncoding()) ? StandardCharsets.UTF_8.name() : csvVariable.getEncoding());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.IGNORE_FIRST_LINE, csvVariable.getIgnoreFirstLine());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.STOP_THREAD, csvVariable.getStopThreadOnEof());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.FILE_NAME, path);
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.SHARE_MODE, shareMode);
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.RECYCLE, csvVariable.getRecycleOnEof());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.VARIABLE_NAMES, csvVariable.getVariableNames());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.DELIMITER, csvVariable.getDelimiter());
        csvDataSet.setProperty(JmeterProperty.CSVDataSetProperty.QUOTED_DATA, csvVariable.getAllowQuotedData());
        tree.add(csvDataSet);
    }
}
