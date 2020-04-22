import {generateId} from "element-ui/src/utils/util";

const assign = function (obj, options) {
  if (options) {
    for (let name in options) {
      if (options.hasOwnProperty(name)) {
        if (!(obj[name] instanceof Array)) {
          obj[name] = options[name];
        }
      }
    }
  }
}

const assigns = function (target, source, type) {
  if (target instanceof Array && source instanceof Array) {
    if (source && source.length > 0) {
      source.forEach((options) => {
        target.push(new type(options));
      })
    }
  }
}

export const BODY_TYPE = {
  KV: "KV",
  TEXT: "TEXT"
}

export const ASSERTION_TYPE = {
  TEXT: "TEXT",
  REGEX: "REGEX",
  RESPONSE_TIME: "RESPONSE_TIME"
}

export class Test {
  constructor(options) {
    this.reset(options);
  }

  reset(options) {
    options = this.getDefaultOptions(options);
    this.id = null;
    this.name = null;
    this.projectId = null;
    this.scenarioDefinition = [];

    assign(this, options);
    assigns(this.scenarioDefinition, options.scenarioDefinition, Scenario);
  }

  getDefaultOptions(options) {
    options = options || {};
    options.scenarioDefinition = options.scenarioDefinition || [new Scenario()];
    return options;
  }
}

export class Scenario {
  constructor(options) {
    options = this.getDefaultOptions(options);
    this.name = null;
    this.url = null;
    this.variables = [];
    this.headers = [];
    this.requests = [];

    assign(this, options);
    assigns(this.variables, options.variables, KeyValue);
    assigns(this.headers, options.headers, KeyValue);
    assigns(this.requests, options.requests, Request);
  }

  getDefaultOptions(options) {
    options = options || {};
    options.requests = options.requests || [new Request()];
    return options;
  }
}

export class Request {
  constructor(options) {
    options = this.getDefaultOptions(options);
    this.randomId = generateId();
    this.name = null;
    this.url = null;
    this.method = null;
    this.parameters = [];
    this.headers = [];
    this.body = null;
    this.assertions = null;
    this.extract = [];

    assign(this, options);
    assigns(this.parameters, options.parameters, KeyValue);
    assigns(this.headers, options.headers, KeyValue);
    // TODO assigns extract
  }

  getDefaultOptions(options) {
    options = options || {};
    options.method = "GET";
    options.body = new Body(options.body);
    options.assertions = new Assertions(options.assertions);
    return options;
  }
}

export class Body {
  constructor(options) {
    options = options || {};
    this.type = null;
    this.text = null;
    this.kvs = [];

    assign(this, options);
    assigns(this.kvs, options.kvs, KeyValue);
  }

  isKV() {
    return this.type === BODY_TYPE.KV;
  }
}

export class KeyValue {
  constructor(options) {
    options = options || {};
    this.key = null;
    this.value = null;

    assign(this, options);
  }
}

export class Assertions {
  constructor(options) {
    options = this.getDefaultOptions(options);
    this.text = [];
    this.regex = [];
    this.responseTime = null;

    assign(this, options);
    assigns(this.text, options.text, KeyValue);
    assigns(this.regex, options.regex, KeyValue);
  }

  getDefaultOptions(options) {
    options = options || {};
    options.responseTime = new ResponseTime(options.responseTime);
    return options;
  }
}

class AssertionType {
  constructor(type) {
    this.type = type;
  }
}

export class Text extends AssertionType {
  constructor(options) {
    options = options || {};
    super(ASSERTION_TYPE.TEXT);
    this.subject = null;
    this.condition = null;
    this.value = null;

    assign(this, options);
  }
}

export class Regex extends AssertionType {
  constructor(options) {
    options = options || {};
    super(ASSERTION_TYPE.REGEX);
    this.subject = null;
    this.expression = null;
    this.description = null;

    assign(this, options);
  }
}

export class ResponseTime extends AssertionType {
  constructor(options) {
    options = options || {};
    super(ASSERTION_TYPE.RESPONSE_TIME);
    this.responseInTime = null;

    assign(this, options);
  }
}

