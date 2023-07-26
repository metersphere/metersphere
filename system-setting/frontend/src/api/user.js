/* 前后端不分离的登录方式 */
import {get, post, put, request} from 'metersphere-frontend/src/plugins/request'

export function login(data) {
  return post('/signin', data)
}

export function logout() {
  return get('/signout')
}

export function isLogin() {
  return get('/is-login')
}

export function getCurrentUser() {
  return get('/currentUser')
}

export function updateInfo(id, data) {
  return put('/samples/user/info/update/' + id, data)
}

export function specialDeleteUserById(id) {
  return get(`/user/special/delete?userId=` + id);
}

export function specialCreateUser(user) {
  return post('/user/special/add', user);
}

export function specialModifyUser(user) {
  return post('/user/special/update', user);
}

export function specialModifyPassword(user) {
  return post('/user/special/password', user);
}

export function specialListUsers(data, page, size) {
  return post(`/user/special/list/${page}/${size}`, data)
}

export function specialGetUserGroup(userId) {
  return get(`/user/special/user/group/${userId}`);
}

export function specialModifyUserDisable(user) {
  return post('/user/special/update_status', user);
}

export function specialBatchProcessUser(params) {
  return post('/user/special/batch-process-user', params);
}

export function getWorkspaceMemberSpecial(param) {
  return post('/user/special/ws/member/list/all', param);
}

export function getWorkspaceMemberListSpecial(goPage, pageSize, param) {
  return post(`/user/special/ws/member/list/${goPage}/${pageSize}`, param);
}

export function addWorkspaceMemberSpecial(param) {
  return post('/user/special/ws/member/add', param);
}

export function delWorkspaceMemberSpecialById(workspaceId, userId) {
  return get(`/user/special/ws/member/delete/${workspaceId}/${userId}`);
}

export function delWorkspaceMemberById(workspaceId, userId) {
  return get(`/user/ws/member/delete/${workspaceId}/${userId}`);
}

export function getUserListByResourceUrl(url) {
  return get(url);
}

export function getWorkspaceMemberPages(goPage, pageSize, param) {
  return post(`/user/ws/member/list/${goPage}/${pageSize}`, param);
}

export function addWorkspaceMember(member) {
  return post('user/ws/member/add', member);
}

export function getProjectMemberPages(goPage, pageSize, workspaceId, param) {
  return post(`/user/ws/project/member/list/${workspaceId}/${goPage}/${pageSize}`, param);
}

export function getCurrentProjectUserList() {
  return get('/user/project/member/list');
}

export function getCurrentProjectUserPages(goPage, pageSize, param) {
  return post(`/user/project/member/list/${goPage}/${pageSize}`, param);
}

export function updateCurrentUser(user) {
  return post('/user/update/current', user);
}

export function delProjectMember(projectId, memberId) {
  return get(`/user/project/member/delete/${projectId}/${memberId}`);
}

export function addProjectMember(member) {
  return post('user/project/member/add', member);
}

export function exportUserExample() {
  fileDownload('/user/export/template');
}

export function fileDownload(url) {
  let config = {
    method: "get",
    url,
    responseType: 'blob'
  }
  let promise = request(config);
  promise.then(response => {
      let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
      let link = document.createElement("a");
      link.href = window.URL.createObjectURL(new Blob([response.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"}));
      link.download = fileName;
      link.click();
    }).catch(() => {
    //
  });
}

export function userImport(file, files, param) {
  let formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  if (files) {
    files.forEach(f => {
      formData.append("files", f);
    });
  }
  formData.append('request', new Blob([JSON.stringify(param)], {type: 'application/json'}));
  let config = {
    method: 'POST',
    url: '/user/import',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(config);
}
