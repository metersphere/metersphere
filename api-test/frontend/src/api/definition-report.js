import { post, get } from "metersphere-frontend/src/plugins/request";

export function getApiReportPage(currentPage, pageSize, condition) {
  let url = "/api/testcase/list-execute-res/" + currentPage + "/" + pageSize;
  return post(url, condition);
}

export function getApiReportDetail(id) {
  let url = "/api/definition/report/get/" + id;
  return get(url);
}
