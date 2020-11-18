import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JSR223PostProcessor",
      testname: "JSR223 PostProcessor",
      enabled: "true"
    },
  }
};

export default class JSR223PostProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.scriptLanguage = this.initStringProp("scriptLanguage", "groovy")
    this.parameters = this.initStringProp("parameters")
    this.filename = this.initStringProp("filename")
    this.cacheKey = this.initStringProp("cacheKey", true)
    this.script = this.initStringProp("script")
  }
}

export const schema = {
  JSR223PostProcessor: JSR223PostProcessor
}
