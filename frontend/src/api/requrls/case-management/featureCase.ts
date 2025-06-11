// 用例管理列表
export const GetCaseListUrl = '/functional/case/page';
// 用例管理-添加
export const CreateCaseUrl = '/functional/case/add';
// 用例管理-更新
export const UpdateCaseUrl = '/functional/case/update';
// 用例管理-删除
export const DeleteCaseUrl = '/functional/case/delete';
// 用例管理-详情
export const DetailCaseUrl = '/functional/case/detail';
// 用例管理-批量移动用例
export const BatchMoveCaseUrl = '/functional/case/batch/move';
// 用例管理-批量删除用例
export const BatchDeleteCaseUrl = '/functional/case/batch/delete-to-gc';
// 用例管理-批量删除用例
export const BatchEditCaseUrl = '/functional/case/batch/edit';
// 用例管理-批量复制
export const BatchCopyCaseUrl = '/functional/case/batch/copy';
// 用例管理-关注/取消关注用例
export const FollowerCaseUrl = '/functional/case/edit/follower';
// 获取用例关注人
export const GetCaseFollowerUrl = '/functional/case/follower';
// 获取表头自定义字段（高级搜索中的自定义字段）
export const GetSearchCustomFieldsUrl = '/functional/case/custom/field';
// 关联文件列表
export const GetAssociatedFilePageUrl = '/attachment/page';
export const SaveCaseMinderUrl = '/functional/mind/case/edit'; // 保存用例脑图
export const GetCaseMinderUrl = '/functional/mind/case/list'; // 获取脑图数据
export const GetCaseMinderTreeUrl = '/functional/mind/case/tree'; // 获取脑图模块树（含文本节点）

// 获取模块树
export const GetCaseModuleTreeUrl = '/functional/case/module/tree';
// 创建模块树
export const CreateCaseModuleTreeUrl = '/functional/case/module/add';
// 更新模块树
export const UpdateCaseModuleTreeUrl = '/functional/case/module/update';
// 移动模块
export const MoveCaseModuleTreeUrl = '/functional/case/module/move';
// 回收站-模块-获取模块树
export const GetTrashCaseModuleTreeUrl = '/functional/case/module/trash/tree';
// 删除模块
export const DeleteCaseModuleTreeUrl = '/functional/case/module/delete';
// 获取默认模板自定义字段
export const GetDefaultTemplateFieldsUrl = '/functional/case/default/template/field';

// 回收站

// 回收站分页
export const GetRecycleCaseListUrl = '/functional/case/trash/page';
// 获取回收站模块数量
export const GetRecycleCaseModulesCountUrl = '/functional/case/trash/module/count';
// 获取全部用例模块数量
export const GetCaseModulesCountUrl = '/functional/case/module/count';
// 恢复回收站用例表
export const RestoreCaseListUrl = '/functional/case/trash/batch/recover';
// 批量彻底删除回收站用例表
export const BatchDeleteRecycleCaseListUrl = '/functional/case/trash/batch/delete';
// 恢复回收站单个用例
export const RecoverRecycleCaseListUrl = '/functional/case/trash/recover';
// 删除回收站单个用例
export const DeleteRecycleCaseListUrl = '/functional/case/trash/delete';

// 关联需求

// 已关联需求列表
export const GetDemandListUrl = '/functional/case/demand/page';
// 添加需求
export const AddDemandUrl = '/functional/case/demand/add';
// 更新需求
export const UpdateDemandUrl = '/functional/case/demand/update';
// 批量关联需求
export const BatchAssociationDemandUrl = '/functional/case/demand/batch/relevance';
// 取消关联
export const CancelAssociationDemandUrl = '/functional/case/demand/cancel';
// 获取三方关联需求的接口
export const GetThirdDemandUrl = '/functional/case/demand/third/list/page';

// 附件管理
// 上传文件并关联用例
export const UploadOrAssociationFileUrl = '/attachment/upload/file';
// 转存文件
export const TransferFileUrl = '/attachment/transfer';
// 预览文件
export const PreviewFileUrl = '/attachment/preview';
// 下载文件
export const DownloadFileUrl = '/attachment/download';
// 删除文件或取消关联用例文件
export const deleteFileOrCancelAssociationUrl = '/attachment/delete/file';
// 获取转存目录
export const getTransferTreeUrl = '/attachment/options';
// 附件是否更新
export const GetFileIsUpdateUrl = '/attachment/update';
// 检查文件是否更新
export const checkFileIsUpdateUrl = '/attachment/check-update';

// 评论列表
export const GetCommentListUrl = '/functional/case/comment/get/list';
// 评审评论
export const GetReviewCommentListUrl = '/functional/case/review/comment';
// 创建评论
export const CreateCommentItemUrl = '/functional/case/comment/save';
// 更新评论
export const UpdateCommentItemUrl = '/functional/case/comment/update';
// 删除评论
export const DeleteCommentItemUrl = '/functional/case/comment/delete';
// 获取详情用例评审
export const GetDetailCaseReviewUrl = '/functional/case/review/page';
// 获取用例详情弹窗关联用例接口用例
export const GetAssociationPublicCasePageUrl = '/functional/case/test/associate/case/page';
// 获取接口测试接口模块数量
export const GetAssociationPublicCaseModuleCountUrl = '/functional/case/test/associate/case/module/count';
// 获取用例详情接口模块树
export const GetAssociationPublicModuleTreeUrl = '/functional/case/test/associate/case/module/tree';
// 获取前后置用例列表
export const GetDependOnPageUrl = '/functional/case/relationship/page';
// 用例管理-功能用例-用例详情-前后置关系
export const GetDependOnRelationUrl = '/functional/case/relationship/relate/page';
// 添加前后置关系
export const AddDependOnRelationUrl = '/functional/case/relationship/add';
// 取消关联前后置关系
export const cancelPreAndPostCaseUrl = '/functional/case/relationship/delete';
// 关联用例
export const publicAssociatedCaseUrl = '/functional/case/test/associate/case';
// 获取关联用例已关联列表
export const GetAssociatedDrawerCaseUrl = '/functional/case/test/has/associate/case/page';
// 获取用例详情缺陷
export const GetDebugDrawerPageUrl = '/functional/case/test/associate/bug/page';
// 关联缺陷
export const AssociatedDebuggerUrl = '/functional/case/test/associate/bug';
// 取消关联缺陷
export const CancelAssociatedDebuggerUrl = '/functional/case/test/disassociate/bug';
// 获取详情已关联缺陷列表
export const GetAssociatedDebuggerUrl = '/functional/case/test/has/associate/bug/page';
// 获取前后置已关联用例ids
export const GetAssociatedCaseIdsUrl = '/functional/case/relationship/get-ids';
// 导入功能
// 功能用例导入excel下载模板
export const DownloadExcelTemplateUrl = '/functional/case/download/excel/template';
// 功能用例导入xmind下载模板
export const DownloadXMindTemplateUrl = '/functional/case/download/xmind/template';
// 富文本所需资源上传
export const EditorUploadFileUrl = '/attachment/upload/temp/file';
// 富文本资源详情预览压缩图
export const PreviewEditorImageUrl = '/attachment/download/file';
// 导入excel文件检查
export const exportExcelCheckUrl = '/functional/case/pre-check/excel';
// 导入xmind文件检查
export const exportXMindCheckUrl = '/functional/case/pre-check/xmind';
// 导入excel文件
export const importExcelCaseUrl = '/functional/case/import/excel';
// 导入xmind文件
export const importXMindCaseUrl = '/functional/case/import/xmind';
// 导出excel文件
export const ExportExcelCaseUrl = '/functional/case/export/excel';
// 导出XMind文件
export const ExportXMindCaseUrl = '/functional/case/export/xmind';
// 检查是否有导出任务
export const CheckCaseExportTaskUrl = '/functional/case/check/export-task';
// 导出字段配置
export const GetCaseExportConfigUrl = '/functional/case/export/columns';
// 下载导出的文件
export const GetCaseDownloadFileUrl = '/functional/case/download/file';
// 停止导出
export const StopCaseExportUrl = '/functional/case/stop';
// 用例拖拽排序
export const dragSortUrl = '/functional/case/edit/pos';
// 获取变更历史
export const getChangeHistoryListUrl = '/functional/case/operation-history';
// 取消关联用例
export const cancelDisassociate = '/functional/case/test/disassociate/case';
// 关联用例关联功能用例项目下拉
export const associatedProjectOptionsUrl = '/project/list/options';

// 获取详情已关联测试计划列表
export const GetAssociatedTestPlanUrl = '/functional/case/test/has/associate/plan/page';

// 评审评论
export const GetPlanExecuteCommentListUrl = '/functional/case/test/plan/comment';
export const SaveAiConfigUrl = '/functional/case/ai/save/config';
export const GetAiConfigUrl = '/functional/case/ai/get/config';
export const CaseAiTransformUrl = '/functional/case/ai/transform'; // AI用例结构转换
export const CaseAiChatUrl = '/functional/case/ai/chat'; // AI用例聊天
export const CaseAiBatchSaveUrl = '/functional/case/ai/batch/save'; // AI用例批量保存
