package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.MinderLabel;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.domain.FunctionalCaseBlobExample;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.request.MinderReviewFunctionalCasePageRequest;
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
    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;
    @Resource
    private CaseReviewMapper caseReviewMapper;


    /**
     * 功能用例-脑图用例列表查询
     * @param request FunctionalCasePageRequest
     * @param deleted 用例是否删除
     * @return FunctionalMinderTreeDTO
     */
    public FunctionalMinderTreeDTO getFunctionalCasePage(FunctionalCasePageRequest request, boolean deleted) {
        //根据查询条件查询出所有符合的功能用例
        List<FunctionalCasePageDTO> functionalCasePage = functionalCaseService.getFunctionalCasePage(request, deleted);
        List<String> caseIds = functionalCasePage.stream().map(FunctionalCase::getId).distinct().toList();
        Map<String, FunctionalCaseBlob> blobMap = getStringFunctionalCaseBlobMap(caseIds);
        //根据功能用例的模块ID，查出每个模块id父级直到根节点
        List<String> moduleIds = functionalCasePage.stream().map(FunctionalCase::getModuleId).distinct().toList();
        Map<String, List<FunctionalCasePageDTO>> moduleCaseMap = functionalCasePage.stream().collect(Collectors.groupingBy(FunctionalCasePageDTO::getModuleId));
        List<BaseTreeNode> baseTreeNodes = getBaseTreeNodes(moduleIds);
        //判断虚拟节点有无数据
        if (moduleCaseMap.get(ModuleConstants.DEFAULT_NODE_ID) == null && CollectionUtils.isEmpty(baseTreeNodes.get(0).getChildren())) {
            baseTreeNodes.remove(0);
        }
        //构建返回数据，主层级应与模块树层级相同
        List<FunctionalMinderTreeDTO> functionalMinderTreeNodeDTOs = new ArrayList<>();
        buildCaseTree(baseTreeNodes, moduleCaseMap, functionalMinderTreeNodeDTOs, blobMap);
        return getRoot(functionalMinderTreeNodeDTOs);
    }

    private static FunctionalMinderTreeDTO getRoot(List<FunctionalMinderTreeDTO> functionalMinderTreeDTOs) {
        FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
        FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
        rootData.setResource(List.of(MinderLabel.MODULE.toString()));
        rootData.setText(Translator.get("case.minder.all.case"));
        root.setChildren(functionalMinderTreeDTOs);
        root.setData(rootData);
        return root;
    }

    private List<BaseTreeNode> getBaseTreeNodes(List<String> moduleIds) {
        List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(moduleIds);
        //根据模块节点构造模块树
        return functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, true, Translator.get("default.module"));
    }

    private Map<String, FunctionalCaseBlob> getStringFunctionalCaseBlobMap(List<String> caseIds) {
        FunctionalCaseBlobExample functionalCaseBlobExample = new FunctionalCaseBlobExample();
        functionalCaseBlobExample.createCriteria().andIdIn(caseIds);
        List<FunctionalCaseBlob> functionalCaseBlobs = functionalCaseBlobMapper.selectByExampleWithBLOBs(functionalCaseBlobExample);
        return functionalCaseBlobs.stream().collect(Collectors.toMap(FunctionalCaseBlob::getId, t -> t));
    }

    private static void buildCaseTree(List<BaseTreeNode> baseTreeNodes, Map<String, List<FunctionalCasePageDTO>> moduleCaseMap, List<FunctionalMinderTreeDTO> functionalMinderTreeNodeDTOs, Map<String, FunctionalCaseBlob> blobMap) {
        baseTreeNodes.forEach(t -> {
            //构建根节点
            FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTO = getFunctionalMinderTreeNodeDTO(t);
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

    private static FunctionalMinderTreeNodeDTO getFunctionalMinderTreeNodeDTO(BaseTreeNode t) {
        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTO = new FunctionalMinderTreeNodeDTO();
        functionalMinderTreeNodeDTO.setId(t.getId());
        functionalMinderTreeNodeDTO.setText(t.getName());
        functionalMinderTreeNodeDTO.setResource(List.of(MinderLabel.MODULE.toString()));
        return functionalMinderTreeNodeDTO;
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
                functionalMinderTreeNodeChild.setResource(List.of(MinderLabel.CASE.toString()));
                functionalMinderTreeChild.setData(functionalMinderTreeNodeChild);
                //将用例的blob记为Case的children
                setBlobInfo(blobMap, functionalCasePageDTO.getId(), functionalCasePageDTO.getCaseEditType(), functionalMinderTreeChild);
                children.add(functionalMinderTreeChild);
            });
        }
    }

    private static void buildModuleChild(Map<String, List<FunctionalCasePageDTO>> moduleCaseMap, Map<String, FunctionalCaseBlob> blobMap, BaseTreeNode t, List<FunctionalMinderTreeDTO> children) {
        if (CollectionUtils.isNotEmpty(t.getChildren())) {
            t.getChildren().forEach(child->{
                FunctionalMinderTreeDTO functionalMinderTreeDTOChild = new FunctionalMinderTreeDTO();
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChild = getFunctionalMinderTreeNodeDTO(child);
                functionalMinderTreeDTOChild.setData(functionalMinderTreeNodeDTOChild);

                List<FunctionalMinderTreeDTO> childChildren = new ArrayList<>();
                buildCaseChild(moduleCaseMap, blobMap, child, childChildren);
                functionalMinderTreeDTOChild.setChildren(childChildren);
                children.add(functionalMinderTreeDTOChild);
                buildModuleChild(moduleCaseMap, blobMap, child, childChildren);
            });
        }

    }

    /**
     * 用例评审-脑图用例列表查询
     * @param request MinderReviewFunctionalCasePageRequest
     * @param deleted 用例是否删除
     * @param userId userId 只看我的
     * @return FunctionalMinderTreeDTO
     */
    public FunctionalMinderTreeDTO getReviewFunctionalCasePage(MinderReviewFunctionalCasePageRequest request, boolean deleted, String userId) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(request.getReviewId());
        List<ReviewFunctionalCaseDTO> page = caseReviewFunctionalCaseService.page(request, deleted, userId);
        List<String> caseIds = page.stream().map(ReviewFunctionalCaseDTO::getCaseId).toList();
        Map<String, FunctionalCaseBlob> blobMap = getStringFunctionalCaseBlobMap(caseIds);
        List<String> moduleIds = page.stream().map(ReviewFunctionalCaseDTO::getModuleId).toList();
        List<BaseTreeNode> baseTreeNodes = getBaseTreeNodes(moduleIds);
        //判断虚拟节点有无数据
        Map<String, List<ReviewFunctionalCaseDTO>> moduleCaseMap = page.stream().collect(Collectors.groupingBy(ReviewFunctionalCaseDTO::getModuleId));
        if (moduleCaseMap.get(ModuleConstants.DEFAULT_NODE_ID) == null && CollectionUtils.isEmpty(baseTreeNodes.get(0).getChildren())) {
            baseTreeNodes.remove(0);
        }
        //自定义字段
        Map<String, List<FunctionalCaseCustomFieldDTO>> caseCustomFiledMap = functionalCaseService.getCaseCustomFiledMap(caseIds);
        //构建返回数据，主层级应与模块树层级相同
        MinderSearchDTO minderSearchDTO = new MinderSearchDTO();
        minderSearchDTO.setBaseTreeNodes(baseTreeNodes);
        minderSearchDTO.setModuleCaseMap(moduleCaseMap);
        minderSearchDTO.setBlobMap(blobMap);
        minderSearchDTO.setCustomFieldMap(caseCustomFiledMap);
        minderSearchDTO.setReviewPassRule(caseReview.getReviewPassRule());
        minderSearchDTO.setViewFlag(request.isViewFlag());
        minderSearchDTO.setViewResult(request.isViewResult());
        List<FunctionalMinderTreeDTO> functionalMinderTreeDTOs = buildReviewCaseTree(minderSearchDTO);
        return getRoot(functionalMinderTreeDTOs);

    }

    private List<FunctionalMinderTreeDTO>  buildReviewCaseTree(MinderSearchDTO minderSearchDTO) {
        List<FunctionalMinderTreeDTO> functionalMinderTreeNodeDTOs = new ArrayList<>();
        minderSearchDTO.getBaseTreeNodes().forEach(t -> {
            //构建根节点
            FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTO = getFunctionalMinderTreeNodeDTO(t);
            functionalMinderTreeDTO.setData(functionalMinderTreeNodeDTO);

            List<FunctionalMinderTreeDTO> children = new ArrayList<>();
            //如果当前节点有用例，则用例是他的子节点
            minderSearchDTO.setBaseTreeNode(t);
            buildReviewCaseChild(minderSearchDTO, children);
            //查询当前节点下的子模块节点
            buildReviewModuleChild(minderSearchDTO, children);
            functionalMinderTreeDTO.setChildren(children);
            functionalMinderTreeNodeDTOs.add(functionalMinderTreeDTO);
        });
        return functionalMinderTreeNodeDTOs;
    }

    private void buildReviewModuleChild(MinderSearchDTO minderSearchDTO, List<FunctionalMinderTreeDTO> children) {
        List<BaseTreeNode> baseTreeNodes = minderSearchDTO.getBaseTreeNode().getChildren();
        if (CollectionUtils.isNotEmpty(baseTreeNodes)) {
            baseTreeNodes.forEach(child->{
                FunctionalMinderTreeDTO functionalMinderTreeDTOChild = new FunctionalMinderTreeDTO();
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChild = getFunctionalMinderTreeNodeDTO(child);
                functionalMinderTreeDTOChild.setData(functionalMinderTreeNodeDTOChild);
                minderSearchDTO.setBaseTreeNode(child);
                List<FunctionalMinderTreeDTO> childChildren = new ArrayList<>();
                buildReviewCaseChild(minderSearchDTO, childChildren);
                functionalMinderTreeDTOChild.setChildren(childChildren);
                children.add(functionalMinderTreeDTOChild);
                buildReviewModuleChild(minderSearchDTO, childChildren);
            });
        }
    }

    private void buildReviewCaseChild(MinderSearchDTO minderSearchDTO, List<FunctionalMinderTreeDTO> children) {
        Map<String, List<ReviewFunctionalCaseDTO>> moduleCaseMap = minderSearchDTO.getModuleCaseMap();
        BaseTreeNode baseTreeNode = minderSearchDTO.getBaseTreeNode();
        String baseTreeNodeId = baseTreeNode.getId();
        if (moduleCaseMap.get(baseTreeNodeId) != null && CollectionUtils.isNotEmpty(moduleCaseMap.get(baseTreeNodeId))) {
            List<ReviewFunctionalCaseDTO> reviewFunctionalCaseDTOS = moduleCaseMap.get(baseTreeNodeId);
            reviewFunctionalCaseDTOS.forEach(reviewFunctionalCaseDTO -> {
                FunctionalMinderTreeDTO functionalMinderTreeChild = new FunctionalMinderTreeDTO();
                //加载用例为children
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChild = new FunctionalMinderTreeNodeDTO();
                functionalMinderTreeNodeChild.setId(reviewFunctionalCaseDTO.getCaseId());
                functionalMinderTreeNodeChild.setText(reviewFunctionalCaseDTO.getName());
                List<FunctionalCaseCustomFieldDTO> functionalCaseCustomFieldDTOS = minderSearchDTO.getCustomFieldMap().get(reviewFunctionalCaseDTO.getCaseId());
                if (CollectionUtils.isNotEmpty(functionalCaseCustomFieldDTOS)) {
                    List<FunctionalCaseCustomFieldDTO> list = functionalCaseCustomFieldDTOS.stream().filter(customField -> customField.getFieldName().equals(Translator.get("custom_field.functional_priority"))).toList();
                    if (CollectionUtils.isNotEmpty(list)) {
                        functionalMinderTreeNodeChild.setPriority(list.get(0).getDefaultValue());
                    }
                }
                if (minderSearchDTO.isViewFlag()&& minderSearchDTO.isViewResult() && StringUtils.equalsIgnoreCase(minderSearchDTO.getReviewPassRule(), CaseReviewPassRule.MULTIPLE.toString())  ) {
                    functionalMinderTreeNodeChild.setStatus(reviewFunctionalCaseDTO.getStatus());
                }
                functionalMinderTreeNodeChild.setResource(List.of(MinderLabel.CASE.toString()));
                functionalMinderTreeChild.setData(functionalMinderTreeNodeChild);
                setBlobInfo(minderSearchDTO.getBlobMap(), reviewFunctionalCaseDTO.getCaseId(), reviewFunctionalCaseDTO.getCaseEditType(), functionalMinderTreeChild);
                children.add(functionalMinderTreeChild);
            });
        }
    }

    private static void setBlobInfo(Map<String, FunctionalCaseBlob> blobMap, String caseId, String caseEditType, FunctionalMinderTreeDTO functionalMinderTreeChild) {
        //将用例的blob记为Case的children
        List<FunctionalMinderTreeDTO> functionalMinderTreeNodeChildChild = new ArrayList<>();
        FunctionalCaseBlob functionalCaseBlob = blobMap.get(caseId);
        if (functionalCaseBlob != null) {
            if (functionalCaseBlob.getPrerequisite() != null) {
                FunctionalMinderTreeDTO functionalMinderTreeDTOPre = new FunctionalMinderTreeDTO();
                //前置条件
                FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildPre = new FunctionalMinderTreeNodeDTO();
                String prerequisite = new String(functionalCaseBlob.getPrerequisite(), StandardCharsets.UTF_8);
                functionalMinderTreeNodeChildPre.setText(prerequisite);
                functionalMinderTreeNodeChildPre.setResource(List.of(MinderLabel.PREREQUISITE.toString()));
                functionalMinderTreeDTOPre.setData(functionalMinderTreeNodeChildPre);
                functionalMinderTreeNodeChildChild.add(functionalMinderTreeDTOPre);
            }

            //步骤描述
            if (StringUtils.equalsIgnoreCase(caseEditType, "TEXT")) {
                if (functionalCaseBlob.getTextDescription() != null) {
                    //1.文本描述(一个)
                    FunctionalMinderTreeDTO functionalMinderTreeDTOText = new FunctionalMinderTreeDTO();
                    FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildText = new FunctionalMinderTreeNodeDTO();
                    String textDescription = new String(functionalCaseBlob.getTextDescription(), StandardCharsets.UTF_8);
                    functionalMinderTreeNodeChildText.setText(textDescription);
                    functionalMinderTreeNodeChildText.setResource(List.of(MinderLabel.TEXT_DESCRIPTION.toString()));
                    functionalMinderTreeDTOText.setData(functionalMinderTreeNodeChildText);
                    List<FunctionalMinderTreeDTO> functionalMinderTreeNodeChildTextChildren = new ArrayList<>();
                    FunctionalMinderTreeDTO functionalMinderTreeNodeChildTextChild = new FunctionalMinderTreeDTO();
                    FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChildTextChild = new FunctionalMinderTreeNodeDTO();
                    String expectedResult = new String(functionalCaseBlob.getExpectedResult(), StandardCharsets.UTF_8);
                    functionalMinderTreeNodeDTOChildTextChild.setText(expectedResult);
                    functionalMinderTreeNodeDTOChildTextChild.setResource(List.of(MinderLabel.EXPECTED_RESULT.toString()));
                    functionalMinderTreeNodeChildTextChild.setData(functionalMinderTreeNodeDTOChildTextChild);
                    functionalMinderTreeNodeChildTextChildren.add(functionalMinderTreeNodeChildTextChild);
                    functionalMinderTreeDTOText.setChildren(functionalMinderTreeNodeChildTextChildren);
                }
            } else {
                if (functionalCaseBlob.getSteps() != null) {
                    //2.步骤描述(多个)
                    String steps = new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8);
                    List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(steps, FunctionalCaseStepDTO.class);
                    for (FunctionalCaseStepDTO functionalCaseStepDTO : functionalCaseStepDTOS) {
                        FunctionalMinderTreeDTO functionalMinderTreeDTOText = new FunctionalMinderTreeDTO();
                        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeChildStep = new FunctionalMinderTreeNodeDTO();
                        functionalMinderTreeNodeChildStep.setText(functionalCaseStepDTO.getDesc());
                        functionalMinderTreeNodeChildStep.setResource(List.of(MinderLabel.TEXT_DESCRIPTION.toString()));

                        List<FunctionalMinderTreeDTO> functionalMinderTreeNodeChildTextChildren = new ArrayList<>();
                        FunctionalMinderTreeDTO functionalMinderTreeNodeChildTextChild = new FunctionalMinderTreeDTO();
                        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTOChildTextChild = new FunctionalMinderTreeNodeDTO();
                        functionalMinderTreeNodeDTOChildTextChild.setText(functionalCaseStepDTO.getResult());
                        functionalMinderTreeNodeDTOChildTextChild.setResource(List.of(MinderLabel.EXPECTED_RESULT.toString()));
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
                functionalMinderTreeNodeChildRemark.setResource(List.of(MinderLabel.DESCRIPTION.toString()));
                functionalMinderTreeDTORemark.setData(functionalMinderTreeNodeChildRemark);
                functionalMinderTreeNodeChildChild.add(functionalMinderTreeDTORemark);
            }
        }
        functionalMinderTreeChild.setChildren(functionalMinderTreeNodeChildChild);
    }
}
