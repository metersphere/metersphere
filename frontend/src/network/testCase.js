import {post, get} from "@/common/js/ajax";

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
      if (callback) {
        callback(response);
      }
    });
  }
}
