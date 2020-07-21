<template>
  <component :is="component" :is-read-only="isReadOnly" :request="request"/>
</template>

<script>
  import {Request, RequestFactory} from "../../model/ScenarioModel";
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";
  import MsApiDubboRequestForm from "./ApiDubboRequestForm";

  export default {
    name: "MsApiRequestForm",
    components: {MsApiDubboRequestForm, MsApiHttpRequestForm},
    props: {
      request: Request,
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
          default:
            name = "MsApiHttpRequestForm";
        }
        return name;
      }
    }
  }
</script>

<style scoped>

</style>
