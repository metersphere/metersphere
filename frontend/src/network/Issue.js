import {post, get} from "@/common/js/ajax";
import {getPageDate} from "@/common/js/tableUtils";
import {getCurrentProjectID, hasLicense} from "@/common/js/utils";
import {baseGet, basePost} from "@/network/base-network";

export function buildIssues(page) {
  let data = page.data;
  for (let i = 0; i < data.length; i++) {
    if (data[i]) {
      if (data[i].customFields) {
        data[i].customFields = JSON.parse(data[i].customFields);
      }
      // if (data[i].platform !== 'Local') {
      //   page.result = buildPlatformIssue(data[i]);
      // }
    }
  }
}

export function getIssues(page) {
  return post('issues/list/' + page.currentPage + '/' + page.pageSize, page.condition, (response) => {
    getPageDate(response, page);
    buildIssues(page);
  });
}

export function getIssuesByCaseId(caseId, page) {
  if (caseId) {
    return get('issues/get/case/' + caseId, (response) => {
      page.data = response.data;
      buildIssues(page);
    });
  }
}

export function getIssuesById(id, callback) {
  return id ? baseGet('/issues/get/' + id, callback) : {};
}

export function getIssuesByPlanId(planId, callback) {
  return planId ? baseGet('/issues/plan/get/' + planId, callback) : {};
}

export function getShareIssuesByPlanId(shareId, planId, callback) {
  return planId ? baseGet('/share/issues/plan/get/' + shareId + '/' + planId, callback) : {};
}

export function buildPlatformIssue(data) {
  data.customFields = JSON.stringify(data.customFields);
  return post("issues/get/platform/issue", data).then(response => {
    let issues = response.data.data;
    if (issues) {
      data.title = issues.title ? issues.title : '--';
      data.description = issues.description ? issues.description : '--';
      data.status = issues.status ? issues.status : 'delete';
      data.customFields = JSON.parse(data.customFields);
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
    getPageDate(response, page);
    buildIssues(page);
  });
}

export function syncIssues(success) {
  let uri = 'issues/sync/';
  if (hasLicense()) {
    uri = 'xpack/issue/sync/';
  }
  return get(uri + getCurrentProjectID(), (response) => {
    if (success) {
      success(response);
    }
  });
}

export function deleteIssueRelate(param, callback) {
  return basePost('/issues/delete/relate', param, callback);
}
