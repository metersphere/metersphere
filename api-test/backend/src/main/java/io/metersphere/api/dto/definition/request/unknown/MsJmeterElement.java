package io.metersphere.api.dto.definition.request.unknown;

import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 暂时存放所有未知的Jmeter Element对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MsJmeterElement extends MsTestElement {
    private String type = ElementConstants.JMETER_ELE;
    private String clazzName = MsJmeterElement.class.getCanonicalName();
    private String elementType;
    private String jmeterElement;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {

        ParameterConfig config = (ParameterConfig) msParameter;

        // 非导出操作，且不是启用状态则直接返回
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }

        try (InputStream inputSource = getStrToStream(jmeterElement)) {
            if (inputSource == null) {
                return;
            }

            Object scriptWrapper = SaveService.loadElement(inputSource);
            if (scriptWrapper == null) {
                return;
            }

            HashTree elementTree = tree;
            this.setElementType(scriptWrapper.getClass().getName());

            if (scriptWrapper instanceof TestElement testElement) {
                testElement.setName(this.getName());
                testElement.setEnabled(this.isEnable());
            }

            // CSV 检查与处理
            handleCSVDataSet(config, scriptWrapper);

            // 取出导入的测试计划中的变量
            if (scriptWrapper instanceof TestPlan testPlan) {
                if (testPlan.getArguments() != null && StringUtils.isNotEmpty(testPlan.getArguments().getName())) {
                    elementTree.add(testPlan.getArguments());
                }
            }

            // 添加到 HashTree
            if (config.isOperating()) {
                elementTree = tree.add(scriptWrapper);
            } else if (!(scriptWrapper instanceof TestPlan) && !(scriptWrapper instanceof ThreadGroup)) {
                elementTree = tree.add(scriptWrapper);
            }

            // 忽略被禁用的线程组
            if (!config.isOperating() && scriptWrapper instanceof ThreadGroup threadGroup && !threadGroup.isEnabled()) {
                LogUtil.info(threadGroup.getName() + " 是被禁用线程组，不加入执行");
                return;
            }

            // 递归处理子元素
            if (CollectionUtils.isNotEmpty(hashTree)) {
                for (MsTestElement el : hashTree) {
                    el.setParent(this);
                    el.toHashTree(elementTree, el.getHashTree(), config);
                }
            }
        } catch (Exception ex) {
            LogUtil.error("Error in toHashTree: " + ex.getMessage(), ex);
            MSException.throwException(ex.getMessage());
        }
    }

    private void handleCSVDataSet(ParameterConfig config, Object scriptWrapper) {
        if (!(scriptWrapper instanceof CSVDataSet csvDataSet)) {
            return;
        }

        String path = csvDataSet.getPropertyAsString(ElementConstants.FILENAME);
        if (!new File(path).exists()) {
            // 检查场景变量中的 CSV 文件是否存在
            String[] pathArr = path.split("\\/");
            String csvPath = this.getCSVPath(config, pathArr[pathArr.length - 1]);
            if (StringUtils.isNotEmpty(csvPath)) {
                csvDataSet.setProperty(ElementConstants.FILENAME, csvPath);
            } else {
                String name = StringUtils.defaultIfEmpty(csvDataSet.getName(), "CSVDataSet");
                MSException.throwException(name + "：[ CSV文件不存在 ]");
            }
        }

        String csvPath = csvDataSet.getPropertyAsString(ElementConstants.FILENAME);
        if (!config.getCsvFilePaths().contains(csvPath)) {
            config.getCsvFilePaths().add(csvPath);
        }
    }


    private String getCSVPath(ParameterConfig config, String name) {
        if (CollectionUtils.isNotEmpty(config.getVariables())) {
            List<ScenarioVariable> list = config.getVariables().stream().filter(ScenarioVariable::isCSVValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                for (ScenarioVariable item : list) {
                    if (CollectionUtils.isNotEmpty(item.getFiles())) {
                        List<String> names = item.getFiles().stream().map(BodyFile::getName).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(names) && !names.contains(name) && name.contains("_")) {
                            String[] pathArr = name.split("_");
                            name = pathArr[pathArr.length - 1];
                        }
                        if (CollectionUtils.isNotEmpty(names) && names.contains(name)) {
                            if (!config.isOperating() && !new File(FileUtils.BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName()).exists()) {
                                MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ CSV文件不存在 ]");
                            } else {
                                return FileUtils.BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static InputStream getStrToStream(String inputString) {
        if (StringUtils.isNotEmpty(inputString)) {
            return new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

}
