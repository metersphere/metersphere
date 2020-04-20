import {generateId} from "element-ui/src/utils/util";

const assign = function (obj, options) {
  for (let name in options) {
    if (options.hasOwnProperty(name)) {
      obj[name] = options[name];
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

export class Scenario {
  constructor(options) {
    this.name = null;
    this.url = null;
    this.variables = [];
    this.headers = [];
    this.requests = [];

    assign(this, options);
  }
}

export class Request {
  constructor(options) {
    this.randomId = generateId();
    this.name = null;
    this.url = null;
    this.method = null;
    this.parameters = [];
    this.headers = [];
    this.body = new Body();
    this.assertions = new Assertions();
    this.extract = [];

    assign(this, options);
  }
}

export class Body {
  constructor(options) {
    this.type = null;
    this.text = null;
    this.kvs = [];

    assign(this, options);
  }

  isKV() {
    return this.type === BODY_TYPE.KV;
  }
}

export class KeyValue {
  constructor(options) {
    this.key = null;
    this.value = null;

    assign(this, options);
  }
}

export class Assertions {
  constructor(options) {
    this.text = [];
    this.regex = [];
    this.responseTime = new ResponseTime();

    assign(this, options);
  }
}

class AssertionType {
  constructor(type) {
    this.type = type;
  }
}

export class Text extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.TEXT);
    this.subject = null;
    this.condition = null;
    this.value = null;

    assign(this, options);
  }
}

export class Regex extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.REGEX);
    this.subject = null;
    this.expression = null;
    this.description = null;

    assign(this, options);
  }
}

export class ResponseTime extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.RESPONSE_TIME);
    this.responseInTime = null;

    assign(this, options);
  }

  isValid() {
    return this.responseInTime !== null && this.responseInTime > 0;
  }
}

