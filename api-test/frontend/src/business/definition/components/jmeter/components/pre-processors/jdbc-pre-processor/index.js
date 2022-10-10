import PostProcessor from "../pre-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JDBCPreProcessor",
      testname: "JDBC PreProcessor",
      enabled: "true"
    },
  }
};

export default class JDBCPreProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "JDBCPreProcessor";
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
  JDBCPreProcessor: JDBCPreProcessor
}
