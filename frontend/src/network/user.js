import {get} from "@/common/js/ajax";
import axios from "axios";
import {ORGANIZATION_ID, PROJECT_ID, TokenKey, WORKSPACE_ID} from "@/common/js/constants";
import {baseGet} from "@/network/base-network";

export function getProjectMember(callBack) {
  return new Promise((resolve) => {
    get('/user/project/member/list', response => {
      if (callBack) {
        callBack(response.data);
      }
      resolve(response.data);
    });
  });
}

export function getProjectMemberUserFilter(callBack) {
  return baseGet('/user/project/member/list', (data) => {
    if (callBack) {
      let filter = data.map(u => {
        return {text: u.name, value: u.id};
      });
      callBack(filter);
    }
  });
}

export function getProjectMemberById(projectId, callback) {
  return projectId ? baseGet('/user/project/member/' + projectId, callback) : {};
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

let baseUrl = '/user/';

export function getProjectMemberOption(callback) {
  return baseGet(baseUrl + 'project/member/option', callback);
}
