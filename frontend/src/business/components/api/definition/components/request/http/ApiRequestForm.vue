<template>
  <div class="request-form">
    <component :is="component" :showScript="showScript" :is-read-only="isReadOnly" :referenced="referenced" :request="request" :headers="headers" :isShowEnable="isShowEnable"/>
  </div>
</template>

<script>
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";

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
      showScript: {
        type: Boolean,
        default: true,
      },
      referenced: {
        type: Boolean,
        default: false
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      component({request: {type}}) {
        return "MsApiHttpRequestForm";
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
