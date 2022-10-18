import {post, get, socket} from "metersphere-frontend/src/plugins/request";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {buildListPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/';

export function testPlanList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function testPlanListAll(param) {
  return post(BASE_URL + 'list/all', param);
}

export function testPlanEdit(param) {
  return post(BASE_URL + 'edit', param);
}

export function testPlanEditStatus(id) {
  return post(BASE_URL + `edit/status/${id}`, {});
}

export function testPlanAdd(param) {
  return post(BASE_URL + 'add', param);
}

export function testPlanCopy(planId) {
  return post(BASE_URL + `copy/${planId}`, {});
}

export function testPlanDelete(planId) {
  return post(BASE_URL + `delete/${planId}`, {});
}

export function testPlanUpdateScheduleEnable(param) {
  return post(BASE_URL + 'update/schedule/enable', param);
}

export function testPlanGetEnableScheduleCount(param) {
  return post(BASE_URL + 'schedule/enable/total', param);
}

export function testPlanRunBatch(param) {
  return post(BASE_URL + 'run/batch', param);
}

export function testPlanRunSave(param) {
  return post(BASE_URL + 'run/save', param);
}

export function testPlanHaveUiCase(id) {
  return get(BASE_URL + `have/ui/case/${id}`);
}

export function testPlanGet(id) {
  return get(BASE_URL + `get/${id}`);
}

export function testPlanAutoCheck(id) {
  return get(BASE_URL + `auto-check/${id}`);
}

export function testPlanGetPrincipal(id) {
  return get(BASE_URL + `principal/${id}`);
}

export function testPlanHaveExecCase(id) {
  return get(BASE_URL + `have/exec/case/${id}`);
}

export function testPlanGetFollow(id) {
  return get(BASE_URL + `follow/${id}`);
}

export function testPlanEditRunConfig(param) {
  return post(BASE_URL + 'edit/run/config', param);
}

export function testPlanEditFollows(id, param) {
  return post(BASE_URL + `edit/follows/${id}`, param);
}

export function getTestPlanReport(planId) {
  if (planId) {
    return get(BASE_URL + 'report/' + planId);
  }
}

export function testPlanRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}

export function getShareTestPlanReport(shareId, planId) {
  if (planId) {
    return get('/share/test/plan/report/' + shareId + '/' + planId);
  }
}

export function planApiTestCasePage(page, pageSize, params) {
  return post(BASE_URL + 'api/case/relevance/list/' + page + '/' + pageSize, params);
}

export function editPlanReport(param) {
  return post(BASE_URL + 'edit/report', param);
}

export function editPlanReportConfig(param) {
  return post(BASE_URL + 'edit/report/config', param);
}

export function getExportReport(planId) {
  return planId ? get(BASE_URL + 'get/report/export/' + planId) : {};
}

export function getTestPlanReportContent(reportId) {
  return reportId ? get(BASE_URL + 'report/db/' + reportId) : {};
}

export function getShareTestPlanReportContent(shareId, reportId) {
  return reportId ? get('/share/test/plan/report/db/' + shareId + '/' + reportId) : {};
}

export function getPlanFunctionAllCase(planId, param) {
  return planId ? post(BASE_URL + 'case/list/all/' + planId, param) : {};
}

export function getSharePlanFunctionAllCase(shareId, planId, param) {
  return planId ? post('/share/test/plan/case/list/all/' + shareId + '/' + planId, param) : {};
}

export function getPlanScenarioFailureCase(planId) {
  return planId ? get(BASE_URL + 'scenario/case/list/failure/' + planId) : {};
}

export function getPlanScenarioErrorReportCase(planId) {
  return planId ? get(BASE_URL + 'scenario/case/list/error-report/' + planId) : {};
}

export function getPlanScenarioUnExecuteCase(planId) {
  return planId ? get(BASE_URL + 'scenario/case/list/pending/' + planId) : {};
}

export function getPlanScenarioAllCase(planId) {
  return planId ? get(BASE_URL + 'scenario/case/list/all/' + planId) : {};
}

export function getSharePlanScenarioFailureCase(shareId, planId) {
  return planId ? get('/share/test/plan/scenario/case/list/failure/' + shareId + '/' + planId) : {};
}

export function getSharePlanScenarioAllCase(shareId, planId) {
  return planId ? get('/share/test/plan/scenario/case/list/all/' + shareId + '/' + planId) : {};
}

export function getSharePlanScenarioErrorReportCase(shareId, planId) {
  return planId ? get('/share/test/plan/scenario/case/list/errorReport/' + shareId + '/' + planId) : {};
}

export function getSharePlanScenarioUnExecuteCase(shareId, planId) {
  return planId ? get('/share/test/plan/scenario/case/list/unExecute/' + shareId + '/' + planId) : {};
}

export function getSharePlanUiScenarioAllCase(shareId, planId, param) {
  return planId ? post('/share/test/plan/uiScenario/case/list/all/' + shareId + '/' + planId, param) : {};
}

export function getPlanApiFailureCase(planId) {
  return planId ? get(BASE_URL + 'api/case/list/failure/' + planId) : {};
}

export function getPlanApiErrorReportCase(planId) {
  return planId ? get(BASE_URL + 'api/case/list/errorReport/' + planId) : {};
}

export function getPlanApiUnExecuteCase(planId) {
  return planId ? get(BASE_URL + 'api/case/list/unExecute/' + planId) : {};
}

export function getPlanApiAllCase(planId) {
  return planId ? get(BASE_URL + 'api/case/list/all/' + planId) : {};
}

export function getSharePlanApiFailureCase(shareId, planId) {
  return planId ? get('/share/test/plan/api/case/list/failure/' + shareId + '/' + planId) : {};
}

export function getSharePlanApiErrorReportCase(shareId, planId) {
  return planId ? get('/share/test/plan/api/case/list/errorReport/' + shareId + '/' + planId) : {};
}

export function getSharePlanApiUnExecuteCase(shareId, planId) {
  return planId ? get('/share/test/plan/api/case/list/unExecute/' + shareId + '/' + planId) : {};
}

export function getSharePlanApiAllCase(shareId, planId) {
  return planId ? get('/share/test/plan/api/case/list/all/' + shareId + '/' + planId) : {};
}

export function getPlanLoadFailureCase(planId) {
  return planId ? get(BASE_URL + 'load/case/list/failure/' + planId) : {};
}

export function getPlanLoadAllCase(planId) {
  return planId ? get(BASE_URL + 'load/case/list/all/' + planId) : {};
}

export function getSharePlanLoadFailureCase(shareId, planId) {
  return planId ? get('/share/test/plan/load/case/list/failure/' + shareId + '/' + planId) : {};
}

export function getSharePlanLoadAllCase(shareId, planId) {
  return planId ? get('/share/test/plan/load/case/list/all/' + shareId + '/' + planId) : {};
}

export function checkoutLoadReport(param) {
  return post(BASE_URL + 'load/case/report/exist', param);
}

export function shareCheckoutLoadReport(shareId, param) {
  return post('/share/test/plan/load/case/report/exist/' + shareId, param);
}

export function editTestPlanTestCaseOrder(request) {
  return post(BASE_URL + 'case/edit/order', request);
}

export function editTestPlanApiCaseOrder(request) {
  return post(BASE_URL + 'api/case/edit/order', request);
}

export function editTestPlanScenarioCaseOrder(request) {
  return post(BASE_URL + 'scenario/case/edit/order', request);
}

export function editTestPlanLoadCaseOrder(request) {
  return post(BASE_URL + 'load/case/edit/order', request);
}

export function getPlanStageOption() {
  let projectID = getCurrentProjectID();
  return projectID ? get(BASE_URL + 'get/stage/option/' + projectID) : {};
}

export function saveTestPlanReport(planId) {
  return planId ? get(BASE_URL + 'report/saveTestPlanReport/' + planId + '/MANUAL') : {};
}

export function getPlanUiScenarioAllCase(planId, param) {
  return planId ? post(BASE_URL + 'uiScenario/case/list/all/' + planId, param) : {};
}

export function updatePlanSchedule(param) {
  return post(BASE_URL + 'schedule/update/disable', param);
}

export function updateScheduleEnableByPrimyKey(param) {
  return post(BASE_URL + 'schedule/update/enable', param);
}


export function getPlanSchedule(planId,taskType){
  return get(BASE_URL + 'schedule/get/'+ planId + '/'+taskType);
}


export function updateBatchScheduleEnable(param) {
  return post(BASE_URL + 'schedule/Batch/updateEnable', param);
}

export function createSchedule(param){
  return post(BASE_URL + 'schedule/create',param);
}

export function updateSchedule(param){
  return post(BASE_URL + 'schedule/update',param);
}

export function getApiScenarioEnv(param){
  return post(BASE_URL + 'api/scenario/env',param);
}

export function getPlanCaseEnv(param){
  return post(BASE_URL + 'case/env',param);
}

export function run(testId, reportId) {
  return get(`${BASE_URL}api/case/run/${testId}/${reportId}`);
}

export function reportSocket(reportId) {
  return socket('/websocket/plan/api/' + reportId);
}

export function testPlanLoadCaseEditStatus(planId) {
  return post(BASE_URL + `edit/status/${planId}`, {});
}

