<template xmlns:el-col="http://www.w3.org/1999/html">
  <!-- 操作按钮 -->
  <div style="background-color: white;">
    <el-row>
      <el-col>
        <!--操作按钮-->
        <div style="float: right;margin-right: 20px;margin-top: 20px">
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s">{{ $t('commons.save') }}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{ $t('commons.test') }}</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- 基础信息 -->
    <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>
    <br/>
    <el-row>
      <el-col>
        <ms-tcp-basic-api :method-types="methodTypes" @createRootModelInTree="createRootModelInTree" :moduleOptions="moduleOptions" :basisData="basisData" ref="basicForm"
          @changeApiProtocol="changeApiProtocol" @callback="callback"/>
      </el-col>
    </el-row>
    <!-- 请求参数 -->
    <div v-if="apiProtocol=='TCP'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <ms-basis-parameters :show-script="false" :request="request"/>
    </div>
    <div v-else-if="apiProtocol=='ESB'">
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <esb-definition v-xpack v-if="showXpackCompnent" :show-script="false" :request="request" ref="esbDefinition"/>
      <p class="tip">{{$t('api_test.definition.request.res_param')}}</p>
      <esb-definition-response :is-api-component="true" :show-options-button="true" :request="request" />
<!--      <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase"/>-->
    </div>


  </div>

</template>

<script>
import MsTcpBasicApi from "./TCPBasicApi";
import MsBasisParameters from "../request/tcp/TcpBasisParameters";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent!=null&&requireComponent.keys().length) > 0 ? requireComponent("./apiDefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent!=null&&requireComponent.keys().length) > 0 ? requireComponent("./apiDefinition/EsbDefinitionResponse.vue") : {};
export default {
  name: "MsAddCompleteTcpApi",
  components: {MsTcpBasicApi, MsBasisParameters,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default},
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    syncTabs:Array,
  },
  data() {
    return {
      validated: false,
      apiProtocol: "TCP",
      methodTypes:["TCP"],
      showXpackCompnent:false,
    }
  },
  created: function () {
    if(this.basisData.method != 'TCP'&& this.basisData.method != 'ESB'){
      this.basisData.method = this.basisData.protocol;
    }
    this.apiProtocol = this.basisData.method;
    if(this.apiProtocol == null || this.apiProtocol == "" ){
      this.apiProtocol = "TCP";
    }
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}'&& JSON.stringify(esbDefinitionResponse) != '{}') {
      this.showXpackCompnent = true;
      if(this.methodTypes.length == 1){
        this.methodTypes.push("ESB");
      }
    }
  },
  watch: {
    syncTabs() {
      if (this.basisData && this.syncTabs && this.syncTabs.includes(this.basisData.id)) {
        // 标示接口在其他地方更新过，当前页面需要同步
        let url = "/api/definition/get/";
        this.$get(url + this.basisData.id, response => {
          if (response.data) {
            let request = JSON.parse(response.data.request);
            let index = this.syncTabs.findIndex(item => {
              if (item === this.basisData.id) {
                return true;
              }
            })
            this.syncTabs.splice(index, 1);
            Object.assign(this.request, request);
          }
        });
      }
    },
  },
  methods: {
    callback() {
      this.validated = true;
    },
    validateApi() {
      this.validated = false;
      this.basisData.method = this.apiProtocol;
      this.$refs['basicForm'].validate();
    },
    saveApi() {
      this.validateApi();
      if (this.validated) {
        if (this.basisData.tags instanceof Array) {
          this.basisData.tags = JSON.stringify(this.basisData.tags);
        }
        if(this.basisData.method == 'ESB'){
          let validataResult = this.$refs.esbDefinition.validateEsbDataStruct(this.request.esbDataStruct);
          if(!validataResult){
            return;
          }
          if(this.request.esbDataStruct != null){
            this.esbDataStruct = JSON.stringify(this.request.esbDataStruct);
            this.basisData.esbDataStruct = this.esbDataStruct;
          }
          if(this.request.backEsbDataStruct != null){
            this.basisData.backEsbDataStruct = JSON.stringify(this.request.backEsbDataStruct);
          }
          if(this.request.backScript != null){
            this.basisData.backScript = JSON.stringify(this.request.backScript);
          }
        }
        this.$emit('saveApi', this.basisData);
      }
    },
    runTest() {
      this.validateApi();
      if (this.validated) {
        this.basisData.request = this.request;
        if (this.basisData.tags instanceof Array) {
          this.basisData.tags = JSON.stringify(this.basisData.tags);
        }
        this.$emit('runTest', this.basisData);
      }
    },
    createRootModelInTree() {
      this.$emit("createRootModelInTree");
    },
    changeApiProtocol(protocol){
      this.apiProtocol = protocol;
    }
  },
}
</script>

<style scoped>
.tip {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 4px;
  border-left: 4px solid #783887;
  margin: 0px 20px 0px;
}
</style>
