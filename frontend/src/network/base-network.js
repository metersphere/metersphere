import {get, post} from "@/common/js/ajax";

export function baseGet(url, callback) {
  return get(url, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function basePost(url, param, callback) {
  return post(url, param, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}
