package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.MinderLabel;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.functional.request.MinderReviewFunctionalCasePageRequest;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;


    /**
     * 功能用例-脑图用例列表查询
     *
     * @param deleted 用例是否删除
     * @return FunctionalMinderTreeDTO
     */
    public List<FunctionalMinderTreeDTO> getMindFunctionalCase(FunctionalCaseMindRequest request, boolean deleted) {
        List<FunctionalMinderTreeDTO> list = new ArrayList<>();
        //查出当前模块下的所有用例
        List<FunctionalCaseMindDTO> functionalCaseMindDTOList = extFunctionalCaseMapper.getMinderCaseList(request, deleted);
        //构造父子级数据
        for (FunctionalCaseMindDTO functionalCaseMindDTO : functionalCaseMindDTOList) {
            FunctionalMinderTreeDTO root = new FunctionalMinderTreeDTO();
            FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
            rootData.setId(functionalCaseMindDTO.getId());
            rootData.setText(functionalCaseMindDTO.getName());
            rootData.setPriority(functionalCaseMindDTO.getPriority());
            rootData.setStatus(functionalCaseMindDTO.getReviewStatus());
            rootData.setResource(List.of(MinderLabel.CASE.toString(), functionalCaseMindDTO.getPriority()));
            List<FunctionalMinderTreeDTO> children = buildChildren(functionalCaseMindDTO);
            root.setChildren(children);
            root.setData(rootData);
            list.add(root);
        }
        return list;
    }

    private List<FunctionalMinderTreeDTO> buildChildren(FunctionalCaseMindDTO functionalCaseMindDTO) {
        List<FunctionalMinderTreeDTO> children = new ArrayList<>();
        if (functionalCaseMindDTO.getSteps() != null) {
            String stepText = new String(functionalCaseMindDTO.getSteps(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO stepFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(stepText, MinderLabel.TEXT_DESCRIPTION.toString());
            children.add(stepFunctionalMinderTreeDTO);
        }
        if (functionalCaseMindDTO.getExpectedResult() != null) {
            String expectedResultText = new String(functionalCaseMindDTO.getExpectedResult(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO expectedResultFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(expectedResultText, MinderLabel.EXPECTED_RESULT.toString());
            children.add(expectedResultFunctionalMinderTreeDTO);
        }
        if (functionalCaseMindDTO.getPrerequisite() != null) {
            String prerequisiteText = new String(functionalCaseMindDTO.getPrerequisite(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO prerequisiteFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(prerequisiteText, MinderLabel.PREREQUISITE.toString());
            children.add(prerequisiteFunctionalMinderTreeDTO);
        }
        if (functionalCaseMindDTO.getDescription() != null) {
            String descriptionText = new String(functionalCaseMindDTO.getDescription(), StandardCharsets.UTF_8);
            FunctionalMinderTreeDTO descriptionFunctionalMinderTreeDTO = getFunctionalMinderTreeDTO(descriptionText, MinderLabel.DESCRIPTION.toString());
            children.add(descriptionFunctionalMinderTreeDTO);
        }
        return children;
    }

    @NotNull
    private static FunctionalMinderTreeDTO getFunctionalMinderTreeDTO(String text, String resource) {
        FunctionalMinderTreeDTO functionalMinderTreeDTO = new FunctionalMinderTreeDTO();
        FunctionalMinderTreeNodeDTO rootData = new FunctionalMinderTreeNodeDTO();
        rootData.setText(text);
        rootData.setResource(List.of(resource));
        functionalMinderTreeDTO.setChildren(new ArrayList<>());
        functionalMinderTreeDTO.setData(rootData);
        return functionalMinderTreeDTO;
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
        return functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, true, Translator.get("functional_case.module.default.name"));
    }


    private static FunctionalMinderTreeNodeDTO getFunctionalMinderTreeNodeDTO(BaseTreeNode t) {
        FunctionalMinderTreeNodeDTO functionalMinderTreeNodeDTO = new FunctionalMinderTreeNodeDTO();
        functionalMinderTreeNodeDTO.setId(t.getId());
        functionalMinderTreeNodeDTO.setText(t.getName());
        functionalMinderTreeNodeDTO.setResource(List.of(MinderLabel.MODULE.toString()));
        return functionalMinderTreeNodeDTO;
    }

    /**
     * 用例评审-脑图用例列表查询
     *
     * @param request MinderReviewFunctionalCasePageRequest
     * @param deleted 用例是否删除
     * @param userId  userId 只看我的
     * @return FunctionalMinderTreeDTO
     */
    public FunctionalMinderTreeDTO getReviewFunctionalCasePage(MinderReviewFunctionalCasePageRequest request, boolean deleted, String userId) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(request.getReviewId());
        List<ReviewFunctionalCaseDTO> page = caseReviewFunctionalCaseService.page(request, deleted, userId, null);
        List<String> caseIds = page.stream().map(ReviewFunctionalCaseDTO::getCaseId).toList();
        List<String> moduleIds = page.stream().map(ReviewFunctionalCaseDTO::getModuleId).toList();
        List<BaseTreeNode> baseTreeNodes = getBaseTreeNodes(moduleIds);
        //判断虚拟节点有无数据
        Map<String, List<ReviewFunctionalCaseDTO>> moduleCaseMap = page.stream().collect(Collectors.groupingBy(ReviewFunctionalCaseDTO::getModuleId));
        if (moduleCaseMap.get(ModuleConstants.DEFAULT_NODE_ID) == null && CollectionUtils.isEmpty(baseTreeNodes.get(0).getChildren())) {
            baseTreeNodes.remove(0);
        }
        //自定义字段
        Map<String, List<FunctionalCaseCustomFieldDTO>> caseCustomFiledMap = functionalCaseService.getCaseCustomFiledMap(caseIds, request.getProjectId());
        //构建返回数据，主层级应与模块树层级相同
        MinderSearchDTO minderSearchDTO = new MinderSearchDTO();
        minderSearchDTO.setBaseTreeNodes(baseTreeNodes);
        minderSearchDTO.setModuleCaseMap(moduleCaseMap);
        minderSearchDTO.setCustomFieldMap(caseCustomFiledMap);
        minderSearchDTO.setReviewPassRule(caseReview.getReviewPassRule());
        minderSearchDTO.setViewFlag(request.isViewFlag());
        minderSearchDTO.setViewResult(request.isViewResult());
        List<FunctionalMinderTreeDTO> functionalMinderTreeDTOs = buildReviewCaseTree(minderSearchDTO);
        return getRoot(functionalMinderTreeDTOs);

    }

    private List<FunctionalMinderTreeDTO> buildReviewCaseTree(MinderSearchDTO minderSearchDTO) {
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
            baseTreeNodes.forEach(child -> {
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
                if (minderSearchDTO.isViewFlag() && minderSearchDTO.isViewResult() && StringUtils.equalsIgnoreCase(minderSearchDTO.getReviewPassRule(), CaseReviewPassRule.MULTIPLE.toString())) {
                    functionalMinderTreeNodeChild.setStatus(reviewFunctionalCaseDTO.getStatus());
                }
                functionalMinderTreeNodeChild.setResource(List.of(MinderLabel.CASE.toString()));
                functionalMinderTreeChild.setData(functionalMinderTreeNodeChild);
                children.add(functionalMinderTreeChild);
            });
        }
    }
}
