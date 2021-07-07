import {getCurrentProjectID} from "@/common/js/utils";
import {post} from "@/common/js/ajax";

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
