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

export enum RequestComposition {
  HEADER = 'HEADER',
  BODY = 'BODY',
  QUERY = 'QUERY',
  REST = 'REST',
  PREFIX = 'PREFIX',
  POST_CONDITION = 'POST_CONDITION',
  ASSERTION = 'ASSERTION',
  AUTH = 'AUTH',
  SETTING = 'SETTING',
}
