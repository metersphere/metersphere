export const ELEMENTS = new Map([
  ['menu_post_processors', ['HtmlExtractor', 'JMESPathExtractor', 'JSONPostProcessor', 'RegexExtractor', 'BoundaryExtractor', 'JSR223PostProcessor', 'Separator', 'JDBCPostProcessor', 'XPath2Extractor', 'XPathExtractor', 'ResultAction', 'DebugPostProcessor', 'BeanShellPostProcessor']],
  ['menu_assertions', ['Assertion', 'JSONPathAssertion', 'SizeAssertion', 'JSR223Assertion', 'XPath2Assertion', 'Separator', 'HTMLAssertion', 'JMESPathAssertion', 'MD5HexAssertion', 'SMIMEAssertion', 'XMLSchemaAssertion', 'XMLAssertion', 'XPathAssertion', 'DurationAssertion', 'CompareAssertion', 'BeanShellAssertion']],
  ['menu_listener', ['ViewResultsFullVisualizer', 'SummaryReport', 'StatVisualizer', 'BackendListener', 'Separator', 'JSR223Listener', 'ResultSaver', 'RespTimeGraphVisualizer', 'GraphVisualizer', 'AssertionVisualizer', 'ComparisonVisualizer', 'StatGraphVisualizer', 'Summariser', 'TableVisualizer', 'SimpleDataWriter', 'MailerVisualizer', 'BeanShellListener']],
  ['menu_pre_processors', ['JSR223PreProcessor', 'UserParameters', 'Separator', 'AnchorModifier', 'URLRewritingModifier', 'JDBCPreProcessor', 'SampleTimeout', 'RegExUserParameters', 'BeanShellPreProcessor']],
  ['menu_logic_controller', ['IfControllerPanel', 'TransactionController', 'LoopControlPanel', 'WhileController', 'Separator', 'ForeachControlPanel', 'IncludeController', 'RunTime', 'CriticalSectionController', 'InterleaveControl', 'OnceOnlyController', 'RecordController', 'LogicController', 'RandomControl', 'RandomOrderController', 'ThroughputController', 'SwitchController', 'ModuleController']],
  ['menu_fragments', ['TestFragmentController']],
  ['menu_non_test_elements', ['ProxyControl', 'HttpMirrorControl', 'GenerateTree', 'PropertyControl']],
  ['menu_generative_controller', ['HttpTestSample', 'TestAction', 'DebugSampler', 'JSR223Sampler', 'Separator', 'AjpSampler', 'AccessLogSampler', 'BeanShellSampler', 'BoltSampler', 'FtpTestSampler', 'GraphQLHTTPSampler', 'JDBCSampler', 'JMSPublisher', 'JMSSampler', 'JMSSubscriber', 'JUnitTestSampler', 'JavaTestSampler', 'LdapExtTestSampler', 'LdapTestSampler', 'SystemSampler', 'SmtpSampler', 'TCPSampler', 'MailReaderSampler']],
  ['menu_threads', ['SetupThreadGroup', 'PostThreadGroup', 'ThreadGroup']],
  ['menu_timer', ['ConstantTimer', 'UniformRandomTimer', 'PreciseThroughputTimer', 'ConstantThroughputTimer', 'Separator', 'JSR223Timer', 'SyncTimer', 'PoissonRandomTimer', 'GaussianRandomTimer', 'BeanShellTimer']],
  ['menu_config_element', ['CSVDataSet', 'HeaderPanel', 'CookiePanel', 'CacheManager', 'HttpDefaults', 'Separator', 'BoltConnectionElement', 'DNSCachePanel', 'FtpConfig', 'AuthPanel', 'DataSourceElement', 'JavaConfig', 'LdapExtConfig', 'LdapConfig', 'TCPConfig', 'KeystoreConfig', 'ArgumentsPanel', 'LoginConfig', 'SimpleConfig', 'CounterConfig', 'RandomVariableConfig']],
])

export function getDefaultSamplerMenu() {
  let array = [];
  array.push(ELEMENTS.get('menu_assertions'));
  array.push(ELEMENTS.get('menu_timer'));
  array.push(ELEMENTS.get('menu_pre_processors'));
  array.push(ELEMENTS.get('menu_post_processors'));
  array.push(ELEMENTS.get('menu_config_element'));
  array.push(ELEMENTS.get('menu_listener'));
  return array;
}

export function getLogicMenu() {
  return ELEMENTS.values();
}
