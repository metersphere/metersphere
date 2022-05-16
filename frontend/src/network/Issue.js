import {post, get} from "@/common/js/ajax";
import {getPageDate, parseCustomFilesForList} from "@/common/js/tableUtils";
import {getCurrentProjectID, hasLicense} from "@/common/js/utils";
import {baseGet, basePost} from "@/network/base-network";
import {getCurrentProject} from "@/network/project";
import {JIRA, LOCAL} from "@/common/js/constants";
import {getIssueTemplate} from "@/network/custom-field-template";

export function buildIssues(page) {
  let data = page.data;
  for (let i = 0; i < data.length; i++) {
    if (data[i]) {
      if (data[i].customFields) {
        data[i].customFields = JSON.parse(data[i].customFields);
      }
    }
  }
}

export function getIssues(page) {
  return post('issues/list/' + page.currentPage + '/' + page.pageSize, page.condition, (response) => {
    getPageDate(response, page);
    parseCustomFilesForList(page.data);
    // buildIssues(page);
  });
}


export function getDashboardIssues(page) {
  return post('issues/dashboard/list/' + page.currentPage + '/' + page.pageSize, page.condition, (response) => {
    getPageDate(response, page);
    buildIssues(page);
  });
}

export function getIssuesByCaseId(refType, caseId, page) {
  if (caseId) {
    return get('issues/get/case/' + refType + '/' + caseId, (response) => {
      page.data = response.data;
      buildIssues(page);
    });
  }
}

export function getIssuesById(id, callback) {
  return id ? baseGet('/issues/get/' + id, callback) : {};
}

export function getIssuesListById(id,projectId,workspaceId,callback) {
  let condition ={
    id:id,
    projectId : projectId,
    workspaceId: workspaceId
  };
  return post('issues/list/' + 1 + '/' + 10, condition, (response) => {
    if (callback) {
      callback(response.data.listObject[0]);
    }
  });
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

export function getIssueThirdPartTemplate() {
  return new Promise(resolve => {
    baseGet('/issues/thirdpart/template/' + getCurrentProjectID(), (data) => {
      let template = data;
      if (template.customFields) {
        template.customFields.forEach(item => {
          if (item.options) {
            item.options = JSON.parse(item.options);
          }
        });
      }
      resolve(template);
    })
  });
}

export function getIssuePartTemplateWithProject(callback) {
  getCurrentProject((project) => {
    let currentProject = project;
    if (enableThirdPartTemplate(currentProject)) {
      getIssueThirdPartTemplate()
        .then((template) => {
          if (callback)
              callback(template, currentProject);
        });
    } else {
      getIssueTemplate()
        .then((template) => {
          if (callback)
            callback(template, currentProject);
        });
    }
  });
}

export function enableThirdPartTemplate(currentProject) {
  return currentProject && currentProject.thirdPartTemplate && currentProject.platform === JIRA;
}

export function isThirdPartEnable(callback) {
  getCurrentProject((project) => {
    if (callback)
      callback(project.platform !== LOCAL);
  });
}

export function getJiraIssueType(param, callback) {
  return basePost('/issues/jira/issuetype', param, callback);
}
