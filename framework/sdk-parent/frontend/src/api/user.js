/* 前后端不分离的登录方式 */
import {get, post} from "../plugins/request";

export function login(url, data) {
  return post(url, data)
}

export function logout() {
  return get("/signout");
}

export function isLogin() {
  return get("/is-login")
}

export function getCurrentUser() {
  return get("/currentUser")
}

export function getLanguage() {
  return get("/language")
}

export function updateInfo(data) {
  return post("/user/update/current", data)
}

export function updatePassword(data) {
  return post("/user/update/password", data)
}

export function handleAuth(param) {
  return post('/personal/relate/issues/user/auth', param);
}

export function getSystemTheme() {
  return get('/system/theme')
}

export function getDisplayInfo() {
  return get('/display/info')
}

export function saveBaseUrl() {
  return get('/system/save/baseurl?baseurl=' + window.location.origin)
}

export function getSystemVersion() {
  return get('/system/version')
}

export function getProjectUsers() {
  return get('/user/project/member/list');
}

export function getAuthSources() {
  return get('/authsource/list/allenable')
}

export function getAuthSource(id) {
  return get('/authsource/' + id)
}

export function getProjectMemberOption() {
  return get('/user/project/member/option');
}

export function getProjectMemberById(projectId) {
  return get(`/user/project/member/${projectId}`);
}

export function checkLdapOpen() {
  return get('/ldap/open')
}

export function getUserInfo() {
  return get(`/user/info`);
}

export function getWsAndPj(userId) {
  return get(`/user/get/ws-pj/${userId}`);
}

export function getUserKeys() {
  return get('/user/key/info')
}

export function deleteUserKey(id) {
  return get(`/user/key/delete/${id}`)
}

export function generateKey() {
  return get(`/user/key/generate`)
}

export function activeUserKey(id) {
  return get(`/user/key/active/${id}`)
}

export function disableUserKey(id) {
  return get(`/user/key/disable/${id}`)
}

export function updateSeleniumServer(data) {
  return post(`/user/update/selenium-server`, data)
}

export function modifyUserByResourceId(resourceId) {
  return get(`/user/update/current-by-resource/${resourceId}`);
}

export function isSuperUser(userId) {
  return get(`/user/is/super/${userId}`);
}
