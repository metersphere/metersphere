import {get, post} from 'metersphere-frontend/src/plugins/request';

/**
 * param: {type: '', resourceId: ''}
 * @param param
 * @returns {Promise | Promise<unknown>}
 */
export function getUserGroupList(param) {
  return post('/user/group/list', param);
}

export function getUserGroupProject(projectId, userId) {
  return get(`/user/group/list/project/${projectId}/${userId}`);
}

export function getUserGroupPermission(param) {
  return post('/user/group/permission', param);
}

export function modifyUserGroupPermission(param) {
  return post('/user/group/permission/edit', param);
}

export function addUser2Group(param) {
  return post('/user/group/add/member', param);
}

export function getUserGroupByResourceUrlAndPage(url, goPage, pageSize, param) {
  return post(`${url}${goPage}/${pageSize}`, param);
}

export function getUserGroupSourceByUserIdAndGroupId(userId, groupId) {
  return get(`/user/group/source/${userId}/${groupId}`);
}

export function modifyUserGroupMember(param) {
  return post('/user/group/edit/member', param);
}

export function rmUserFromGroup(userId, groupId) {
  return get(`/user/group/rm/${userId}/${groupId}`);
}

export function createUserGroup(group) {
  return post('/user/group/add', group);
}

export function modifyUserGroup(group) {
  return post('/user/group/edit', group);
}

export function getWorkspaceMemberGroup(workspaceId, userId) {
  return get(`/user/group/list/ws/${workspaceId}/${userId}`);
}

export function getCurrentProjectUserGroupPages(goPage, pageSize, condition) {
  return post(`/user/group/get/current/project/${goPage}/${pageSize}`, condition);
}

export function deleteUserGroup(groupId) {
  return get(`/user/group/delete/${groupId}`);
}

export function getGroupResource(groupId, groupType) {
  return get(`/user/group/workspace/list/resource/${groupId}/${groupType}`);
}
