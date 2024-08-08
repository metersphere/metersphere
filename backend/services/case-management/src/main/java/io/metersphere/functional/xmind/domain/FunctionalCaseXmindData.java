package io.metersphere.functional.xmind.domain;

import io.metersphere.system.dto.sdk.BaseTreeNode;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseXmindData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<FunctionalCaseXmindDTO> functionalCaseList;
    private String moduleName;
    private String moduleId;
    private List<FunctionalCaseXmindData> children = new ArrayList<>();


    public void setItem(LinkedList<BaseTreeNode> modulePathDataList, List<FunctionalCaseXmindDTO> dataList) {
        if (CollectionUtils.isNotEmpty(modulePathDataList) && CollectionUtils.isNotEmpty(dataList)) {
            if (modulePathDataList.size() == 1) {
                this.setData(modulePathDataList.getFirst(), dataList);
            } else {
                BaseTreeNode caseNode = modulePathDataList.getFirst();
                if (caseNode != null) {
                    FunctionalCaseXmindData matchedData = null;
                    for (FunctionalCaseXmindData item : children) {
                        if (StringUtils.equals(item.getModuleId(), caseNode.getId())) {
                            matchedData = item;
                            break;
                        }
                    }
                    if(matchedData == null){
                        matchedData = new FunctionalCaseXmindData();
                        matchedData.setModuleId(caseNode.getId());
                        matchedData.setModuleName(caseNode.getName());
                        this.children.add(matchedData);
                    }
                    modulePathDataList.removeFirst();
                    matchedData.setItem(modulePathDataList,dataList);
                }
            }
        }
    }

    private void setData(BaseTreeNode caseNode, List<FunctionalCaseXmindDTO> dataList) {
        if (caseNode != null && CollectionUtils.isNotEmpty(dataList)) {
            boolean matching = false;
            for (FunctionalCaseXmindData item : children) {
                if (StringUtils.equals(item.getModuleId(), caseNode.getId())) {
                    matching = true;
                    item.setFunctionalCaseList(dataList);
                }
            }
            if (!matching) {
                FunctionalCaseXmindData child = new FunctionalCaseXmindData();
                child.setModuleId(caseNode.getId());
                child.setModuleName(caseNode.getName());
                child.setFunctionalCaseList(dataList);
                this.children.add(child);
            }
        }
    }
}
