import {post, get, fileUploadWithProcessAndCancel} from "metersphere-frontend/src/plugins/request";
import {getPageDate, parseCustomFilesForList} from "metersphere-frontend/src/utils/tableUtils";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getCurrentProject} from "./project";
import {JIRA, LOCAL} from "metersphere-frontend/src/utils/constants";
import {getIssueTemplate} from "./custom-field-template";
import {$success, $warning} from "metersphere-frontend/src/plugins/message";
import i18n from "../i18n";

const BASE_URL = "/issues/";


export function issueDemandList(projectId) {
  return get(BASE_URL + `demand/list/${projectId}`);
}

export function closeIssue(id) {
  return get(BASE_URL + `close/${id}`);
}

export function deleteIssue(id) {
  return get(BASE_URL + `delete/${id}`);
}

export function issueStatusChange(param) {
  return post(BASE_URL + 'change/status', param);
}

export function saveOrUpdateIssue(url, param) {
  return post(url, param);
}

export function saveFollow(id, param) {
  return post(BASE_URL + "up/follows/" + id, param);
}

export function getFollow(id) {
  return get(BASE_URL + "follow/" + id);
}

export function getComments(id) {
  return get(BASE_URL + "comment/list/" + id);
}

export function saveComment(param) {
  return post(BASE_URL + "comment/save", param);
}

export function getIssues(currentPage, pageSize, param) {
  return post(BASE_URL + "list/" + currentPage + '/' + pageSize, param);
}

export function getZentaoBuilds(param) {
  return post(BASE_URL + "zentao/builds", param);
}

export function getZentaoUser(param) {
  return post(BASE_URL + "zentao/user", param);
}

export function getTapdUser(param) {
  return post(BASE_URL + "tapd/user", param);
}

export function getDashboardIssues(page) {
  return post('issues/dashboard/list/' + page.currentPage + '/' + page.pageSize, page.condition)
    .then((response) => {
      getPageDate(response, page);
      buildIssues(page);
    });
}

export function getIssuesByCaseId(refType, caseId, page) {
  if (caseId) {
    return get('issues/get/case/' + refType + '/' + caseId)
      .then((response) => {
        page.data = response.data;
        buildIssues(page);
      });
  }
}

export function getOriginIssuesByCaseId(refType, caseId) {
  return get('issues/get/case/' + refType + '/' + caseId);
}

export function getIssuesById(id) {
  return id ? get('/issues/get/' + id) : {};
}

export function getIssuesForMinder(id, projectId, workspaceId) {
  let condition = {
    id: id,
    projectId: projectId,
    workspaceId: workspaceId
  };
  return new Promise(resolve => {
    post('issues/list/' + 1 + '/' + 10, condition)
      .then((r) => {
        parseCustomFilesForList(r.data.listObject);
        resolve({data: r.data.listObject[0]});
      });
  });
}

export function getIssuesByPlanId(planId) {
  return planId ? get('/issues/plan/get/' + planId) : {};
}

export function getShareIssuesByPlanId(shareId, planId) {
  return planId ? get('/share/issues/plan/get/' + shareId + '/' + planId) : {};
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

export function testCaseIssueRelate(param) {
  return post('test/case/issues/relate', param);
}

export function getRelateIssues(page) {
  return post('issues/list/relate/' + page.currentPage + '/' + page.pageSize, page.condition)
    .then((response) => {
      getPageDate(response, page);
      buildIssues(page);
    });
}

export function syncIssues(param) {
  let url = 'issues/sync';
  if (hasLicense()) {
    url = 'xpack/issue/sync';
  }
  // 浏览器默认策略，请求同一个url，可能导致 stalled 时间过长，加个uuid防止请求阻塞
  url = url + "?stamp=" + getUUID();
  return post(url, param);
}

// 轮询同步状态
export function checkSyncIssues(loading, isNotFirst) {
  let url = 'issues/sync/check/' + getCurrentProjectID() + "?stamp=" + getUUID();
  return get(url)
    .then((response) => {
      if (response.data === false) {
        if (loading === true) {
          if (!isNotFirst) {
            // 第一次才提示
            $warning(i18n.t('test_track.issue.issue_sync_tip'));
          }
          setTimeout(() => checkSyncIssues(loading, true), 1000);
        }
      } else {
        if (loading === true) {
          $success(i18n.t('test_track.issue.sync_complete'));
          loading = false;
        }
      }
    });
}

export function deleteIssueRelate(param) {
  return post('/issues/delete/relate', param);
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

export function isThirdPartEnable(callback) {
  getCurrentProject().then((project) => {
    if (callback)
      callback(project.platform !== LOCAL);
  });
}

export function getJiraIssueType(param) {
  return post('/issues/jira/issuetype', param);
}

export function getPlatformTransitions(param) {
  return post('/issues/platform/transitions', param);
}

export function enableThirdPartTemplate(currentProject) {
  return currentProject && currentProject.thirdPartTemplate && currentProject.platform === JIRA;
}

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
