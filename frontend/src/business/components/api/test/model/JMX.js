const INDENT = '  '; // 缩进2空格

export class Element {
  constructor(name, attributes, value) {
    this.indent = '';
    this.name = name;
    this.attributes = attributes || {};
    this.value = undefined;
    this.elements = [];

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

  commonValue(tag, name, value) {
    return this.add(new Element(tag, {name: name}, value));
  }

  boolProp(name, value) {
    return this.commonValue('boolProp', name, value);
  }

  intProp(name, value) {
    return this.commonValue('intProp', name, value);
  }

  longProp(name, value) {
    return this.commonValue('longProp', name, value);
  }

  stringProp(name, value) {
    return this.commonValue('stringProp', name, value);
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
    let str = this.indent + '<' + this.name;
    for (let key in this.attributes) {
      if (this.attributes.hasOwnProperty(key)) {
        str += ' ' + key + '="' + this.attributes[key] + '"';
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
      return this.value;
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
    let str = '</' + this.name + '>\n';
    if (!this.isEmptyValue()) {
      return str;
    }
    if (!this.isEmptyElement()) {
      return this.indent + str;
    }
  }
}

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
      testname: testname || tag + ' Name',
      enabled: enabled || true
    });
  }
}

export class TestPlan extends DefaultTestElement {
  constructor(testName) {
    super('TestPlan', 'TestPlanGui', 'TestPlan', testName || 'TestPlan');

    this.boolProp("TestPlan.functional_mode", false);
    this.boolProp("TestPlan.serialize_threadgroups", false);
    this.boolProp("TestPlan.tearDown_on_shutdown", true);
    this.stringProp("TestPlan.comments", "");
    this.stringProp("TestPlan.user_define_classpath", "");
  }
}

export class ThreadGroup extends DefaultTestElement {
  constructor(testName) {
    super('ThreadGroup', 'ThreadGroupGui', 'ThreadGroup', testName);

    this.intProp("ThreadGroup.num_threads", 1);
    this.intProp("ThreadGroup.ramp_time", 1);
    this.longProp("ThreadGroup.delay", 0);
    this.longProp("ThreadGroup.duration", 0);
    this.stringProp("ThreadGroup.on_sample_error", "continue");
    this.boolProp("ThreadGroup.scheduler", false);

    let loopAttrs = {
      name: "ThreadGroup.main_controller",
      elementType: "LoopController",
      guiclass: "LoopControlPanel",
      testclass: "LoopController",
      testname: "Loop Controller",
      enabled: "true"
    };
    let loopController = this.add(new Element('elementProp', loopAttrs));
    loopController.boolProp('LoopController.continue_forever', false);
    loopController.stringProp('LoopController.loops', 1);
  }
}

export class HTTPSamplerProxy extends DefaultTestElement {
  constructor(testName, request) {
    super('HTTPSamplerProxy', 'HttpTestSampleGui', 'HTTPSamplerProxy', testName || 'HTTP Request');
    this.request = request || {};

    this.stringProp("HTTPSampler.domain", this.request.hostname);
    this.stringProp("HTTPSampler.protocol", this.request.protocol.split(":")[0]);
    this.stringProp("HTTPSampler.path", this.request.pathname);
    this.stringProp("HTTPSampler.method", this.request.method);
    if (this.request.port) {
      this.stringProp("HTTPSampler.port", "");
    } else {
      this.stringProp("HTTPSampler.port", this.request.port);
    }
  }

  addRequestArguments(arg) {
    if (arg instanceof HTTPSamplerArguments) {
      this.add(arg);
    }
  }

  addRequestBody(body) {
    if (body instanceof HTTPSamplerArguments) {
      this.boolProp('HTTPSampler.postBodyRaw', true);
      this.add(body);
    }
  }

  putRequestHeader(header) {
    if (header instanceof HeaderManager) {
      this.put(header);
    }
  }

  putResponseAssertion(assertion) {
    if (assertion instanceof ResponseAssertion) {
      this.put(assertion);
    }
  }

  putDurationAssertion(assertion) {
    if (assertion instanceof DurationAssertion) {
      this.put(assertion);
    }
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
      elementProp.boolProp('HTTPArgument.always_encode', false);
      elementProp.boolProp('HTTPArgument.use_equals', true);
      if (arg.name) {
        elementProp.stringProp('Argument.name', arg.name);
      }
      elementProp.stringProp('Argument.value', arg.value);
      elementProp.stringProp('Argument.metadata', "=");
    });
  }
}

export class DurationAssertion extends DefaultTestElement {
  constructor(testName, duration) {
    super('DurationAssertion', 'DurationAssertionGui', 'DurationAssertion', testName || 'Duration Assertion');
    this.duration = duration || 0;
    this.stringProp('DurationAssertion.duration', this.duration);
  }
}

export class ResponseAssertion extends DefaultTestElement {
  constructor(testName, assertion) {
    super('ResponseAssertion', 'AssertionGui', 'ResponseAssertion', testName || 'Response Assertion');
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
    super('HeaderManager', 'HeaderPanel', 'HeaderManager', testName || 'HTTP Header manager');
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
    super('Arguments', 'ArgumentsPanel', 'Arguments', testName || 'User Defined Variables');
    this.args = args || [];

    let collectionProp = this.collectionProp('Arguments.arguments');
    this.args.forEach(arg => {
      let elementProp = collectionProp.elementProp(arg.name, 'HTTPArgument');
      elementProp.boolProp('HTTPArgument.always_encode', true);
      elementProp.stringProp('Argument.name', arg.name);
      elementProp.stringProp('Argument.value', arg.value);
      elementProp.stringProp('Argument.metadata', "=");
    });
  }
}

export class BackendListener extends DefaultTestElement {
  constructor(testName, className, args) {
    super('BackendListener', 'BackendListenerGui', 'BackendListener', testName || 'Backend Listener');
    this.stringProp('classname', className);
    this.add(new ElementArguments(args));
  }
}

export class ElementArguments extends Element {
  constructor(args) {
    super('elementProp', {
      name: "arguments",
      elementType: "Arguments",
      guiclass: "ArgumentsPanel",
      testclass: "Arguments",
      enabled: "true"
    });

    let collectionProp = this.collectionProp('Arguments.arguments');
    args.forEach(arg => {
      let elementProp = collectionProp.elementProp(arg.name, 'Argument');
      elementProp.stringProp('Argument.name', arg.name);
      elementProp.stringProp('Argument.value', arg.value);
      elementProp.stringProp('Argument.metadata', "=");
    });
  }
}

