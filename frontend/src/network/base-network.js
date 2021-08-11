import {get} from "@/common/js/ajax";

export function baseGet(url, callback) {
  return get(url, (response) => {
    if (callback) {
      callback(response.data);
    }
  });
}
