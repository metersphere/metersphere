export const TokenKey = 'Admin-Token';
export const DEFAULT_LANGUAGE = 'default_language';

export const ROLE_ADMIN = 'admin';
export const ROLE_ORG_ADMIN = 'org_admin';
export const ROLE_TEST_MANAGER = 'test_manager';
export const ROLE_TEST_USER = 'test_user';
export const ROLE_TEST_VIEWER = 'test_viewer';

export const WORKSPACE_ID = 'workspace_id';
export const CURRENT_PROJECT = 'current_project';

export const REFRESH_SESSION_USER_URL = 'user/refresh';
export const WORKSPACE = 'workspace';
export const ORGANIZATION = 'organization';
export const DEFAULT = 'default';

export const ZH_CN = 'zh_CN';
export const ZH_TW = 'zh_TW';
export const EN_US = 'en_US';

export const SCHEDULE_TYPE = {
  API_TEST: 'API_TEST',
  PERFORMANCE_TEST: 'PERFORMANCE_TEST'
}

export const REQUEST_HEADERS = [
  {value: 'Accept'},
  {value: 'Accept-Charset'},
  {value: 'Accept-Language'},
  {value: 'Accept-Datetime'},
  {value: 'Authorization'},
  {value: 'Cache-Control'},
  {value: 'Connection'},
  {value: 'Cookie'},
  {value: 'Content-Length'},
  {value: 'Content-MD5'},
  {value: 'Content-Type'},
  {value: 'Date'},
  {value: 'Expect'},
  {value: 'From'},
  {value: 'Host'},
  {value: 'If-Match'},
  {value: 'If-Modified-Since'},
  {value: 'If-None-Match'},
  {value: 'If-Range'},
  {value: 'If-Unmodified-Since'},
  {value: 'Max-Forwards'},
  {value: 'Origin'},
  {value: 'Pragma'},
  {value: 'Proxy-Authorization'},
  {value: 'Range'},
  {value: 'Referer'},
  {value: 'TE'},
  {value: 'User-Agent'},
  {value: 'Upgrade'},
  {value: 'Via'},
  {value: 'Warning'}
]

export const MOCKJS_FUNC = [
  {name: '@boolean'},
  {name: '@natural'},
  {name: '@integer'},
  {name: '@float'},
  {name: '@character'},
  {name: '@string'},
  {name: '@range'},
  {name: '@date'},
  {name: '@time'},
  {name: '@datetime'},
  {name: '@now'},
  {name: '@img'},
  {name: '@dataImage'},
  {name: '@color'},
  {name: '@hex'},
  {name: '@rgb'},
  {name: '@rgba'},
  {name: '@hsl'},
  {name: '@paragraph'},
  {name: '@sentence'},
  {name: '@word'},
  {name: '@title'},
  {name: '@cparagraph'},
  {name: '@csentence'},
  {name: '@cword'},
  {name: '@ctitle'},
  {name: '@first'},
  {name: '@last'},
  {name: '@name'},
  {name: '@cfirst'},
  {name: '@clast'},
  {name: '@cname'},
  {name: '@url'},
  {name: '@domain'},
  {name: '@protocol'},
  {name: '@tld'},
  {name: '@email'},
  {name: '@ip'},
  {name: '@region'},
  {name: '@province'},
  {name: '@city'},
  {name: '@county'},
  {name: '@zip'},
  {name: '@capitalize'},
  {name: '@upper'},
  {name: '@lower'},
  {name: '@pick'},
  {name: '@shuffle'},
  {name: '@guid'},
  {name: '@id'},
  {name: '@increment'}
]

export const JMETER_FUNC = [
  {name: "${__threadNum}"},
  {name: "${__samplerName}"},
  {name: "${__machineIP}"},
  {name: "${__machineName}"},
  {name: "${__time}"},
  {name: "${__log}"},
  {name: "${__logn}"},
  {name: "${__StringFromFile}"},
  {name: "${__FileToString}"},
  {name: "${__CSVRead}"},
  {name: "${__XPath}"},
  {name: "${__counter}"},
  {name: "${__intSum}"},
  {name: "${__longSum}"},
  {name: "${__Random}"},
  {name: "${__RandomString}"},
  {name: "${__UUID}"},
  {name: "${__BeanShell}"},
  {name: "${__javaScript}"},
  {name: "${__jexl}"},
  {name: "${__jexl2}"},
  {name: "${__property}"},
  {name: "${__P}"},
  {name: "${__setProperty}"},
  {name: "${__split}"},
  {name: "${__V}"},
  {name: "${__eval}"},
  {name: "${__evalVar}"},
  {name: "${__regexFunction}"},
  {name: "${__escapeOroRegexpChars}"},
  {name: "${__char}"},
  {name: "${__unescape}"},
  {name: "${__unescapeHtml}"},
  {name: "${__escapeHtml}"},
  {name: "${__TestPlanName}"},
]
