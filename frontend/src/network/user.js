import {getCurrentProjectID} from "@/common/js/utils";
import {post} from "@/common/js/ajax";
import axios from "axios";
import {ORGANIZATION_ID, PROJECT_ID, TokenKey, WORKSPACE_ID} from "@/common/js/constants";

export function getProjectMember(callBack) {
  return new Promise((resolve) => {
    post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
      if (callBack) {
        callBack(response.data);
      }
      resolve(response.data);
    });
  });
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
