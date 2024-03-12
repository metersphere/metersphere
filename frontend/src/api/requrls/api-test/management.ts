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
export const GetDefinitionDetailUrl = '/api/definition/get-detail'; // 获取接口定义详情
export const TransferFileUrl = '/api/definition/transfer'; // 文件转存
export const TransferFileModuleOptionUrl = '/api/definition/transfer/options'; // 文件转存目录
export const UploadTempFileUrl = '/api/definition/upload/temp/file'; // 临时文件上传
export const DeleteDefinitionUrl = '/api/definition/delete-to-gc'; // 删除接口定义
export const ImportDefinitionUrl = '/api/definition/import'; // 导入接口定义
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

/**
 * Mock
 */
export const DefinitionMockPageUrl = '/api/definition/mock/page'; // mock列表
export const UpdateMockStatusUrl = '/api/definition/mock/enable/'; // 更新mock状态
export const DeleteMockUrl = '/api/definition/mock/delete'; // 刪除mock

/**
 * 接口引用关系
 */
export const DefinitionReferenceUrl = '/api/definition/get-reference'; // 获取接口引用关系
