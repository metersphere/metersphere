import {get} from "metersphere-frontend/src/plugins/request"

export function getMyCreatedCaseGroupContMap(param) {
  return get('/workstation/creat_case_count/list/'+param);
}

export function getFollowTotalCount(workstationId) {
  return get('/workstation/follow/total/count/'+ workstationId);
}

export function getUpcomingTotalCount(workstationId) {
  return get('/workstation/coming/total/count/'+ workstationId);
}
