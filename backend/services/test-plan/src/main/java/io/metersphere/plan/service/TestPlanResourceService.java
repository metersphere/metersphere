package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.dto.ModuleSortCountResultDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

//测试计划关联表 通用方法
@Service
@Transactional(rollbackFor = Exception.class)
public abstract class TestPlanResourceService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;

    protected static final long DEFAULT_NODE_INTERVAL_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

    public abstract long getNextOrder(String testPlanId);

    public abstract void updatePos(String id, long pos);

    public abstract void refreshPos(String testPlanId);

    public abstract int deleteBatchByTestPlanId(List<String> testPlanIdList);

    private static final String MOVE_POS_OPERATOR_LESS = "lessThan";
    private static final String MOVE_POS_OPERATOR_MORE = "moreThan";
    private static final String DRAG_NODE_NOT_EXIST = "drag_node.not.exist";

    /**
     * 关联资源od
     *
     * @return
     */
    public TestPlanAssociationResponse association(
            String resourceType,
            TestPlanAssociationRequest request,
            @Validated LogInsertModule logInsertModule,
            Function<ResourceSelectParam, List<String>> selectByResourceIdFunc,
            Function<ResourceSelectParam, List<String>> selectByModuleIdFunc,
            Consumer<TestPlanResourceAssociationParam> saveResourceFunc) {
        TestPlanAssociationResponse response = new TestPlanAssociationResponse();
        if (request.isEmpty()) {
            return response;
        }
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(request.getTestPlanId());
        boolean repeatCase = testPlanConfig.getRepeatCase();
        List<String> associationIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            //获取有效ID
            associationIdList.addAll(
                    selectByResourceIdFunc.apply(
                            new ResourceSelectParam(request.getTestPlanId(), request.getSelectIds(), null, repeatCase, request.getOrderString())));
        }
        if (CollectionUtils.isNotEmpty(request.getSelectModuleIds())) {
            //获取有效ID
            associationIdList.addAll(
                    selectByModuleIdFunc.apply(
                            new ResourceSelectParam(request.getTestPlanId(), null, request.getSelectModuleIds(), repeatCase, request.getOrderString())));
        }
        associationIdList = new ArrayList<>(associationIdList.stream().distinct().toList());
        associationIdList.removeAll(request.getExcludeIds());
        if (CollectionUtils.isNotEmpty(associationIdList)) {
            TestPlanResourceAssociationParam associationParam = new TestPlanResourceAssociationParam(associationIdList, testPlan.getProjectId(), testPlan.getId(), testPlan.getNum(), logInsertModule.getOperator());
            saveResourceFunc.accept(associationParam);
            response.setAssociationCount(associationIdList.size());
            testPlanResourceLogService.saveAddLog(testPlan, new ResourceLogInsertModule(resourceType, logInsertModule));
        }
        return response;
    }

    /**
     * 构建节点排序的参数
     *
     * @param request           拖拽的前端请求参数
     * @param selectIdNodeFunc  通过id查询节点的函数
     * @param selectPosNodeFunc 通过parentId和pos运算符查询节点的函数
     * @return
     */
    public AssociationNodeSortDTO getNodeSortDTO(ResourceSortRequest request, Function<String, AssociationNode> selectIdNodeFunc, Function<NodeSortQueryParam, AssociationNode> selectPosNodeFunc) {
        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
            //两种节点不能一样
            throw new MSException(Translator.get("invalid_parameter"));
        }

        AssociationNode dragNode = selectIdNodeFunc.apply(request.getDragNodeId());
        if (dragNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDragNodeId());
        }

        AssociationNode dropNode = selectIdNodeFunc.apply(request.getDropNodeId());
        if (dropNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDropNodeId());
        }

        AssociationNode previousNode;
        AssociationNode nextNode;

        if (request.getDropPosition() == 1) {
            //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
            previousNode = dropNode;

            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setParentId(request.getTestPlanId());
            sortParam.setPos(previousNode.getPos());
            sortParam.setOperator(MOVE_POS_OPERATOR_MORE);
            nextNode = selectPosNodeFunc.apply(sortParam);
        } else if (request.getDropPosition() == -1) {
            //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
            nextNode = dropNode;
            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setPos(nextNode.getPos());
            sortParam.setParentId(request.getTestPlanId());
            sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
            previousNode = selectPosNodeFunc.apply(sortParam);
        } else {
            throw new MSException(Translator.get("invalid_parameter"));
        }

        return new AssociationNodeSortDTO(request.getTestPlanId(), dragNode, previousNode, nextNode);
    }

    //排序
    public void sort(AssociationNodeSortDTO sortDTO) {

        // 获取相邻节点
        AssociationNode previousNode = sortDTO.getPreviousNode();
        AssociationNode nextNode = sortDTO.getNextNode();

        ModuleSortCountResultDTO countResultDTO = NodeSortUtils.countModuleSort(
                previousNode == null ? -1 : previousNode.getPos(),
                nextNode == null ? -1 : nextNode.getPos());

        updatePos(sortDTO.getSortNode().getId(), countResultDTO.getPos());
        if (countResultDTO.isRefreshPos()) {
            refreshPos(sortDTO.getTestPlanId());
        }
    }

}
