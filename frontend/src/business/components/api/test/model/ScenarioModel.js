import {
  Arguments, BeanShellPostProcessor, BeanShellPreProcessor,
  CookieManager,
  DNSCacheManager,
  DubboSample,
  DurationAssertion,
  Element,
  HashTree,
  HeaderManager,
  HTTPSamplerArguments, HTTPsamplerFiles,
  HTTPSamplerProxy,
  JSONPathAssertion,
  JSONPostProcessor, JSR223PostProcessor, JSR223PreProcessor,
  RegexExtractor,
  ResponseCodeAssertion,
  ResponseDataAssertion,
  ResponseHeadersAssertion,
  TestElement,
  TestPlan,
  ThreadGroup,
  XPath2Extractor,
} from "./JMX";
import Mock from "mockjs";
import {funcFilters} from "@/common/js/func-filter";

export const uuid = function () {
  let d = new Date().getTime()
  let d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    let r = Math.random() * 16;
    if (d > 0) {
      r = (d + r) % 16 | 0;
      d = Math.floor(d / 16);
    } else {
      r = (d2 + r) % 16 | 0;
      d2 = Math.floor(d2 / 16);
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}

export const BODY_FILE_DIR = "/opt/metersphere/data/body"; //存放body文件上传目录

export const calculate = function (itemValue) {
  if (!itemValue) {
    return;
  }
  try {
    if (itemValue.trim().startsWith("${")) {
      // jmeter 内置函数不做处理
      return itemValue;
    }
    let funcs = itemValue.split("|");
    let value = Mock.mock(funcs[0].trim());
    if (funcs.length === 1) {
      return value;
    }
    for (let i = 1; i < funcs.length; i++) {
      let func = funcs[i].trim();
      let args = func.split(":");
      let strings = [];
      if (args[1]) {
        strings = args[1].split(",");
      }
      value = funcFilters[args[0].trim()](value, ...strings);
    }
    return value;
  } catch (e) {
    return itemValue;
  }
}

export const BODY_TYPE = {
  KV: "KeyValue",
  FORM_DATA: "Form Data",
  RAW: "Raw"
}

export const BODY_FORMAT = {
  TEXT: "text",
  JSON: "json",
  XML: "xml",
  HTML: "html",
}

export const ASSERTION_TYPE = {
  TEXT: "Text",
  REGEX: "Regex",
  JSON_PATH: "JSON",
  DURATION: "Duration"
}

export const ASSERTION_REGEX_SUBJECT = {
  RESPONSE_CODE: "Response Code",
  RESPONSE_HEADERS: "Response Headers",
  RESPONSE_DATA: "Response Data"
}

export const EXTRACT_TYPE = {
  REGEX: "Regex",
  JSON_PATH: "JSONPath",
  XPATH: "XPath"
}

export class BaseConfig {

  set(options) {
    options = this.initOptions(options)
    for (let name in options) {
      if (options.hasOwnProperty(name)) {
        if (!(this[name] instanceof Array)) {
          this[name] = options[name];
        }
      }
    }
  }

  sets(types, options) {
    options = this.initOptions(options)
    if (types) {
      for (let name in types) {
        if (types.hasOwnProperty(name) && options.hasOwnProperty(name)) {
          options[name].forEach(o => {
            this[name].push(new types[name](o));
          })
        }
      }
    }
  }

  initOptions(options) {
    return options || {};
  }

  isValid() {
    return true;
  }
}

export class Test extends BaseConfig {
  constructor(options) {
    super();
    this.type = "MS API CONFIG";
    this.version = '1.3.0';
    this.id = uuid();
    this.name = undefined;
    this.projectId = undefined;
    this.scenarioDefinition = [];
    this.schedule = {};

    this.set(options);
    this.sets({scenarioDefinition: Scenario}, options);
  }

  export() {
    let obj = {
      type: this.type,
      version: this.version,
      scenarios: this.scenarioDefinition
    };

    return JSON.stringify(obj);
  }

  initOptions(options) {
    options = options || {};
    options.scenarioDefinition = options.scenarioDefinition || [new Scenario()];
    return options;
  }

  isValid() {
    for (let i = 0; i < this.scenarioDefinition.length; i++) {
      let validator = this.scenarioDefinition[i].isValid();
      if (!validator.isValid) {
        return validator;
      }
    }
    if (!this.projectId) {
      return {
        isValid: false,
        info: 'api_test.select_project'
      }
    } else if (!this.name) {
      return {
        isValid: false,
        info: 'api_test.input_name'
      }
    }
    return {isValid: true};
  }

  toJMX() {
    return {
      name: this.name + '.jmx',
      xml: new JMXGenerator(this).toXML()
    };
  }
}

export class Scenario extends BaseConfig {
  constructor(options = {}) {
    super();
    this.id = undefined;
    this.name = undefined;
    this.url = undefined;
    this.variables = [];
    this.headers = [];
    this.requests = [];
    this.environmentId = undefined;
    this.dubboConfig = undefined;
    this.environment = undefined;
    this.enableCookieShare = false;
    this.enable = true;

    this.set(options);
    this.sets({variables: KeyValue, headers: KeyValue, requests: RequestFactory}, options);
  }

  initOptions(options = {}) {
    options.id = options.id || uuid();
    options.requests = options.requests || [new RequestFactory()];
    options.dubboConfig = new DubboConfig(options.dubboConfig);
    return options;
  }

  clone() {
    let clone = new Scenario(this);
    clone.id = uuid();
    return clone;
  }

  isValid() {
    if (this.enable) {
      for (let i = 0; i < this.requests.length; i++) {
        let validator = this.requests[i].isValid(this.environmentId);
        if (!validator.isValid) {
          return validator;
        }
      }
    }
    return {isValid: true};
  }

  isReference() {
    return this.id.indexOf("#") !== -1
  }
}

class DubboConfig extends BaseConfig {
  constructor(options = {}) {
    super();
    this.configCenter = new ConfigCenter(options.configCenter)
    this.registryCenter = new RegistryCenter(options.registryCenter)
    if (options.consumerAndService === undefined) {
      options.consumerAndService = {
        timeout: undefined,
        version: undefined,
        retries: undefined,
        cluster: undefined,
        group: undefined,
        connections: undefined,
        async: undefined,
        loadBalance: undefined
      }
    }
    this.consumerAndService = new ConsumerAndService(options.consumerAndService)
  }
}

export class RequestFactory {
  static TYPES = {
    HTTP: "HTTP",
    DUBBO: "DUBBO",
  }

  constructor(options = {}) {
    options.type = options.type || RequestFactory.TYPES.HTTP
    switch (options.type) {
      case RequestFactory.TYPES.DUBBO:
        return new DubboRequest(options);
      default:
        return new HttpRequest(options);
    }
  }
}

export class Request extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }

  showType() {
    return this.type;
  }

  showMethod() {
    return "";
  }
}

export class HttpRequest extends Request {
  constructor(options) {
    super(RequestFactory.TYPES.HTTP);
    this.id = undefined;
    this.name = undefined;
    this.url = undefined;
    this.path = undefined;
    this.method = undefined;
    this.parameters = [];
    this.headers = [];
    this.body = undefined;
    this.assertions = undefined;
    this.extract = undefined;
    this.environment = undefined;
    this.useEnvironment = undefined;
    this.debugReport = undefined;
    this.beanShellPreProcessor = undefined;
    this.beanShellPostProcessor = undefined;
    this.jsr223PreProcessor = undefined;
    this.jsr223PostProcessor = undefined;
    this.enable = true;
    this.connectTimeout = 60 * 1000;
    this.responseTimeout = undefined;
    this.followRedirects = true;

    this.set(options);
    this.sets({parameters: KeyValue, headers: KeyValue}, options);
  }

  initOptions(options = {}) {
    options.id = options.id || uuid();
    options.method = options.method || "GET";
    options.body = new Body(options.body);
    options.assertions = new Assertions(options.assertions);
    options.extract = new Extract(options.extract);
    options.jsr223PreProcessor = new JSR223Processor(options.jsr223PreProcessor);
    options.jsr223PostProcessor = new JSR223Processor(options.jsr223PostProcessor);
    return options;
  }

  isValid(environmentId) {
    if (this.enable) {
      if (this.useEnvironment) {
        if (!environmentId) {
          return {
            isValid: false,
            info: 'api_test.request.please_configure_environment_in_scenario'
          }
        }
      } else {
        if (!this.url) {
          return {
            isValid: false,
            info: 'api_test.request.input_url'
          }
        }
        try {
          new URL(this.url)
        } catch (e) {
          return {
            isValid: false,
            info: 'api_test.request.url_invalid'
          }
        }
      }
    }
    return {
      isValid: true
    }
  }

  showType() {
    return this.type;
  }

  showMethod() {
    return this.method.toUpperCase();
  }

}

export class DubboRequest extends Request {
  static PROTOCOLS = {
    DUBBO: "dubbo://",
    RMI: "rmi://",
  }

  constructor(options = {}) {
    super(RequestFactory.TYPES.DUBBO);
    this.id = options.id || uuid();
    this.name = options.name;
    this.protocol = options.protocol || DubboRequest.PROTOCOLS.DUBBO;
    this.interface = options.interface;
    this.method = options.method;
    this.configCenter = new ConfigCenter(options.configCenter);
    this.registryCenter = new RegistryCenter(options.registryCenter);
    this.consumerAndService = new ConsumerAndService(options.consumerAndService);
    this.args = [];
    this.attachmentArgs = [];
    this.assertions = new Assertions(options.assertions);
    this.extract = new Extract(options.extract);
    // Scenario.dubboConfig
    this.dubboConfig = undefined;
    this.debugReport = undefined;
    this.beanShellPreProcessor = new BeanShellProcessor(options.beanShellPreProcessor);
    this.beanShellPostProcessor = new BeanShellProcessor(options.beanShellPostProcessor);
    this.enable = options.enable === undefined ? true : options.enable;
    this.jsr223PreProcessor = new JSR223Processor(options.jsr223PreProcessor);
    this.jsr223PostProcessor = new JSR223Processor(options.jsr223PostProcessor);

    this.sets({args: KeyValue, attachmentArgs: KeyValue}, options);
  }

  isValid() {
    if (this.enable) {
      if (!this.interface) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_interface'
        }
      }
      if (!this.method) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_method'
        }
      }
      if (!this.registryCenter.isValid()) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_registry_center'
        }
      }
      if (!this.consumerAndService.isValid()) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_consumer_service'
        }
      }
    }
    return {
      isValid: true
    }
  }

  showType() {
    return "RPC";
  }

  showMethod() {
    // dubbo:// -> DUBBO
    return this.protocol.substr(0, this.protocol.length - 3).toUpperCase();
  }

  clone() {
    return new DubboRequest(this);
  }
}

export class ConfigCenter extends BaseConfig {
  static PROTOCOLS = ["zookeeper", "nacos", "apollo"];

  constructor(options) {
    super();
    this.protocol = undefined;
    this.group = undefined;
    this.namespace = undefined;
    this.username = undefined;
    this.address = undefined;
    this.password = undefined;
    this.timeout = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.protocol || !!this.group || !!this.namespace || !!this.username || !!this.address || !!this.password || !!this.timeout;
  }
}

export class RegistryCenter extends BaseConfig {
  static PROTOCOLS = ["none", "zookeeper", "nacos", "apollo", "multicast", "redis", "simple"];

  constructor(options) {
    super();
    this.protocol = undefined;
    this.group = undefined;
    this.username = undefined;
    this.address = undefined;
    this.password = undefined;
    this.timeout = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.protocol || !!this.group || !!this.username || !!this.address || !!this.password || !!this.timeout;
  }
}

export class ConsumerAndService extends BaseConfig {
  static ASYNC_OPTIONS = ["sync", "async"];
  static LOAD_BALANCE_OPTIONS = ["random", "roundrobin", "leastactive", "consistenthash"];

  constructor(options) {
    super();
    this.timeout = "1000";
    this.version = "1.0";
    this.retries = "0";
    this.cluster = "failfast";
    this.group = undefined;
    this.connections = "100";
    this.async = "sync";
    this.loadBalance = "random";

    this.set(options);
  }

  isValid() {
    return !!this.timeout || !!this.version || !!this.retries || !!this.cluster || !!this.group || !!this.connections || !!this.async || !!this.loadBalance;
  }
}

export class Body extends BaseConfig {
  constructor(options) {
    super();
    this.type = undefined;
    this.raw = undefined;
    this.kvs = [];

    this.set(options);
    this.sets({kvs: KeyValue}, options);
  }

  isValid() {
    if (this.isKV()) {
      return this.kvs.some(kv => {
        return kv.isValid();
      })
    } else {
      return !!this.raw;
    }
  }

  isKV() {
    return this.type === BODY_TYPE.KV;
  }
}

export class KeyValue extends BaseConfig {
  constructor() {
    let options, key, value, type, enable, uuid;
    if (arguments.length === 1) {
      options = arguments[0];
    }

    if (arguments.length === 2) {
      key = arguments[0];
      value = arguments[1];
    }
    if (arguments.length === 3) {
      key = arguments[0];
      value = arguments[1];
      type = arguments[2];
    }
    if (arguments.length === 5) {
      key = arguments[0];
      value = arguments[1];
      type = arguments[2];
      enable = arguments[3];
      uuid = arguments[4];
    }
    super();
    this.name = key;
    this.value = value;
    this.type = type;
    this.files = undefined;
    this.enable = enable;
    this.uuid = uuid;
    this.set(options);
  }

  isValid() {
    return (!!this.name || !!this.value) && this.type !== 'file';
  }

  isFile() {
    return (!!this.name || !!this.value) && this.type === 'file';
  }
}

export class Assertions extends BaseConfig {
  constructor(options) {
    super();
    this.text = [];
    this.regex = [];
    this.jsonPath = [];
    this.duration = undefined;

    this.set(options);
    this.sets({text: Text, regex: Regex, jsonPath: JSONPath}, options);
  }

  initOptions(options) {
    options = options || {};
    options.duration = new Duration(options.duration);
    return options;
  }
}

export class AssertionType extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }
}

export class BeanShellProcessor extends BaseConfig {
  constructor(options) {
    super();
    this.script = undefined;
    this.set(options);
  }
}


export class JSR223Processor extends BaseConfig {
  constructor(options) {
    super();
    this.script = undefined;
    this.language = "beanshell";
    this.set(options);
  }
}

export class Text extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.TEXT);
    this.subject = undefined;
    this.condition = undefined;
    this.value = undefined;

    this.set(options);
  }
}

export class Regex extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.REGEX);
    this.subject = undefined;
    this.expression = undefined;
    this.description = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.subject && !!this.expression;
  }
}

export class JSONPath extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.JSON_PATH);
    this.expression = undefined;
    this.expect = undefined;
    this.description = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.expression;
  }
}

export class Duration extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.DURATION);
    this.value = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.value;
  }
}

export class Extract extends BaseConfig {
  constructor(options) {
    super();
    this.regex = [];
    this.json = [];
    this.xpath = [];

    this.set(options);
    let types = {
      json: ExtractJSONPath,
      xpath: ExtractXPath,
      regex: ExtractRegex
    }
    this.sets(types, options);
  }
}

export class ExtractType extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }
}

export class ExtractCommon extends ExtractType {
  constructor(type, options) {
    super(type);
    this.variable = undefined;
    this.useHeaders = undefined;
    this.value = ""; // ${variable}
    this.expression = undefined;
    this.description = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.variable && !!this.expression;
  }
}

export class ExtractRegex extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.REGEX, options);
  }
}

export class ExtractJSONPath extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.JSON_PATH, options);
  }
}

export class ExtractXPath extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.XPATH, options);
  }
}

/** ------------------------------------------------------------------------ **/
const JMX_ASSERTION_CONDITION = {
  MATCH: 1,
  CONTAINS: 1 << 1,
  NOT: 1 << 2,
  EQUALS: 1 << 3,
  SUBSTRING: 1 << 4,
  OR: 1 << 5
}

class JMXHttpRequest {
  constructor(request, environment) {
    if (request && request instanceof HttpRequest) {
      this.useEnvironment = request.useEnvironment;
      this.method = request.method;
      if (!request.useEnvironment) {
        if (!request.url.startsWith("http://") && !request.url.startsWith("https://")) {
          request.url = 'http://' + request.url;
        }
        let url = new URL(request.url);
        this.domain = decodeURIComponent(url.hostname);
        this.port = url.port;
        this.protocol = url.protocol.split(":")[0];
        this.path = this.getPostQueryParameters(request, decodeURIComponent(url.pathname));
      } else {
        this.domain = environment.domain;
        this.port = environment.port;
        this.protocol = environment.protocol;
        let url = new URL(environment.protocol + "://" + environment.socket);
        this.path = this.getPostQueryParameters(request, decodeURIComponent(url.pathname + (request.path ? request.path : '')));
      }
      this.connectTimeout = request.connectTimeout;
      this.responseTimeout = request.responseTimeout;
      this.followRedirects = request.followRedirects;

    }
  }

  getPostQueryParameters(request, path) {
    if (this.method.toUpperCase() !== "GET") {
      let parameters = [];
      request.parameters.forEach(parameter => {
        if (parameter.name && parameter.value && parameter.enable === true) {
          parameters.push(parameter);
        }
      });
      if (parameters.length > 0) {
        path += '?';
      }
      for (let i = 0; i < parameters.length; i++) {
        let parameter = parameters[i];
        path += (parameter.name + '=' + parameter.value);
        if (i !== parameters.length - 1) {
          path += '&';
        }
      }
    }
    return path;
  }
}

class JMXDubboRequest {
  constructor(request, dubboConfig) {
    // Request 复制
    let obj = request.clone();
    // 去掉无效的kv
    obj.args = obj.args.filter(arg => {
      return arg.isValid();
    });
    obj.attachmentArgs = obj.attachmentArgs.filter(arg => {
      return arg.isValid();
    });

    // Scenario DubboConfig复制
    this.copy(obj.configCenter, dubboConfig.configCenter);
    this.copy(obj.registryCenter, dubboConfig.registryCenter);
    this.copy(obj.consumerAndService, dubboConfig.consumerAndService);

    return obj;
  }

  copy(target, source) {
    for (let key in source) {
      if (source.hasOwnProperty(key)) {
        if (source[key] !== undefined && !target[key]) {
          target[key] = source[key];
        }
      }
    }
  }
}

class JMeterTestPlan extends Element {
  constructor() {
    super('jmeterTestPlan', {
      version: "1.2", properties: "5.0", jmeter: "5.2.1"
    });

    this.add(new HashTree());
  }

  put(te) {
    if (te instanceof TestElement) {
      this.elements[0].add(te);
    }
  }
}

class JMXGenerator {
  constructor(test) {
    if (!test || !test.id || !(test instanceof Test)) return undefined;

    let testPlan = new TestPlan(test.name);
    this.addScenarios(testPlan, test.id, test.scenarioDefinition);

    this.jmeterTestPlan = new JMeterTestPlan();
    this.jmeterTestPlan.put(testPlan);
  }

  addScenarios(testPlan, testId, scenarios) {
    scenarios.forEach(s => {

      if (s.enable) {
        let scenario = s.clone();

        let threadGroup = new ThreadGroup(scenario.name || "");

        this.addScenarioVariables(threadGroup, scenario);

        this.addScenarioHeaders(threadGroup, scenario);

        this.addScenarioCookieManager(threadGroup, scenario);
        // 放在计划或线程组中，不建议放具体某个请求中
        this.addDNSCacheManager(threadGroup, scenario.requests[0]);

        scenario.requests.forEach(request => {
          if (request.enable) {
            if (!request.isValid()) return;
            let sampler;

            if (request instanceof DubboRequest) {
              sampler = new DubboSample(request.name || "", new JMXDubboRequest(request, scenario.dubboConfig));
            }

            if (request instanceof HttpRequest) {
              sampler = new HTTPSamplerProxy(request.name || "", new JMXHttpRequest(request, scenario.environment));
              this.addRequestHeader(sampler, request);
              if (request.method.toUpperCase() === 'GET') {
                this.addRequestArguments(sampler, request);
              } else {
                this.addRequestBody(sampler, request, testId);
              }
            }

            this.addRequestExtractor(sampler, request);

            this.addRequestAssertion(sampler, request);

            this.addJSR223PreProcessor(sampler, request);

            threadGroup.put(sampler);
          }
        })

        testPlan.put(threadGroup);
      }

    })
  }

  addEnvironments(environments, target) {
    let keys = new Set();
    target.forEach(item => {
      keys.add(item.name);
    });
    let envArray = environments;
    if (!(envArray instanceof Array)) {
      envArray = JSON.parse(environments);
      envArray.forEach(item => {
        if (item.name && !keys.has(item.name)) {
          target.push(new KeyValue(item.name, item.value));
        }
      })
    }
  }

  addScenarioVariables(threadGroup, scenario) {
    let environment = scenario.environment;
    if (environment) {
      this.addEnvironments(environment.variables, scenario.variables)
    }
    let args = this.filterKV(scenario.variables);
    if (args.length > 0) {
      let name = scenario.name + " Variables"
      threadGroup.put(new Arguments(name, args));
    }
  }

  addScenarioCookieManager(threadGroup, scenario) {
    if (scenario.enableCookieShare) {
      threadGroup.put(new CookieManager(scenario.name));
    }
  }

  addDNSCacheManager(threadGroup, request) {
    if (request.environment && request.environment.hosts) {
      let name = request.name + " DNSCacheManager";
      let hosts = JSON.parse(request.environment.hosts);
      if (hosts.length > 0) {
        //let domain = request.environment.protocol + "://" + request.environment.domain;
        threadGroup.put(new DNSCacheManager(name, request.environment.domain, hosts));
      }
    }
  }

  addScenarioHeaders(threadGroup, scenario) {
    let environment = scenario.environment;
    if (environment) {
      this.addEnvironments(environment.headers, scenario.headers)
    }
    let headers = this.filterKV(scenario.headers);
    if (headers.length > 0) {
      let name = scenario.name + " Headers"
      threadGroup.put(new HeaderManager(name, headers));
    }
  }

  addRequestHeader(httpSamplerProxy, request) {
    let name = request.name + " Headers";
    this.addBodyFormat(request);
    let headers = this.filterKV(request.headers);
    if (headers.length > 0) {
      httpSamplerProxy.put(new HeaderManager(name, headers));
    }
  }

  addJSR223PreProcessor(sampler, request) {
    let name = request.name;
    if (request.jsr223PreProcessor && request.jsr223PreProcessor.script) {
      sampler.put(new JSR223PreProcessor(name, request.jsr223PreProcessor));
    }
    if (request.jsr223PostProcessor && request.jsr223PostProcessor.script) {
      sampler.put(new JSR223PostProcessor(name, request.jsr223PostProcessor));
    }
  }

  addBodyFormat(request) {
    let bodyFormat = request.body.format;
    if (!request.body.isKV() && bodyFormat) {
      switch (bodyFormat) {
        case BODY_FORMAT.JSON:
          this.addContentType(request, 'application/json');
          break;
        case BODY_FORMAT.HTML:
          this.addContentType(request, 'text/html');
          break;
        case BODY_FORMAT.XML:
          this.addContentType(request, 'text/xml');
          break;
        default:
          break;
      }
    }
  }

  addContentType(request, type) {
    for (let index in request.headers) {
      if (request.headers.hasOwnProperty(index)) {
        if (request.headers[index].name === 'Content-Type') {
          request.headers.splice(index, 1);
          break;
        }
      }
    }
    request.headers.push(new KeyValue('Content-Type', type));
  }

  addRequestArguments(httpSamplerProxy, request) {
    let args = this.filterKV(request.parameters);
    if (args.length > 0) {
      httpSamplerProxy.add(new HTTPSamplerArguments(args));
    }
  }

  addRequestBody(httpSamplerProxy, request, testId) {
    let body = [];
    if (request.body.isKV()) {
      body = this.filterKV(request.body.kvs);
      this.addRequestBodyFile(httpSamplerProxy, request, testId);
    } else {
      httpSamplerProxy.boolProp('HTTPSampler.postBodyRaw', true);
      body.push({name: '', value: request.body.raw, encode: false, enable: true});
    }

    httpSamplerProxy.add(new HTTPSamplerArguments(body));
  }

  addRequestBodyFile(httpSamplerProxy, request, testId) {
    let files = [];
    let kvs = this.filterKVFile(request.body.kvs);
    kvs.forEach(kv => {
      if (kv.files) {
        kv.files.forEach(file => {
          let arg = {};
          arg.name = kv.name;
          arg.value = BODY_FILE_DIR + '/' + testId + '/' + file.id + '_' + file.name;
          files.push(arg);
        });
      }
    });
    httpSamplerProxy.add(new HTTPsamplerFiles(files));
  }

  addRequestAssertion(httpSamplerProxy, request) {
    let assertions = request.assertions;
    if (assertions.regex.length > 0) {
      assertions.regex.filter(this.filter).forEach(regex => {
        httpSamplerProxy.put(this.getResponseAssertion(regex));
      })
    }

    if (assertions.jsonPath.length > 0) {
      assertions.jsonPath.filter(this.filter).forEach(item => {
        httpSamplerProxy.put(this.getJSONPathAssertion(item));
      })
    }

    if (assertions.duration.isValid()) {
      let name = "Response In Time: " + assertions.duration.value
      httpSamplerProxy.put(new DurationAssertion(name, assertions.duration.value));
    }
  }

  getJSONPathAssertion(jsonPath) {
    let name = jsonPath.description;
    return new JSONPathAssertion(name, jsonPath);
  }

  getResponseAssertion(regex) {
    let name = regex.description;
    let type = JMX_ASSERTION_CONDITION.CONTAINS; // 固定用Match，自己写正则
    let value = regex.expression;
    switch (regex.subject) {
      case ASSERTION_REGEX_SUBJECT.RESPONSE_CODE:
        return new ResponseCodeAssertion(name, type, value);
      case ASSERTION_REGEX_SUBJECT.RESPONSE_DATA:
        return new ResponseDataAssertion(name, type, value);
      case ASSERTION_REGEX_SUBJECT.RESPONSE_HEADERS:
        return new ResponseHeadersAssertion(name, type, value);
    }
  }

  addRequestExtractor(httpSamplerProxy, request) {
    let extract = request.extract;
    if (extract.regex.length > 0) {
      extract.regex.filter(this.filter).forEach(regex => {
        httpSamplerProxy.put(this.getExtractor(regex));
      })
    }

    if (extract.json.length > 0) {
      extract.json.filter(this.filter).forEach(json => {
        httpSamplerProxy.put(this.getExtractor(json));
      })
    }

    if (extract.xpath.length > 0) {
      extract.xpath.filter(this.filter).forEach(xpath => {
        httpSamplerProxy.put(this.getExtractor(xpath));
      })
    }
  }

  getExtractor(extractCommon) {
    let props = {
      name: extractCommon.variable,
      expression: extractCommon.expression,
    }
    let testName = props.name
    switch (extractCommon.type) {
      case EXTRACT_TYPE.REGEX:
        testName += " RegexExtractor";
        props.headers = extractCommon.useHeaders; // 对应jMeter body
        props.template = "$1$";
        return new RegexExtractor(testName, props);
      case EXTRACT_TYPE.JSON_PATH:
        testName += " JSONExtractor";
        return new JSONPostProcessor(testName, props);
      case EXTRACT_TYPE.XPATH:
        testName += " XPath2Evaluator";
        return new XPath2Extractor(testName, props);
    }
  }

  filter(config) {
    return config.isValid();
  }

  filterKV(kvs) {
    return kvs.filter(this.filter);
  }

  filterKVFile(kvs) {
    return kvs.filter(kv => {
      return kv.isFile();
    });
  }

  toXML() {
    let xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
    xml += this.jmeterTestPlan.toXML();
    return xml;
  }
}


