<template>
  <div class="request-form">
    <component :is="component" :is-read-only="isReadOnly" :request="request" :isShowEnable="isShowEnable"/>
    <el-divider v-if="isCompleted"></el-divider>
  </div>
</template>

<script>
  import {JSR223Processor, Request, RequestFactory} from "../../model/ApiTestModel";
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";
  // import MsApiTcpRequestForm from "./ApiTcpRequestForm";
  // import MsApiDubboRequestForm from "./ApiDubboRequestForm";
  // import MsApiSqlRequestForm from "./ApiSqlRequestForm";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiHttpRequestForm},
    props: {
      request: Request,
      isShowEnable: {
        type: Boolean,
        default: true
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      component({request: {type}}) {
        let name;
        switch (type) {
          case RequestFactory.TYPES.DUBBO:
            name = "MsApiDubboRequestForm";
            break;
          case RequestFactory.TYPES.SQL:
            name = "MsApiSqlRequestForm";
            break;
          case RequestFactory.TYPES.TCP:
            name = "MsApiTcpRequestForm";
            break;
          default:
            name = "MsApiHttpRequestForm";
        }
        return name;
      },
      isCompleted() {
        return !!this.request.debugReport;
      }
    },
    mounted() {
      //兼容旧版本 beanshell
      if (!this.request.jsr223PreProcessor.script && this.request.beanShellPreProcessor) {
        this.request.jsr223PreProcessor = new JSR223Processor(this.request.beanShellPreProcessor);
      }
      if (!this.request.jsr223PostProcessor.script && this.request.beanShellPostProcessor) {
        this.request.jsr223PostProcessor = new JSR223Processor(this.request.beanShellPostProcessor);
      }
    },
  }
</script>

<style scoped>

  .scenario-results {
    margin-top: 20px;
  }

  .request-form >>> .debug-button {
    margin-left: auto;
    display: block;
    margin-right: 10px;
  }

</style>
