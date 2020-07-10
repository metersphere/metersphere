import {
  Element,
  TestElement,
  HashTree,
  TestPlan,
  ThreadGroup,
  HeaderManager,
  HTTPSamplerProxy,
  HTTPSamplerArguments,
  Arguments,
  DurationAssertion,
  ResponseCodeAssertion,
  ResponseDataAssertion,
  ResponseHeadersAssertion,
  RegexExtractor, JSONPostProcessor, XPath2Extractor,
} from "./JMX";

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

export const BODY_TYPE = {
  KV: "KeyValue",
  FORM_DATA: "Form Data",
  RAW: "Raw"
}

export const ASSERTION_TYPE = {
  TEXT: "Text",
  REGEX: "Regex",
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
          options[name].forEach((o) => {
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
    this.version = '1.0.0';
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
      if (this.scenarioDefinition[i].isValid()) {
        return this.projectId && this.name;
      }
    }
    return false;
  }

  toJMX() {
    return {
      name: this.name + '.jmx',
      xml: new JMXGenerator(this).toXML()
    };
  }
}

export class Scenario extends BaseConfig {
  constructor(options) {
    super();
    this.name = undefined;
    this.url = undefined;
    this.variables = [];
    this.headers = [];
    this.requests = [];
    this.environmentId = undefined;

    this.set(options);
    this.sets({variables: KeyValue, headers: KeyValue, requests: Request}, options);
  }

  initOptions(options) {
    options = options || {};
    options.requests = options.requests || [new Request()];
    return options;
  }

  clone() {
    return new Scenario(this);
  }

  isValid() {
    for (let i = 0; i < this.requests.length; i++) {
      if (!this.requests[i].isValid()) {
        return false;
      }
    }
    if (!this.name) {
      return false;
    }
    return true;
  }
}

export class Request extends BaseConfig {
  constructor(options) {
    super();
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

    this.set(options);
    this.sets({parameters: KeyValue, headers: KeyValue}, options);
  }

  initOptions(options) {
    options = options || {};
    options.method = options.method || "GET";
    options.body = new Body(options.body);
    options.assertions = new Assertions(options.assertions);
    options.extract = new Extract(options.extract);
    return options;
  }

  isValid() {
    return ((!this.useEnvironment && !!this.url) || (this.useEnvironment && !!this.path && this.environment)) && !!this.method
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
    let options, key, value;
    if (arguments.length === 1) {
      options = arguments[0];
    }

    if (arguments.length === 2) {
      key = arguments[0];
      value = arguments[1];
    }

    super();
    this.name = key;
    this.value = value;

    this.set(options);
  }

  isValid() {
    return !!this.name || !!this.value;
  }
}

export class Assertions extends BaseConfig {
  constructor(options) {
    super();
    this.text = [];
    this.regex = [];
    this.duration = undefined;

    this.set(options);
    this.sets({text: Text, regex: Regex}, options);
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

class JMXRequest {
  constructor(request) {
    if (request && request instanceof Request && request.url) {
      let url = new URL(request.url);
      this.method = request.method;
      this.hostname = decodeURIComponent(url.hostname);
      this.pathname = decodeURIComponent(url.pathname);
      this.path = decodeURIComponent(request.path);
      this.useEnvironment = request.useEnvironment;
      this.environment = request.environment;
      this.port = url.port;
      this.protocol = url.protocol.split(":")[0];
      if (this.method.toUpperCase() !== "GET") {
        // this.pathname += url.search.replace('&', '&amp;');
        this.pathname += '?';
        request.parameters.forEach(parameter => {
          if (parameter.name) {
            this.pathname += (parameter.name + '=' + parameter.value + '&');
          }
        });
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
    this.addScenarios(testPlan, test.scenarioDefinition);

    this.jmeterTestPlan = new JMeterTestPlan();
    this.jmeterTestPlan.put(testPlan);
  }

  addScenarios(testPlan, scenarios) {
    scenarios.forEach(s => {
      let scenario = s.clone();

      let threadGroup = new ThreadGroup(scenario.name || "");

      this.addScenarioVariables(threadGroup, scenario);

      this.addScenarioHeaders(threadGroup, scenario);

      scenario.requests.forEach(request => {
        if (!request.isValid()) return;

        let httpSamplerProxy = new HTTPSamplerProxy(request.name || "", new JMXRequest(request));

        this.addRequestHeader(httpSamplerProxy, request);

        if (request.method.toUpperCase() === 'GET') {
          this.addRequestArguments(httpSamplerProxy, request);
        } else {
          this.addRequestBody(httpSamplerProxy, request);
        }

        this.addRequestAssertion(httpSamplerProxy, request);

        this.addRequestExtractor(httpSamplerProxy, request);

        threadGroup.put(httpSamplerProxy);
      })

      testPlan.put(threadGroup);
    })
  }

  addScenarioVariables(threadGroup, scenario) {
    let scenarioVariableKeys = new Set();
    scenario.variables.forEach(item => {
      scenarioVariableKeys.add(item.name);
    });
    let environment = scenario.environment;
    if (environment) {
      let envVariables = environment.variables;
      if (!(envVariables instanceof Array)) {
        envVariables = JSON.parse(environment.variables);
        envVariables.forEach(item => {
          if (item.name && !scenarioVariableKeys.has(item.name)) {
            scenario.variables.push(new KeyValue(item.name, item.value));
          }
        })
      }
    }
    let args = this.filterKV(scenario.variables);
    if (args.length > 0) {
      let name = scenario.name + " Variables"
      threadGroup.put(new Arguments(name, args));
    }
  }

  addScenarioHeaders(threadGroup, scenario) {
    let scenarioHeaderKeys = new Set();
    scenario.headers.forEach(item => {
      scenarioHeaderKeys.add(item.name);
    });
    let environment = scenario.environment;
    if (environment) {
      let envHeaders = environment.headers;
      if (!(envHeaders instanceof Array)) {
        envHeaders = JSON.parse(environment.headers);
        envHeaders.forEach(item => {
          if (item.name && !scenarioHeaderKeys.has(item.name)) {
            scenario.headers.push(new KeyValue(item.name, item.value));
          }
        })
      }
    }
    let headers = this.filterKV(scenario.headers);
    if (headers.length > 0) {
      let name = scenario.name + " Headers"
      threadGroup.put(new HeaderManager(name, headers));
    }
  }

  addRequestHeader(httpSamplerProxy, request) {
    let name = request.name + " Headers";
    let headers = this.filterKV(request.headers);
    if (headers.length > 0) {
      httpSamplerProxy.put(new HeaderManager(name, headers));
    }
  }

  addRequestArguments(httpSamplerProxy, request) {
    let args = this.filterKV(request.parameters);
    if (args.length > 0) {
      httpSamplerProxy.add(new HTTPSamplerArguments(args));
    }
  }

  addRequestBody(httpSamplerProxy, request) {
    let body = [];
    if (request.body.isKV()) {
      body = this.filterKV(request.body.kvs);
    } else {
      httpSamplerProxy.boolProp('HTTPSampler.postBodyRaw', true);
      body.push({name: '', value: request.body.raw, encode: false});
    }

    httpSamplerProxy.add(new HTTPSamplerArguments(body));
  }

  addRequestAssertion(httpSamplerProxy, request) {
    let assertions = request.assertions;
    if (assertions.regex.length > 0) {
      assertions.regex.filter(this.filter).forEach(regex => {
        httpSamplerProxy.put(this.getAssertion(regex));
      })
    }

    if (assertions.duration.isValid()) {
      let name = "Response In Time: " + assertions.duration.value
      httpSamplerProxy.put(new DurationAssertion(name, assertions.duration.value));
    }
  }

  getAssertion(regex) {
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

  toXML() {
    let xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
    xml += this.jmeterTestPlan.toXML();
    return xml;
  }
}


