// 测试计划模块树
export const GetTestPlanModuleUrl = `/test-plan/module/tree`;
// 添加测试计划模块树
export const addTestPlanModuleUrl = `/test-plan/module/add`;
// 更新计划测试模块树
export const updateTestPlanModuleUrl = '/test-plan/module/update';
// 移动计划测试模块树
export const MoveTestPlanModuleUrl = '/test-plan/module/move';
// 删除计划测试模块树
export const DeleteTestPlanModuleUrl = '/test-plan/module/delete';
// 测试计划模块树数量
export const GetTestPlanModuleCountUrl = '/test-plan/module/count';
// 测试计划列表
export const GetTestPlanListUrl = '/test-plan/page';
// 测试计划列表 (无分页)
export const GetTestPlanListWithoutPageUrl = '/test-plan/test-plan-list';
// 创建测试计划
export const AddTestPlanUrl = '/test-plan/add';
// 功能用例列表
export const GetTestPlanCaseListUrl = '/test-plan/association/page';
// 获取测试计划详情
export const GetTestPlanDetailUrl = '/test-plan';
// 更新测试计划
export const UpdateTestPlanUrl = '/test-plan/update';
// 批量删除测试计划
export const batchDeletePlanUrl = '/test-plan/batch-delete';
// 删除测试计划
export const deletePlanUrl = '/test-plan/delete';
// 测试计划批量编辑
export const BatchEditTestPlanUrl = '/test-plan/batch-edit';
// 获取统计数量
export const getStatisticalCountUrl = '/test-plan/getCount';
// 归档
export const archivedPlanUrl = '/test-plan/archived';
// 批量复制
export const batchCopyPlanUrl = '/test-plan/batch-copy';
// 批量移动
export const batchMovePlanUrl = '/test-plan/batch-move';
// 批量归档
export const batchArchivedPlanUrl = '/test-plan/batch-archived';
// 计划详情缺陷管理列表
export const planDetailBugPageUrl = '/test-plan/bug/page';
// 关注测试计划
export const followPlanUrl = '/test-plan/edit/follower';
// 生成报告
export const GenerateReportUrl = '/test-plan/report/auto-gen';
// 复制测试计划
export const copyTestPlanUrl = '/test-plan/copy';
// 测试计划通过率执行进度
export const planPassRateUrl = '/test-plan/statistics';
// 计划详情-功能用例列表
export const GetPlanDetailFeatureCaseListUrl = '/test-plan/functional/case/page';
// 计划详情-功能用例-获取模块数量
export const GetFeatureCaseModuleCountUrl = '/test-plan/functional/case/module/count';
// 计划详情-功能用例模块树
export const GetFeatureCaseModuleUrl = '/test-plan/functional/case/tree';
// 计划详情-功能用例列表-拖拽排序
export const SortFeatureCaseUrl = '/test-plan/functional/case/sort';
// 计划详情-功能用例-取消关联用例
export const DisassociateCaseUrl = '/test-plan/functional/case/disassociate';
// 计划详情-功能用例-批量取消关联用例
export const BatchDisassociateCaseUrl = '/test-plan/functional/case/batch/disassociate';
// 计划详情-功能用例-执行
export const RunFeatureCaseUrl = '/test-plan/functional/case/run';
// 计划详情-功能用例列表-批量移动
export const BatchMoveFeatureCaseUrl = '/test-plan/functional/case/batch/move';
// 测试计划-用例详情-缺陷列表
export const GetAssociatedBugUrl = '/test-plan/functional/case/has/associate/bug/page';
// 测试计划-用例详情
export const TestPlanCaseDetailUrl = '/test-plan/functional/case/detail';
// 测试计划-用例详情-关联缺陷
export const TestPlanAssociateBugUrl = '/test-plan/functional/case/associate/bug';
// 测试计划-用例详情-取消关联缺陷
export const TestPlanCancelBugUrl = '/test-plan/functional/case/disassociate/bug';
// 计划详情-功能用例-批量执行
export const BatchRunCaseUrl = '/test-plan/functional/case/batch/run';
// 计划详情-功能用例-获取用户列表
export const GetTestPlanUsersUrl = '/test-plan/functional/case/user-option';
// 计划详情-功能用例-批量更新执行人
export const BatchUpdateCaseExecutorUrl = '/test-plan/functional/case/batch/update/executor';
// 计划详情-功能用例-执行历史
export const ExecuteHistoryUrl = '/test-plan/functional/case/exec/history';
// 计划详情-执行历史
export const PlanDetailExecuteHistoryUrl = '/test-plan/his/page';
// 功能用例-关联用例-接口用例-API
export const TestPlanApiAssociatedPageUrl = '/test-plan/association/api/page';
// 功能用例-关联用例-接口用例-CASE
export const TestPlanCaseAssociatedPageUrl = '/test-plan/association/api/case/page';
// 功能用例-关联用例-接口用例-CASE
export const TestPlanScenarioAssociatedPageUrl = '/test-plan/association/api/scenario/page';
// 测试计划-复制
export const TestPlanAndGroupCopyUrl = '/test-plan/copy';
// 测试计划-计划组下拉
export const TestPlanGroupOptionsUrl = 'test-plan/group-list';
// 测试计划-拖拽测试计划
export const dragPlanOnGroupUrl = '/test-plan/sort';
// 测试计划-创建定时任务
export const ConfigScheduleUrl = '/test-plan/schedule-config';
// 测试计划-批量配置定时任务
export const BatchConfigScheduleUrl = '/test-plan/batch-schedule-config';
// 测试计划-计划&计划组-执行
export const ExecuteSinglePlanUrl = '/test-plan-execute/single';
// 测试计划-计划&计划组-执行&批量执行
export const BatchExecutePlanUrl = '/test-plan-execute/batch';
// 测试计划-删除定时任务
export const DeleteScheduleTaskUrl = 'test-plan/schedule-config-delete';

// 计划详情-接口用例列表
export const GetPlanDetailApiCaseListUrl = '/test-plan/api/case/page';
// 计划详情-接口用例模块树
export const GetApiCaseModuleUrl = '/test-plan/api/case/tree';
// 计划详情-接口用例-获取模块数量
export const GetApiCaseModuleCountUrl = '/test-plan/api/case/module/count';
// 计划详情-接口用例列表-拖拽排序
export const SortApiCaseUrl = '/test-plan/api/case/sort';
// 计划详情-接口用例列表-执行
export const RunApiCaseUrl = '/test-plan/api/case/run';
// 计划详情-接口用例列表-取消关联用例
export const DisassociateApiCaseUrl = '/test-plan/api/case/disassociate';
// 计划详情-接口用例列表-批量取消关联用例
export const BatchDisassociateApiCaseUrl = '/test-plan/api/case/batch/disassociate';
// 计划详情-接口用例列表-批量执行
export const BatchRunApiCaseUrl = '/test-plan/api/case/batch/run';
// 计划详情-接口用例列表-批量移动
export const BatchMoveApiCaseUrl = '/test-plan/api/case/batch/move';
// 计划详情-接口用例列表-报告详情
export const ApiCaseReportDetailUrl = '/test-plan/api/case/report/get';
// 计划详情-接口用例列表-步骤详情
export const ApiCaseReportDetailStepUrl = '/test-plan/api/case/report/get/detail';

// 计划详情-接口场景列表
export const GetPlanDetailApiScenarioListUrl = '/test-plan/api/scenario/page';
// 计划详情-接口场景模块树
export const GetApiScenarioModuleUrl = '/test-plan/api/scenario/tree';
// 计划详情-接口场景-获取模块数量
export const GetApiScenarioModuleCountUrl = '/test-plan/api/scenario/module/count';
// 计划详情-接口场景列表-拖拽排序
export const SortApiScenarioUrl = '/test-plan/api/scenario/sort';
// 计划详情-接口场景列表-执行
export const RunApiScenarioUrl = '/test-plan/api/scenario/run';
// 计划详情-接口场景列表-取消关联用例
export const DisassociateApiScenarioUrl = '/test-plan/api/scenario/disassociate';
// 计划详情-接口场景列表-批量取消关联用例
export const BatchDisassociateApiScenarioUrl = '/test-plan/api/scenario/batch/disassociate';
// 计划详情-接口场景列表-批量执行
export const BatchRunApiScenarioUrl = '/test-plan/api/scenario/batch/run';
// 计划详情-接口场景列表-批量移动
export const BatchMoveApiScenarioUrl = '/test-plan/api/scenario/batch/move';
// 计划详情-接口场景列表-报告详情
export const ApiScenarioReportDetailUrl = '/test-plan/api/scenario/report/get';
// 计划详情-接口场景列表-步骤详情
export const ApiScenarioReportDetailStepUrl = '/test-plan/api/scenario/report/get/detail';

// 测试规划脑图
export const GetPlanMinderUrl = '/test-plan/mind/data';
// 修改测试规划脑图
export const EditPlanMinderUrl = '/test-plan/mind/data/edit';
// 获取测试计划-关联用例-接口模块数量
export const TestPlanAssociationUrl = '/test-plan/association/api/case/module/count';
// 获取执行人下拉选项
export const GetTestPlanExecutorOptionsUrl = '/test-plan-execute/user-option';
// 获取测试计划未关联缺陷列表
export const GetUnAssociatedListUrl = '/test-plan/functional/case/associate/bug/page';
// 获取测试计划未关联缺陷列表
export const GetUnAssociatedApiBugUrl = '/test-plan/api/case/associate/bug/page';
// 获取测试计划未关联缺陷列表
export const GetUnAssociatedScenarioBugUrl = '/test-plan/api/scenario/associate/bug/page';
// 测试计划-接口用例-单个用例关联缺陷
export const AssociatedBugToApiCaseUrl = '/test-plan/api/case/associate/bug';
// 测试计划-场景用例-单个用例关联缺陷
export const AssociatedBugToScenarioCaseUrl = '/test-plan/api/scenario/associate/bug';
// 测试计划-接口用例-取消关联联缺陷
export const CancelBugFromApiCaseUrl = '/test-plan/api/case/disassociate/bug';
// 测试计划-场景用例-取消关联联缺陷
export const CancelBugFromScenarioCaseUrl = '/test-plan/api/scenario/disassociate/bug';
// 测试计划-详情-用例列表-批量关联缺陷
export const BatchAssociatedBugToCaseUrl = '/test-plan/functional/case/batch/associate-bug';
// 测试计划-详情-用例列表-批量新建缺陷
export const BatchAddBugToFunctionalCaseUrl = '/test-plan/functional/case/batch/add-bug';
// 测试计划-详情-接口列表-批量新建缺陷
export const BatchAddBugToApiCaseUrl = '/test-plan/api/case/batch/add-bug';
// 测试计划-详情-场景列表-批量新建缺陷
export const BatchAddBugToScenarioCaseUrl = '/test-plan/api/scenario/batch/add-bug';
// 测试计划-详情-接口用例列表-批量关联缺陷
export const BatchLinkBugToApiCaseUrl = '/test-plan/api/case/batch/associate-bug';
// 测试计划-详情-场景用例列表-批量关联缺陷
export const BatchLinkBugToScenarioCaseUrl = '/test-plan/api/scenario/batch/associate-bug';
export const BatchAddBugToCaseUrl = '/test-plan/functional/case/batch/add-bug';
// 测试计划-详情-用例列表-脑图批量关联缺陷
export const BatchAssociatedBugToMinderCaseUrl = '/test-plan/functional/case/minder/batch/associate-bug';
// 测试计划-详情-用例列表-脑图批量新建缺陷
export const BatchAddBugToMinderCaseUrl = '/test-plan/functional/case/minder/batch/add-bug';
// 测试计划/组-执行结果
export const TaskResultUrl = '/test-plan/report/get-task';
