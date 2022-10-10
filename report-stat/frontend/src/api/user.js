/* 前后端不分离的登录方式 */
import {get, post, put} from "metersphere-frontend/src/plugins/request"

export function login(data) {
  return post("/signin", data)
}

export function logout() {
  return get("/signout")
}

export function isLogin() {
  return get("/is-login")
}

export function getUesrGroup(projectId) {
  return get("/enterprise/test/report/initUserGroupInfo/" + projectId)
}

export function getCurrentUser() {
  return get("/currentUser")
}

export function updateInfo(id, data) {
  return put("/samples/user/info/update/" + id, data)
}



