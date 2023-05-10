import {get, post, fileUploadWithProcessAndCancel, fileDownloadGet} from "metersphere-frontend/src/plugins/request"
const BASE_URL = "/attachment/";

export function deleteTestCaseAttachment(caseId) {
  return get(BASE_URL + `delete/testcase/${caseId}`);
}

export function deleteIssueAttachment(fileId) {
  return get(BASE_URL + `delete/issue/${fileId}`);
}

export function attachmentList(param) {
  return post(BASE_URL + `metadata/list`, param);
}

export function uploadIssueAttachment(file, param, cancelToken, cancelTokens, progressCallback) {
  return fileUploadWithProcessAndCancel(BASE_URL + "issue/upload", file, param, cancelToken, cancelTokens, progressCallback);
}

export function uploadTestCaseAttachment(file, param, cancelToken, cancelTokens, progressCallback) {
  return fileUploadWithProcessAndCancel(BASE_URL + "testcase/upload", file, param, cancelToken, cancelTokens, progressCallback);
}

export function unrelatedTestCaseAttachment(param) {
  return post(BASE_URL + "testcase/metadata/unrelated", param)
}

export function unrelatedIssueAttachment(param) {
  return post(BASE_URL + "issue/metadata/unrelated", param)
}

export function relatedTestCaseAttachment(param) {
  return post(BASE_URL + "testcase/metadata/relate", param)
}

export function relatedIssueAttachment(param) {
  return post(BASE_URL + "issue/metadata/relate", param)
}

export function dumpAttachment(param) {
  return post(BASE_URL + "metadata/dump", param)
}

export function downloadAttachment(fileId, isLocal, filename) {
  return fileDownloadGet(BASE_URL + "download/" + fileId + "/" + isLocal, filename, null)
}
