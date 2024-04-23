export const getPlatform = '/bug/current-platform/';
export const checkBugExist = '/bug/check-exist/';
export const postTableListUrl = '/bug/page';
export const postUpdateBugUrl = '/bug/update';
export const postBatchUpdateBugUrl = '/bug/batch-update';
export const postCreateBugUrl = '/bug/add';
export const getDeleteBugUrl = '/bug/delete/';
export const postBatchDeleteBugUrl = '/bug/batch-delete';
export const getTemplateUrl = '/bug/template/detail';
export const getTemplateOption = '/bug/template/option';
export const getExportConfigUrl = '/bug/export/columns/';
export const getTemplateDetailUrl = '/bug/template/detail';
// 同步缺陷开源
export const getSyncBugOpenSourceUrl = '/bug/sync/';
// 同步缺陷企业版
export const getSyncBugEnterpriseUrl = '/bug/sync/all';
// 获取同步状态
export const getSyncStatusUrl = '/bug/sync/check/';
export const postExportBugUrl = '/bug/export';
// 获取关联文件列表
export const postAssociatedFileListUrl = '/bug/attachment/file/page';
export const getBugDetailUrl = '/bug/get/';
export const getFollowBugUrl = '/bug/follow/';
export const getUnFollowBugUrl = '/bug/unfollow/';
export const postUpdateCommentUrl = '/bug/comment/update';
export const postCreateCommentUrl = '/bug/comment/add';
export const getCommentListUrl = '/bug/comment/get/';
export const getDeleteCommentUrl = '/bug/comment/delete/';
export const getCustomFieldHeaderUrl = '/bug/header/custom-field/';
export const getCustomOptionHeaderUrl = '/bug/header/columns-option/';
// 上传or关联文件
export const uploadOrAssociationFileUrl = '/bug/attachment/upload';
// 转存文件
export const transferFileUrl = '/bug/attachment/transfer';
// 获取文件转存目录
export const getTransferTreeUrl = '/bug/attachment/transfer/options/';
// 预览文件
export const previewFileUrl = '/bug/attachment/preview';
// 下载文件
export const downloadFileUrl = '/bug/attachment/download';
// 检查文件是否更新
export const checkFileIsUpdateUrl = '/bug/attachment/check-update';
// 更新文件
export const getFileIsUpdateUrl = '/bug/attachment/update';
// 删除文件或取消关联用例文件
export const deleteFileOrCancelAssociationUrl = '/bug/attachment/delete';
// 获取附件列表
export const getAttachmentListUrl = '/bug/attachment/list/';
// 富文本编辑器上传图片
export const editorUploadFileUrl = '/bug/attachment/upload/md/file';

// 获取回收站列表
export const getRecycleListUrl = '/bug/trash/page';
// 单个恢复
export const getRecoverSingleUrl = '/bug/trash/recover/';
// 批量恢复
export const getBatchRecoverUrl = '/bug/trash/batch-recover';
// 删除
export const getDeleteSingleUrl = '/bug/trash/delete/';
// 批量删除
export const getBatchDeleteUrl = '/bug/trash/batch-delete';

// 获取关联的需求列表
export const getDemandListUrl = '/bug/case/page';
// 批量添加关联
export const postAddDemandUrl = '/bug/case/relate';
// 单个取消关联
export const getCancelDemandUrl = '/bug/case/un-relate';
// 未关联的用例列表
export const getUnrelatedDemandListUrl = '/bug/case/un-relate/page';
// 未关联的模块树
export const getUnrelatedModuleTreeUrl = '/bug/case/un-relate/module/tree';
// 未关联的模块树 数量
export const getUnrelatedModuleTreeCountUrl = '/bug/case/un-relate/module/count';

// 缺陷管理-变更历史-列表
export const getChangeHistoryListUrl = '/bug/history/page';

// 缺陷用例跳转用例是否具备权限
export const checkCasePermissionUrl = '/bug/case/check-permission';
// 缺陷预览富文本url
export const EditorPreviewFileUrl = '/bug/attachment/preview/md';
