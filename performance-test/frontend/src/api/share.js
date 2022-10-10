import {generateShareUrl, get, post} from "metersphere-frontend/src/plugins/request";


export function generateShareInfoWithExpired(param) {
  return post("/share/generate/expired", param);
}

export function getShareInfo(id) {
  return get('/share/info/get/' + id);
}

export function getShareId() {
  let herfUrl = window.location.href;
  if (herfUrl.indexOf('shareId=') > -1) {
    let shareId = '';
    new URL(herfUrl).searchParams.forEach((value, key) => {
      if (key === 'shareId') {
        shareId = value;
      }
    });
    return shareId;
  } else {
    if (herfUrl.indexOf("?") > 0) {
      let paramArr = herfUrl.split("?");
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
  let name = '/share-report';
  return generateShareUrl(name, data.shareUrl);
}
