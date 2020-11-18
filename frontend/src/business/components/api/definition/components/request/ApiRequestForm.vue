<template>
  <div class="request-form">
    <component :is="component" :is-read-only="isReadOnly" :request="request" :headers="headers" :isShowEnable="isShowEnable"/>
  </div>
</template>

<script>
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";
  import {Request} from "../jmeter/components";

  // import MsApiTcpRequestForm from "./ApiTcpRequestForm";
  // import MsApiDubboRequestForm from "./ApiDubboRequestForm";
  // import MsApiSqlRequestForm from "./ApiSqlRequestForm";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiHttpRequestForm},
    props: {
      request: {},
      headers: Array,
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
          case Request.TYPES.DUBBO:
            name = "MsApiDubboRequestForm";
            break;
          case Request.TYPES.SQL:
            name = "MsApiSqlRequestForm";
            break;
          case Request.TYPES.TCP:
            name = "MsApiTcpRequestForm";
            break;
          default:
            name = "MsApiHttpRequestForm";
        }
        return name;
      }
    }
  }
</script>

<style scoped>
  .request-form >>> .debug-button {
    margin-left: auto;
    display: block;
    margin-right: 10px;
  }

</style>
