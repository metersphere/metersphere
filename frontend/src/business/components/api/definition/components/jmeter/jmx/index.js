import Element from "../element";
import {loadComponent} from "../components";
import JmeterTestPlan from "../components/jmeter-test-plan";
import TestPlan from "../components/test-plan";
import {js2xml, xml2js} from "xml-js";

const DEFAULT_OPTIONS = {
  declaration: {attributes: {version: "1.0", encoding: "UTF-8"}}
}

export default class JMX extends Element {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.declaration = options.declaration;
    if (options.elements) {
      this.elements = [];
      options.elements.forEach(e => {
        this.elements.push(loadComponent(e));
      })
    }
  }

  toJson() {
    let json = {
      declaration: this.declaration
    };
    if (this.elements) {
      json.elements = [];
      this.elements.forEach(e => {
        json.elements.push(e.toJson());
      })
    }
    return json;
  }

  toXML() {
    return js2xml(this.toJson(), {spaces: 2});
  }

  static create() {
    let jmx = new JMX();
    let jmeterTestPlan = new JmeterTestPlan();
    let testPlan = new TestPlan();
    jmeterTestPlan.hashTree = [testPlan];
    jmx.elements = [jmeterTestPlan];
    return jmx;
  }

  static fromJMX(xml) {
    return new JMX(xml2js(xml));
  }
}
