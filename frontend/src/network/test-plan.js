import {post, get} from "@/common/js/ajax";
import {success} from "@/common/js/message";
import i18n from "@/i18n/i18n";
import {baseGet} from "@/network/base-network";

export function getTestPlanReport(planId, callback) {
  if (planId) {
    return get('/test/plan/report/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function getShareTestPlanReport(planId, callback) {
  if (planId) {
    return get('/share/test/plan/report/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function editPlanReport(param) {
  return post('/test/plan/edit/report', param, () => {
    success(i18n.t('commons.save_success'));
  });
}

export function getPlanFunctionFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/case/list/failure/' + planId, callback) : {};
}

export function getSharePlanFunctionFailureCase(planId, callback) {
  return planId ? baseGet('/share/test/plan/case/list/failure/' + planId, callback) : {};
}

export function getPlanScenarioFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/failure/' + planId, callback) : {};
}

export function getSharePlanScenarioFailureCase(planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/failure/' + planId, callback) : {};
}

export function getPlanApiFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/failure/' + planId, callback) : {};
}

export function getSharePlanApiFailureCase(planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/failure/' + planId, callback) : {};
}

export function getPlanLoadFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/load/case/list/failure/' + planId, callback) : {};
}

export function getSharePlanLoadFailureCase(planId, callback) {
  return planId ? baseGet('/share/test/plan/load/case/list/failure/' + planId, callback) : {};
}
