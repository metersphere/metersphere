// 接口请求方法
export enum RequestMethods {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH',
  OPTIONS = 'OPTIONS',
  HEAD = 'HEAD',
  CONNECT = 'CONNECT',
}
// 接口组成部分
export enum RequestComposition {
  HEADER = 'HEADER',
  BODY = 'BODY',
  QUERY = 'QUERY',
  REST = 'REST',
  PRECONDITION = 'PRECONDITION',
  POST_CONDITION = 'POST_CONDITION',
  ASSERTION = 'ASSERTION',
  AUTH = 'AUTH',
  SETTING = 'SETTING',
}
// 接口请求体格式
export enum RequestBodyFormat {
  NONE = 'none',
  FORM_DATA = 'form-data',
  X_WWW_FORM_URLENCODED = 'x-www-form-urlencoded',
  JSON = 'json',
  XML = 'xml',
  RAW = 'raw',
  BINARY = 'binary',
}
// 接口响应体格式
export enum RequestContentTypeEnum {
  JSON = 'application/json',
  TEXT = 'application/text',
  OGG = 'application/ogg',
  PDF = 'application/pdf',
  JAVASCRIPT = 'application/javascript',
  OCTET_STREAM = 'application/octet-stream',
  VND_API_JSON = 'application/vnd.api+json',
  ATOM_XML = 'application/atom+xml',
  ECMASCRIPT = 'application/ecmascript',
}
