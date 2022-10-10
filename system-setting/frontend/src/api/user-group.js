import {get, post} from 'metersphere-frontend/src/plugins/request';

/**
 * param: {type: '', resourceId: ''}
 * @param param
 * @returns {Promise | Promise<unknown>}
 */
export function getUserGroupList(param) {
  return post('/user/group/list', param);
}

export function getWorkspaceMemberGroup(workspaceId, userId) {
  return get(`/user/group/list/ws/${workspaceId}/${userId}`);
}

export function getProjectMemberGroup(projectId, userId) {
  return get(`/user/group/list/project/${projectId}/${userId}`);
}

export function getUserGroupListByPage(goPage, pageSize, param) {
  return post(`/user/group/get/${goPage}/${pageSize}`, param);
}

export function getAllUserGroupByType(param) {
  return post('/user/group/get', param);
}

export function getUserGroupsByTypeAndId(type, id) {
  return get(`/user/group/${type}/${id}`);
}

export function getAllUserGroup() {
  return get(`/user/group/get/all`);
}

export function delUserGroupById(groupId) {
  return get(`/user/group/delete/${groupId}`);
}

export function getUserGroupPermission(param) {
  return post('/user/group/permission', param);
}

export function modifyUserGroupPermission(param) {
  return post('/user/group/permission/edit', param);
}

export function createUserGroup(group) {
  return post('/user/group/add', group);
}

export function modifyUserGroup(group) {
  return post('/user/group/edit', group);
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

export function addUser2Group(param) {
  return post('/user/group/add/member', param);
}

export function getUserAllGroups(userId) {
  return get(`/user/group/all/${userId}`);
}
