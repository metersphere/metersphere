import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JDBCPostProcessor",
      testname: "JDBC PostProcessor",
      enabled: "true"
    },
  }
};

export default class JDBCPostProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "JDBCPostProcessor";
    this.hashTree = [];
    this.variables = [];
    this.environmentId = undefined;
    this.dataSource = undefined;
    this.dataSourceId = undefined;
    this.query = undefined;
    this.queryType = undefined;
    this.queryArguments = undefined;
    this.queryArgumentsTypes = undefined;
    this.queryTimeout = undefined;
    this.resultSetHandler = undefined;
    this.resultSetMaxRows = undefined;
    this.resultVariable = undefined;
    this.variableNames = undefined;
    this.enable = true;
  }
}

export const schema = {
  JDBCPostProcessor: JDBCPostProcessor
}
