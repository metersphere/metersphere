export function STEP() {
  let map = new Map([
    ['ALL', init()],
    ['scenario', init()],
    ['HTTPSamplerProxy', getDefaultSamplerMenu()],
    ['DubboSampler', getDefaultSamplerMenu()],
    ['JDBCSampler', getDefaultSamplerMenu()],
    ['TCPSampler', getDefaultSamplerMenu()],
    ['OT_IMPORT', getDefaultSamplerMenu()],
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
    ['AllSamplerProxy', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "Sampler", "AbstractSampler"]],
    ['DEFINITION', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler"]],
    ['AllCanExecType', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor", "AbstractSampler"]]]);
  return map
}

export const ELEMENT_TYPE = {
  scenario: "scenario",
  HTTPSamplerProxy: "HTTPSamplerProxy",
  OT_IMPORT: "OT_IMPORT",
  IfController: "IfController",
  TransactionController: "TransactionController",
  ConstantTimer: "ConstantTimer",
  JSR223Processor: "JSR223Processor",
  JSR223PreProcessor: "JSR223PreProcessor",
  JSR223PostProcessor: "JSR223PostProcessor",
  JDBCPostProcessor: "JDBCPostProcessor",
  JDBCPreProcessor: "JDBCPreProcessor",
  Assertions: "Assertions",
  Extract: "Extract",
  CustomizeReq: "CustomizeReq",
  LoopController: "LoopController",
  Plugin: "Plugin"
}

export const TYPE_TO_C = new Map([
  ['scenario', "io.metersphere.api.dto.definition.request.MsScenario"],
  ['HTTPSamplerProxy', "io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy"],
  ['DubboSampler', "io.metersphere.api.dto.definition.request.sampler.MsDubboSampler"],
  ['JDBCSampler', "io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler"],
  ['TCPSampler', "io.metersphere.api.dto.definition.request.sampler.MsTCPSampler"],
  ['IfController', "io.metersphere.api.dto.definition.request.controller.MsIfController"],
  ['TransactionController', "io.metersphere.api.dto.definition.request.controller.MsTransactionController"],
  ['LoopController', "io.metersphere.api.dto.definition.request.controller.MsLoopController"],
  ['ConstantTimer', "io.metersphere.api.dto.definition.request.timer.MsConstantTimer"],
  ['JSR223Processor', "io.metersphere.api.dto.definition.request.processors.MsJSR223Processor"],
  ['JSR223PreProcessor', "io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor"],
  ['JSR223PostProcessor', "io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor"],
  ['JDBCPreProcessor', "io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor"],
  ['JDBCPostProcessor', "io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor"],
  ['Assertions', "io.metersphere.api.dto.definition.request.assertions.MsAssertions"],
  ['Extract', "io.metersphere.api.dto.definition.request.extract.MsExtract"],
  ['JmeterElement', "io.metersphere.api.dto.definition.request.unknown.MsJmeterElement"],
  ['TestPlan', "io.metersphere.api.dto.definition.request.MsTestPlan"],
  ['ThreadGroup', "io.metersphere.api.dto.definition.request.MsThreadGroup"],
  ['DNSCacheManager', "io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager"],
  ['DebugSampler', "io.metersphere.api.dto.definition.request.sampler.MsDebugSampler"],
  ['AuthManager', "io.metersphere.api.dto.definition.request.auth.MsAuthManager"]
])

export const PLUGIN_ELEMENTS = new Map([
  ['menu_post_processors', ['HtmlExtractor', 'JMESPathExtractor', 'JSONPostProcessor', 'RegexExtractor', 'BoundaryExtractor', 'JSR223PostProcessor', 'Separator', 'JDBCPostProcessor', 'XPath2Extractor', 'XPathExtractor', 'ResultAction', 'DebugPostProcessor', 'BeanShellPostProcessor']],
  ['menu_assertions', ["Assertions", "Extract", 'Assertion', 'JSONPathAssertion', 'SizeAssertion', 'JSR223Assertion', 'XPath2Assertion', 'Separator', 'HTMLAssertion', 'JMESPathAssertion', 'MD5HexAssertion', 'SMIMEAssertion', 'XMLSchemaAssertion', 'XMLAssertion', 'XPathAssertion', 'DurationAssertion', 'CompareAssertion', 'BeanShellAssertion']],
  ['menu_listener', ['AbstractVisualizer', 'AbstractListener', 'ViewResultsFullVisualizer', 'SummaryReport', 'StatVisualizer', 'BackendListener', 'Separator', 'JSR223Listener', 'ResultSaver', 'RespTimeGraphVisualizer', 'GraphVisualizer', 'AssertionVisualizer', 'ComparisonVisualizer', 'StatGraphVisualizer', 'Summariser', 'TableVisualizer', 'SimpleDataWriter', 'MailerVisualizer', 'BeanShellListener']],
  ['menu_pre_processors', ['AbstractPostProcessor', 'JSR223PreProcessor', 'UserParameters', 'Separator', 'AnchorModifier', 'URLRewritingModifier', 'JDBCPreProcessor', 'SampleTimeout', 'RegExUserParameters', 'BeanShellPreProcessor']],
  ['menu_logic_controller', ['GenericController', "IfController", "LoopController", 'IfControllerPanel', 'TransactionController', 'LoopControlPanel', 'WhileController', 'Separator', 'ForeachControlPanel', 'IncludeController', 'RunTime', 'CriticalSectionController', 'InterleaveControl', 'OnceOnlyController', 'RecordController', 'LogicController', 'RandomControl', 'RandomOrderController', 'ThroughputController', 'SwitchController', 'ModuleController']],
  ['menu_fragments', ['TestFragmentController']],
  ['menu_non_test_elements', ['ProxyControl', 'HttpMirrorControl', 'GenerateTree', 'PropertyControl']],
  ['menu_generative_controller', ["HTTPSamplerProxy", "JSR223Processor", "DubboSampler", "JDBCSampler", "TCPSampler", "Sampler", 'AbstractSampler', 'CustomizeReq', 'HttpTestSample', 'TestAction', 'DebugSampler', 'JSR223Sampler', 'Separator', 'AjpSampler', 'AccessLogSampler', 'BeanShellSampler', 'BoltSampler', 'FtpTestSampler', 'GraphQLHTTPSampler', 'JDBCSampler', 'JMSPublisher', 'JMSSampler', 'JMSSubscriber', 'JUnitTestSampler', 'JavaTestSampler', 'LdapExtTestSampler', 'LdapTestSampler', 'SystemSampler', 'SmtpSampler', 'TCPSampler', 'MailReaderSampler']],
  ['menu_threads', ['SetupThreadGroup', 'PostThreadGroup', 'ThreadGroup']],
  ['menu_timer', ['ConstantTimer', 'UniformRandomTimer', 'PreciseThroughputTimer', 'ConstantThroughputTimer', 'Separator', 'JSR223Timer', 'SyncTimer', 'PoissonRandomTimer', 'GaussianRandomTimer', 'BeanShellTimer']],
  ['menu_config_element', ['CSVDataSet', 'HeaderPanel', 'CookiePanel', 'CacheManager', 'HttpDefaults', 'Separator', 'BoltConnectionElement', 'DNSCachePanel', 'FtpConfig', 'AuthPanel', 'DataSourceElement', 'JavaConfig', 'LdapExtConfig', 'LdapConfig', 'TCPConfig', 'KeystoreConfig', 'ArgumentsPanel', 'LoginConfig', 'SimpleConfig', 'CounterConfig', 'RandomVariableConfig']],
])

export function getDefaultSamplerMenu() {
  let array = [];
  array = array.concat(PLUGIN_ELEMENTS.get('menu_assertions'));
  array = array.concat(PLUGIN_ELEMENTS.get('menu_timer'));
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
  allArray = allArray.concat(["scenario", "ConstantTimer", "JSR223Processor"]);
  return allArray;
}

export function getAll() {
  let allArray = [];
  allArray = allArray.concat(getDefaultSamplerMenu());
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_logic_controller'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_non_test_elements'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_generative_controller'));
  allArray = allArray.concat(PLUGIN_ELEMENTS.get('menu_threads'));
  return allArray;
}
