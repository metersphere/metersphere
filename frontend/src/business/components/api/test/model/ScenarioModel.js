import {generateId} from "element-ui/src/utils/util";

export const BODY_TYPE = {
  KV: "KV",
  TEXT: "TEXT"
}

export const ASSERTION_TYPE = {
  TEXT: "TEXT",
  REGEX: "REGEX",
  RESPONSE_TIME: "RESPONSE_TIME"
}

class BaseConfig {

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
}

export class Test extends BaseConfig {
  constructor(options) {
    super();
    this.id = null;
    this.name = null;
    this.projectId = null;
    this.scenarioDefinition = [];

    this.set(options);
    this.sets({scenarioDefinition: Scenario}, options);
  }

  initOptions(options) {
    options = options || {};
    options.scenarioDefinition = options.scenarioDefinition || [new Scenario()];
    return options;
  }
}

export class Scenario extends BaseConfig {
  constructor(options) {
    super();
    this.name = null;
    this.url = null;
    this.variables = [];
    this.headers = [];
    this.requests = [];

    this.set(options);
    this.sets({variables: KeyValue, headers: KeyValue, requests: Request}, options);
  }

  initOptions(options) {
    options = options || {};
    options.requests = options.requests || [new Request()];
    return options;
  }
}

export class Request extends BaseConfig {
  constructor(options) {
    super();
    this.randomId = generateId();
    this.name = null;
    this.url = null;
    this.method = null;
    this.parameters = [];
    this.headers = [];
    this.body = null;
    this.assertions = null;
    this.extract = [];

    this.set(options);
    this.sets({parameters: KeyValue, headers: KeyValue}, options);
    // TODO assigns extract
  }

  initOptions(options) {
    options = options || {};
    options.method = "GET";
    options.body = new Body(options.body);
    options.assertions = new Assertions(options.assertions);
    return options;
  }
}

export class Body extends BaseConfig {
  constructor(options) {
    super();
    this.type = null;
    this.text = null;
    this.kvs = [];

    this.set(options);
    this.sets({kvs: KeyValue}, options);
  }

  isKV() {
    return this.type === BODY_TYPE.KV;
  }
}

export class KeyValue extends BaseConfig {
  constructor(options) {
    super();
    this.key = null;
    this.value = null;

    this.set(options);
  }
}

export class Assertions extends BaseConfig {
  constructor(options) {
    super();
    this.text = [];
    this.regex = [];
    this.responseTime = null;

    this.set(options);
    this.sets({text: KeyValue, regex: KeyValue}, options);
  }

  initOptions(options) {
    options = options || {};
    options.responseTime = new ResponseTime(options.responseTime);
    return options;
  }
}

class AssertionType extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }
}

export class Text extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.TEXT);
    this.subject = null;
    this.condition = null;
    this.value = null;

    this.set(options);
  }
}

export class Regex extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.REGEX);
    this.subject = null;
    this.expression = null;
    this.description = null;

    this.set(options);
  }
}

export class ResponseTime extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.RESPONSE_TIME);
    this.responseInTime = null;

    this.set(options);
  }
}

