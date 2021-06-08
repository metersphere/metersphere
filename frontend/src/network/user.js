import {getCurrentProjectID} from "@/common/js/utils";
import {post} from "@/common/js/ajax";

export function getProjectMember(callBack) {
  return post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
    if (callBack) {
      callBack(response.data);
    }
  });
}
