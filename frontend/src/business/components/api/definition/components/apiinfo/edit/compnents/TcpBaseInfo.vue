<template>

  <div class="card-container">
    <div v-if="versionEnable">
      <slot name="rightHeader"></slot>
    </div>
    <el-card class="card-content">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          {{ $t('api_test.definition.request.run_env') }}：
          <environment-select :type="'TCP'" :current-data="api" :project-id="projectId" ref="environmentSelect"/>
        </el-form-item>
        <el-form-item>
          <el-button size="small" type="primary" v-if="apiData.operationType==='create'||apiData.operationType==='edit'"
                     @click="runTest">
            {{ $t('commons.test') }}
          </el-button>
          <!-- 操作按钮 -->
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                       @command="handleCommand" size="small" v-else-if="!runLoading">
            {{ $t('commons.test') }}
            <el-dropdown-menu slot="dropdown" v-if="isEdit">
              <el-dropdown-item command="load_case">{{ $t('api_test.definition.request.load_case') }}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_case">{{ $t('api_test.definition.request.save_as_case') }}
              </el-dropdown-item>
              <el-dropdown-item command="update_api">{{
                  $t('api_test.definition.request.update_api')
                }}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_api">{{ $t('api_test.definition.request.save_as') }}</el-dropdown-item>
            </el-dropdown-menu>
            <el-dropdown-menu slot="dropdown" v-else>
              <el-dropdown-item command="save_as_case">{{ $t('api_test.definition.request.save_as_case') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button size="small" type="primary" v-else @click.once="stop">
            {{ $t('report.stop_btn') }}
          </el-button>
        </el-form-item>

        <div>
          <p class="tip" style="display: inline-block">{{ $t('api_test.definition.request.req_param') }} </p>
          <el-select v-model="api.method" slot="prepend" style="width: 100px" size="small">
            <el-option v-for="item in methodTypes" :key="item.key" :label="item.value" :value="item.key"/>
          </el-select>
        </div>
        <!-- TCP 请求参数 -->
        <div v-if="api.method=='TCP'" v-loading="loading">
          <ms-tcp-format-parameters :show-script="false" :request="api.request" @callback="runTest"
                                    :response="responseData"
                                    ref="requestForm"/>
          <ms-request-result-tail :response="responseData" ref="runResult"/>
        </div>
        <div v-else-if="api.method=='ESB'" v-loading="loading">
          <esb-definition v-xpack v-if="showXpackCompnent" :show-script="false" :request="api.request"
                          @callback="runTest" ref="requestForm"/>
        </div>
        <div v-if="api.method=='ESB'">
          <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
          <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="false" :show-options-button="false"
                                   :request="api.request" :response-data="responseData"/>
        </div>
      </el-form>
      <api-other-info :api="api" ref="apiOtherInfo"/>
    </el-card>

    <!-- 加载用例 -->
    <ms-api-case-list @apiCaseClose="apiCaseClose" @refresh="refresh" @selectTestCase="selectTestCase" :currentApi="api"
                      :refreshSign="refreshSign"
                      :loaded="loaded" :createCase="createCase"
                      ref="caseList"/>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="api.environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>

  </div>
</template>

<script>
import {getUUID, hasLicense} from "@/common/js/utils";

import MsTcpFormatParameters from "@/business/components/api/definition/components/request/tcp/TcpFormatParameters";
import MsApiRequestForm from "@/business/components/api/definition/components/request/http/ApiHttpRequestForm";
import MsApiCaseList from "@/business/components/api/definition/components/case/ApiCaseList";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsBottomContainer from "@/business/components/api/definition/components/BottomContainer";
import MsRequestResultTail from "@/business/components/api/definition/components/response/RequestResultTail";
import MsRun from "@/business/components/api/definition/components/Run";
import {REQ_METHOD} from "@/business/components/api/definition/model/JsonData";
import EnvironmentSelect from "@/business/components/api/definition/components/environment/EnvironmentSelect";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
export default {
  name: "TcpBaseInfo",
  components: {
    EnvironmentSelect,
    MsApiRequestForm,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    MsRequestResultTail,
    MsRun,
    MsTcpFormatParameters, ApiOtherInfo,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default
  },
  data() {
    return {
      visible: false,
      api: {},
      loaded: false,
      loading: false,
      currentRequest: {},
      responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
      reqOptions: REQ_METHOD,
      environments: [],
      refreshSign: "",
      createCase: "",
      methodTypes: [
        {
          'key': "TCP",
          'value': this.$t('api_test.request.tcp.general_format'),
        }
      ],
      rules: {
        environmentId: [{required: true, message: this.$t('api_test.definition.request.run_env'), trigger: 'change'}],
      },
      runData: [],
      reportId: "",
      showXpackCompnent: false,
      runLoading: false,
      versionEnable: false,
    }
  },
  props: {apiData: {}, currentProtocol: String, syncTabs: Array, projectId: String, isEdit: Boolean},
  methods: {
    setRequestParam(param, isEnvironmentMock) {
      this.init();
      if (this.api.method === "TCP" && param && this.api.request) {
        if (param.reportType) {
          this.api.request.reportType = param.reportType;
        }
        if (param.jsonDataStruct) {
          this.api.request.jsonDataStruct = param.jsonDataStruct;
        }
        if (param.rawDataStruct) {
          this.api.request.rawDataStruct = param.rawDataStruct;
        }
        if (param.xmlDataStruct) {
          this.api.request.xmlDataStruct = param.xmlDataStruct;
        }
        this.$nextTick(() => {
          this.$refs.requestForm.reload();
          this.$refs.requestForm.setReportType(param.reportType);
        });
      }
      if (isEnvironmentMock) {
        this.$nextTick(() => {
          let url = "/api/definition/getMockEnvironment/";
          this.$get(url + this.projectId, response => {
            let mockEnvironment = response.data;
            if (mockEnvironment !== null) {
              this.$refs.environmentSelect.setEnvironment(mockEnvironment.id);
            }
          });
        })
      }
    },
    handleCommand(e) {
      switch (e) {
        case "load_case":
          return this.loadCase();
        case "save_as_case":
          return this.saveAsCase();
        case "update_api":
          return this.updateApi();
        case "save_as_api":
          return this.saveAsApi();
        default:
          return this.$refs['requestForm'].validate();
      }
    },
    refresh() {
      this.$emit('refresh');
    },
    runTest() {
      this.$refs['apiData'].validate((valid) => {
        if (valid) {
          this.runLoading = true;
          this.loading = true;
          this.api.request.name = this.api.id;
          this.api.protocol = this.currentProtocol;
          this.runData = [];
          this.runData.push(this.api.request);
          /*触发执行操作*/
          this.reportId = getUUID().substring(0, 8);
        }
      })
    },
    runRefresh(data) {
      this.responseData = data;
      this.loading = false;
      this.runLoading = false;
    },
    errorRefresh() {
      this.loading = false;
      this.runLoading = false;
    },
    saveAs() {
      this.$emit('saveAs', this.api);
    },
    loadCase() {
      this.refreshSign = getUUID();
      this.$refs.caseList.open();
      this.visible = true;
    },
    apiCaseClose() {
      this.visible = false;
    },
    getBodyUploadFiles() {
      let bodyUploadFiles = [];
      this.api.bodyUploadIds = [];
      let request = this.api.request;
      if (request.body) {
        request.body.kvs.forEach(param => {
          if (param.files) {
            param.files.forEach(item => {
              if (item.file) {
                let fileId = getUUID().substring(0, 8);
                item.name = item.file.name;
                item.id = fileId;
                this.api.bodyUploadIds.push(fileId);
                bodyUploadFiles.push(item.file);
              }
            });
          }
        });
      }
      return bodyUploadFiles;
    },
    saveAsCase() {
      this.$emit('saveApiAndCase', this.api);
    },
    saveAsApi() {
      let data = {};
      let req = this.api.request;
      req.id = getUUID();
      data.request = JSON.stringify(req);
      data.method = this.api.method;
      data.status = this.api.status;
      data.userId = this.api.userId;
      data.description = this.api.description;
      this.$emit('saveAsApi', data);
      this.$emit('refresh');
    },
    compatibleHistory(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.compatibleHistory(stepArray[i].hashTree);
          }
        }
      }
    },
    updateApi() {
      let url = "/api/definition/update";
      let bodyFiles = this.getBodyUploadFiles();
      if (Object.prototype.toString.call(this.api.response).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
        this.api.response = JSON.parse(this.api.response);
      }
      if (this.api.tags instanceof Array) {
        this.api.tags = JSON.stringify(this.api.tags);
      }
      if (this.api.method === 'ESB') {
        this.api.esbDataStruct = JSON.stringify(this.api.request.esbDataStruct);
        this.api.backEsbDataStruct = JSON.stringify(this.api.request.backEsbDataStruct);
      }
      // 历史数据兼容处理
      if (this.api.request) {
        this.api.request.clazzName = TYPE_TO_C.get(this.api.request.type);
        this.compatibleHistory(this.api.request.hashTree);
      }
      this.$fileUpload(url, null, bodyFiles, this.api, () => {
        this.$success(this.$t('commons.save_success'));
        if (this.syncTabs.indexOf(this.api.id) === -1) {
          this.syncTabs.push(this.api.id);
        }
        this.$emit('saveApi', this.api);
      });
    },
    selectTestCase(item) {
      if (item != null) {
        this.api.request = item.request;
      } else {
        this.api.request = this.currentRequest;
      }
    },
    getResult() {
      let url = "/api/definition/report/getReport/" + this.api.id;
      this.$get(url, response => {
        if (response.data) {
          let data = JSON.parse(response.data.content);
          this.responseData = data;
        }
      });
    },
    stop() {
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.runLoading = false;
        this.loading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
        });
      }
    },
    init() {
      // 深度复制
      this.api = JSON.parse(JSON.stringify(this.apiData));
      this.api.protocol = this.currentProtocol;
      if (!this.api.environmentId) {
        this.api.environmentId = this.$store.state.useEnvironment;
      }
      this.currentRequest = this.api.request;
      this.runLoading = false;
      this.getResult();
      if (requireComponent != null && JSON.stringify(esbDefinition) !== '{}') {
        this.showXpackCompnent = true;
      }
      this.checkVersionEnable();
    },
    getApi() {
      if (this.api.method === 'ESB') {
        this.api.esbDataStruct = JSON.stringify(this.api.request.esbDataStruct);
        this.api.backEsbDataStruct = JSON.stringify(this.api.request.backEsbDataStruct);
      }
      return this.api;
    }
  },
  created() {
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
    this.init();
  }
}
</script>

<style scoped>
.ms-htt-width {
  width: 350px;
}

.environment-button {
  margin-left: 20px;
  padding: 7px;
}

/deep/ .el-drawer {
  overflow: auto;
}
</style>
