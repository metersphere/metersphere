package io.metersphere.api.dto.definition.request.assertions;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "Assertions")
public class MsAssertions extends MsTestElement {
    private List<MsAssertionRegex> regex;
    private List<MsAssertionJsonPath> jsonPath;
    private List<MsAssertionJSR223> jsr223;
    private List<MsAssertionXPath2> xpath2;
    private MsAssertionDuration duration;
    private String type = "Assertions";

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
//        final HashTree testPlanTree = tree.add(getPlan());
//        if (CollectionUtils.isNotEmpty(hashTree)) {
//            hashTree.forEach(el -> {
//                el.toHashTree(testPlanTree, el.getHashTree());
//            });
//        }
    }

}
