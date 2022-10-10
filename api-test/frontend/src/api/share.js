import {post, generateShareUrl, get} from "metersphere-frontend/src/plugins/request"

export function generateApiDocumentShareInfo(param) {
  return post("/share/generate/api/document", param);
}

export function selectApiInfoByParam(param, currentPage, pageSize) {
  return post("/share/list/" + currentPage + "/" + pageSize, param);
}

export function generateShareInfoWithExpired(param) {
  return post("/share/generate/expired", param);
}

export function getShareContent(shareId, stepId) {
  let url = "/share/" + shareId + "/scenario/report/detail/" + stepId;
  return get(url);
}

export function getShareApiReport(shareId, testId) {
  return get('/share/api/definition/report/getReport/' + shareId + '/' + testId);
}

export function getShareInfo(id) {
  return get('/share/info/get/' + id);
}

export function getShareId() {
  let hrefUrl = window.location.href;
  if (hrefUrl.indexOf('shareId=') > -1) {
    let shareId = '';
    new URL(hrefUrl).searchParams.forEach((value, key) => {
      if (key === 'shareId') {
        shareId = value;
      }
    });
    return shareId;
  } else {
    if (hrefUrl.indexOf("?") > 0) {
      let paramArr = hrefUrl.split("?");
      if (paramArr.length > 1) {
        let shareId = paramArr[1];
        if (shareId.indexOf("#") > 0) {
          shareId = shareId.split("#")[0];
        }
        return shareId;
      }
    }
  }
  return "";
}

export function getShareRedirectUrl(data) {
  let name = '/share-api-report';
  return generateShareUrl(name, data.shareUrl);
}

export function documentShareUrl(data) {
  let name = '/share-document';
  return generateShareUrl(name, data.shareUrl);
}
