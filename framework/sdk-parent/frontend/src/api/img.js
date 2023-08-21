import {getUUID} from "../utils";
import {fileUpload, get, post} from "../plugins/request"


export function uploadMarkDownImg(file) {
  let param = {
    id: getUUID().substring(0, 8)
  };
  file.prefix = param.id;
  param.fileName = file.name.substring(file.name.lastIndexOf('.'));
  return fileUpload('/resource/md/upload', file, param);
}

/**
 * 上传图片到临时目录
 * @param file
 * @returns {Promise | Promise<unknown>}
 */
export function uploadTempMarkDownImg(file) {
  let param = {
    id: getUUID().substring(0, 8)
  };
  file.prefix = param.id;
  param.fileName = file.name.substring(file.name.lastIndexOf('.'));
  return fileUpload('/resource/md/temp/upload', file, param);
}

export function deleteMarkDownImg(file) {
  return deleteMarkDownImg(file[1].name);
}

export function deleteMarkDownImgByName(name) {
  return get('/resource/md/delete/' + name);
}

export function saveMarkDownImg(request) {
  return post('/resource/md/save/image', request);
}
