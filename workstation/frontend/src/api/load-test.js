import {post} from "metersphere-frontend/src/plugins/request";


export function editLoadTestCaseOrder(request, callback) {
  return post('/performance/edit/order', request, callback);
}
