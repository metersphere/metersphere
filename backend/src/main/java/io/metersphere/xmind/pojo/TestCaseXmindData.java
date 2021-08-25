package io.metersphere.xmind.pojo;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.track.dto.TestCaseDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/8/3 4:37 下午
 */
@Setter
@Getter
public class TestCaseXmindData {
    private List<TestCaseDTO> testCaseList;
    private String moduleName;
    private String moduleId;

    private List<TestCaseXmindData> children = new ArrayList<>();

    public TestCaseXmindData(String moduleId, String moduleName) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.testCaseList = new ArrayList<>();
    }

    public void setItem(LinkedList<TestCaseNode> modulePathDataList, List<TestCaseDTO> dataList) {
        if (CollectionUtils.isNotEmpty(modulePathDataList) && CollectionUtils.isNotEmpty(dataList)) {
            if (modulePathDataList.size() == 1) {
                this.setData(modulePathDataList.getFirst(), dataList);
            } else {
                TestCaseNode caseNode = modulePathDataList.getFirst();
                if (caseNode != null) {
                    TestCaseXmindData matchedData = null;
                    for (TestCaseXmindData item : children) {
                        if (StringUtils.equals(item.getModuleId(), caseNode.getId())) {
                            matchedData = item;
                            break;
                        }
                    }
                    if(matchedData == null){
                        matchedData = new TestCaseXmindData(caseNode.getId(), caseNode.getName());
                        this.children.add(matchedData);
                    }
                    modulePathDataList.removeFirst();
                    matchedData.setItem(modulePathDataList,dataList);
                }
            }
        }
    }

    public void setData(TestCaseNode caseNode, List<TestCaseDTO> dataList) {
        if (caseNode != null && CollectionUtils.isNotEmpty(dataList)) {
            boolean matching = false;
            for (TestCaseXmindData item : children) {
                if (StringUtils.equals(item.getModuleId(), caseNode.getId())) {
                    matching = true;
                    item.getTestCaseList().addAll(dataList);
                }
            }
            if (!matching) {
                TestCaseXmindData child = new TestCaseXmindData(caseNode.getId(), caseNode.getName());
                child.setTestCaseList(dataList);
                this.children.add(child);
            }
        }
    }
}
