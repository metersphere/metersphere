import { post, get } from 'metersphere-frontend/src/plugins/request';

export function getApiReportPage(currentPage, pageSize, condition) {
  let url = '/api/testcase/list-execute-res/' + currentPage + '/' + pageSize;
  return post(url, condition);
}

export function getApiReportDetail(id) {
  let url = '/api/definition/report/get/' + id;
  return get(url);
}

export function getLastResult(id) {
  return get(`/api/definition/exec/result/last-result/${id}`);
}

export function getLastResultDetail(id, _this) {
  if (id) {
    getLastResult(id).then((response) => {
      if (response.data && response.data.content) {
        let data = JSON.parse(response.data.content);
        _this.responseData = data;
      }
    });
  }
}
