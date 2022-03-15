<template>
  <div>
    <!-- 请求参数 -->
    <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
    <ms-basis-parameters :showScript="false" :is-read-only="true" :request="api.request"/>
  </div>
</template>

<script>
import MsBasisParameters from "@/business/components/api/definition/components/request/database/BasisParameters";
export default {
  name: "SqlApiInformation",
  components:{MsBasisParameters},
  props:{
    api:{},
  }
}
</script>

<style scoped>

</style>
