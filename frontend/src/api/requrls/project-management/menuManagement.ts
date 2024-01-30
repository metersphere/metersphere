export const getConfigByMenuTypeUrl = '/project/application/';
export const updateConfigByMenuTypeUrl = '/project/application/update/';
// 获取表格最外层的数据
export const getMenuListUrl = '/project/application/module-setting/';
// 根据组织id获取bug平台下拉选项
export const getPlatformOptionUrlByBug = '/project/application/bug/platform/';
// 根据组织id获取case平台下拉选项
export const getPlatformOptionUrlByCase = '/project/application/case/platform/';
// 根据插件id获取case插件信息
export const getPluginInfoByCase = '/project/application/case/platform/info/';
// 根据插件id获取bug插件信息
export const getPluginInfoByBug = '/project/application/bug/platform/info/';

// 缺陷管理-同步缺陷配置
export const postSyncBugConfigUrl = '/project/application/update/bug/sync/';
// 用例管理-关联需求配置
export const postRelatedCaseConfigUrl = '/project/application/update/case/related/';
// 误报规则列表查询
export const postFakeTableUrl = '/fake/error/list';
// 误报规则列表删除
export const getFakeTableDeleteUrl = '/fake/error/delete';
// 误报规则列表新增
export const postFakeTableAddUrl = '/fake/error/add';
// 误报规则列表更改
export const postFakeTableUpdateUrl = '/fake/error/update';
// 误报规则列表启用或禁用
export const postFakeTableEnableUrl = '/fake/error/update/enable';
// JIRAKEY 校验
export const postValidateJiraKeyUrl = '/project/application/validate/';
// 缺陷管理-获取同步信息
export const getBugSyncInfoUrl = '/project/application/bug/sync/info/';
// 用例管理-获取关联需求信息
export const getCaseRelatedInfoUrl = '/project/application/case/related/info/';
