import {get} from "metersphere-frontend/src/plugins/request";
import axios from "axios";
import {ORGANIZATION_ID, PROJECT_ID, TokenKey, WORKSPACE_ID} from "metersphere-frontend/src/utils/constants";

export function getProjectMember() {
  return get('/user/project/member/list');
}

export function getProjectMemberUserFilter(callBack) {
  return get('/user/project/member/list').then((r) => {
    if (callBack) {
      let filter = r.data.map(u => {
        return {text: u.name, value: u.id};
      });
      callBack(filter);
    }
  });
}

export function getProjectMemberById(projectId) {
  return projectId ? get('/user/project/member/' + projectId) : {};
}

export function logout() {
  axios.get("/signout").then(response => {
    if (response.data.success) {
      localStorage.removeItem(TokenKey);

      sessionStorage.removeItem(ORGANIZATION_ID);
      sessionStorage.removeItem(WORKSPACE_ID);
      sessionStorage.removeItem(PROJECT_ID);
      window.location.href = "/login";
    }
  }).catch(error => {
    localStorage.removeItem(TokenKey);

    sessionStorage.removeItem(ORGANIZATION_ID);
    sessionStorage.removeItem(WORKSPACE_ID);
    sessionStorage.removeItem(PROJECT_ID);
    window.location.href = "/login";
  });
}

const BASE_URL = '/user/';


export function getCurrentByResourceId(id) {
  return get('/user/update/currentByResourceId/' + id, () => {
  });
}
