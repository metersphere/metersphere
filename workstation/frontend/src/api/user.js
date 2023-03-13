/* 前后端不分离的登录方式 */
import {get} from "metersphere-frontend/src/plugins/request"


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

