let Xml4js = function (options = {}) {
  this.depth = 0;
  this.encoding = options.encoding || 'UTF-8';
  this.xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
};

Xml4js.prototype = {
  toString: function () {
    return this.xml;
  },
  write: function () {
    let context = this,
      elem,
      attr = null,
      value = null,
      callback = false,
      fn = null,
      hasValue = null,
      empty = null;

    elem = arguments[0];

    if (elem === '<hashTree/>') {
      this.xml += '<hashTree/>\n'
      return;
    }

    if (typeof arguments[1] == 'object') {
      attr = arguments[1];
    } else if (typeof arguments[1] == 'function') {
      callback = true;
      fn = arguments[1];
    } else {
      value = arguments[1];
    }

    if (typeof arguments[2] == 'function') {
      if (!callback) {
        callback = true;
        fn = arguments[2];
      }
    } else {
      if (value === null) {
        value = arguments[2];
      }
    }

    hasValue = value !== null;
    empty = !hasValue && !callback;

    function indent(depth) {
      if (depth == null)
        return '';

      let space = '';
      for (let i = 0; i < depth; i++)
        if (context.tabs)
          space += "\t";
        else
          space += '  ';

      return space;
    }

    if (elem === 'cdata') {
      this.xml += indent(this.depth) + '<![CDATA[' + value + ']]>\n';
    } else {
      this.xml += indent(this.depth) + '<' + elem;
      if (attr) {
        for (let key in attr) {
          if (attr.hasOwnProperty(key)) {
            this.xml += ' ';
            this.xml += key + '="' + attr[key] + '"';
          }
        }
      }

      if (value !== null)
        this.xml += '>' + value + '</' + elem + '>\n';

      if (callback) {
        this.xml += '>\n'
        this.depth++;
        if (fn instanceof Function) {
          fn();
        }
        this.depth--;
        this.xml += indent(this.depth) + '</' + elem + '>\n'
      }

      if (empty)
        this.xml += '/>\n';
    }
  }
};

/**
 *
 *JMX 构造对象
 */
let Jmx = function (data, name, ds) {
  this.data = data;
  this.name = name;
  let domains = {};
  ds.forEach(function (d) {
    domains[d] = d;
  });
  this.domains = domains;

  this.xml = new Xml4js();
};

Jmx.prototype = {
  generate: function () {
    generateXml(this.xml, this.preprocessing());
    return this.xml.toString();
  },
  preprocessing: function () {
    let domainAlias = {};
    let index = 1;
    Object.getOwnPropertyNames(this.domains).forEach(function (key) {
      domainAlias[key] = "BASE_URL_" + index;
      index = index + 1;
    });
    let UserAgent = null;
    let hsps = [];
    let domains = this.domains;

    this.data.forEach(function (item) {
      let url = new URL(item.url);
      if (domains[url.hostname]) {
        let hsp = new HTTPSamplerProxy(item.label.replace(/&/gi, "&amp;"));
        hsp.stringProp("HTTPSampler.domain", "${" + domainAlias[url.hostname] + "}");
        hsp.stringProp("HTTPSampler.protocol", url.protocol.split(":")[0]);
        hsp.stringProp("HTTPSampler.path", url.pathname);
        hsp.stringProp("HTTPSampler.method", item.method);
        if (url.port === "") {
          hsp.intProp("HTTPSampler.port", 0);
        } else {
          hsp.intProp("HTTPSampler.port", url.port);
        }

        hsp.addArguments(item.url, item.body);
        hsps.push(hsp);

        if (item.headers.length === 0) {
          hsps.push("<hashTree/>");
        } else {
          let cphmh = new CollectionPropHeaderManagerHeaders();

          item.headers.forEach(function (h) {
            if (h.name === 'User-Agent') {
              UserAgent = h.value;
            } else {
              cphmh.addValue(new elementPropHeaderManager(h.name, h.value));
            }
          });

          // Add Header
          let ephm = new HeaderManager();
          ephm.addValue(cphmh);
          let ht = new HashTree();
          ht.addValue(ephm);
          ht.addValue("<hashTree/>");
          hsps.push(ht);
        }
      }
    });


    let jmeterTestPlan = new JmeterTestPlan();
    let hashTree = new HashTree();
    jmeterTestPlan.addValue(hashTree);

    let testPlan = new TestPlan(this.name);
    testPlan.boolProp("TestPlan.functional_mode", false);
    testPlan.boolProp("TestPlan.serialize_threadgroups", false);
    testPlan.boolProp("TestPlan.tearDown_on_shutdown", true);
    testPlan.stringProp("TestPlan.comments", "");
    testPlan.stringProp("TestPlan.user_define_classpath", "");
    testPlan.addArguments();
    hashTree.addValue(testPlan);

    let hashTree1 = new HashTree();
    hashTree.addValue(hashTree1);

    //HeaderManager
    let hm = new HeaderManager();
    hm.userAgent(UserAgent);
    hashTree1.addValue(hm);
    hashTree1.addValue("<hashTree/>");

    //Arguments
    let arg = new Arguments();
    arg.addArguments(domainAlias);
    hashTree1.addValue(arg);
    hashTree1.addValue("<hashTree/>");

    //DNSCacheManager
    let dns = new DNSCacheManager();
    dns.init();
    hashTree1.addValue(dns);
    hashTree1.addValue("<hashTree/>");

    //AuthManager
    let auth = new AuthManager();
    auth.init();
    hashTree1.addValue(auth);
    hashTree1.addValue("<hashTree/>");

    //CookieManager
    let cookie = new CookieManager();
    cookie.init();
    hashTree1.addValue(cookie);
    hashTree1.addValue("<hashTree/>");

    //CacheManager
    let cache = new CacheManager();
    cache.boolProp("clearEachIteration", true);
    cache.boolProp("useExpires", false);
    cache.boolProp("CacheManager.controlledByThread", false);
    hashTree1.addValue(cache);
    hashTree1.addValue("<hashTree/>");

    let threadGroup = new ThreadGroup();
    hashTree1.addValue(threadGroup);
    threadGroup.intProp("ThreadGroup.num_threads", 1);
    threadGroup.intProp("ThreadGroup.ramp_time", 1);
    threadGroup.longProp("ThreadGroup.delay", 0);
    threadGroup.longProp("ThreadGroup.duration", 0);
    threadGroup.stringProp("ThreadGroup.on_sample_error", "continue");
    threadGroup.boolProp("ThreadGroup.scheduler", false);

    let lc = new LoopController();
    threadGroup.addValue(lc);
    lc.boolProp("LoopController.continue_forever", false);
    lc.stringProp("LoopController.loops", 1);

    let ht = new HashTree();

    hashTree1.addValue(ht);

    hsps.forEach(function (hsp) {
      ht.addValue(hsp);
    });
    return jmeterTestPlan;
  }
};


function generateXml(xml, hashTree) {
  if (hashTree instanceof HashTree) {
    if (hashTree.values.length > 0 && !(hashTree.values[0] instanceof HashTree)) {
      xml.write(hashTree.element, hashTree.attributes, hashTree.values[0]);
    } else {
      xml.write(hashTree.element, hashTree.attributes, function () {
        if (hashTree.values.length > 0) {
          hashTree.values.forEach(function (v) {
            generateXml(xml, v);
          })
        }
      });
    }
  } else {
    xml.write(hashTree);
  }


}


/**
 * HashTree 初始对象
 */
let HashTree = function (element, attributes, value) {
  this.element = element || "hashTree";
  this.attributes = attributes || {};
  this.values = [];
  if (value !== undefined) {
    this.values.push(value);
  }
};

HashTree.prototype.addValue = function (hashTree) {
  this.values.push(hashTree)
};

HashTree.prototype.commonValue = function (element, name, value) {
  this.values.push(new HashTree(element, {name: name}, value))
};

HashTree.prototype.intProp = function (name, value) {
  this.commonValue("intProp", name, value);
};

HashTree.prototype.longProp = function (name, value) {
  this.commonValue("longProp", name, value);
};

HashTree.prototype.stringProp = function (name, value) {
  this.commonValue("stringProp", name, value);
};

HashTree.prototype.boolProp = function (name, value) {
  this.commonValue("boolProp", name, value);
};


/**
 * HashTree 的帮助对象
 */

let CHashTree = function (element, guiclass, testclass, testname, enabled) {
  HashTree.call(this, element, {
    guiclass: guiclass,
    testclass: testclass,
    testname: testname || "TEST",
    enabled: enabled || "true"
  });
};
CHashTree.prototype = new HashTree();


/**
 * JmeterTestPlan
 *
 */
let JmeterTestPlan = function (options = {}) {
  HashTree.call(this, "jmeterTestPlan",
    {
      version: options.version || "1.2",
      properties: options.properties || "5.0",
      jmeter: options.jmeter || "5.2.1"
    });
};
JmeterTestPlan.prototype = new HashTree();


/**
 * TestPlan
 *
 */
let TestPlan = function (name) {
  CHashTree.call(this, "TestPlan", "TestPlanGui", "TestPlan", name);
};
TestPlan.prototype = new CHashTree();

TestPlan.prototype.addArguments = function () {
  let ht = new HashTree("elementProp", {
    name: "TestPlan.user_defined_variables",
    elementType: "Arguments"
  });
  ht.addValue(new HashTree("collectionProp", {name: "Arguments.arguments"}));
  this.values.push(ht);
};


/**
 * HeaderManager
 *
 */
let HeaderManager = function () {
  CHashTree.call(this, "HeaderManager", "HeaderPanel", "HeaderManager", "HTTP Header manager");
};
HeaderManager.prototype = new CHashTree();

HeaderManager.prototype.userAgent = function (userAgent) {
  let cp = new HashTree("collectionProp", {
    name: "HeaderManager.headers",
  });

  let ep = new HashTree("elementProp", {
    name: "User-Agent",
    elementType: "Header"
  });

  ep.stringProp("Header.name", "User-Agent");
  ep.stringProp("Header.value", userAgent);

  cp.addValue(ep);

  this.values.push(cp);
};

/**
 * Arguments
 *
 */

let Arguments = function () {
  CHashTree.call(this, "Arguments", "ArgumentsPanel", "Arguments", "User Defined Variables");
};
Arguments.prototype = new CHashTree();

Arguments.prototype.addArguments = function (domianAlias) {
  let cp = new HashTree("collectionProp", {name: "Arguments.arguments"});

  if (domianAlias) {
    Object.getOwnPropertyNames(domianAlias).forEach(function (key) {
      cp.addValue(addArgumentsCommon(domianAlias[key], key, true, true));
    });
  }

  this.values.push(cp);
};


/**
 * DNSCacheManager
 *
 */

let DNSCacheManager = function () {
  CHashTree.call(this, "DNSCacheManager", "DNSCachePanel", "DNSCacheManager", "DNS Cache Manager");
};
DNSCacheManager.prototype = new CHashTree();

DNSCacheManager.prototype.init = function () {
  let cp = new HashTree("collectionProp", {name: "DNSCacheManager.servers"});
  this.values.push(cp);
  this.boolProp("DNSCacheManager.clearEachIteration", true);
  this.boolProp("DNSCacheManager.isCustomResolver", false);
};


/**
 * AuthManager
 *
 */

let AuthManager = function () {
  CHashTree.call(this, "AuthManager", "AuthPanel", "AuthManager", "HTTP Authorization Manager");
};
AuthManager.prototype = new CHashTree();
AuthManager.prototype.init = function () {
  let cp = new HashTree("collectionProp", {name: "AuthManager.auth_list"});
  this.values.push(cp);
  this.boolProp("AuthManager.controlledByThreadGroup", false);
};

/**
 * CookieManager
 *
 */

let CookieManager = function () {
  CHashTree.call(this, "CookieManager", "CookiePanel", "CookieManager", "HTTP Cookie Manager");
};
CookieManager.prototype = new CHashTree();
CookieManager.prototype.init = function () {
  let cp = new HashTree("collectionProp", {name: "CookieManager.cookies"});
  this.values.push(cp);
  this.boolProp("CookieManager.clearEachIteration", true);
  this.boolProp("CookieManager.controlledByThreadGroup", false);
};

/**
 * CacheManager
 *
 */


let CacheManager = function () {
  CHashTree.call(this, "CacheManager", "CacheManagerGui", "CacheManager", "HTTP Cache Manager");
};
CacheManager.prototype = new CHashTree();


/**
 * ThreadGroup
 *
 */
let ThreadGroup = function () {
  CHashTree.call(this, "ThreadGroup", "ThreadGroupGui", "ThreadGroup", "Group1");
};
ThreadGroup.prototype = new CHashTree();


let LoopController = function () {
  HashTree.call(this, "elementProp", {
    name: "ThreadGroup.main_controller",
    elementType: "LoopController",
    guiclass: "LoopControlPanel",
    testclass: "LoopController",
    testname: "Loop Controller",
    enabled: "true"
  });
};
LoopController.prototype = new HashTree();


/**
 * HTTPSamplerProxy
 *
 */
let HTTPSamplerProxy = function (name) {
  HashTree.call(this, "HTTPSamplerProxy", {
    guiclass: "HttpTestSampleGui",
    testclass: "HTTPSamplerProxy",
    testname: name,
    enabled: "true"
  });
};

HTTPSamplerProxy.prototype = new HashTree();

HTTPSamplerProxy.prototype.addArguments = function (url, body) {
  let ht = new HashTree("elementProp", {
    name: "HTTPSampler.Arguments",
    elementType: "Arguments",
    guiclass: "HTTPArgumentsPanel",
    testclass: "Arguments",
    enabled: "true"
  });
  let cp = new HashTree("collectionProp", {name: "Arguments.arguments"});
  ht.addValue(cp);

  let params = getUrlParams(url);
  params.forEach(function (item) {
    cp.addValue(addArgumentsCommon(item.name, item.value, false));
  });

  if (body) {
    this.boolProp("HTTPSampler.postBodyRaw", true);
    if (body instanceof Array) {
      cp.addValue(addArgumentsBody(body[0], true));
    } else {
      cp.addValue(addArgumentsBody(body, true));
    }

  }


  this.values.push(ht);
};


function addArgumentsCommon(name, value, alwaysEncode, metadata) {
  let ht = new HashTree("elementProp", {
    name: name,
    elementType: "HTTPArgument"
  });
  ht.boolProp("HTTPArgument.always_encode", alwaysEncode);
  if (typeof (name) == 'string') {
    ht.stringProp("Argument.name", name);
  }
  ht.stringProp("Argument.value", value);
  if (!metadata) {
    ht.stringProp("Argument.metadata", "=");
  }
  return ht;
}

function addArgumentsBody(value, alwaysEncode, metadata) {
  let ht = new HashTree("elementProp", {
    name: name,
    elementType: "HTTPArgument"
  });
  ht.boolProp("HTTPArgument.always_encode", alwaysEncode);
  ht.stringProp("Argument.value", value);
  if (!metadata) {
    ht.stringProp("Argument.metadata", "=");
  }
  return ht;
}

function getUrlParams(url) {
  let params = [];
  if (url.indexOf('?') === -1) {
    return params;
  }
  let queryString = url.split('?')[1];
  queryString = queryString.split('#')[0];

  let arr = queryString.split('&');

  arr.forEach(function (item) {
    let a = item.split('=');
    params.push({
      name: a[0],
      value: a[1]
    });
  });

  return params;
}


let ElementPropHeaderManager = function () {
  HashTree.call(this, "elementProp", {
    name: "HTTPSampler.header_manager",
    elementType: "HeaderManager"
  });
};
ElementPropHeaderManager.prototype = new HashTree();

let CollectionPropHeaderManagerHeaders = function () {
  HashTree.call(this, "collectionProp", {
    name: "HeaderManager.headers"
  });
};
CollectionPropHeaderManagerHeaders.prototype = new HashTree();


let elementPropHeaderManager = function (name, value) {
  let ht = new HashTree("elementProp", {
    name: name,
    elementType: "Header"
  });
  ht.stringProp("Header.name", name);
  ht.stringProp("Header.value", value);
  return ht;
};

export default Jmx;




