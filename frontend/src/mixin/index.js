import {PROJECT_ID, TokenKey, WORKSPACE_ID} from "@/common/js/constants";

const mixin = {
  data() {
    return {
      msCurrentProjectId: sessionStorage.getItem(PROJECT_ID),
      msCurrentWorkspaceId: sessionStorage.getItem(WORKSPACE_ID),
      msCurrentUser: JSON.parse(localStorage.getItem(TokenKey)),
      msCurrentUserId: JSON.parse(localStorage.getItem(TokenKey)).id
    }
  }
}

export default mixin
