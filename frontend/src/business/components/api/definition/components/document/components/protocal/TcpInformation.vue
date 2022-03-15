<template>
  <div>
    <!-- 请求参数 -->
    <div v-if="apiProtocol=='TCP'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <ms-tcp-format-parameters :is-read-only="true" :show-pre-script="true" :show-script="false" :request="api.request"
                                ref="tcpFormatParameter"/>
    </div>
    <div v-else-if="apiProtocol=='ESB'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <esb-definition v-xpack :show-pre-script="true" v-if="showXpackCompnent" :show-script="false" :request="api.request"
                      ref="esbDefinition"/>
      <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
      <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="true" :show-options-button="true"
                               :request="api.request"/>
    </div>
  </div>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
import MsTcpFormatParameters from "@/business/components/api/definition/components/request/tcp/TcpFormatParameters";

export default {
  name: "TcpInformation",
  components:{
    MsTcpFormatParameters,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default,
  },
  props:{
    api:{},
  },
  data(){
    return{
      apiProtocol: "TCP",
      showXpackCompnent: false,
      methodTypes: [
        {
          'key': "TCP",
          'value': this.$t('api_test.request.tcp.general_format'),
        }
      ],
    }
  },
  created() {
    this.apiProtocol = this.api.method;
    if (this.apiProtocol == null || this.apiProtocol == "") {
      this.apiProtocol = "TCP";
    }
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
      this.showXpackCompnent = true;
      if (hasLicense()) {
        if (this.methodTypes.length == 1) {
          let esbMethodType = {};
          esbMethodType.key = "ESB";
          esbMethodType.value = "ESB";
          this.methodTypes.push(esbMethodType);
        }
      }
    }
  }
}
</script>

<style scoped>

</style>
