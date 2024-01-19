package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.domain.FunctionalCaseBlobExample;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能用例脑图
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseMinderService {

    @Resource
    private FunctionalCaseService functionalCaseService;

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;
    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;


    public FunctionalMinderTreeDTO getFunctionalCasePage(FunctionalCasePageRequest request, boolean deleted) {
        //根据查询条件查询出所有符合的功能用例
        List<FunctionalCasePageDTO> functionalCasePage = functionalCaseService.getFunctionalCasePage(request, deleted);
        List<String> caseIds = functionalCasePage.stream().map(FunctionalCase::getId).distinct().toList();
        FunctionalCaseBlobExample functionalCaseBlobExample = new FunctionalCaseBlobExample();
        functionalCaseBlobExample.createCriteria().andIdIn(caseIds);
        List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExampleWithBLOBs(functionalCaseBlobExample);
        Map<String, FunctionalCaseBlob> blobMap = functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, t -> t));
        //根据功能用例的模块ID，查出每个模块id父级直到根节点
        List<String> moduleIds = functionalCasePage.stream().map(FunctionalCase::getModuleId).distinct().toList();
        Map<String, List<FunctionalCasePageDTO>> moduleCaseMap = functionalCasePage.stream().collect(Collectors.groupingBy(FunctionalCasePageDTO::getModuleId));
        List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(moduleIds);
        //根据模块节点构造模块树
        List<BaseTreeNode> baseTreeNodes = functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, true, Translator.get("default.module"));
        //判断虚拟节点有无数据
        if (moduleCaseMap.get(ModuleConstants.DEFAULT_NODE_ID) == null && CollectionUtils.isEmpty(baseTreeNodes.get(0).getChildren())) {
            baseTreeNodes.remove(0);
        }
        //构建返回数据，主层级应与模块树层级相同
        List<FunctionalMinderTreeDTO> functionalMinderTreeNodeDTOs = new ArrayList<>();
        buildCaseTree(baseTreeNodes, moduleCaseMap, functionalMinderTreeNodeDTOs, blobMap);
        FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
        FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
        rootData.setResource(List.of("模块"));
        rootData.setText("全部用例");
        root.setChildren(functionalMinderTreeNodeDTOs);
        root.setData(rootData);
        return root;
    }

    private static void buildCaseTree(List<BaseTreeNode> baseTreeNodes, Map<String, List<FunctionalCasePageDTO>> moduleCaseMap, List<FunctionalMinderTreeDTO> functionalMinderTreeNodeDTOs, Map<String, FunctionalCaseBlob> blobMap) {
        baseTreeNodes.forEach(t -> {
            //构建根节点
            FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTO = new FunctionalMinderTreeNodeDTO();
            functionalMinderTreeNodeDTO.setId(t.getId());
            functionalMinderTreeNodeDTO.setText(t.getName());
            functionalMinderTreeNodeDTO.setResource(List.of("模块"));
            functionalMinderTreeDTO.setData(functionalMinderTreeNodeDTO);

            List<FunctionalMinderTreeDTO> children = new ArrayList<>();
            //如果当前节点有用例，则用例是他的子节点
            buildCaseChild(moduleCaseMap, blobMap, t, children);
            //查询当前节点下的子模块节点
            buildModuleChild(moduleCaseMap, blobMap, t, children);
            functionalMinderTreeDTO.setChildren(children);
            functionalMinderTreeNodeDTOs.add(functionalMinderTreeDTO);
        });
    }

    private static void buildCaseChild(Map<String, List<FunctionalCasePageDTO>> moduleCaseMap, Map<String, FunctionalCaseBlob> blobMap, BaseTreeNode t, List<FunctionalMinderTreeDTO> children) {
        if (moduleCaseMap.get(t.getId()) != null && CollectionUtils.isNotEmpty(moduleCaseMap.get(t.getId()))) {
            List<FunctionalCasePageDTO> functionalCasePageDTOS = moduleCaseMap.get(t.getId());
            functionalCasePageDTOS.forEach(functionalCasePageDTO -> {
                FunctionalMinderTreeDTO functionalMinderTreeChild = new FunctionalMinderTreeDTO();
                //加载用例为children
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChild = new FunctionalMinderTreeNodeDTO();
                functionalMinderTreeNodeChild.setId(functionalCasePageDTO.getId());
                functionalMinderTreeNodeChild.setText(functionalCasePageDTO.getName());
                if (CollectionUtils.isNotEmpty(functionalCasePageDTO.getCustomFields())) {
                    List<FunctionalCaseCustomFieldDTO> list = functionalCasePageDTO.getCustomFields().stream().filter(customField -> customField.getFieldName().equals(Translator.get("custom_field.functional_priority"))).toList();
                    if (CollectionUtils.isNotEmpty(list)) {
                        functionalMinderTreeNodeChild.setPriority(list.get(0).getDefaultValue());
                    }
                }
                functionalMinderTreeNodeChild.setResource(List.of("用例"));
                //将用例的blob记为Case的children
                List<FunctionalMinderTreeDTO> functionalMinderTreeNodeChildChild = new ArrayList<>();
                FunctionalCaseBlob functionalCaseBlob = blobMap.get(functionalCasePageDTO.getId());
                if (functionalCaseBlob != null) {
                    if (functionalCaseBlob.getPrerequisite() != null) {
                        FunctionalMinderTreeDTO functionalMinderTreeDTOPre = new FunctionalMinderTreeDTO();
                        //前置条件
                        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildPre = new FunctionalMinderTreeNodeDTO();
                        String prerequisite = new String(functionalCaseBlob.getPrerequisite(), StandardCharsets.UTF_8);
                        functionalMinderTreeNodeChildPre.setText(prerequisite);
                        functionalMinderTreeNodeChildPre.setResource(List.of("前置条件"));
                        functionalMinderTreeDTOPre.setData(functionalMinderTreeNodeChildPre);
                        functionalMinderTreeNodeChildChild.add(functionalMinderTreeDTOPre);
                    }

                    //步骤描述
                    if (StringUtils.equalsIgnoreCase(functionalCasePageDTO.getCaseEditType(),"TEXT") ) {
                        if (functionalCaseBlob.getTextDescription()!=null) {
                            //1.文本描述(一个)
                            FunctionalMinderTreeDTO functionalMinderTreeDTOText = new FunctionalMinderTreeDTO();
                            FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildText= new FunctionalMinderTreeNodeDTO();
                            String textDescription = new String(functionalCaseBlob.getTextDescription(), StandardCharsets.UTF_8);
                            functionalMinderTreeNodeChildText.setText(textDescription);
                            functionalMinderTreeNodeChildText.setResource(List.of("步骤描述"));
                            functionalMinderTreeDTOText.setData(functionalMinderTreeNodeChildText);
                            List<FunctionalMinderTreeDTO>functionalMinderTreeNodeChildTextChildren = new ArrayList<>();
                            FunctionalMinderTreeDTO functionalMinderTreeNodeChildTextChild = new FunctionalMinderTreeDTO();
                            FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChildTextChild= new FunctionalMinderTreeNodeDTO();
                            String expectedResult = new String(functionalCaseBlob.getExpectedResult(), StandardCharsets.UTF_8);
                            functionalMinderTreeNodeDTOChildTextChild.setText(expectedResult);
                            functionalMinderTreeNodeDTOChildTextChild.setResource(List.of("预期结果"));
                            functionalMinderTreeNodeChildTextChild.setData(functionalMinderTreeNodeDTOChildTextChild);
                            functionalMinderTreeNodeChildTextChildren.add(functionalMinderTreeNodeChildTextChild);
                            functionalMinderTreeDTOText.setChildren(functionalMinderTreeNodeChildTextChildren);
                        }
                    }else {
                        if (functionalCaseBlob.getSteps() != null) {
                            //2.步骤描述(多个)
                            String steps = new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8);
                            List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(steps, FunctionalCaseStepDTO.class);
                            for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
                                FunctionalMinderTreeDTO functionalMinderTreeDTOText = new FunctionalMinderTreeDTO();
                                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildStep = new FunctionalMinderTreeNodeDTO();
                                functionalMinderTreeNodeChildStep.setText(functionalCaseStepDTO.getDesc());
                                functionalMinderTreeNodeChildStep.setResource(List.of("步骤描述"));

                                List<FunctionalMinderTreeDTO>functionalMinderTreeNodeChildTextChildren = new ArrayList<>();
                                FunctionalMinderTreeDTO functionalMinderTreeNodeChildTextChild = new FunctionalMinderTreeDTO();
                                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChildTextChild= new FunctionalMinderTreeNodeDTO();
                                functionalMinderTreeNodeDTOChildTextChild.setText(functionalCaseStepDTO.getResult());
                                functionalMinderTreeNodeDTOChildTextChild.setResource(List.of("预期结果"));
                                functionalMinderTreeNodeChildTextChild.setData(functionalMinderTreeNodeDTOChildTextChild);
                                functionalMinderTreeNodeChildTextChildren.add(functionalMinderTreeNodeChildTextChild);

                                functionalMinderTreeDTOText.setData(functionalMinderTreeNodeChildStep);
                                functionalMinderTreeDTOText.setChildren(functionalMinderTreeNodeChildTextChildren);
                                functionalMinderTreeNodeChildChild.add(functionalMinderTreeDTOText);
                            }
                        }
                    }

                    //备注
                    if (functionalCaseBlob.getDescription() != null) {
                        FunctionalMinderTreeDTO functionalMinderTreeDTORemark = new FunctionalMinderTreeDTO();
                        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildRemark = new FunctionalMinderTreeNodeDTO();
                        String description = new String(functionalCaseBlob.getDescription(), StandardCharsets.UTF_8);
                        functionalMinderTreeNodeChildRemark.setText(description);
                        functionalMinderTreeNodeChildRemark.setResource(List.of("备注"));
                        functionalMinderTreeDTORemark.setData(functionalMinderTreeNodeChildRemark);
                        functionalMinderTreeNodeChildChild.add(functionalMinderTreeDTORemark);
                    }
                }
                functionalMinderTreeChild.setData(functionalMinderTreeNodeChild);
                functionalMinderTreeChild.setChildren(functionalMinderTreeNodeChildChild);
                children.add(functionalMinderTreeChild);
            });
        }
    }

    private static void buildModuleChild(Map<String, List<FunctionalCasePageDTO>> moduleCaseMap, Map<String, FunctionalCaseBlob> blobMap, BaseTreeNode t, List<FunctionalMinderTreeDTO> children) {
        if (CollectionUtils.isNotEmpty(t.getChildren())) {
            t.getChildren().forEach(child->{
                FunctionalMinderTreeDTO functionalMinderTreeDTOChild = new FunctionalMinderTreeDTO();
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChild = new FunctionalMinderTreeNodeDTO();
                functionalMinderTreeNodeDTOChild.setId(child.getId());
                functionalMinderTreeNodeDTOChild.setText(child.getName());
                functionalMinderTreeNodeDTOChild.setResource(List.of("模块"));
                functionalMinderTreeDTOChild.setData(functionalMinderTreeNodeDTOChild);

                List<FunctionalMinderTreeDTO> childChildren = new ArrayList<>();
                buildCaseChild(moduleCaseMap, blobMap, child, childChildren);
                functionalMinderTreeDTOChild.setChildren(childChildren);
                children.add(functionalMinderTreeDTOChild);
                buildModuleChild(moduleCaseMap, blobMap, child, childChildren);
            });
        }

    }

}
