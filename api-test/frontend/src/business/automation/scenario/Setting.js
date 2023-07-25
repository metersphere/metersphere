import {
  API_SCENARIO_RESULT,
  CREATE_TIME,
  CREATOR,
  FOLLOW_PEOPLE,
  ID,
  NAME,
  OPERATORS,
  PRIORITY,
  STEP_COUNT,
  TAGS,
  UPDATE_TIME,
} from 'metersphere-frontend/src/components/search/search-components';

export function STEP() {
  let map = new Map([
    ['ALL', init()],
    ['scenario', init()],
    ['HTTPSamplerProxy', getDefaultSamplerMenu()],
    ['DubboSampler', getDefaultSamplerMenu()],
    ['JDBCSampler', getDefaultSamplerMenu()],
    ['TCPSampler', getDefaultSamplerMenu()],
    ['TO_IMPORT', getDefaultSamplerMenu()],
    ['AbstractSampler', getDefaultSamplerMenu()],
    ['IfController', getAll()],
    ['TransactionController', getAll()],
    ['LoopController', getAll()],
    ['ConstantTimer', []],
    ['JSR223Processor', getDefaultSamplerMenu()],
    ['JSR223PreProcessor', []],
    ['JSR223PostProcessor', []],
    ['JDBCPreProcessor', []],
    ['JDBCPostProcessor', []],
    ['Assertions', []],
    ['Extract', []],
    ['JmeterElement', []],
    ['CustomizeReq', getDefaultSamplerMenu()],
    ['MaxSamplerProxy', getDefaultSamplerMenu()],
    ['GenericController', getAll()],
    [
      'SpecialSteps',
      [
        'HTTPSamplerProxy',
        'Assertions',
        'DubboSampler',
        'JDBCSampler',
        'TCPSampler',
        'Sampler',
        'AbstractSampler',
        'JSR223Processor',
        'API',
        'MsUiCommand',
      ],
    ],
    [
      'AllSamplerProxy',
      [
        'HTTPSamplerProxy',
        'DubboSampler',
        'JDBCSampler',
        'TCPSampler',
        'Sampler',
        'AbstractSampler',
        'JSR223Processor',
        'API',
        'MsUiCommand',
      ],
    ],
    ['DEFINITION', ['HTTPSamplerProxy', 'DubboSampler', 'JDBCSampler', 'TCPSampler']],
    [
      'ALlSamplerStep',
      [
        'JSR223PreProcessor',
        'JSR223PostProcessor',
        'JDBCPreProcessor',
        'JDBCPostProcessor',
        'Assertions',
        'Extract',
        'ConstantTimer',
      ],
    ],
    [
      'AllCanExecType',
      ['HTTPSamplerProxy', 'DubboSampler', 'JDBCSampler', 'TCPSampler', 'JSR223Processor', 'AbstractSampler'],
    ],
  ]);
  return map;
}

export const ELEMENT_TYPE = {
  scenario: 'scenario',
  HTTPSamplerProxy: 'HTTPSamplerProxy',
  TO_IMPORT: 'TO_IMPORT',
  IfController: 'IfController',
  TransactionController: 'TransactionController',
  ConstantTimer: 'ConstantTimer',
  JSR223Processor: 'JSR223Processor',
  JSR223PreProcessor: 'JSR223PreProcessor',
  JSR223PostProcessor: 'JSR223PostProcessor',
  JDBCPostProcessor: 'JDBCPostProcessor',
  JDBCPreProcessor: 'JDBCPreProcessor',
  Assertions: 'Assertions',
  Extract: 'Extract',
  CustomizeReq: 'CustomizeReq',
  LoopController: 'LoopController',
  Plugin: 'Plugin',
};

export const TYPE_TO_C = new Map([
  ['scenario', 'io.metersphere.api.dto.definition.request.MsScenario'],
  ['UiScenario', 'io.metersphere.xpack.ui.hashtree.MsUiScenario'],
  ['HTTPSamplerProxy', 'io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy'],
  ['DubboSampler', 'io.metersphere.api.dto.definition.request.sampler.MsDubboSampler'],
  ['JDBCSampler', 'io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler'],
  ['TCPSampler', 'io.metersphere.api.dto.definition.request.sampler.MsTCPSampler'],
  ['IfController', 'io.metersphere.api.dto.definition.request.controller.MsIfController'],
  ['TransactionController', 'io.metersphere.api.dto.definition.request.controller.MsTransactionController'],
  ['LoopController', 'io.metersphere.api.dto.definition.request.controller.MsLoopController'],
  ['ConstantTimer', 'io.metersphere.api.dto.definition.request.timer.MsConstantTimer'],
  ['JSR223Processor', 'io.metersphere.api.dto.definition.request.processors.MsJSR223Processor'],
  ['JSR223PreProcessor', 'io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor'],
  ['JSR223PostProcessor', 'io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor'],
  ['JDBCPreProcessor', 'io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor'],
  ['JDBCPostProcessor', 'io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor'],
  ['Assertions', 'io.metersphere.api.dto.definition.request.assertions.MsAssertions'],
  ['Extract', 'io.metersphere.api.dto.definition.request.extract.MsExtract'],
  ['JmeterElement', 'io.metersphere.api.dto.definition.request.unknown.MsJmeterElement'],
  ['TestPlan', 'io.metersphere.api.dto.definition.request.MsTestPlan'],
  ['ThreadGroup', 'io.metersphere.api.dto.definition.request.MsThreadGroup'],
  ['DNSCacheManager', 'io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager'],
  ['DebugSampler', 'io.metersphere.api.dto.definition.request.sampler.MsDebugSampler'],
  ['AuthManager', 'io.metersphere.api.dto.definition.request.auth.MsAuthManager'],
]);

export const PLUGIN_ELEMENTS = new Map([
  [
    'menu_post_processors',
    [
      'HtmlExtractor',
      'JMESPathExtractor',
      'JSONPostProcessor',
      'RegexExtractor',
      'BoundaryExtractor',
      'Separator',
      'XPath2Extractor',
      'XPathExtractor',
      'ResultAction',
      'DebugPostProcessor',
      'BeanShellPostProcessor',
    ],
  ],
  [
    'menu_assertions',
    [
      'JSONPathAssertion',
      'SizeAssertion',
      'JSR223Assertion',
      'XPath2Assertion',
      'Separator',
      'HTMLAssertion',
      'JMESPathAssertion',
      'MD5HexAssertion',
      'SMIMEAssertion',
      'XMLSchemaAssertion',
      'XMLAssertion',
      'XPathAssertion',
      'DurationAssertion',
      'CompareAssertion',
      'BeanShellAssertion',
    ],
  ],
  [
    'menu_listener',
    [
      'AbstractVisualizer',
      'AbstractListener',
      'ViewResultsFullVisualizer',
      'SummaryReport',
      'StatVisualizer',
      'BackendListener',
      'Separator',
      'JSR223Listener',
      'ResultSaver',
      'RespTimeGraphVisualizer',
      'GraphVisualizer',
      'AssertionVisualizer',
      'ComparisonVisualizer',
      'StatGraphVisualizer',
      'Summariser',
      'TableVisualizer',
      'SimpleDataWriter',
      'MailerVisualizer',
      'BeanShellListener',
    ],
  ],
  [
    'menu_pre_processors',
    [
      'AbstractPostProcessor',
      'UserParameters',
      'Separator',
      'AnchorModifier',
      'URLRewritingModifier',
      'SampleTimeout',
      'RegExUserParameters',
      'BeanShellPreProcessor',
    ],
  ],
  [
    'menu_logic_controller',
    [
      'GenericController',
      'scenario',
      'IfController',
      'LoopController',
      'IfControllerPanel',
      'TransactionController',
      'LoopControlPanel',
      'WhileController',
      'Separator',
      'ForeachControlPanel',
      'IncludeController',
      'RunTime',
      'CriticalSectionController',
      'InterleaveControl',
      'OnceOnlyController',
      'RecordController',
      'LogicController',
      'RandomControl',
      'RandomOrderController',
      'ThroughputController',
      'SwitchController',
      'ModuleController',
    ],
  ],
  ['menu_fragments', ['TestFragmentController']],
  ['menu_non_test_elements', ['ProxyControl', 'HttpMirrorControl', 'GenerateTree', 'PropertyControl']],
  [
    'menu_generative_controller',
    [
      'HTTPSamplerProxy',
      'JSR223Processor',
      'DubboSampler',
      'JDBCSampler',
      'TCPSampler',
      'Sampler',
      'AbstractSampler',
      'CustomizeReq',
      'HttpTestSample',
      'TestAction',
      'DebugSampler',
      'JSR223Sampler',
      'Separator',
      'AjpSampler',
      'AccessLogSampler',
      'BeanShellSampler',
      'BoltSampler',
      'FtpTestSampler',
      'GraphQLHTTPSampler',
      'JDBCSampler',
      'JMSPublisher',
      'JMSSampler',
      'JMSSubscriber',
      'JUnitTestSampler',
      'JavaTestSampler',
      'LdapExtTestSampler',
      'LdapTestSampler',
      'SystemSampler',
      'SmtpSampler',
      'TCPSampler',
      'MailReaderSampler',
    ],
  ],
  ['menu_threads', ['SetupThreadGroup', 'PostThreadGroup', 'ThreadGroup']],
  [
    'menu_timer',
    [
      'ConstantTimer',
      'UniformRandomTimer',
      'PreciseThroughputTimer',
      'ConstantThroughputTimer',
      'Separator',
      'JSR223Timer',
      'SyncTimer',
      'PoissonRandomTimer',
      'GaussianRandomTimer',
      'BeanShellTimer',
    ],
  ],
  [
    'menu_config_element',
    [
      'CSVDataSet',
      'HeaderPanel',
      'CookiePanel',
      'CacheManager',
      'HttpDefaults',
      'Separator',
      'BoltConnectionElement',
      'DNSCachePanel',
      'FtpConfig',
      'AuthPanel',
      'DataSourceElement',
      'JavaConfig',
      'LdapExtConfig',
      'LdapConfig',
      'TCPConfig',
      'KeystoreConfig',
      'ArgumentsPanel',
      'LoginConfig',
      'SimpleConfig',
      'CounterConfig',
      'RandomVariableConfig',
    ],
  ],
]);

export function getDefaultSamplerMenu() {
  let array = [];
  array = array.concat(PLUGIN_ELEMENTS.get('menu_assertions'));
  array = array.concat(PLUGIN_ELEMENTS.get('menu_pre_processors'));
  array = array.concat(PLUGIN_ELEMENTS.get('menu_post_processors'));
  array = array.concat(PLUGIN_ELEMENTS.get('menu_config_element'));
  array = array.concat(PLUGIN_ELEMENTS.get('menu_listener'));
  return array;
}

export function init() {
  let allArray = [];
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_generative_controller'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_logic_controller'));
  allArray = allArray.concat(['scenario', 'ConstantTimer', 'JSR223Processor', 'Assertions']);
  return allArray;
}

export function getAll() {
  let allArray = [];
  allArray = allArray.concat(getDefaultSamplerMenu());
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_logic_controller'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_non_test_elements'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_generative_controller'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_threads'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_timer'));
  return allArray;
}

function _getModuleTree(options) {
  return {
    key: 'moduleIds',
    name: 'MsTableSearchNodeTree',
    label: 'test_track.case.module',
    operator: {
      value: OPERATORS.IN.value,
      options: [OPERATORS.IN, OPERATORS.NOT_IN],
    },
    options: options,
    init: undefined, // 高级搜索框非首次打开时会执行该函数，在组件首次created时给其赋值
  };
}

export const SCENARIO_MODULE_TREE = _getModuleTree({
  url: '/api/automation/module/list',
  type: 'GET',
  params: {},
});

export const SCENARIO_MODULE_TRASH_TREE = _getModuleTree({
  url: '/api/automation/module/trash/list',
  type: 'GET',
  params: {},
});

export const API_STATUS_TRASH = {
  key: 'status',
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN],
  },
  options: [{ value: 'Trash', label: 'test_track.plan.plan_status_trash' }],
  props: {
    // 尾部控件的props，一般为element ui控件的props
    multiple: true,
  },
};

export const API_SCENARIO_CONFIGS_TRASH = [
  ID,
  NAME,
  PRIORITY,
  TAGS,
  API_SCENARIO_RESULT,
  UPDATE_TIME,
  CREATE_TIME,
  CREATOR,
  FOLLOW_PEOPLE,
  STEP_COUNT,
  SCENARIO_MODULE_TRASH_TREE,
  API_STATUS_TRASH,
];
