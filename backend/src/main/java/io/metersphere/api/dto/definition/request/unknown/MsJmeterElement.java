package io.metersphere.api.dto.definition.request.unknown;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 暂时存放所有未知的Jmeter Element对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JmeterElement")
public class MsJmeterElement extends MsTestElement {
    private String type = "JmeterElement";
    private String elementType;
    private String jmeterElement;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        try {
            // 非导出操作，且不是启用状态则跳过执行
            if (!config.isOperating() && !this.isEnable()) {
                return;
            }
            InputStream inputSource = getStrToStream(jmeterElement);
            if (inputSource != null) {
                Object scriptWrapper = SaveService.loadElement(inputSource);
                HashTree elementTree = tree;
                this.setElementType(scriptWrapper.getClass().getName());
                if (scriptWrapper instanceof TestElement) {
                    ((TestElement) scriptWrapper).setName(this.getName());
                    ((TestElement) scriptWrapper).setEnabled(this.isEnable());
                }
                // csv 检查处理
                if (!config.isOperating() && scriptWrapper instanceof CSVDataSet && ((CSVDataSet) scriptWrapper).isEnabled()) {
                    String path = ((CSVDataSet) scriptWrapper).getPropertyAsString("filename");
                    if (!new File(path).exists()) {
                        // 检查场景变量中的csv文件是否存在
                        String pathArr[] = path.split("\\/");
                        String csvPath = this.getCSVPath(config, pathArr[pathArr.length - 1]);
                        if (StringUtils.isNotEmpty(csvPath)) {
                            ((CSVDataSet) scriptWrapper).setProperty("filename", csvPath);
                        } else {
                            MSException.throwException(StringUtils.isEmpty(((CSVDataSet) scriptWrapper).getName()) ? "CSVDataSet" : ((CSVDataSet) scriptWrapper).getName() + "：[ CSV文件不存在 ]");
                        }
                    }

                    String csvPath = ((CSVDataSet) scriptWrapper).getPropertyAsString("filename");
                    if (config.getCsvFilePaths().contains(csvPath)) {
                        return;
                    } else {
                        config.getCsvFilePaths().add(csvPath);
                    }
                }

                if (config.isOperating()) {
                    elementTree = tree.add(scriptWrapper);
                } else if (!(scriptWrapper instanceof TestPlan) && !(scriptWrapper instanceof ThreadGroup)) {
                    elementTree = tree.add(scriptWrapper);
                }
                if (!config.isOperating() && scriptWrapper instanceof ThreadGroup && !((ThreadGroup) scriptWrapper).isEnabled()) {
                    LogUtil.info(((ThreadGroup) scriptWrapper).getName() + "是被禁用线程组不加入执行");
                } else {
                    if (CollectionUtils.isNotEmpty(hashTree)) {
                        for (MsTestElement el : hashTree) {
                            // 给所有孩子加一个父亲标志
                            el.setParent(this);
                            el.toHashTree(elementTree, el.getHashTree(), config);
                        }
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MSException.throwException(ex.getMessage());
        }
    }

    private String getCSVPath(ParameterConfig config, String name) {
        if (CollectionUtils.isNotEmpty(config.getVariables())) {
            List<ScenarioVariable> list = config.getVariables().stream().filter(ScenarioVariable::isCSVValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                for (ScenarioVariable item : list) {
                    if (CollectionUtils.isNotEmpty(item.getFiles())) {
                        List<String> names = item.getFiles().stream().map(BodyFile::getName).collect(Collectors.toList());
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

    public static InputStream getStrToStream(String sInputString) {
        if (StringUtils.isNotEmpty(sInputString)) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
