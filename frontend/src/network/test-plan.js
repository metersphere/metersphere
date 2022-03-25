import {post, get} from "@/common/js/ajax";
import {success} from "@/common/js/message";
import i18n from "@/i18n/i18n";
import {baseGet, basePost} from "@/network/base-network";
import {getCurrentWorkspaceId} from "@/common/js/utils";

export function getTestPlanReport(planId, callback) {
  if (planId) {
    return get('/test/plan/report/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function getShareTestPlanReport(shareId, planId, callback) {
  if (planId) {
    return get('/share/test/plan/report/' + shareId + '/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function editPlanReport(param, callback) {
  return post('/test/plan/edit/report', param, () => {
    if (callback) {
      callback();
    } else {
      success(i18n.t('commons.save_success'));
    }
  });
}

export function editPlanReportConfig(param, callback) {
  return basePost('/test/plan/edit/report/config', param, (data) => {
    success(i18n.t('commons.save_success'));
    if (callback) {
      callback(data);
    }
  });
}

export function getExportReport(planId, callback) {
  return planId ? baseGet('/test/plan/get/report/export/' + planId, callback) : {};
}

export function getTestPlanReportContent(reportId, callback) {
  return reportId ? baseGet('/test/plan/report/db/' + reportId, callback) : {};
}

export function getShareTestPlanReportContent(shareId, reportId, callback) {
  return reportId ? baseGet('/share/test/plan/report/db/' + shareId + '/' + reportId, callback) : {};
}

export function getPlanFunctionFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/case/list/failure/' + planId, callback) : {};
}

export function getPlanFunctionAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/case/list/all/' + planId, callback) : {};
}


export function getSharePlanFunctionFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanFunctionAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function getPlanScenarioFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/failure/' + planId, callback) : {};
}

export function getPlanScenarioErrorReportCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/errorReport/' + planId, callback) : {};
}

export function getPlanScenarioUnExecuteCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/unExecute/' + planId, callback) : {};
}

export function getPlanScenarioAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/all/' + planId, callback) : {};
}

export function getSharePlanScenarioFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanScenarioAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanScenarioErrorReportCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/errorReport/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanScenarioUnExecuteCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/unExecute/' + shareId + '/' + planId, callback) : {};
}

export function getPlanApiFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/failure/' + planId, callback) : {};
}

export function getPlanApiErrorReportCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/errorReport/' + planId, callback) : {};
}
export function getPlanApiUnExecuteCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/unExecute/' + planId, callback) : {};
}
export function getPlanApiAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/all/' + planId, callback) : {};
}

export function getSharePlanApiFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanApiErrorReportCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/errorReport/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanApiUnExecuteCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/unExecute/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanApiAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function getPlanLoadFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/load/case/list/failure/' + planId, callback) : {};
}

export function getPlanLoadAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/load/case/list/all/' + planId, callback) : {};
}

export function getSharePlanLoadFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/load/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanLoadAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/load/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function checkoutLoadReport(param, callback) {
  return basePost('/test/plan/load/case/report/exist', param, callback);
}

export function shareCheckoutLoadReport(shareId, param, callback) {
  return basePost('/share/test/plan/load/case/report/exist/' + shareId, param, callback);
}

export function editTestPlanTestCaseOrder(request, callback) {
  return basePost('/test/plan/case/edit/order', request, callback);
}

export function editTestPlanApiCaseOrder(request, callback) {
  return basePost('/test/plan/api/case/edit/order', request, callback);
}

export function editTestPlanScenarioCaseOrder(request, callback) {
  return basePost('/test/plan/scenario/case/edit/order', request, callback);
}

export function editTestPlanLoadCaseOrder(request, callback) {
  return basePost('/test/plan/load/case/edit/order', request, callback);
}

export function getPlanStageOption(callback) {
  let wId = getCurrentWorkspaceId();
  return wId ? baseGet('/test/plan/get/stage/option/' + wId, callback) : {};
}


export function saveTestPlanReport(planId, callback) {
  return planId ? baseGet('/test/plan/report/saveTestPlanReport/' + planId + '/MANUAL', callback) : {};
}
