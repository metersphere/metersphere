import {post} from "@/common/js/ajax";

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
