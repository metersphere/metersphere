import {boolProp, elementProp, stringProp} from "../../../props";
import Sampler from "../sampler";
import {BaseConfig, BODY_TYPE, KeyValue,Body} from "../../../../../model/ApiTestModel";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "HttpTestSampleGui",
      testclass: "HTTPSamplerProxy",
      testname: "HTTPSamplerProxy",
      enabled: "true"
    },
  }
};
export default class HTTPSamplerProxy extends Sampler {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.protocol = this.initStringProp('HTTPSampler.protocol', "https");
    this.domain = this.initStringProp('HTTPSampler.domain');
    this.port = this.initStringProp('HTTPSampler.port');

    this.method = this.initStringProp('HTTPSampler.method', "GET");
    this.path = this.initStringProp('HTTPSampler.path');
    this.contentEncoding = this.initStringProp('HTTPSampler.contentEncoding', "UTF-8");

    this.autoRedirects = this.initBoolProp('HTTPSampler.auto_redirects');
    this.followRedirects = this.initBoolProp('HTTPSampler.follow_redirects', true);
    this.useKeepalive = this.initBoolProp('HTTPSampler.use_keepalive', true);
    this.postBodyRaw = this.initBoolProp('HTTPSampler.postBodyRaw');
    this.doMultipartPost = this.initBoolProp('HTTPSampler.DO_MULTIPART_POST', false);
    this.browserCompatibleMultipart = this.initBoolProp('HTTPSampler.BROWSER_COMPATIBLE_MULTIPART');
    this.embeddedUrlRe = this.initStringProp('HTTPSampler.embedded_url_re');
    this.connectTimeout = this.initStringProp('HTTPSampler.connect_timeout');
    this.responseTimeout = this.initStringProp('HTTPSampler.response_timeout');
    // 初始化认证对象 和主体对象
    this.authConfig = {};
    this.body = new Body();

    this.arguments = [];

    this.rest = [];

    let elementProp = this.initElementProp('HTTPsampler.Arguments', 'Arguments');
    let collectionProp = elementProp.initCollectionProp('Arguments.arguments');

    collectionProp.forEach(elementProp => {
      let name = elementProp.initStringProp('Argument.name').value;
      let value = elementProp.initStringProp('Argument.value').value;

      let alwaysEncode = elementProp.initBoolProp('HTTPArgument.always_encode').value;
      let useEquals = elementProp.initBoolProp('HTTPArgument.use_equals', true).value;
      let contentType = elementProp.initStringProp('HTTPArgument.content_type', "text/plain").value;
      let arg = {
        name: name,
        value: value,
        alwaysEncode: alwaysEncode,
        useEquals: useEquals,
        contentType: contentType,
        enable: true
      };

      this.arguments.push(arg);
    })

    if (this.arguments.length > 0 && this.arguments[0].name === "") {
      this.body = this.arguments[0].value;
    }

    this.files = [];
    let filesProp = this.initElementProp("HTTPsampler.Files", "HTTPFileArgs");
    let filesCollectionProp = filesProp.initCollectionProp('HTTPFileArgs.files');
    filesCollectionProp.forEach(elementProp => {
      let path = elementProp.initStringProp('File.path').value;
      let name = elementProp.initStringProp('File.paramname').value;
      let type = elementProp.initBoolProp('File.mimetype').value;
      let file = {path: path, name: name, type: type, enable: true};

      this.files.push(file);
    });
  }

  updateProps() {
    if (this.body && this.postBodyRaw.value) {
      this.arguments = [{name: "", value: this.body, alwaysEncode: false}];
    }
    let collectionProp = this.props['HTTPsampler.Arguments'].elements['Arguments.arguments'];
    collectionProp.clear();
    this.arguments.forEach(variable => {
      if (variable.enable !== false) {
        let ep = elementProp(variable.name, "HTTPArgument");
        ep.add(stringProp("Argument.name", variable.name));
        ep.add(stringProp("Argument.value", variable.value));
        ep.add(stringProp("Argument.metadata", "="));
        ep.add(boolProp("HTTPArgument.always_encode", variable.alwaysEncode));
        ep.add(boolProp("HTTPArgument.use_equals", variable.useEquals));
        if (variable.contentType && variable.contentType !== "text/plain") {
          ep.add(stringProp("HTTPArgument.content_type", variable.contentType));
        }
        collectionProp.add(ep)
      }
    })

    let filesProp = this.props["HTTPsampler.Files"].elements["HTTPFileArgs.files"];
    filesProp.clear();
    this.files.forEach(file => {
      if (file.enable !== false) {
        let ep = elementProp(file.path, "HTTPFileArg");
        ep.add(stringProp("File.path", file.path));
        ep.add(stringProp("File.paramname", file.name));
        ep.add(stringProp("File.mimetype", file.type));
        filesProp.add(ep);
      }
    })
  }

  toJson() {
    this.updateProps();
    return super.toJson();
  }
}

export const schema = {
  HTTPSamplerProxy: HTTPSamplerProxy
}

