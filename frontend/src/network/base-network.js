import {get, post} from "@/common/js/ajax";

export function baseGet(url, callback) {
  return get(url, callback ? (response) => {
    if (callback) {
      callback(response.data);
    }
  } : callback);
}

export function basePost(url, param, callback) {
  return post(url, param, callback ? (response) => {
    if (callback) {
      callback(response.data);
    }
  } : callback);
}
