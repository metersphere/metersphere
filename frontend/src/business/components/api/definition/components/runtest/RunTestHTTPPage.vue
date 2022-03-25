<template>

  <div class="card-container">
    <div class="ms-opt-btn" v-if="versionEnable">
      {{ $t('project.version.name') }}: {{ apiData.versionName }}
    </div>
    <el-card class="card-content">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>
        <!-- 请求方法 -->
        <el-form-item :label="$t('api_report.request')" prop="method">
          <el-select v-model="api.request.method" style="width: 100px" size="small">
            <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          <environment-select :current-data="api" :project-id="projectId" ref="environmentSelect"/>
        </el-form-item>

        <!-- 请求地址 -->
        <el-form-item prop="path">
          <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="api.request.path"
                    class="ms-htt-width"
                    size="small" :disabled="false"/>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                       @command="handleCommand" size="small" v-if="!runLoading">
            {{ $t('commons.test') }}
            <el-dropdown-menu slot="dropdown">
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
          </el-dropdown>

          <el-button size="small" type="primary" v-else @click.once="stop">{{ $t('report.stop_btn') }}</el-button>

          <el-button size="small" type="primary" @click.stop @click="generate"
                     style="margin-left: 10px"
                     v-if="hasPermission('PROJECT_API_DEFINITION:READ+CREATE_API') && hasLicense()">
            {{ $t('commons.generate_test_data') }}
          </el-button>

        </el-form-item>


      </el-form>
      <div v-loading="loading">
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :isShowEnable="true" :definition-test="true" :headers="api.request.headers" :response="responseData"
                             v-if="loadRequest"
                             :request="api.request" ref="apiRequestForm"/>
        <!--返回结果-->
        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
        <ms-request-result-tail :response="responseData" ref="runResult"/>
      </div>

    </el-card>

    <!-- 加载用例 -->
    <ms-api-case-list @selectTestCase="selectTestCase" @refresh="refresh"
                      :loaded="loaded"
                      :refreshSign="refreshSign"
                      :createCase="createCase"
                      :currentApi="api"
                      ref="caseList"/>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="api.environment" :reportId="reportId" :run-data="runData" :env-map="envMap"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>

  </div>
</template>

<script>
import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import {getUUID, hasLicense, hasPermission} from "@/common/js/utils";
import MsApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsRequestResultTail from "../response/RequestResultTail";
import MsRun from "../Run";
import {REQ_METHOD} from "../../model/JsonData";
import EnvironmentSelect from "../environment/EnvironmentSelect";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "RunTestHTTPPage",
  components: {
    EnvironmentSelect,
    MsApiRequestForm,
    MsApiCaseList,
    MsContainer,
    MsRequestResultTail,
    MsRun,
  },
  data() {
    return {
      visible: false,
      api: {},
      loaded: false,
      loading: false,
      loadRequest: true,
      createCase: "",
      currentRequest: {},
      refreshSign: "",
      responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
      reqOptions: REQ_METHOD,
      rules: {
        method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}],
        environmentId: [{required: true, message: this.$t('api_test.definition.request.run_env'), trigger: 'change'}],
      },
      runData: [],
      reportId: "",
      envMap: new Map,
      runLoading: false,
      versionEnable: false,
    }
  },
  props: {apiData: {}, currentProtocol: String, syncTabs: Array, projectId: String},
  computed: {
    'api.environmentId'() {
      return this.$store.state.useEnvironment;
    }
  },
  watch: {
    '$store.state.useEnvironment': function () {
      this.api.environmentId = this.$store.state.useEnvironment;
    }
  },
  methods: {
    hasPermission, hasLicense,
    generate() {
      this.$refs.apiRequestForm.generate();
    },
    setRequestParam(param, isEnvironmentMock) {
      this.init();
      if (param) {
        if (param.headers) {
          this.api.request.headers = param.headers;
        }
        if (param.arguments !== null && param.arguments.length > 0) {
          for (let i = 0; i < param.arguments.length; i++) {
            if (!param.arguments[i].required) {
              param.arguments[i].required = true;
            }
          }
          this.api.request.arguments = param.arguments;
        }
        if (param.body) {
          if (param.body.kvs) {
            for (let i = 0; i < param.body.kvs.length; i++) {
              if (!param.body.kvs[i].required) {
                param.body.kvs[i].required = true;
              }
            }
          }

          this.api.request.body = param.body;
        }
        if (param.rest) {
          for (let i = 0; i < param.rest.length; i++) {
            if (!param.rest[i].required) {
              param.rest[i].required = true;
            }
          }
          this.api.request.rest = param.rest;
        }
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
      this.loadRequest = false;
      this.$nextTick(() => {
        this.loadRequest = true;
      })
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
          return this.runTest();
      }
    },
    runTest() {
      this.$refs['apiData'].validate((valid) => {
        if (valid) {
          this.runLoading = true;
          this.loading = true;
          this.api.request.name = this.api.id;
          this.api.request.url = undefined;
          this.api.request.useEnvironment = this.api.environmentId;
          this.api.protocol = this.currentProtocol;
          this.runData = [];
          this.runData.push(this.api.request);
          /*触发执行操作*/
          this.reportId = getUUID().substring(0, 8);
        }
      })
    },
    errorRefresh() {
      this.loading = false;
      this.runLoading = false;
    },
    runRefresh(data) {
      this.responseData = {type: 'HTTP', responseResult: {responseCode: ""}, subRequestResults: []};
      if (data) {
        this.responseData = data;
      }
      this.loading = false;
      this.runLoading = false;
    },
    saveAs() {
      this.$emit('saveAs', this.api);
    },
    loadCase() {
      this.refreshSign = getUUID();
      this.loaded = true;
      this.$refs.caseList.open();
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
                item.name = item.file.name;
                bodyUploadFiles.push(item.file);
              }
            });
          }
        });
      }
      return bodyUploadFiles;
    },
    saveAsCase() {
      //用于触发创建操作
      this.$emit('saveAsCase', this.api);
    },
    saveAsApi() {
      let data = {};
      let req = this.api.request;
      req.id = getUUID();
      data.request = JSON.stringify(req);
      data.method = req.method;
      data.path = req.path;
      data.url = this.api.url;
      data.status = this.api.status;
      data.userId = this.api.userId;
      data.description = this.api.description;
      this.$emit('saveAsApi', data);
    },
    compatibleHistory(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
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
      this.api.method = this.api.request.method;
      this.api.path = this.api.request.path;
      if (Object.prototype.toString.call(this.api.response).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
        this.api.response = JSON.parse(this.api.response);
      }
      if (this.api.tags instanceof Array) {
        this.api.tags = JSON.stringify(this.api.tags);
      }
      // 历史数据兼容处理
      if (this.api.request) {
        this.api.request.clazzName = TYPE_TO_C.get(this.api.request.type);
        this.compatibleHistory(this.api.request.hashTree);
      }
      this.$fileUpload(url, null, bodyFiles, this.api, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('saveApi', this.api);
        if (this.syncTabs.indexOf(this.api.id) === -1) {
          this.syncTabs.push(this.api.id);
        }
      });
    },
    selectTestCase(item) {
      if (item != null) {
        this.api.request = item.request;
      } else {
        this.api.request = this.currentRequest;
      }
    },

    refresh() {
      this.$emit('refresh');
    },
    getResult() {
      if (this.api.id) {
        let url = "/api/definition/report/getReport/" + this.api.id;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.responseData = data;
          }
        });
      }
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
      this.currentRequest = this.api.request;
      if (!this.api.environmentId && this.$store.state.useEnvironment) {
        this.api.environmentId = this.$store.state.useEnvironment;
      }
      this.runLoading = false;
      this.checkVersionEnable();
    }
  },
  created() {
    this.init();
  }
}
</script>

<style scoped>
.ms-htt-width {
  width: 350px;
}

/deep/ .el-drawer {
  overflow: auto;
}
</style>
