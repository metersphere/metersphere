const INDENT = '  '; // 缩进2空格

export class Element {
  constructor(name, attributes, value) {
    this.indent = '';
    this.name = name;  // 标签名
    this.attributes = attributes || {}; // 属性
    this.value = undefined; // 基础类型的内容
    this.elements = []; // 子节点

    if (value instanceof Element) {
      this.elements.push(value);
    } else {
      this.value = value;
    }
  }

  set(value) {
    this.elements = [];
    this.value = value;
  }

  add(element) {
    if (element instanceof Element) {
      this.value = undefined;
      this.elements.push(element);
      return element;
    }
  }

  getDefault(value, defaultValue) {
    return value === undefined ? defaultValue : value;
  }

  commonValue(tag, name, value, defaultValue) {
    let v = this.getDefault(value, defaultValue);
    return this.add(new Element(tag, {name: name}, v));
  }

  boolProp(name, value, defaultValue) {
    return this.commonValue('boolProp', name, value, defaultValue);
  }

  intProp(name, value, defaultValue) {
    return this.commonValue('intProp', name, value, defaultValue);
  }

  longProp(name, value, defaultValue) {
    return this.commonValue('longProp', name, value, defaultValue);
  }

  stringProp(name, value, defaultValue) {
    return this.commonValue('stringProp', name, value, defaultValue);
  }

  collectionProp(name) {
    return this.commonValue('collectionProp', name);
  }

  elementProp(name, elementType) {
    return this.add(new Element('elementProp', {name: name, elementType: elementType}));
  }

  isEmptyValue() {
    return this.value === undefined || this.value === '';
  }

  isEmptyElement() {
    return this.elements.length === 0;
  }

  isEmpty() {
    return this.isEmptyValue() && this.isEmptyElement();
  }

  replace(str) {
    if (!str || !(typeof str === 'string')) return str;
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&apos;").replace(/"/g, "&quot;");
  }

  toXML(indent) {
    if (indent) {
      this.indent = indent;
    }

    let str = this.start();
    str += this.content();
    str += this.end();
    return str;
  }

  start() {
    let str = this.indent + '<' + this.replace(this.name);
    for (let key in this.attributes) {
      if (this.attributes.hasOwnProperty(key)) {
        str += ' ' + this.replace(key) + '="' + this.replace(this.attributes[key]) + '"';
      }
    }
    if (this.isEmpty()) {
      str += '/>';
    } else {
      str += '>';
    }
    return str;
  }

  content() {
    if (!this.isEmptyValue()) {
      return this.replace(this.value);
    }

    let str = '';
    let parent = this;
    if (this.elements.length > 0) {
      str += '\n';
      this.elements.forEach(e => {
        e.indent += parent.indent + INDENT;
        str += e.toXML();
      });
    }
    return str;
  }

  end() {
    if (this.isEmpty()) {
      return '\n';
    }
    let str = '</' + this.replace(this.name) + '>\n';
    if (!this.isEmptyValue()) {
      return str;
    }
    if (!this.isEmptyElement()) {
      return this.indent + str;
    }
  }
}

// HashTree, 只能添加TestElement的子元素，没有基础类型内容
export class HashTree extends Element {
  constructor() {
    super('hashTree');
  }

  add(te) {
    if (te instanceof TestElement) {
      super.add(te);
    }
  }
}

// TestElement包含2部分，Element 和 HashTree
export class TestElement extends Element {
  constructor(name, attributes, value) {
    // Element, 只能添加Element
    super(name, attributes, value);
    // HashTree, 只能添加TestElement
    this.hashTree = new HashTree();
  }

  put(te) {
    this.hashTree.add(te);
  }

  toXML() {
    let str = super.toXML();
    str += this.hashTree.toXML(this.indent);
    return str;
  }
}

export class DefaultTestElement extends TestElement {
  constructor(tag, guiclass, testclass, testname, enabled) {
    super(tag, {
      guiclass: guiclass,
      testclass: testclass,
      testname: testname === undefined ? tag + ' Name' : testname,
      enabled: enabled || true
    });
  }
}

export class TestPlan extends DefaultTestElement {
  constructor(testName, props) {
    super('TestPlan', 'TestPlanGui', 'TestPlan', testName);

    props = props || {};
    this.boolProp("TestPlan.functional_mode", props.mode, false);
    this.boolProp("TestPlan.serialize_threadgroups", props.stg, false);
    this.boolProp("TestPlan.tearDown_on_shutdown", props.tos, true);
    this.stringProp("TestPlan.comments", props.comments);
    this.stringProp("TestPlan.user_define_classpath", props.classpath);
    this.add(new ElementArguments(props.args, "TestPlan.user_defined_variables", "User Defined Variables"));
  }
}

export class ThreadGroup extends DefaultTestElement {
  constructor(testName, props) {
    super('ThreadGroup', 'ThreadGroupGui', 'ThreadGroup', testName);

    props = props || {};
    this.intProp("ThreadGroup.num_threads", props.threads, 1);
    this.intProp("ThreadGroup.ramp_time", props.ramp, 1);
    this.longProp("ThreadGroup.delay", props.delay, 0);
    this.longProp("ThreadGroup.duration", props.delay, 0);
    this.stringProp("ThreadGroup.on_sample_error", props.error, "continue");
    this.boolProp("ThreadGroup.scheduler", props.scheduler, false);

    let loopAttrs = {
      name: "ThreadGroup.main_controller",
      elementType: "LoopController",
      guiclass: "LoopControlPanel",
      testclass: "LoopController",
      testname: "Loop Controller",
      enabled: "true"
    };
    let loopProps = props.loopProps || {};
    let loopController = this.add(new Element('elementProp', loopAttrs));
    loopController.boolProp('LoopController.continue_forever', loopProps.continue, false);
    loopController.stringProp('LoopController.loops', loopProps.loops, 1);
  }
}

export class HTTPSamplerProxy extends DefaultTestElement {
  constructor(testName, request) {
    super('HTTPSamplerProxy', 'HttpTestSampleGui', 'HTTPSamplerProxy', testName);
    this.request = request || {};

    if (request.useEnvironment) {
      this.stringProp("HTTPSampler.domain", this.request.environment.domain);
      this.stringProp("HTTPSampler.protocol", this.request.environment.protocol);
      this.stringProp("HTTPSampler.path", this.request.path);
    } else {
      this.stringProp("HTTPSampler.domain", this.request.hostname);
      this.stringProp("HTTPSampler.protocol", this.request.protocol.split(":")[0]);
      this.stringProp("HTTPSampler.path", this.request.pathname);
    }
    this.stringProp("HTTPSampler.method", this.request.method);
    this.stringProp("HTTPSampler.contentEncoding", this.request.encoding, "UTF-8");
    if (!this.request.port) {
      this.stringProp("HTTPSampler.port", "");
    } else {
      this.stringProp("HTTPSampler.port", this.request.port);
    }

    this.boolProp("HTTPSampler.follow_redirects", this.request.follow, true);
    this.boolProp("HTTPSampler.use_keepalive", this.request.keepalive, true);
  }
}

// 这是一个Element
export class HTTPSamplerArguments extends Element {
  constructor(args) {
    super('elementProp', {
      name: "HTTPsampler.Arguments", // s必须小写
      elementType: "Arguments",
      guiclass: "HTTPArgumentsPanel",
      testclass: "Arguments",
      enabled: "true"
    });

    this.args = args || [];

    let collectionProp = this.collectionProp('Arguments.arguments');
    this.args.forEach(arg => {
      let elementProp = collectionProp.elementProp(arg.name, 'HTTPArgument');
      elementProp.boolProp('HTTPArgument.always_encode', arg.encode, true);
      elementProp.boolProp('HTTPArgument.use_equals', arg.equals, true);
      if (arg.name) {
        elementProp.stringProp('Argument.name', arg.name);
      }
      elementProp.stringProp('Argument.value', arg.value);
      elementProp.stringProp('Argument.metadata', arg.metadata || "=");
    });
  }
}

export class DurationAssertion extends DefaultTestElement {
  constructor(testName, duration) {
    super('DurationAssertion', 'DurationAssertionGui', 'DurationAssertion', testName);
    this.duration = duration || 0;
    this.stringProp('DurationAssertion.duration', this.duration);
  }
}

export class ResponseAssertion extends DefaultTestElement {
  constructor(testName, assertion) {
    super('ResponseAssertion', 'AssertionGui', 'ResponseAssertion', testName);
    this.assertion = assertion || {};

    this.stringProp('Assertion.test_field', this.assertion.field);
    this.boolProp('Assertion.assume_success', false);
    this.intProp('Assertion.test_type', this.assertion.type);
    this.stringProp('Assertion.custom_message', this.assertion.message);

    let collectionProp = this.collectionProp('Asserion.test_strings');
    let random = Math.floor(Math.random() * 10000);
    collectionProp.stringProp(random, this.assertion.value);
  }
}

export class ResponseCodeAssertion extends ResponseAssertion {
  constructor(testName, type, value, message) {
    let assertion = {
      field: 'Assertion.response_code',
      type: type,
      value: value,
      message: message,
    }
    super(testName, assertion)
  }
}

export class ResponseDataAssertion extends ResponseAssertion {
  constructor(testName, type, value, message) {
    let assertion = {
      field: 'Assertion.response_data',
      type: type,
      value: value,
      message: message,
    }
    super(testName, assertion)
  }
}

export class ResponseHeadersAssertion extends ResponseAssertion {
  constructor(testName, type, value, message) {
    let assertion = {
      field: 'Assertion.response_headers',
      type: type,
      value: value,
      message: message,
    }
    super(testName, assertion)
  }
}

export class HeaderManager extends DefaultTestElement {
  constructor(testName, headers) {
    super('HeaderManager', 'HeaderPanel', 'HeaderManager', testName);
    this.headers = headers || [];

    let collectionProp = this.collectionProp('HeaderManager.headers');
    this.headers.forEach(header => {
      let elementProp = collectionProp.elementProp('', 'Header');
      elementProp.stringProp('Header.name', header.name);
      elementProp.stringProp('Header.value', header.value);
    });
  }
}

export class Arguments extends DefaultTestElement {
  constructor(testName, args) {
    super('Arguments', 'ArgumentsPanel', 'Arguments', testName);
    this.args = args || [];

    let collectionProp = this.collectionProp('Arguments.arguments');
    this.args.forEach(arg => {
      let elementProp = collectionProp.elementProp(arg.name, 'Argument');
      elementProp.stringProp('Argument.name', arg.name);
      elementProp.stringProp('Argument.value', arg.value);
      elementProp.stringProp('Argument.desc', arg.desc);
      elementProp.stringProp('Argument.metadata', arg.metadata, "=");
    });
  }
}

export class ElementArguments extends Element {
  constructor(args, name, testName) {
    super('elementProp', {
      name: name || "arguments",
      elementType: "Arguments",
      guiclass: "ArgumentsPanel",
      testclass: "Arguments",
      testname: testName || "",
      enabled: "true"
    });

    let collectionProp = this.collectionProp('Arguments.arguments');
    if (args) {
      args.forEach(arg => {
        let elementProp = collectionProp.elementProp(arg.name, 'Argument');
        elementProp.stringProp('Argument.name', arg.name);
        elementProp.stringProp('Argument.value', arg.value);
        elementProp.stringProp('Argument.metadata', arg.metadata, "=");
      });
    }
  }
}

export class RegexExtractor extends DefaultTestElement {
  constructor(testName, props) {
    super('RegexExtractor', 'RegexExtractorGui', 'RegexExtractor', testName);
    this.props = props || {}
    this.stringProp('RegexExtractor.useHeaders', props.headers);
    this.stringProp('RegexExtractor.refname', props.name);
    this.stringProp('RegexExtractor.regex', props.expression);
    this.stringProp('RegexExtractor.template', props.template);
    this.stringProp('RegexExtractor.default', props.default);
    this.stringProp('RegexExtractor.match_number', props.match);
  }
}

export class JSONPostProcessor extends DefaultTestElement {
  constructor(testName, props) {
    super('JSONPostProcessor', 'JSONPostProcessorGui', 'JSONPostProcessor', testName);
    this.props = props || {}
    this.stringProp('JSONPostProcessor.referenceNames', props.name);
    this.stringProp('JSONPostProcessor.jsonPathExprs', props.expression);
    this.stringProp('JSONPostProcessor.match_numbers', props.match);
  }
}

export class XPath2Extractor extends DefaultTestElement {
  constructor(testName, props) {
    super('XPath2Extractor', 'XPath2ExtractorGui', 'XPath2Extractor', testName);
    this.props = props || {}
    this.stringProp('XPathExtractor2.default', props.default);
    this.stringProp('XPathExtractor2.refname', props.name);
    this.stringProp('XPathExtractor2.xpathQuery', props.expression);
    this.stringProp('XPathExtractor2.namespaces', props.namespaces);
    this.stringProp('XPathExtractor2.matchNumber', props.match);
  }
}
