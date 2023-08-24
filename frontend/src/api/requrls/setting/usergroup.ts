/** 修改用户组 */
export const updateUserGroupU = `/user/role/global/update`;
/** 编辑用户组对应的权限配置 */
export const editGlobalUSettingUrl = `/user/role/global/permission/update`;
/** 添加用户组 */
export const addUserGroupU = `/user/role/global/add`;
/** 获取用户组对应的权限配置 */
export const getGlobalUSettingUrl = `/user/role/global/permission/setting/`;
/** 获取用户组 */
export const getUserGroupU = `/user/role/global/list`;
/** 获取单个用户组信息 */
export const getUsergroupInfoU = `/user/role/global/get/`;
/** 删除用户组 */
export const deleteUserGroupU = `/user/role/global/delete/`;

/** 根据用户组获取用户列表 */
export const postUserByUserGroupUrl = `/user/role/relation/global/list`;
/** 创建用户组添加用户 */
export const addUserToUserGroupUrl = `/user/role/relation/global/add`;
/** 删除用户组用户 */
export const deleteUserFromUserGroupUrl = `/user/role/relation/global/delete/`;

// 组织下的用户组

export const updateOrgUserGroupU = `/user/role/organization/update`;
/** 编辑用户组对应的权限配置 */
export const editOrgUSettingUrl = `/user/role/organization/permission/update`;
/** 添加用户组 */
export const addOrgUserGroupU = `/user/role/organization/add`;
/** 获取用户组对应的权限配置 */
export const getOrgUSettingUrl = `/user/role/organization/permission/setting/`;
/** 获取用户组 */
export const getOrgUserGroupU = `/user/role/organization/list/`;
/** 获取单个用户组信息 */
export const getOrgUsergroupInfoU = `/user/role/organization/get/`;
/** 删除用户组 */
export const deleteOrgUserGroupU = `/user/role/organization/delete/`;

/** 根据用户组获取用户列表 */
export const postOrgUserByUserGroupUrl = `/user/role/relation/organization/list`;
/** 创建用户组添加用户 */
export const addOrgUserToUserGroupUrl = `/user/role/relation/organization/add`;
/** 删除用户组用户 */
export const deleteOrgUserFromUserGroupUrl = `/user/role/relation/organization/delete/`;
