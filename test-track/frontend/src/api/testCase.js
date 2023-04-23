import {post, get} from "metersphere-frontend/src/plugins/request";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {buildPagePath} from "@/api/base-network";

const BASE_URL = "/test/case/";

export function testCaseList({pageNum, pageSize}, param) {
  let url = buildPagePath({pageNum, pageSize, path: 'list'});
  return post(BASE_URL + url, param);
}

export function testCasePublicList({pageNum, pageSize}, param) {
  let url = buildPagePath({pageNum, pageSize, path: 'public/list'});
  return post(BASE_URL + url, param);
}

export function testCaseAdd(param) {
  return post(BASE_URL + 'save', param);
}

export function getTestCase(id) {
  return get(BASE_URL + `get/${id}`);
}

export function getSimpleTestCase(id) {
  return get(BASE_URL + `get/simple/${id}`);
}

export function getEditSimpleTestCase(id) {
  return get(BASE_URL + `get/edit/simple/${id}`);
}

export function getTestCaseByVersionId(refId, versionId) {
  return get(BASE_URL + `get/version/${refId}/${versionId}`);
}

export function hasTestCaseOtherInfo(id) {
  return get(BASE_URL + `hasOtherInfo/${id}`);
}

export function testCaseGetByVersionId(versionId, id) {
  return get(BASE_URL + `get/${versionId}/${id}`);
}

export function getTestCaseStep(id) {
  return get(BASE_URL + `get/step/${id}`);
}

export function getTestCaseVersions(id) {
  return get(BASE_URL + `versions/${id}`);
}

export function deletePublicTestCaseVersion(versionId, refId) {
  return get(BASE_URL + `deletePublic/${versionId}/${refId}`);
}

export function deleteTestCaseVersion(versionId, refId) {
  return get(BASE_URL + `delete/${versionId}/${refId}`);
}

export function testCaseDelete(id) {
  return post(BASE_URL + `delete/${id}`);
}

export function testCaseDeleteToGc(id) {
  return post(BASE_URL + `deleteToGc/${id}`);
}

export function testCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testCaseBatchDeleteToGc(param) {
  return post(BASE_URL + 'batch/deleteToGc', param);
}

export function testCaseReduction(param) {
  return post(BASE_URL + 'reduction', param);
}

export function testCaseBatchEdit(param) {
  return post(BASE_URL + 'batch/edit', param);
}

export function testCaseBatchCopy(param) {
  return post(BASE_URL + 'batch/copy', param);
}

export function testCasePublicBatchCopy(param) {
  return post(BASE_URL + 'batch/copy/public', param);
}

export function testCasePublicBatchDeleteToGc(param) {
  return post(BASE_URL + 'batch/movePublic/deleteToGc', param);
}

export function testCaseBatchRelateDemand(param) {
  return post(BASE_URL + 'batch/relate/demand', param);
}

export function getTestCaseFollow(id) {
  return get(BASE_URL + `follow/${id}`);
}

export function testCaseEditFollows(id, param) {
  return post(BASE_URL + `edit/follows/${id}`, param);
}

export function addTestCaseRelationship(param) {
  return post(BASE_URL + `relationship/add`, param);
}

export function testCaseRelationshipRelateList({pageNum, pageSize}, param) {
  let url = buildPagePath({pageNum, pageSize, path: 'relationship/relate'});
  return post(BASE_URL + url, param);
}

export function testCaseRelateList({pageNum, pageSize}, param) {
  let url = buildPagePath({pageNum, pageSize, path: 'relate'});
  return post(BASE_URL + url, param);
}

export const minderPageInfoMap = new Map();

function getMinderPageInfo(request) {
  if (!minderPageInfoMap.get(request.nodeId)) {
    minderPageInfoMap.set(request.nodeId, {
      pageNum: 1,
      pageSize: 100
    });
  }
  return minderPageInfoMap.get(request.nodeId);
}

export function getTestCasesForMinder(request, callback) {
  let minderPageInfo = getMinderPageInfo(request);
  let url = '/test/case/list/minder/' + minderPageInfo.pageNum + '/' + minderPageInfo.pageSize;
  return post(url, request).then((response) => {
    if (callback) {
      minderPageInfo.total = response.data.itemCount;
      callback(response.data.listObject);
    }
  });
}

export function getPlanCasesForMinder(request, callback) {
  let minderPageInfo = getMinderPageInfo(request);
  let url = '/test/plan/case/list/minder/' + minderPageInfo.pageNum + '/' + minderPageInfo.pageSize;
  return post(url, request).then((response) => {
    if (callback) {
      minderPageInfo.total = response.data.itemCount;
      callback(response.data.listObject);
    }
  });
}

export function getReviewCasesForMinder(request, callback) {
  let minderPageInfo = getMinderPageInfo(request);
  let url = '/test/review/case/list/minder/' + minderPageInfo.pageNum + '/' + minderPageInfo.pageSize;
  return post(url, request).then((response) => {
    if (callback) {
      minderPageInfo.total = response.data.itemCount;
      callback(response.data.listObject);
    }
  });
}

export function getRelateTest(caseId) {
  return get('/test/case/relate/test/list/' + caseId);
}

export function deleteRelateTest(caseId, testId) {
  return get('/test/case/relate/delete/' + caseId + '/' + testId);
}

export function editTestCaseOrder(request) {
  return post('/test/case/edit/order', request);
}

export function getMinderExtraNode(groupId, nodeId) {
  return get('/minder/extra/node/list/' + groupId + '/' + nodeId);
}

export function testCaseMinderEdit(param) {
  return post(BASE_URL + 'minder/edit', param);
}

export function editTestReviewTestCaseOrder(request) {
  return post('/test/review/case/edit/order', request);
}

export function getTestCaseNodesByCaseFilter(projectId, param) {
  return post('/case/node/list/' + projectId, param);
}

export function getTestCaseNodesCountMap(projectId, param) {
  return post('/case/node/count/' + projectId, param);
}

export function getTestPlanCaseNodesByCaseFilter(planId, param) {
  return post('/case/node/list/plan/' + planId, param);
}

export function getTestReviewCaseNodesByCaseFilter(reviewId, param) {
  return post('/case/node/list/review/' + reviewId, param);
}

export function getTestCasePublicNodes(param) {
  return post('/case/node/list/public/' + getCurrentWorkspaceId(), param);
}

export function getTestCaseTrashNodes(param) {
  return post('/case/node/list/trash/' + getCurrentProjectID(), param);
}

export function getRelationshipCase(id, relationshipType) {
  return get('/test/case/relationship/case/' + id + '/' + relationshipType);
}

export function getRelationshipCountCase(id) {
  return get('/test/case/relationship/case/count/' + id);
}

export function getTestPlanTestCase(pageNum, pageSize, param) {
  return post('/test/plan/case/list/' + pageNum + '/' + pageSize, param);
}

export function getTestReviewTestCase(pageNum, pageSize, param) {
  return post('/test/review/case/list/' + pageNum + '/' + pageSize, param);
}

export function getMinderTreeExtraNodeCount(param) {
  return post('/case/node/minder/extraNode/count', param);
}

export function getTestCaseIssueList(param) {
  return post(BASE_URL + "issues/list", param);
}

export function getTestCaseRelateIssue(pageNum, pageSize, param) {
  return post(BASE_URL + "relate/issue/" + pageNum + "/" + pageSize, param);
}

export function getTestCaseRelevanceApiList(pageNum, pageSize, param) {
  return post(BASE_URL + "relevance/api/list/" + pageNum + "/" + pageSize, param);
}

export function getTestCaseRelevanceScenarioList(pageNum, pageSize, param) {
  return post(BASE_URL + "relevance/scenario/list/" + pageNum + "/" + pageSize, param);
}

export function getTestCaseRelevanceUiScenarioList(pageNum, pageSize, param) {
  return post(BASE_URL + "relevance/uiScenario/list/" + pageNum + "/" + pageSize, param);
}

export function getTestCaseRelevanceLoadList(pageNum, pageSize, param) {
  return post(BASE_URL + "relevance/load/list/" + pageNum + "/" + pageSize, param);
}

export function saveCaseRelevanceApi(caseId, param) {
  return post(BASE_URL + "relate/test/testcase/" + caseId, param);
}

export function saveCaseRelevanceScenario(caseId, param) {
  return post(BASE_URL + "relate/test/automation/" + caseId, param);
}

export function saveUiCaseRelevanceScenario(caseId, param) {
  return post(BASE_URL + "relate/test/uiAutomation/" + caseId, param);
}

export function saveCaseRelevanceLoad(caseId, param) {
  return post(BASE_URL + "relate/test/performance/" + caseId, param);
}

export function checkProjectPermission(projectId) {
  return get(BASE_URL + "check/permission/" + projectId);
}

