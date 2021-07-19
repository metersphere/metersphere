export const ELEMENTS = new Map([
  ['ALL', ["scenario", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "IfController","TransactionController", "LoopController", "ConstantTimer", "JSR223Processor", "CustomizeReq"]],
  ['scenario', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "CASE", "OT_IMPORT", "IfController", "ConstantTimer", "JSR223Processor", "CustomizeReq"]],
  ['HTTPSamplerProxy', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor',"Assertions", "Extract"]],
  ['DubboSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor",  'JDBCPreProcessor','JDBCPostProcessor',"Assertions", "Extract"]],
  ['JDBCSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor', "Assertions", "Extract"]],
  ['TCPSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor', "Assertions", "Extract"]],
  ['OT_IMPORT', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor', "Assertions", "Extract"]],
  ['IfController', ["IfController", "TransactionController","scenario", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "ConstantTimer", "JSR223Processor", "JSR223PreProcessor", "JSR223PostProcessor",  'JDBCPreProcessor','JDBCPostProcessor',"Assertions", "Extract", "CustomizeReq"]],
  ['TransactionController', ["TransactionController", "scenario", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "ConstantTimer", "JSR223Processor", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor', "Assertions", "Extract", "CustomizeReq"]],
  ['LoopController', ["IfController", "TransactionController","scenario", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "ConstantTimer", "JSR223Processor", "JSR223PreProcessor", "JSR223PostProcessor", 'JDBCPreProcessor','JDBCPostProcessor', "Assertions", "Extract", "CustomizeReq"]],
  ['ConstantTimer', []],
  ['JSR223Processor', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['JSR223PreProcessor', []],
  ['JSR223PostProcessor', []],
  ['JDBCPreProcessor', []],
  ['JDBCPostProcessor', []],
  ['Assertions', []],
  ['Extract', []],
  ['JmeterElement', []],
  ['CustomizeReq', ["ConstantTimer", "JSR223PreProcessor","JSR223PostProcessor", "JDBCPostProcessor", "JDBCPreProcessor", "Assertions", "Extract"]],
  ['MaxSamplerProxy', ["JSR223PreProcessor", "JSR223PostProcessor",  "JDBCPreProcessor","JDBCPostProcessor","Assertions", "Extract"]],
  ['AllSamplerProxy', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler"]],
  ['AllCanExecType', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor"]]
])

export const ELEMENT_TYPE = {
  scenario: "scenario",
  HTTPSamplerProxy: "HTTPSamplerProxy",
  OT_IMPORT: "OT_IMPORT",
  IfController: "IfController",
  TransactionController: "TransactionController",
  ConstantTimer: "ConstantTimer",
  JSR223Processor: "JSR223Processor",
  JSR223PreProcessor: "JSR223PreProcessor",
  JSR223PostProcessor: "JSR223PostProcessor",
  JDBCPostProcessor : "JDBCPostProcessor",
  JDBCPreProcessor : "JDBCPreProcessor",
  Assertions: "Assertions",
  Extract: "Extract",
  CustomizeReq: "CustomizeReq",
  LoopController: "LoopController"
}

