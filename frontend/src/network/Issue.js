import {post} from "@/common/js/ajax";
import {getPageDate} from "@/network/network-utils";

export function buildIssues(data, page) {
  for (let i = 0; i < data.length; i++) {
    if (data[i]) {
      if (data[i].platform !== 'Local') {
        page.result = buildPlatformIssue(data[i]);
      }
    }
  }
  page.data = data;
}

export function getIssues(page) {
  return post('issues/list/' + page.currentPage + '/' + page.pageSize, page.condition, (response) => {
    let data = getPageDate(response, page);
    buildIssues(data, page);
  });
}

export function buildPlatformIssue(data) {
  return post("issues/get/platform/issue", data).then(response => {
    let issues = response.data.data;
    if (issues) {
      data.title = issues.title ? issues.title : '--';
      data.description = issues.description ? issues.description : '--';
      data.status = issues.status ? issues.status : 'delete';
    }
  }).catch(() => {
    data.title = '--';
    data.description = '--';
    data.status = '--';
  });
}

export function testCaseIssueRelate(param, success) {
  return post('test/case/issues/relate', param, (response) => {
    if (success) {
      success(response);
    }
  });
}

export function getRelateIssues(page) {
  return post('issues/list/relate/' + page.currentPage + '/' + page.pageSize, page.condition, (response) => {
    let data = getPageDate(response, page);
    buildIssues(data, page);
  });
}


