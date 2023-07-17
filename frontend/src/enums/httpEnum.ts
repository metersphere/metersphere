/**
 * 请求结果枚举
 */
export enum ResultEnum {
  SUCCESS = 100200,
  ERROR = 1,
  TIMEOUT = 401,
  TYPE = 'success',
}

/**
 * 请求方法枚举
 */
export enum RequestEnum {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}

/**
 * 请求响应体格式
 */
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload
  FORM_DATA = 'multipart/form-data;charset=UTF-8',
}
