import {post} from "@/common/js/ajax";
import {baseGet} from "@/network/base-network";


export function generateApiDocumentShareInfo(param, callback) {
  return post("/share/info/generateApiDocumentShareInfo", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function generateShareInfo(param, callback) {
  return post("/share/info/generateShareInfo", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function getShareInfo(id, callback) {
  return id ? baseGet('/share/info/get/' + id, callback) : {};
}

