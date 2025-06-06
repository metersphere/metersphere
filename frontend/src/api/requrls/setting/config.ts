// 测试邮件链接
export const TestEmailUrl = '/system/parameter/test/email';
// 保存基本信息
export const SaveBaseInfoUrl = '/system/parameter/save/base-info';
// 替换站点 url
export const SaveBaseUrlUrl = '/system/parameter/save/base-url';
// 保存邮件配置
export const SaveEmailInfoUrl = '/system/parameter/edit/email-info';
// 获取邮件配置
export const GetEmailInfoUrl = '/system/parameter/get/email-info';
// 获取基础信息
export const GetBaseInfoUrl = '/system/parameter/get/base-info';
// 保存界面配置
export const SavePageConfigUrl = '/display/save';
// 获取界面配置
export const GetPageConfigUrl = '/display/info';
// 更新认证源
export const UpdateAuthUrl = '/system/authsource/update';
// 更新认证源状态
export const UpdateAuthStatusUrl = '/system/authsource/update/status';
// 获取认证源列表
export const GetAuthListUrl = '/system/authsource/list';
// 添加认证源
export const AddAuthUrl = '/system/authsource/add';
// 获取认证源详情
export const GetAuthDetailUrl = '/system/authsource/get';
// 根据类型获取认证源详情
export const GetAuthDetailByTypeUrl = '/authentication/get/by/type';
// 删除认证源
export const DeleteAuthUrl = '/system/authsource/delete';
// 测试ldap连接
export const TestLdapConnectUrl = '/system/authsource/ldap/test-connect';
// 测试ldap登录
export const TestLdapLoginUrl = '/system/authsource/ldap/test-login';
// 内存清理配置保存
export const SaveCleanConfigUrl = '/system/parameter/edit/clean-config';
// 获取内存清理配置
export const GetCleanConfigUrl = '/system/parameter/get/clean-config';
// 设置上传配置
export const EditUploadConfigUrl = '/system/parameter/edit/upload-config';

// 获取系统主页左上角图片
export const GetTitleImgUrl = `${import.meta.env.VITE_API_BASE_URL}/base-display/get/logo-platform`;
// 获取登录 logo
export const GetLoginLogoUrl = `${import.meta.env.VITE_API_BASE_URL}/base-display/get/login-logo`;
// 获取登录大图
export const GetLoginImageUrl = `${import.meta.env.VITE_API_BASE_URL}/base-display/get/login-image`;
// 获取平台标签图标
export const GetPlatformIconUrl = `${import.meta.env.VITE_API_BASE_URL}/base-display/get/icon`;
// 模型配置
// 系统设置-查看模型集合
export const ModelConfigListUrl = '/ai/config/source/list';
// 编辑模型设置
export const EditModelConfigUrl = '/ai/config/edit-source';
// 获取模型信息
export const GetModelConfigDetailUrl = '/ai/config/get';
// 查看模型名称集合
export const GetModelConfigNameListUrl = '/ai/config/source/name/list';
// 删除模型
export const DeleteModelConfigUrl = '/ai/config/delete';
