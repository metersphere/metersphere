import {get, post} from "metersphere-frontend/src/plugins/request"
import {getIssueTemplate} from "metersphere-frontend/src/api/custom-field-template";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProject} from "@/api/project";
import {JIRA} from "metersphere-frontend/src/utils/constants";

export function getJiraIssueType(param) {
  return post('/issues/jira/issuetype', param);
}


export function getIssues(page) {
  return post(`issues/list/${page.currentPage}/${page.pageSize}`, page.condition);
}

export function syncIssues(success) {
  let url = 'issues/sync/';
  if (hasLicense()) {
    url = 'xpack/issue/sync/';
  }
  // 浏览器默认策略，请求同一个url，可能导致 stalled 时间过长，加个uuid防止请求阻塞
  url = url + getCurrentProjectID() + "?stamp=" + getUUID();
  return get(url, success);
}

export function getDashboardIssues(page) {
  return post(`issues/dashboard/list/${page.currentPage}/${page.pageSize}`, page.condition);
}


export function getIssuePartTemplateWithProject(callback) {
  getCurrentProject().then((response) => {
    let currentProject = response.data;
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

export function getIssueThirdPartTemplate() {
  return get('/issues/thirdpart/template/' + getCurrentProjectID())
    .then((response) => {
      let template = response.data;
      if (template.customFields) {
        template.customFields.forEach(item => {
          if (item.options) {
            item.options = JSON.parse(item.options);
          }
        });
      }
      return template
    });
}

export function getIssuesCount(param) {
  return post('/issues/status/count', param);
}

export function getIssuesWeekCount(workstationId) {
  return get('/workstation/issue/week/count/'+workstationId);
}

