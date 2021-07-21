<template>
  <div>
    <api-base-component
      v-loading="loading"
      @copy="copyRow"
      @remove="remove"
      @active="active"
      :is-show-name-input="!isDeletedOrRef"
      :data="request"
      :draggable="draggable"
      :color="displayColor.color"
      :background-color="displayColor.backgroundColor"
      :is-max="isMax"
      :show-btn="showBtn"
      :title="displayTitle">
      <template v-slot:message>
        <span class="ms-tag ms-step-name-api">{{ getProjectName(request.projectId) }}</span>
      </template>

      <template v-slot:behindHeaderLeft>
        <el-tag size="mini" class="ms-tag" v-if="request.referenced==='Deleted'" type="danger">{{ $t('api_test.automation.reference_deleted') }}</el-tag>
        <el-tag size="mini" class="ms-tag" v-if="request.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
        <el-tag size="mini" class="ms-tag" v-if="request.referenced ==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
      </template>
      <template v-slot:debugStepCode>
         <span class="ms-step-debug-code" :class="request.requestResult[0].success?'ms-req-success':'ms-req-error'" v-if="!loading && request.debug && request.requestResult[0] && request.requestResult[0].responseResult">
          {{ request.requestResult[0].success ? 'success' : 'error' }}
        </span>
      </template>
      <template v-slot:button>
        <el-tooltip :content="$t('api_test.run')" placement="top">
          <el-button :disabled="!request.enable" @click="run" icon="el-icon-video-play" style="padding: 5px" class="ms-btn" size="mini" circle/>
        </el-tooltip>
      </template>

      <!--请求内容-->
      <template v-slot:request>
        <legend style="width: 100%">
          <customize-req-info :is-customize-req="isCustomizeReq" :request="request"/>
          <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
          <ms-api-request-form v-if="request.protocol==='HTTP' || request.type==='HTTPSamplerProxy'"
                               :isShowEnable="true"
                               :referenced="true"
                               :headers="request.headers "
                               :is-read-only="isCompReadOnly"
                               :request="request"/>

          <esb-definition v-if="showXpackCompnent&&request.esbDataStruct!=null"
                          v-xpack
                          :request="request"
                          :showScript="false"
                          :is-read-only="isCompReadOnly" ref="esbDefinition"/>

          <ms-tcp-format-parameters v-if="(request.protocol==='TCP'|| request.type==='TCPSampler')&& request.esbDataStruct==null "
                                    :is-read-only="isCompReadOnly"
                                    :show-script="false" :request="request"/>

          <ms-sql-basis-parameters v-if="request.protocol==='SQL'|| request.type==='JDBCSampler'"
                                   :request="request"
                                   :is-read-only="isCompReadOnly"
                                   :showScript="false"/>

          <ms-dubbo-basis-parameters v-if="request.protocol==='DUBBO' || request.protocol==='dubbo://'|| request.type==='DubboSampler'"
                                     :request="request"
                                     :is-read-only="isCompReadOnly"
                                     :showScript="false"/>
        </legend>
      </template>
      <!-- 执行结果内容 -->
      <template v-slot:result>
        <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
        <div v-if="request.result">
          <el-tabs v-model="request.activeName" closable class="ms-tabs">
            <el-tab-pane :label="item.name" :name="item.name" v-for="(item,index) in request.result.scenarios" :key="index">
              <div v-for="(result,i) in item.requestResults" :key="i" style="margin-bottom: 5px">
                <api-response-component v-if="result.id===request.id" :result="result"/>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div v-else-if="showXpackCompnent&&request.backEsbDataStruct != null">
          <esb-definition-response v-xpack v-if="showXpackCompnent" :currentProtocol="request.protocol" :request="request" :is-api-component="false"
                                   :show-options-button="false" :show-header="true" :result="request.requestResult"/>
        </div>
        <div v-else>
          <div v-for="(item,i) in request.requestResult" :key="i" style="margin-bottom: 5px">
            <api-response-component :currentProtocol="request.protocol" :apiActive="true" :result="item"/>
          </div>
        </div>
      </template>
    </api-base-component>
    <ms-run :debug="true" :reportId="reportId" :run-data="runData" :env-map="envMap"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>

  </div>
</template>

<script>
import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
import MsTcpFormatParameters from "../../../definition/components/request/tcp/TcpFormatParameters";
import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
import MsApiRequestForm from "../../../definition/components/request/http/ApiHttpRequestForm";
import MsRequestResultTail from "../../../definition/components/response/RequestResultTail";
import MsRun from "../../../definition/components/Run";
import {getUUID, getCurrentProjectID} from "@/common/js/utils";
import ApiBaseComponent from "../common/ApiBaseComponent";
import ApiResponseComponent from "./ApiResponseComponent";
import CustomizeReqInfo from "@/business/components/api/automation/scenario/common/CustomizeReqInfo";
import TemplateComponent from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TemplateComponent";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};

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
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    currentEnvironmentId: String,
    projectList: Array,
    expandedNode: Array,
    envMap: Map,
    message: String
  },
  components: {
    TemplateComponent,
    CustomizeReqInfo,
    ApiBaseComponent, ApiResponseComponent,
    MsSqlBasisParameters, MsTcpFormatParameters, MsDubboBasisParameters, MsApiRequestForm, MsRequestResultTail, MsRun,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default
  },
  data() {
    return {
      loading: false,
      reportId: "",
      runData: [],
      isShowInput: false,
      showXpackCompnent: false,
      environment: {},
      result: {},
      apiActive: false,
    }
  },
  created() {
    if (!this.request.requestResult) {
      this.request.requestResult = {responseResult: {}};
    }
    // 跨项目关联，如果没有ID，则赋值本项目ID
    if (!this.request.projectId) {
      this.request.projectId = getCurrentProjectID();
    }
    this.request.customizeReq = this.isCustomizeReq;
    // 加载引用对象数据
    this.getApiInfo();
    if (this.request.protocol === 'HTTP') {
      this.setUrl(this.request.url);
      this.setUrl(this.request.path);
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
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
      this.showXpackCompnent = true;
    }
    this.getEnvironments();
  },
  watch: {
    envMap() {
      this.getEnvironments();
    },
    message() {
      this.reload();
    },
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
    isCompReadOnly() {
      if (this.request) {
        if (this.request.disabled) {
          return this.request.disabled;
        } else {
          return false;
        }
      } else {
        return false;
      }
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
        return true;
      }
      return false;
    },
    isDeletedOrRef() {
      if (this.request.referenced != undefined && this.request.referenced === 'Deleted' || this.request.referenced === 'REF') {
        return true;
      }
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    initDataSource() {
      let databaseConfigsOptions = [];
      if (this.request.protocol === 'SQL' || this.request.type === 'JDBCSampler') {
        if (this.environment && this.environment.config) {
          let config = JSON.parse(this.environment.config);
          if (config && config.databaseConfigs) {
            config.databaseConfigs.forEach(item => {
              databaseConfigsOptions.push(item);
            });
          }
        }
      }
      if (databaseConfigsOptions.length > 0 && this.request.environmentId !== this.environment.id) {
        this.request.dataSourceId = databaseConfigsOptions[0].id;
        this.request.environmentId = this.environment.id;
      }
    },
    getEnvironments() {
      this.environment = {};
      let id = this.envMap.get(this.request.projectId);
      if (id) {
        this.$get('/api/environment/get/' + id, response => {
          this.environment = response.data;
          this.initDataSource();
        });
      }
    },
    remove() {
      this.$emit('remove', this.request, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.request, this.node);
    },
    setUrl(url) {
      try {
        new URL(url);
        this.request.url = url;
      } catch (e) {
        if (url && (!url.startsWith("http://") || !url.startsWith("https://"))) {
          if (!this.isCustomizeReq) {
            this.request.path = url;
            this.request.url = undefined;
          }
        }
      }
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
            this.request.referenced = "REF";
            this.request.enable = enable;
            if (response.data.path && response.data.path != null) {
              this.request.path = response.data.path;
              this.request.url = response.data.url;
              this.setUrl(this.request.path);
            }
            if (response.data.method && response.data.method != null) {
              this.request.method = response.data.method;
            }
            this.request.requestResult = requestResult;
            this.request.id = response.data.id;
            this.request.disabled = true;
            this.request.root = true;
            this.request.projectId = response.data.projectId;
            this.reload();
            this.sort();
          } else {
            this.request.referenced = "Deleted";
          }
        })
      }
    },
    sort(arr) {
      if (!arr) {
        arr = this.request.hashTree;
      }
      for (let i in arr) {
        arr[i].disabled = true;
        arr[i].index = Number(i) + 1;
        if (!arr[i].resourceId) {
          arr[i].resourceId = getUUID();
        }
        if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
          this.sort(arr[i].hashTree);
        }
      }
    },
    active(item) {
      this.request.active = !this.request.active;
      if (this.node) {
        this.node.expanded = this.request.active;
      }
      if (this.node.expanded && this.expandedNode && this.expandedNode.indexOf(this.request.resourceId) === -1) {
        this.expandedNode.push(this.request.resourceId);
      } else {
        if (this.expandedNode && this.expandedNode.indexOf(this.request.resourceId) !== -1) {
          this.expandedNode.splice(this.expandedNode.indexOf(this.request.resourceId), 1);
        }
      }
      this.apiActive = this.request.active;
      this.reload();
    },
    run() {
      if (this.isApiImport || this.request.isRefEnvironment) {
        if (this.request.type && (this.request.type === "HTTPSamplerProxy" || this.request.type === "JDBCSampler" || this.request.type === "TCPSampler")) {
          if (!this.envMap || this.envMap.size === 0) {
            this.$warning(this.$t('api_test.automation.env_message'));
            return false;
          } else if (this.envMap && this.envMap.size > 0) {
            const env = this.envMap.get(this.request.projectId);
            if (!env) {
              this.$warning(this.$t('api_test.automation.env_message'));
              return false;
            }
          }
        }
      }
      this.request.active = true;
      this.loading = true;
      this.runData = [];
      this.runData.projectId = this.request.projectId;
      this.request.useEnvironment = this.currentEnvironmentId;
      this.request.customizeReq = this.isCustomizeReq;
      let debugData = {
        id: this.currentScenario.id, name: this.currentScenario.name, type: "scenario",
        variables: this.currentScenario.variables, referenced: 'Created', headers: this.currentScenario.headers,
        enableCookieShare: this.enableCookieShare, environmentId: this.currentEnvironmentId, hashTree: [this.request],
      };
      this.runData.push(debugData);
      this.request.requestResult = undefined;
      this.request.result = undefined;
      /*触发执行操作*/
      this.reportId = getUUID();
    },
    errorRefresh() {
      this.loading = false;
    },
    runRefresh(data) {
      this.request.requestResult = [data];
      this.request.result = undefined;
      this.loading = false;
      this.$emit('refReload', this.request, this.node);
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    getProjectName(id) {
      if (this.projectId !== id) {
        const project = this.projectList.find(p => p.id === id);
        return project ? project.name : "";
      }

    }
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
  padding: 6px 10px;
}

.icon.is-active {
  transform: rotate(90deg);
}

.ms-tabs >>> .el-icon-close:before {
  content: "";

}

.ms-btn {
  background-color: #409EFF;
  color: white;
}

.ms-btn-flot {
  margin: 20px;
  float: right;
}

.ms-step-name-api {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 60px;
}

.ms-tag {
  margin-left: 10px;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 60px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-req-success {
  color: #67C23A;
}
</style>
