// 项目---模板

// 获取模版列表
export const GetProjectTemplateUrl = '/project/template/list';
// 设置模版
export const SetProjectTemplateUrl = '/project/template/set-default';
// 创建模版
export const CreateProjectTemplateUrl = '/project/template/add';
// 更新模版
export const UpdateProjectTemplateUrl = '/project/template/update';
// 获取模版详情
export const GetProjectTemplateDetailUrl = '/project/template/get';
// 删除模版
export const DeleteProjectTemplateUrl = '/project/template/delete';

// 组织--- 模板

// 获取模版列表
export const GetOrganizeTemplateUrl = '/organization/template/list';
// 设置模版
export const SetOrganizeTemplateUrl = '/organization/template/set-default';
// 创建模版
export const CreateOrganizeTemplateUrl = '/organization/template/add';
// 更新模版
export const UpdateOrganizeTemplateUrl = '/organization/template/update';
// 获取模版详情
export const GetOrganizeTemplateDetailUrl = '/organization/template/get';
// 删除模版
export const DeleteOrganizeTemplateUrl = '/organization/template/delete';
// 关闭组织模板，开启项目模版
export const EnableOrOffTemplateUrl = '/organization/template/disable';
// 是否启用组织模板
export const isEnableTemplateUrl = '/organization/template/enable/config';

// 系统设置-组织-自定义字段

// 获取自定义字段列表
export const GetDefinedFieldListUrl = '/organization/custom/field/list/';
// 创建自定义字段
export const CreateFieldUrl = '/organization/custom/field/add';
// 更新自定义字段
export const UpdateFieldUrl = '/organization/custom/field/update';
// 获取自定义字段详情
export const GetFieldDetailUrl = '/organization/custom/field/get';
// 删除自定义字段
export const DeleteFieldDetailUrl = '/organization/custom/field/delete';

// 系统设置-组织-获取工作流列表
export const OrdWorkFlowUrl = '/organization/status/flow/setting/get';
// 状态流更新
export const OrdWorkFlowSetting = '/organization/status/flow/setting/status/update';
// 创建工作流状态
export const OrdCreateFlowStatusUrl = '/organization/status/flow/setting/status/add';
// 更新工作流状态
export const OrdUpdateFlowStatusUrl = '/organization/status/flow/setting/status/update';
// 删除工作流状态
export const OrdDeleteFlowStatusUrl = '/organization/status/flow/setting/status/delete';
// 设置工作流状态为初始态或者标记结束状态
export const OrdSetStateUrl = '/organization/status/flow/setting/status/definition/update';
// 设置选项排序
export const OrdStateSortUrl = '/organization/status/flow/setting/status/sort';
// 更新状态流转
export const OrdUpdateStateFlowUrl = '/organization/status/flow/setting/status/flow/update';
