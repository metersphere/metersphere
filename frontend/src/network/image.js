import {getUUID} from "@/common/js/utils";
import {fileUpload} from "@/common/js/ajax";
import {baseGet} from "@/network/base-network";

export function uploadMarkDownImg(file, callback) {
  let param = {
    id: getUUID().substring(0, 8)
  };
  file.prefix = param.id;
  param.fileName = file.name.substring(file.name.lastIndexOf('.'));
  return fileUpload('/resource/md/upload', file, null, param, (response) => {
    if (callback) {
      callback(response.data, param);
    }
  });
}

export function deleteMarkDownImg(file, callback) {
  if (file) {
    return baseGet('/resource/md/delete/' + file[1].name, callback);
  }
  return {};
}
