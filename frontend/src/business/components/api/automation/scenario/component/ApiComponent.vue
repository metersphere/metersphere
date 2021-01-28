<template>
  <api-base-component
    v-loading="loading"
    @copy="copyRow"
    @remove="remove"
    :is-show-name-input="!isDeletedOrRef"
    :data="request"
    :draggable="true"
    :color="displayColor.color"
    :background-color="displayColor.backgroundColor"
    :title="displayTitle">

    <template v-slot:behindHeaderLeft>
      <el-tag size="mini" style="margin-left: 20px" v-if="request.referenced==='Deleted'" type="danger">{{$t('api_test.automation.reference_deleted')}}</el-tag>
      <el-tag size="mini" style="margin-left: 20px" v-if="request.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
      <el-tag size="mini" style="margin-left: 20px" v-if="request.referenced ==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
      <ms-run :debug="true" :reportId="reportId" :run-data="runData"
              @runRefresh="runRefresh" ref="runTest"/>

    </template>

    <template v-slot:button>
      <el-tooltip :content="$t('api_test.run')" placement="top">
        <el-button @click="run" icon="el-icon-video-play" style="background-color: #409EFF;color: white;" size="mini" circle/>
      </el-tooltip>
    </template>

    <div v-if="request.protocol === 'HTTP'">
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-if="request.url" v-model="request.url" style="width: 85%;margin-top: 10px" size="small">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-else v-model="request.path" style="width: 85%;margin-top: 10px" size="small">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
    </div>
    <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
    <ms-api-request-form :isShowEnable="true" :referenced="true" :headers="request.headers " :request="request" v-if="request.protocol==='HTTP' || request.type==='HTTPSamplerProxy'"/>
    <ms-tcp-basis-parameters :request="request" v-if="request.protocol==='TCP'|| request.type==='TCPSampler'"/>
    <ms-sql-basis-parameters :request="request" v-if="request.protocol==='SQL'|| request.type==='JDBCSampler'" :showScript="false"/>
    <ms-dubbo-basis-parameters :request="request" v-if="request.protocol==='DUBBO' || request.protocol==='dubbo://'|| request.type==='DubboSampler'" :showScript="false"/>

    <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
    <api-response-component :currentProtocol="request.protocol" :result="request.requestResult"/>

    <!-- 保存操作 -->
    <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(item)" v-if="!request.referenced">
      {{$t('commons.save')}}
    </el-button>

  </api-base-component>
</template>

<script>
  import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
  import MsTcpBasisParameters from "../../../definition/components/request/tcp/TcpBasisParameters";
  import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
  import MsApiRequestForm from "../../../definition/components/request/http/ApiHttpRequestForm";
  import {REQ_METHOD} from "../../../definition/model/JsonData";
  import MsRequestResultTail from "../../../definition/components/response/RequestResultTail";
  import MsRun from "../../../definition/components/Run";
  import {getUUID} from "@/common/js/utils";
  import ApiBaseComponent from "../common/ApiBaseComponent";
  import ApiResponseComponent from "./ApiResponseComponent";

  export default {
    name: "MsApiComponent",
    props: {
      request: {},
      currentScenario: {},
      node: {},
      draggable: {
        type: Boolean,
        default: false,
      },
      currentEnvironmentId: String,
    },
    components: {
      ApiBaseComponent, ApiResponseComponent,
      MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm, MsRequestResultTail, MsRun
    },
    data() {
      return {
        loading: false,
        reqOptions: REQ_METHOD,
        reportId: "",
        runData: [],
        isShowInput: false,
      }
    },
    created() {
      if (!this.request.requestResult) {
        this.request.requestResult = {responseResult: {}};
      }
      // 加载引用对象数据
      this.getApiInfo();
      if (this.request.protocol === 'HTTP') {
        try {
          let urlObject = new URL(this.request.url);
          let url = urlObject.protocol + "//" + urlObject.host + "/";
        } catch (e) {
          if (this.request.url) {
            this.request.path = this.request.url;
            this.request.url = undefined;
          }
        }
        // 历史数据 auth 处理
        if (this.request.hashTree) {
          for (let index in this.request.hashTree) {
            if (this.request.hashTree[index].type == 'AuthManager') {
              this.request.authManager = this.request.hashTree[index];
              this.request.hashTree.splice(index, 1);
            }
          }
        }
      }
    },
    computed: {
      displayColor() {
        if (this.isApiImport) {
          return {
            color: "#F56C6C",
            backgroundColor: "#FCF1F1"
          }
        } else if (this.isExternalImport) {
          return {
            color: "#409EFF",
            backgroundColor: "#EEF5FE"
          }
        } else if (this.isCustomizeReq) {
          return {
            color: "#008080",
            backgroundColor: "#EBF2F2"
          }
        }
        return {};
      },
      displayTitle() {
        if (this.isApiImport) {
          return this.$t('api_test.automation.api_list_import');
        } else if (this.isExternalImport) {
          return this.$t('api_test.automation.external_import');
        } else if (this.isCustomizeReq) {
          return this.$t('api_test.automation.customize_req');
        }
        return "";
      },
      isApiImport() {
        if (this.request.referenced != undefined && this.request.referenced === 'Deleted' || this.request.referenced == 'REF' || this.request.referenced === 'Copy') {
          return true
        }
        return false;
      },
      isExternalImport() {
        if (this.request.referenced != undefined && this.request.referenced === 'OT_IMPORT') {
          return true
        }
        return false;
      },
      isCustomizeReq() {
        if (this.request.referenced == undefined || this.request.referenced === 'Created') {
          return true
        }
        return false;
      },
      isDeletedOrRef() {
        if (this.request.referenced != undefined && this.request.referenced === 'Deleted' || this.request.referenced === 'REF') {
          return true
        }
        return false;
      }
    },
    methods: {
      remove() {
        this.$emit('remove', this.request, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.request, this.node);
      },
      getApiInfo() {
        if (this.request.id && this.request.referenced === 'REF') {
          let requestResult = this.request.requestResult;
          let url = this.request.refType && this.request.refType === 'CASE' ? "/api/testcase/get/" : "/api/definition/get/";
          let enable = this.request.enable;
          this.$get(url + this.request.id, response => {
            if (response.data) {
              Object.assign(this.request, JSON.parse(response.data.request));
              this.request.name = response.data.name;
              this.request.enable = enable;
              if (response.data.path && response.data.path != null) {
                this.request.path = response.data.path;
                this.request.url = response.data.path;
              }
              if (response.data.method && response.data.method != null) {
                this.request.method = response.data.method;
              }
              this.request.requestResult = requestResult;
              this.request.id = response.data.id;
              this.request.disabled = true;
              this.reload();
              this.sort();
            } else {
              this.request.referenced = "Deleted";
            }
          })
        }
      },
      recursiveSorting(arr) {
        for (let i in arr) {
          arr[i].disabled = true;
          arr[i].index = Number(i) + 1;
          if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
            this.recursiveSorting(arr[i].hashTree);
          }
        }
      },
      sort() {
        for (let i in this.request.hashTree) {
          this.request.hashTree[i].disabled = true;
          this.request.hashTree[i].index = Number(i) + 1;
          if (this.request.hashTree[i].hashTree != undefined && this.request.hashTree[i].hashTree.length > 0) {
            this.recursiveSorting(this.request.hashTree[i].hashTree);
          }
        }
      },
      active(item) {
        this.request.active = !this.request.active;
        this.reload();
      },
      run() {
        if (!this.currentEnvironmentId) {
          this.$error(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.request.active = true;
        this.loading = true;
        this.runData = [];
        this.request.useEnvironment = this.currentEnvironmentId;
        let debugData = {
          id: this.currentScenario.id, name: this.currentScenario.name, type: "scenario",
          variables: this.currentScenario.variables, referenced: 'Created', enableCookieShare: this.enableCookieShare,
          environmentId: this.currentEnvironmentId, hashTree: [this.request]
        };
        this.runData.push(debugData);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      },
      runRefresh(data) {
        this.request.requestResult = data;
        this.loading = false;
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
    }
  }
</script>

<style scoped>
  .ms-api-col-ot-import-button {
    background-color: #EEF5FE;
    margin-right: 20px;
    color: #409EFF;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }

  .name-input {
    width: 30%;
  }

  .el-icon-arrow-right {
    margin-right: 5px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }
</style>
