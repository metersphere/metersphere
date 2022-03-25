import i18n from "@/i18n/i18n";

export const TEST_CASE_LIST = 'test_case_list'
export const TEST_CASE_REVIEW_LIST = 'test_case_review_list'
export const API_LIST = 'api_list'
export const API_CASE_LIST = 'api_case_list'
export const API_SCENARIO_LIST = 'api_scenario_list'
export const TEST_CASE_REVIEW_CASE_LIST = 'test_case_review_case_list'
export const TEST_PLAN_LIST = 'test_plan_list'
export const TEST_PLAN_FUNCTION_TEST_CASE = 'test_plan_function_test_case'
export const TEST_PLAN_API_CASE = 'test_plan_api_case'
export const TEST_PLAN_LOAD_CASE = 'test_plan_load_case'
export const TEST_PLAN_SCENARIO_CASE = 'test_plan_scenario_case'

export const TokenKey = 'Admin-Token';
export const LicenseKey = 'License';
export const PROJECT_VERSION_ENABLE = 'PROJECT_VERSION_ENABLE';
export const DEFAULT_LANGUAGE = 'default_language';
export const CURRENT_LANGUAGE = 'current_language';

export const ROLE_ADMIN = 'admin';
export const ROLE_ORG_ADMIN = 'org_admin';
export const ROLE_TEST_MANAGER = 'test_manager';
export const ROLE_TEST_USER = 'test_user';
export const ROLE_TEST_VIEWER = 'test_viewer';

export const ORGANIZATION_ID = 'organization_id';
export const WORKSPACE_ID = 'workspace_id';
export const CURRENT_PROJECT = 'current_project';
export const PROJECT_ID = 'project_id';
export const PROJECT_NAME = 'project_name';

export const REFRESH_SESSION_USER_URL = 'user/refresh';
export const WORKSPACE = 'workspace';
export const ORGANIZATION = 'organization';
export const DEFAULT = 'default';

export const ZH_CN = 'zh_CN';
export const ZH_TW = 'zh_TW';
export const EN_US = 'en_US';

export const TAPD = 'Tapd';
export const JIRA = 'Jira';
export const ZEN_TAO = 'Zentao';
export const LOCAL = 'Local';
export const AZURE_DEVOPS = 'AzureDevops';

export const GROUP_SYSTEM = 'SYSTEM';
export const GROUP_WORKSPACE = 'WORKSPACE';
export const GROUP_PROJECT = 'PROJECT';

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
  {name: '@boolean', des: i18n.t('api_test.request.boolean'), ex: true},
  {name: '@natural', des: i18n.t('api_test.request.natural'), ex: 72834},
  {name: '@integer', des: i18n.t('api_test.request.integer'), ex: 79750},
  {name: '@float', des: i18n.t('api_test.request.float'), ex: 24.2},
  {name: '@character', des: i18n.t('api_test.request.character'), ex: "k"},
  {name: '@string', des: i18n.t('api_test.request.string'), ex: "hello"},
  {name: '@range', des: i18n.t('api_test.request.range'), ex: "org.mozilla.javascript.NavicatArray@1739f809"},
  {name: '@date', des: i18n.t('api_test.request.date'), ex: "1973-01-08"},
  {name: '@time', des: i18n.t('api_test.request.time'), ex: "06:15:27"},
  {name: '@datetime', des: i18n.t('api_test.request.datetime'), ex: "1975-10-12 02:32:04"},
  {name: '@now', des: i18n.t('api_test.request.now'), ex: (new Date()).toLocaleTimeString().toLocaleString()},
  {name: '@img', des: i18n.t('api_test.request.img'), ex: "http://dummyimage.com/120x60"},
  {name: '@dataImage', des: i18n.t('api_test.request.dataImage'), ex: "data:image/png;base64,iVBORw0KG=="},
  {name: '@color', des: i18n.t('api_test.request.color'), ex: "#b479f2"},
  {name: '@hex', des: i18n.t('api_test.request.hex'), ex: "#f27984"},
  {name: '@rgb', des: i18n.t('api_test.request.rgb'), ex: "rgb(203, 242, 121)"},
  {name: '@rgba', des: i18n.t('api_test.request.rgba'), ex: "rgba(242, 121, 238, 0.66)"},
  {name: '@hsl', des: i18n.t('api_test.request.hsl'), ex: "hsl(164, 82, 71)"},
  {name: '@paragraph', des: i18n.t('api_test.request.paragraph'), ex: "Iwvh qxuvn uigzjw xijvntv dfidxtof"},
  {name: '@sentence', des: i18n.t('api_test.request.sentence'), ex: "Hfi fpqnqerrs sghxldx oqpghvnmy"},
  {name: '@word', des: i18n.t('api_test.request.word'), ex: "shnjlyazvi"},
  {name: '@title', des: i18n.t('api_test.request.title'), ex: "Tefsdc Vhs Ujx"},
  {name: '@cparagraph', des: i18n.t('api_test.request.cparagraph'), ex: "色青元处才不米拉律消叫别金如上。"},
  {name: '@csentence', des: i18n.t('api_test.request.csentence'), ex: "与形府部速她运改织图集料进完。"},
  {name: '@cword', des: i18n.t('api_test.request.cword'), ex: "满"},
  {name: '@ctitle', des: i18n.t('api_test.request.ctitle'), ex: "运满前省快"},
  {name: '@first', des: i18n.t('api_test.request.first'), ex: "Mary"},
  {name: '@last', des: i18n.t('api_test.request.last'), ex: "Miller"},
  {name: '@name', des: i18n.t('api_test.request.name'), ex: "Robert Lee"},
  {name: '@cfirst', des: i18n.t('api_test.request.cfirst'), ex: "龚"},
  {name: '@clast', des: i18n.t('api_test.request.clast'), ex: "刚"},
  {name: '@cname', des: i18n.t('api_test.request.cname'), ex: "江娟"},
  {name: '@url', des: i18n.t('api_test.request.url'), ex: "wais://jopnwwj.bh/lqnhn"},
  {name: '@domain', des: i18n.t('api_test.request.domain'), ex: "rsh.bt"},
  {name: '@protocol', des: i18n.t('api_test.request.protocol'), ex: "rlogin"},
  {name: '@tld', des: i18n.t('api_test.request.tld'), ex: "sa"},
  {name: '@email', des: i18n.t('api_test.request.email'), ex: "d.somdg@edntlm.cd"},
  {name: '@ip', des: i18n.t('api_test.request.ip'), ex: "22.151.93.255"},
  {name: '@region', des: i18n.t('api_test.request.region'), ex: "东北"},
  {name: '@province', des: i18n.t('api_test.request.province'), ex: "陕西省"},
  {name: '@city', des: i18n.t('api_test.request.city'), ex: "珠海市"},
  {name: '@county', des: i18n.t('api_test.request.county'), ex: "正宁县"},
  {name: '@zip', des: i18n.t('api_test.request.zip'), ex: 873247},
  {name: '@capitalize', des: i18n.t('api_test.request.capitalize'), ex: "Undefined"},
  {name: '@upper', des: i18n.t('api_test.request.upper'), ex: "UNDEFINED"},
  {name: '@lower', des: i18n.t('api_test.request.lower'), ex: "undefined"},
  {name: '@pick', des: i18n.t('api_test.request.pick'), ex: "None example"},
  {name: '@shuffle', des: i18n.t('api_test.request.shuffle'), ex: "org.mozilla.javascript.NavicatArray@2264545d"},
  {name: '@guid', des: i18n.t('api_test.request.guid'), ex: "4f9CeC2c-8d59-40f6-ec4F-2Abbc5C94Ddf"},
  {name: '@id', des: i18n.t('api_test.request.id'), ex: "450000197511051762"},
  {name: '@increment', des: i18n.t('api_test.request.increment'), ex: 1}
]

export const JMETER_FUNC = [
  {type: "Information", name: "${__threadNum}", description: "get thread number"},
  {type: "Information", name: "${__samplerName}", description: "get the sampler name (label)"},
  {type: "Information", name: "${__machineIP}", description: "get the local machine IP address"},
  {type: "Information", name: "${__machineName}", description: "get the local machine name"},
  {type: "Information", name: "${__time}", description: "return current time in various formats"},
  {type: "Information", name: "${__log}", description: "log (or display) a message (and return the value)"},
  {type: "Information", name: "${__logn}", description: "log (or display) a message (empty return value)"},
  {type: "Input", name: "${__StringFromFile}", description: "read a line from a file"},
  {type: "Input", name: "${__FileToString}", description: "read an entire file"},
  {type: "Input", name: "${__CSVRead}", description: "read from CSV definitioned file"},
  {type: "Input", name: "${__XPath}", description: "Use an XPath expression to read from a file"},
  {type: "Calculation", name: "${__counter}", description: "generate an incrementing number"},
  {type: "Calculation", name: "${__intSum}", description: "add int numbers"},
  {type: "Calculation", name: "${__longSum}", description: "add long numbers"},
  {type: "Calculation", name: "${__Random}", description: "generate a random number"},
  {type: "Calculation", name: "${__RandomString}", description: "generate a random string"},
  {type: "Calculation", name: "${__UUID}", description: "generate a random type 4 UUID"},
  {type: "Scripting", name: "${__BeanShell}", description: "run a BeanShell script"},
  {type: "Scripting", name: "${__javaScript}", description: "process JavaScript (Mozilla Rhino)"},
  {type: "Scripting", name: "${__jexl}", description: "evaluate a Commons Jexl expression"},
  {type: "Scripting", name: "${__jexl2}", description: "evaluate a Commons Jexl expression"},
  {type: "Properties", name: "${__property}", description: "read a property"},
  {type: "Properties", name: "${__P}", description: "read a property (shorthand method)"},
  {type: "Properties", name: "${__setProperty}", description: "set a JMeter property"},
  {type: "Variables", name: "${__split}", description: "Split a string into variables"},
  {type: "Variables", name: "${__V}", description: "evaluate a variable name"},
  {type: "Variables", name: "${__eval}", description: "evaluate a variable expression"},
  {type: "Variables", name: "${__evalVar}", description: "evaluate an expression stored in a variable"},
  {type: "String", name: "${__regexFunction}", description: "parse previous response using a regular expression"},
  {type: "String", name: "${__escapeOroRegexpChars}", description: "quote meta chars used by ORO regular expression"},
  {type: "String", name: "${__char}", description: "generate Unicode char values from a list of numbers"},
  {type: "String", name: "${__unescape}", description: "Process strings containing Java escapes (e.g. & )"},
  {type: "String", name: "${__unescapeHtml}", description: "Decode HTML-encoded strings"},
  {type: "String", name: "${__escapeHtml}", description: "Encode strings using HTML encoding"},
  {type: "String", name: "${__TestPlanName}", description: "Return name of current test plan"},
]

export const ORIGIN_COLOR = '#2c2a48';
export const ORIGIN_COLOR_SHALLOW = '#595591';
export const COUNT_NUMBER = '#6C317C';
export const COUNT_NUMBER_SHALLOW = '#CDB9D2';
export const PRIMARY_COLOR = '#783887';

export const CONFIG_TYPE = {
  NOT: "NOT",
  NORMAL: "NORMAL",
  ABNORMAL: "ABNORMAL"
}

export const WORKSTATION={
  UPCOMING:"upcoming",
  FOCUS:"focus",
  NODE:"node"
}

export const ENV_TYPE = {
  JSON: "JSON",
  GROUP: "GROUP"
}

export const DEFAULT_XSS_ATTR = ['style', 'class'];
