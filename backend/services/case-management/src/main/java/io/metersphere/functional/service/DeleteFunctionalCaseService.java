package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistoryExample;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.mapper.TestPlanCaseExecuteHistoryMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.utils.RelationshipEdgeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DeleteFunctionalCaseService {

    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;
    @Resource
    private FunctionalCaseDemandMapper functionalCaseDemandMapper;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;
    @Resource
    private FunctionalCaseRelationshipEdgeMapper functionalCaseRelationshipEdgeMapper;
    @Resource
    private ExtFunctionalCaseRelationshipEdgeMapper extFunctionalCaseRelationshipEdgeMapper;
    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;


    public void deleteFunctionalCaseResource(List<String> ids, String projectId) {
        //TODO 删除各种关联关系？ 1.测试用例(接口/场景/ui/性能)？ 2.关联缺陷(是否需要同步？) 3.关联需求(是否需要同步？) 4.依赖关系？ 5.关联评审？ 6.关联测试计划？ 7.操作记录？ 8.评论？ 9.附件？ 10.自定义字段？ 11.用例基本信息(主表、附属表)？ 12...?
        //1.刪除用例与其他用例关联关系
        FunctionalCaseTestExample caseTestExample = new FunctionalCaseTestExample();
        caseTestExample.createCriteria().andCaseIdIn(ids);
        functionalCaseTestMapper.deleteByExample(caseTestExample);
        //3.删除关联需求
        FunctionalCaseDemandExample functionalCaseDemandExample = new FunctionalCaseDemandExample();
        functionalCaseDemandExample.createCriteria().andCaseIdIn(ids);
        functionalCaseDemandMapper.deleteByExample(functionalCaseDemandExample);
        //4.删除依赖关系
        FunctionalCaseRelationshipEdgeExample relationshipEdgeExample = new FunctionalCaseRelationshipEdgeExample();
        relationshipEdgeExample.createCriteria()
                .andSourceIdIn(ids);
        relationshipEdgeExample.or(
                relationshipEdgeExample.createCriteria()
                        .andTargetIdIn(ids)
        );
        List<FunctionalCaseRelationshipEdge> edgeList = functionalCaseRelationshipEdgeMapper.selectByExample(relationshipEdgeExample);
        if (CollectionUtils.isNotEmpty(edgeList)) {
            List<String> edgeIds = edgeList.stream().map(FunctionalCaseRelationshipEdge::getId).toList();
            edgeIds.forEach(id -> {
                RelationshipEdgeUtils.updateGraphId(id, extFunctionalCaseRelationshipEdgeMapper::getGraphId, extFunctionalCaseRelationshipEdgeMapper::getEdgeByGraphId, extFunctionalCaseRelationshipEdgeMapper::update);
            });
            relationshipEdgeExample.clear();
            relationshipEdgeExample.createCriteria().andIdIn(edgeIds);
            functionalCaseRelationshipEdgeMapper.deleteByExample(relationshipEdgeExample);
        }


        //5.删除关联评审
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andCaseIdIn(ids);
        caseReviewFunctionalCaseMapper.deleteByExample(caseReviewFunctionalCaseExample);

        //8.评论
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdIn(ids);
        functionalCaseCommentMapper.deleteByExample(functionalCaseCommentExample);
        //9.附件 todo 删除关联关系
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdIn(ids);
        functionalCaseAttachmentMapper.deleteByExample(functionalCaseAttachmentExample);
        //删除文件
        FileRequest request = new FileRequest();
        // 删除文件所在目录
        for (String id : ids) {
            request.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, id));
            try {
                FileCenter.getDefaultRepository().deleteFolder(request);
                request.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, id));
                FileCenter.getDefaultRepository().deleteFolder(request);
            } catch (Exception e) {
                LogUtils.error("彻底删除功能用例，文件删除失败：{}", e);
            }
        }
        //10.自定义字段
        FunctionalCaseCustomFieldExample fieldExample = new FunctionalCaseCustomFieldExample();
        fieldExample.createCriteria().andCaseIdIn(ids);
        functionalCaseCustomFieldMapper.deleteByExample(fieldExample);
        //11.用例基本信息
        FunctionalCaseBlobExample blobExample = new FunctionalCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        functionalCaseBlobMapper.deleteByExample(blobExample);
        FunctionalCaseExample caseExample = new FunctionalCaseExample();
        caseExample.createCriteria().andIdIn(ids).andProjectIdEqualTo(projectId);
        functionalCaseMapper.deleteByExample(caseExample);
        //删除评审历史
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdIn(ids);
        caseReviewHistoryMapper.deleteByExample(caseReviewHistoryExample);
        //删除和测试计划的关联关系
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andFunctionalCaseIdIn(ids);
        testPlanFunctionalCaseMapper.deleteByExample(testPlanFunctionalCaseExample);
        //删除执行历史
        TestPlanCaseExecuteHistoryExample testPlanCaseExecuteHistoryExample = new TestPlanCaseExecuteHistoryExample();
        testPlanCaseExecuteHistoryExample.createCriteria().andCaseIdIn(ids);
        testPlanCaseExecuteHistoryMapper.deleteByExample(testPlanCaseExecuteHistoryExample);

    }
}
