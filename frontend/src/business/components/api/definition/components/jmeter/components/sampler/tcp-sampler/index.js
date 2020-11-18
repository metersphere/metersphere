import Sampler from "../sampler";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TCPSamplerGui",
      testclass: "TCPSampler",
      testname: "TCPSampler",
      enabled: "true"
    },
  }
};

export default class TCPSampler extends Sampler {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.classname = this.initStringProp("TCPSampler.classname", "TCPClientImpl")

    this.server = this.initStringProp("TCPSampler.server")
    this.port = this.initStringProp("TCPSampler.port")

    this.connectTimeout = this.initStringProp("TCPSampler.ctimeout")
    this.responseTimeout = this.initStringProp("TCPSampler.timeout")

    this.reUseConnection = this.initBoolProp("TCPSampler.reUseConnection", true)
    this.closeConnection = this.initBoolProp("TCPSampler.closeConnection", false)
    this.nodelay = this.initBoolProp("TCPSampler.nodelay", false)

    this.soLinger = this.initStringProp("TCPSampler.soLinger")
    this.eolByte = this.initStringProp("TCPSampler.EolByte")

    this.request = this.initStringProp("TCPSampler.request")
    this.username = this.initStringProp("ConfigTestElement.username")
    this.password = this.initStringProp("ConfigTestElement.password")

  }
}

export const schema = {
  TCPSampler: TCPSampler
}
