import Sampler from "../sampler";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JDBCSampler",
      testname: "JDBC Request",
      enabled: "true"
    },
  }
};

export default class JDBCSampler extends Sampler {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);

    this.dataSource = this.initStringProp("dataSource")
    this.query = this.initStringProp("query")
    this.queryType = this.initStringProp("queryType")
    this.queryArguments = this.initStringProp("queryArguments")
    this.queryArgumentsTypes = this.initStringProp("queryArgumentsTypes")
    this.queryTimeout = this.initStringProp("queryTimeout")
    this.resultSetHandler = this.initStringProp("resultSetHandler")
    this.resultSetMaxRows = this.initStringProp("resultSetMaxRows")
    this.resultVariable = this.initStringProp("resultVariable")
    this.variableNames = this.initStringProp("variableNames")
  }
}

export const schema = {
  JDBCSampler: JDBCSampler
}
