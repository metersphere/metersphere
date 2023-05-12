import {get, post} from "@/common/js/ajax";
import {baseGet} from "@/network/base-network";


export function generateApiDocumentShareInfo(param, callback) {
  return post("/share/info/generateApiDocumentShareInfo", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function generateShareInfoWithExpired(param, callback) {
  return post("/share/info/generateShareInfoWithExpired", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}
export function selectStepContent(shareId,stepId, callback) {
  return get("/share/" + shareId + "/scenario/report/selectReportContent/"+stepId,  response => {
    if (callback) {
      callback(response.data);
    }
  });
}


export function getShareInfo(id, callback) {
  return id ? baseGet('/share/info/get/' + id, callback) : {};
}

