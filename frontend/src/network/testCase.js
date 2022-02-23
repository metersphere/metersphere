import {post, get} from "@/common/js/ajax";
import {success} from "@/common/js/message";
import i18n from "@/i18n/i18n";
import {basePost} from "@/network/base-network";
import {baseGet} from "./base-network";

export function getTestCasesForMinder(request, callback) {
  return post('/test/case/list/minder', request, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function getPlanCasesForMinder(request, callback) {
  return post('/test/plan/case/list/minder', request, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function getReviewCasesForMinder(request, callback) {
  return post('/test/review/case/list/minder', request, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function getRelateTest(caseId, callback) {
  if (caseId) {
    return get('/test/case/relate/test/list/' + caseId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
  return {};
}


export function deleteRelateTest(caseId, testId, callback) {
  if (caseId && testId) {
    return get('/test/case/relate/delete/' + caseId + '/' + testId, (response) => {
      success(i18n.t('commons.save_success'));
      if (callback) {
        callback(response);
      }
    });
  }
}

export function editTestCaseOrder(request, callback) {
  return basePost('/test/case/edit/order', request, callback);
}

export function getMinderExtraNode(groupId, nodeId, callback) {
  return baseGet('/minder/extra/node/list/' + groupId + '/' + nodeId, callback);
}

export function editTestReviewTestCaseOrder(request, callback) {
  return basePost('/test/review/case/edit/order', request, callback);
}

export function getTestCaseNodes(projectId, callback) {
  return baseGet('/case/node/list/' + projectId, callback);
}

export function getRelationshipCase(id, relationshipType, callback) {
  return baseGet('/test/case/relationship/case/' + id + '/' + relationshipType, callback);
}

export function getRelationshipCountCase(id, callback) {
  return baseGet('/test/case/relationship/case/count/' + id + '/', callback);
}

export function getTestPlanTestCase(pageNum, pageSize, param, callback) {
  return basePost('/test/plan/case/list/' + pageNum + '/' + pageSize, param, callback);
}

export function getTestReviewTestCase(pageNum, pageSize, param, callback) {
  return basePost('/test/review/case/list/' + pageNum + '/' + pageSize, param, callback);
}
