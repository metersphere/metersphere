export const UpdateModuleUrl = '/api/definition/module/update'; // 更新模块
export const GetModuleTreeUrl = '/api/definition/module/tree'; // 查找模块
export const GetModuleOnlyTreeUrl = '/api/definition/module/only/tree'; // 查找不包含接口的模块树
export const MoveModuleUrl = '/api/definition/module/move'; // 移动模块
export const GetEnvModuleUrl = '/api/definition/module/env/tree'; // 获取环境的模块树
export const GetModuleCountUrl = '/api/definition/module/count'; // 获取模块统计数量
export const AddModuleUrl = '/api/definition/module/add'; // 添加模块
export const DeleteModuleUrl = '/api/definition/module/delete'; // 删除模块
/**
 * 接口定义
 */
export const DefinitionPageUrl = '/api/definition/page'; // 接口定义列表
export const AddDefinitionUrl = '/api/definition/add'; // 添加接口定义
export const UpdateDefinitionUrl = '/api/definition/update'; // 更新接口定义
export const StopApiExportUrl = '/api/definition/stop';
export const GetApiDownloadFileUrl = '/api/definition/download/file';
export const GetDefinitionDetailUrl = '/api/definition/get-detail'; // 获取接口定义详情
export const TransferFileUrl = '/api/definition/transfer'; // 文件转存
export const TransferFileModuleOptionUrl = '/api/definition/transfer/options'; // 文件转存目录
export const UploadTempFileUrl = '/api/definition/upload/temp/file'; // 临时文件上传
export const DeleteDefinitionUrl = '/api/definition/delete-to-gc'; // 删除接口定义
export const ImportDefinitionUrl = '/api/definition/import'; // 导入接口定义
export const ExportDefinitionUrl = '/api/definition/export'; // 导入接口定义
export const SortDefinitionUrl = '/api/definition/edit/pos'; // 接口定义拖拽
export const CopyDefinitionUrl = '/api/definition/copy'; // 复制接口定义
export const BatchUpdateDefinitionUrl = '/api/definition/batch-update'; // 批量更新接口定义
export const BatchMoveDefinitionUrl = '/api/definition/batch-move'; // 批量移动接口定义
export const BatchDeleteDefinitionUrl = '/api/definition/batch/delete-to-gc'; // 批量删除接口定义
export const UpdateDefinitionScheduleUrl = '/api/definition/schedule/update'; // 接口定义-定时同步-更新
export const CheckDefinitionScheduleUrl = '/api/definition/schedule/check'; // 接口定义-定时同步-检查 url 是否存在
export const AddDefinitionScheduleUrl = '/api/definition/schedule/add'; // 接口定义-定时同步-添加
export const SwitchDefinitionScheduleUrl = '/api/definition/schedule/switch'; // 接口定义-定时同步-开启关闭
export const GetDefinitionScheduleUrl = '/api/definition/schedule/get'; // 接口定义-定时同步-查询
export const DeleteDefinitionScheduleUrl = '/api/definition/schedule/delete'; // 接口定义-定时同步-删除
export const DebugDefinitionUrl = '/api/definition/debug'; // 接口定义-调试
export const ToggleFollowDefinitionUrl = '/api/definition/follow'; // 接口定义-关注/取消关注
export const OperationHistoryUrl = '/api/definition/operation-history'; // 接口定义-变更历史
export const SaveOperationHistoryUrl = '/api/definition/operation-history/save'; // 接口定义-另存变更历史为指定版本
export const RecoverOperationHistoryUrl = '/api/definition/operation-history/recover'; // 接口定义-变更历史恢复
export const DefinitionReferenceUrl = '/api/definition/get-reference'; // 获取接口引用关系
export const ConvertJsonSchemaToJsonUrl = '/api/definition/json-schema/preview'; // 将json-schema转换为 json 数据
export const JsonSchemaAutoGenerateUrl = '/api/definition/json-schema/auto-generate'; // 将json-schema转换为 json 数据
export const DefinitionFileCopyUrl = '/api/definition/file/copy'; // 接口文件复制

/**
 * Mock
 */
export const DefinitionMockPageUrl = '/api/definition/mock/page'; // mock列表
export const UpdateMockStatusUrl = '/api/definition/mock/enable'; // 更新mock状态
export const DeleteMockUrl = '/api/definition/mock/delete'; // 刪除mock
export const UploadTempMockFileUrl = '/api/definition/mock/upload/temp/file'; // mock临时上传文件
export const TransferMockFileUrl = '/api/definition/mock/transfer'; // mock临时文件转存
export const TransferMockFileModuleOptionUrl = '/api/definition/mock/transfer/options'; // mock临时文件转存目录下拉框
export const UpdateMockUrl = '/api/definition/mock/update'; // mock更新
export const MockDetailUrl = '/api/definition/mock/detail'; // mock详情
export const CopyMockUrl = '/api/definition/mock/copy'; // 复制mock
export const BatchEditMockUrl = '/api/definition/mock/batch/edit'; // 批量编辑mock
export const BatchDeleteMockUrl = '/api/definition/mock/batch/delete'; // 批量删除mock
export const AddMockUrl = '/api/definition/mock/add'; // 添加mock
export const GetMockUrlUrl = '/api/definition/mock/get-url'; // 获取mock url

/**
 * api回收站
 */
export const RecoverDefinitionUrl = '/api/definition/recover'; // 回收站-接口定义-恢复
export const DeleteRecycleApiUrl = '/api/definition/delete'; // 回收站-接口定义-彻底删除
export const BatchRecoverApiUrl = '/api/definition/batch-recover'; // 回收站-接口定义-批量恢复
export const BatchCleanOutApiUrl = '/api/definition/batch/delete'; // 回收站-接口定义-批量彻底删除
export const GetTrashModuleTreeUrl = '/api/definition/module/trash/tree'; // 回收站查找模块
export const GetTrashModuleCountUrl = '/api/definition/module/trash/count'; // 获取回收站模块统计数量

// --------------------用例
export const CasePageUrl = '/api/case/page'; // 接口用例列表
export const UpdateCaseUrl = '/api/case/update'; // 接口用例更新
export const UpdateCaseStatusUrl = '/api/case/update-status'; // 接口用例更新状态
export const UpdateCasePriorityUrl = '/api/case/update-priority'; // 接口用例更新等级
export const DeleteCaseUrl = '/api/case/delete-to-gc'; // 删除接口用例
export const BatchDeleteCaseUrl = '/api/case/batch/delete-to-gc'; // 批量删除接口用例
export const BatchEditCaseUrl = '/api/case/batch/edit'; // 批量编辑接口用例
export const SortCaseUrl = '/api/case/edit/pos'; // 接口用例拖拽
export const DebugCaseUrl = '/api/case/debug'; // 接口用例调试
export const TransferFileCaseUrl = '/api/case/transfer'; // 文件转存
export const TransferFileModuleOptionCaseUrl = '/api/case/transfer/options'; // 文件转存目录
export const UploadTempFileCaseUrl = '/api/case/upload/temp/file'; // 临时文件上传
export const GetCaseDetailUrl = '/api/case/get-detail'; // 获取接口用例详情
export const BatchExecuteCaseUrl = '/api/case/batch/run'; // 批量执行接口用例
export const ExecuteCaseUrl = '/api/case/run'; // 单独执行接口用例
export const GetExecuteHistoryUrl = '/api/case/execute/page'; // 获取用的执行历史
export const GetDependencyUrl = '/api/case/get-reference'; // 获取用例的依赖关系
export const GetChangeHistoryUrl = '/api/case/operation-history/page'; // 获取用例的依赖关系
export const ToggleFollowCaseUrl = '/api/case/follow'; // 接口用例-关注
export const ToggleUnFollowCaseUrl = '/api/case/unfollow'; // 接口用例-取消关注
export const RunCaseUrl = '/api/case/run'; // 执行接口用例
export const GetCaseReportByIdUrl = '/api/report/case/get/'; // 接口用例报告获取
export const GetCaseReportDetailUrl = '/api/report/case/get/detail/'; // 接口用例报告获取
export const CaseExportLogUrl = '/api/report/case/export'; // 接口用例导出报告日志记录
export const CaseBatchExportLogUrl = '/api/report/case/batch-export'; // 接口用例批量导出报告日志记录
export const GetCaseBatchExportParamsUrl = '/api/report/case/batch-param'; // 接口用例批量导出报告id集合
export const CaseStatisticsUrl = '/api/case/statistics'; // 用例执行率统计
export const getSyncedCaseDetailUrl = '/api/case/api-change/sync'; // 接口测试-接口用例-定义对比用例-同步-获取同步后的用例详情
export const clearThisChangeUrl = '/api/case/api-change/clear'; // 接口定义-变更对比-清除本次变更
export const caseTableBatchSyncUrl = '/api/case/batch/api-change/sync'; // 接口测试-接口管理-接口用例-批量同步
export const ignoreEveryTimeApiChangeUrl = '/api/case/api-change/ignore'; // 接口测试-接口用例-忽略每次接口变更
export const diffDataUrl = '/api/case/api/compare'; // 接口测试-接口用例-定义对比用例
export const CaseFileCopyUrl = '/api/case/file/copy'; // 接口用例文件复制
export const SaveAiConfigUrl = '/api/case/ai/save/config'; // 接口用例AI配置保存
export const GetAiConfigUrl = '/api/case/ai/get/config'; // 接口用例AI配置获取
export const ApiAiChatUrl = '/api/case/ai/chat'; // 接口用例AI聊天
export const ApiAiTransformUrl = '/api/case/ai/transform'; // 接口用例AI转换
export const ApiAiCaseBatchSaveUrl = '/api/case/ai/batch/save'; // 接口用例AI批量保存

/**
 * 接口用例回收站
 */
export const RecycleCasePageUrl = '/api/case/trash/page'; // 接口用例回收站列表
export const RecoverCaseUrl = '/api/case/recover'; // 接口用例恢复
export const BatchRecoverCaseUrl = '/api/case/batch/recover'; // 接口用例批量恢复
export const DeleteRecycleCaseUrl = '/api/case/delete'; // 接口用例彻底删除
export const BatchDeleteRecycleCaseUrl = '/api/case/batch/delete'; // 接口用例批量彻底删除
export const AddCaseUrl = '/api/case/add'; // 添加用例

export const GetPoolOptionUrl = '/api/test/pool-option'; // 获取接口资源池
export const GetPoolId = '/api/test/get-pool/'; // 获取项目应用设置的资源池id

// 接口定义文档
export const AddShareUrl = '/api/doc/share/add'; // 接口测试-接口管理-新增分享
export const UpdateShareUrl = '/api/doc/share/update'; // 接口测试-接口管理-更新分享
export const DeleteShareUrl = '/api/doc/share/delete'; // 接口测试-接口管理-删除分享
export const GetSharePageUrl = '/api/doc/share/page'; // 接口测试-接口管理-分享列表
export const checkSharePsdUrl = '/api/doc/share/check'; // 接口测试-接口管理-校验分享密码
export const shareDetailUrl = '/api/doc/share/detail'; // 接口测试-接口管理-查看链接
export const shareModuleTreeUrl = '/api/doc/share/module/tree'; // 接口测试-接口管理-模块树
export const shareModuleCountUrl = '/api/doc/share/module/count'; // 接口测试-接口管理-模块数量
export const ExportShareDefinitionUrl = '/api/doc/share/export'; // 导入分享接口定义
export const GetShareApiDownloadFileUrl = '/api/doc/share/download/file'; // 下载导出的文档
export const StopShareApiExportUrl = '/api/doc/share/stop'; // 停止分享导出
export const GetShareDefinitionDetailUrl = '/api/doc/share/get-detail'; // 获取接口定义文档分享详情
export const GetShareDefinitionPluginUrl = '/api/doc/share/plugin/script'; // 获取分享接口定义的插件脚本
