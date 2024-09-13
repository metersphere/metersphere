// 系统-组织
// 修改组织
export const postModifyOrgUrl = '/system/organization/update';
// 修改组织名称
export const postModifyOrgNameUrl = '/system/organization/rename';
// 获取系统下所有组织-下拉选项
export const postOrgOptionsUrl = '/system/organization/option/all';
// 获取系统下所有组织-Table
export const postOrgTableUrl = '/system/organization/list';
// 获取组织下所有项目-Table
export const postProjectTableByOrgUrl = '/system/organization/list-project';
// 获取组织成员
export const postOrgMemberUrl = '/system/organization/list-member';
// 添加组织
export const postAddOrgUrl = '/system/organization/add';
// 添加组织成员
export const postAddOrgMemberUrl = '/system/organization/add-member';
// 删除组织成员
export const getDeleteOrgMemberUrl = '/system/organization/remove-member/';
// 恢复组织
export const getRecoverOrgUrl = '/system/organization/recover/';
// 启用组织
export const getEnableOrgUrl = '/system/organization/enable/';
// 禁用组织
export const getDisableOrgUrl = '/system/organization/disable/';
// 删除组织
export const getDeleteOrgUrl = '/system/organization/delete/';
// 获取系统默认组织
export const getOrgDefaultUrl = '/system/organization/default';
// 系统设置-系统-组织与项目-获取添加成员列表
export const getMemberListPageUrl = '/system/organization/member-list';

// 系统-项目
// 更新项目信息
export const postModifyProjectUrl = '/system/project/update';
// 获取项目列表
export const postProjectTableUrl = '/system/project/page';
// 获取项目成员
export const postProjectMemberUrl = '/system/project/member-list';
// 添加项目
export const postAddProjectUrl = '/system/project/add';
// 修改项目名称
export const postModifyProjectNameUrl = '/system/project/rename';
// 添加项目成员
export const postAddProjectMemberUrl = '/system/project/add-member';
// 撤销项目
export const getRevokeProjectUrl = '/system/project/revoke/';
// 移除项目成员
export const getDeleteProjectMemberUrl = '/system/project/remove-member/';
// 删除项目
export const getDeleteProjectUrl = '/system/project/delete/';
// 系统-组织及项目，获取用户下拉选项
export const getUserByOrgOrProjectUrl = '/system/organization/get-option/';
// 系统-组织及项目，获取管理员下拉选项
export const getAdminByOrgOrProjectUrl = '/system/project/user-list';
// 启用项目
export const getEnableProjectUrl = '/system/project/enable/';
// 禁用项目
export const getDisableProjectUrl = '/system/project/disable/';
// 获取组织或项目的管理员
export const getOrgOrProjectAdminUrl = '/system/project/user-list';

// 获取项目和组织的总数
export const getOrgAndProjectCountUrl = '/system/organization/total';

// 组织-项目
// 修改项目
export const postModifyProjectByOrgUrl = '/organization/project/update';
// 获取项目列表
export const postProjectTableByOrgIdUrl = '/organization/project/page';
// 获取项目下的成员列表
export const postProjectMemberByOrgIdUrl = '/organization/project/member-list';
// 添加项目
// 修改项目名称
export const postModifyProjectNameByOrgUrl = '/organization/project/rename';
export const postAddProjectByOrgUrl = '/organization/project/add';
// 添加项目成员
export const postAddProjectMemberByOrgUrl = '/organization/project/add-members';
// 恢复项目
export const getRecoverProjectByOrgUrl = '/organization/project/revoke/';
// 移除项目成员
export const getDeleteProjectMemberByOrgUrl = '/organization/project/remove-member/';
// 启用项目
export const getEnableProjectByOrgUrl = '/organization/project/enable/';
// 禁用项目
export const getDisableProjectByOrgUrl = '/organization/project/disable/';
// 删除项目
export const getDeleteProjectByOrgUrl = '/organization/project/delete/';
// 获取成员下拉选项
export const getUserByOrganizationOrProjectUrl = '/organization/project/user-member-list/';
// 获取管理员下拉选项
export const getAdminByOrganizationOrProjectUrl = '/organization/project/user-admin-list/';
// 系统或组织-获取项目资源池下拉选项
export const getProjectPoolByOrgOrSystemUrl = '/system/project/pool-options';
// 组织-获取项目资源池下拉选项
export const getProjectPoolByOrg = '/organization/project/pool-options';
