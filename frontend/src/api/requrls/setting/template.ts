// 项目---模板

// 获取模板列表
export const GetProjectTemplateUrl = '/project/template/list';
// 设置模板
export const SetProjectTemplateUrl = '/project/template/set-default';
// 创建模板
export const CreateProjectTemplateUrl = '/project/template/add';
// 更新模板
export const UpdateProjectTemplateUrl = '/project/template/update';
// 获取模板详情
export const GetProjectTemplateDetailUrl = '/project/template/get';
// 删除模板
export const DeleteProjectTemplateUrl = '/project/template/delete';
// 是否启用项目模板
export const getProjectTemplateStateUrl = '/project/template/enable/config';

// 组织--- 模板

// 获取模板列表
export const GetOrganizeTemplateUrl = '/organization/template/list';
// 设置模板
export const SetOrganizeTemplateUrl = '/organization/template/set-default';
// 创建模板
export const CreateOrganizeTemplateUrl = '/organization/template/add';
// 更新模板
export const UpdateOrganizeTemplateUrl = '/organization/template/update';
// 获取模板详情
export const GetOrganizeTemplateDetailUrl = '/organization/template/get';
// 删除模板
export const DeleteOrganizeTemplateUrl = '/organization/template/delete';
// 关闭组织模板，开启项目模板
export const EnableOrOffTemplateUrl = '/organization/template/disable';
// 获取所有模板状态
export const getOrdTemplateStateUrl = '/organization/template/enable/config';

// 系统设置-组织-自定义字段

// 获取自定义字段列表
export const GetDefinedFieldListUrl = '/organization/custom/field/list';
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

// 项目管理-模板-自定义字段

// 获取自定义字段列表
export const GetDefinedProjectFieldListUrl = '/project/custom/field/list';
// 创建自定义字段
export const CreateProjectFieldUrl = '/project/custom/field/add';
// 更新自定义字段
export const UpdateProjectFieldUrl = '/project/custom/field/update';
// 获取自定义字段详情
export const GetFieldProjectDetailUrl = '/project/custom/field/get';
// 删除自定义字段
export const DeleteProjectFieldDetailUrl = '/project/custom/field/delete';

// 项目管理-模板-获取工作流列表
export const ProjectWorkFlowUrl = '/project/status/flow/setting/get';
// 状态流更新
export const ProjectWorkFlowSetting = '/project/status/flow/setting/status/update';
// 创建工作流状态
export const ProjectCreateFlowStatusUrl = '/project/status/flow/setting/status/add';
// 更新工作流状态
export const ProjectUpdateFlowStatusUrl = '/project/status/flow/setting/status/update';
// 删除工作流状态
export const ProjectDeleteFlowStatusUrl = '/project/status/flow/setting/status/delete';
// 设置工作流状态为初始态或者标记结束状态
export const ProjectSetStateUrl = '/project/status/flow/setting/status/definition/update';
// 设置选项排序
export const ProjectStateSortUrl = '/project/status/flow/setting/status/sort';
// 更新状态流转
export const ProjectUpdateStateFlowUrl = '/project/status/flow/setting/status/flow/update';
// 组织模板富文本图片链接
export const orgRichUploadImageUrl = '/organization/template/upload/temp/img';
// 项目模板富文本图片链接
export const proRichUploadImageUrl = '/project/template/upload/temp/img';
// 组织预览富文本图片
export const previewOrgImageUrl = '/organization/template/img/preview';
// 项目预览富文本图片
export const previewProImageUrl = '/project/template/img/preview';
