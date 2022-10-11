import {getUUID} from "../utils";
import {fileUpload, get} from "../plugins/request"


export function uploadMarkDownImg(file) {
  let param = {
    id: getUUID().substring(0, 8)
  };
  file.prefix = param.id;
  param.fileName = file.name.substring(file.name.lastIndexOf('.'));
  return fileUpload('/resource/md/upload', file, param);
}

export function deleteMarkDownImg(file) {
  return deleteMarkDownImg(file[1].name);
}

export function deleteMarkDownImgByName(name) {
  return get('/resource/md/delete/' + name);
}
