<template>
  <div class="card-container">
    <div class="ms-opt-btn" v-if="versionEnable">{{ $t('project.version.name') }}: {{ apiData.versionName }}</div>
    <el-card class="card-content">
      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">
        <p class="tip">{{ $t('test_track.plan_view.base_info') }}</p>
        <!-- 请求方法 -->
        <el-form-item :label="$t('api_report.request')" prop="method">
          <el-select v-model="api.request.method" style="width: 100px" size="small">
            <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          <environment-select :current-data="api" :project-id="projectId" ref="environmentSelect" />
        </el-form-item>

        <!-- 请求地址 -->
        <el-form-item prop="path">
          <el-input
            :placeholder="$t('api_test.definition.request.path_info')"
            v-model="api.request.path"
            class="ms-htt-width"
            size="small"
            :disabled="false" />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-dropdown
            split-button
            type="primary"
            @click="handleCommand('add')"
            @command="handleCommand"
            size="small"
            v-if="!runLoading"
            v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API', 'PROJECT_API_DEFINITION:READ+CREATE_CASE']">
            {{ $t('commons.test') }}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="load_case"
                >{{ $t('api_test.definition.request.load_case') }}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_case"
                >{{ $t('api_test.definition.request.save_as_case') }}
              </el-dropdown-item>
              <el-dropdown-item command="update_api"
                >{{ $t('api_test.definition.request.update_api') }}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_api">{{ $t('api_test.definition.request.save_as') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

          <el-button size="small" type="primary" v-else @click.once="stop">{{ $t('report.stop_btn') }}</el-button>

          <el-button
            size="small"
            type="primary"
            @click.stop
            @click="generate"
            style="margin-left: 10px"
            v-if="hasPermission('PROJECT_API_DEFINITION:READ+CREATE_API')">
            {{ $t('commons.generate_test_data') }}
          </el-button>
        </el-form-item>
      </el-form>
      <div v-loading="loading">
        <p class="tip">{{ $t('api_test.definition.request.req_param') }}</p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form
          :isShowEnable="true"
          :definition-test="true"
          :headers="api.request.headers"
          :response="responseData"
          v-if="loadRequest"
          :request="api.request"
          ref="apiRequestForm" />
        <!--返回结果-->
        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
        <ms-request-result-tail :response="responseData" ref="runResult" />
      </div>
    </el-card>
    <api-sync-case-config
      :is-xpack="isXpack"
      :api-sync-rule-relation="apiSyncRuleRelation"
      :batch-sync-api-visible="batchSyncApiVisible"
      :show-api-sync-config="true"
      @batchSync="batchSync"
      ref="syncCaseConfig">
    </api-sync-case-config>
    <!-- 加载用例 -->
    <ms-api-case-list
      @selectTestCase="selectTestCase"
      @refresh="refresh"
      :loaded="loaded"
      :refreshSign="refreshSign"
      :createCase="createCase"
      :currentApi="api"
      :save-button-text="loadCaseConfirmButton"
      ref="caseList" />

    <!-- 执行组件 -->
    <ms-run
      :debug="false"
      :reportId="reportId"
      :run-data="runData"
      :env-map="envMap"
      @runRefresh="runRefresh"
      @errorRefresh="errorRefresh"
      ref="runTest" />
  </div>
</template>

<script>
import { citedApiScenarioCount, getDefinitionVersions, getMockEnvironment, updateDefinition } from '@/api/definition';
import { relationGet, updateRuleRelation, versionEnableByProjectId } from '@/api/xpack';
import MsApiRequestForm from '../request/http/ApiHttpRequestForm';
import { hasLicense, hasPermission } from 'metersphere-frontend/src/utils/permission';
import { getUUID } from 'metersphere-frontend/src/utils';
import MsApiCaseList from '../case/EditApiCase';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsRequestResultTail from '../response/RequestResultTail';
import MsRun from '../Run';
import { REQ_METHOD } from '../../model/JsonData';
import EnvironmentSelect from '@/business/environment/components/EnvironmentSelect';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import { mergeRequestDocumentData } from '@/business/definition/api-definition';
import { execStop } from '@/api/scenario';
import { useApiStore } from '@/store';
import { apiTestCaseCount } from '@/api/api-test-case';
import ApiSyncCaseConfig from '@/business/definition/components/sync/ApiSyncCaseConfig';
import { deepClone } from 'metersphere-frontend/src/utils/tableUtils';

const store = useApiStore();
export default {
  name: 'RunTestHTTPPage',
  components: {
    EnvironmentSelect,
    MsApiRequestForm,
    MsApiCaseList,
    MsContainer,
    MsRequestResultTail,
    MsRun,
    ApiSyncCaseConfig,
  },
  data() {
    let validateURL = (rule, value, callback) => {
      if (!this.api.request.path || !this.api.request.path.startsWith('/')) {
        callback(this.$t('api_test.definition.request.path_valid_info'));
      }
      callback();
    };
    return {
      visible: false,
      api: {},
      loaded: false,
      loading: false,
      loadRequest: true,
      createCase: '',
      currentRequest: {},
      refreshSign: '',
      loadCaseConfirmButton: this.$t('commons.confirm'),
      responseData: { type: 'HTTP', responseResult: {}, subRequestResults: [] },
      reqOptions: REQ_METHOD,
      rules: {
        method: [
          {
            required: true,
            message: this.$t('test_track.case.input_maintainer'),
            trigger: 'change',
          },
        ],
        path: [
          {
            required: true,
            message: this.$t('api_test.definition.request.path_info'),
            trigger: 'blur',
            validator: validateURL,
          },
        ],
      },
      runData: [],
      reportId: '',
      envMap: new Map(),
      runLoading: false,
      versionEnable: false,
      beforeHttpForm: { environmentId: '', path: '', tags: [] },
      beforeRequest: { arguments: [] },
      beforeResponse: {},
      citedScenarioCount: 0,
      apiSyncRuleRelation: {
        caseCreator: true,
        scenarioCreator: true,
        showUpdateRule: false,
        apiSyncCaseRequest: '',
        apiSyncConfig: {},
        syncCase: true,
        sendNotice: true,
      },
      noShowSyncRuleRelation: false,
      batchSyncApiVisible: false,
      isXpack: false,
    };
  },
  props: {
    apiData: {},
    currentProtocol: String,
    syncTabs: Array,
    projectId: String,
  },
  computed: {
    'api.environmentId'() {
      return store.useEnvironment;
    },
    storeUseEnvironment() {
      return store.useEnvironment;
    },
  },
  watch: {
    storeUseEnvironment: function () {
      this.api.environmentId = store.useEnvironment;
    },
    batchSyncApiVisible() {
      if (!this.batchSyncApiVisible && this.apiSyncRuleRelation.showUpdateRule) {
        this.noShowSyncRuleRelation = true;
      }
    },
  },
  methods: {
    hasPermission,
    hasLicense,
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
          getMockEnvironment(this.projectId).then((response) => {
            let mockEnvironment = response.data;
            if (mockEnvironment !== null) {
              this.$refs.environmentSelect.setEnvironment(mockEnvironment.id);
            }
          });
        });
      }
      this.loadRequest = false;
      this.$nextTick(() => {
        this.loadRequest = true;
      });
    },
    handleCommand(e) {
      mergeRequestDocumentData(this.api.request);
      switch (e) {
        case 'load_case':
          return this.loadCase();
        case 'save_as_case':
          return this.saveAsCase();
        case 'update_api':
          return this.updateApi();
        case 'save_as_api':
          return this.saveAsApi();
        default:
          return this.runTest();
      }
    },
    runTest() {
      if (!this.api.environmentId) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
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
      });
    },
    errorRefresh() {
      this.loading = false;
      this.runLoading = false;
    },
    runRefresh(data) {
      this.responseData = {
        type: 'HTTP',
        responseResult: { responseCode: '' },
        subRequestResults: [],
      };
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
    getBodyUploadFiles(data) {
      let bodyUploadFiles = [];
      data.bodyUploadIds = [];
      let request = data.request;
      if (request.body) {
        if (request.body.kvs) {
          request.body.kvs.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
                if (item.file) {
                  item.name = item.file.name;
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        if (request.body.binary) {
          request.body.binary.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
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
      data.moduleId = this.api.moduleId;
      data.modulePath = this.api.modulePath;
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
      let bodyFiles = this.getBodyUploadFiles(this.api);
      this.api.method = this.api.request.method;
      this.api.path = this.api.request.path;
      if (
        Object.prototype.toString
          .call(this.api.response)
          .match(/\[object (\w+)\]/)[1]
          .toLowerCase() !== 'object'
      ) {
        this.api.response = JSON.parse(this.api.response);
      }
      if (this.api.tags instanceof Array) {
        this.api.tags = JSON.stringify(this.api.tags);
      }
      if (this.beforeHttpForm) {
        if (this.beforeHttpForm.tags instanceof Array) {
          this.beforeHttpForm.tags = JSON.stringify(this.beforeHttpForm.tags);
        }
      }
      // 历史数据兼容处理
      if (this.api.request) {
        this.api.request.clazzName = TYPE_TO_C.get(this.api.request.type);
        this.compatibleHistory(this.api.request.hashTree);
      }
      if (hasLicense() && (this.api.caseTotal > 0 || this.citedScenarioCount > 0)) {
        if (this.api.method !== this.beforeHttpForm.method && !this.noShowSyncRuleRelation) {
          this.batchSyncApiVisible = true;
          this.$refs.syncCaseConfig.show();
        }
        if (this.api.path !== this.beforeHttpForm.path && !this.noShowSyncRuleRelation) {
          this.batchSyncApiVisible = true;
          this.$refs.syncCaseConfig.show();
        }
        if (this.api.request.headers && this.beforeRequest.headers) {
          if (this.api.request.headers.length === this.beforeRequest.headers.length) {
            let requestHeaders = [];
            let beforeHeaders = [];
            for (let i = 0; i < this.api.request.headers.length; i++) {
              this.beforeRequest.headers[i].valid = this.api.request.headers[i].valid;
              if (this.api.request.headers[i].isEdit !== undefined) {
                this.beforeRequest.headers[i].isEdit = this.api.request.headers[i].isEdit;
              }
              if (this.api.request.headers[i].uuid) {
                this.beforeRequest.headers[i].uuid = this.api.request.headers[i].uuid;
              }
              if (this.api.request.headers[i].time) {
                this.beforeRequest.headers[i].time = this.api.request.headers[i].time;
              }
              if (this.api.request.headers[i].name === undefined) {
                this.beforeRequest.headers[i].name = undefined;
              }
              let newRequest = this.api.request.headers[i];
              const ordered = {};
              Object.keys(newRequest)
                .sort()
                .forEach(function (key) {
                  ordered[key] = newRequest[key];
                });
              requestHeaders.push(ordered);
              let beforeRequest = this.beforeRequest.headers[i];
              const beforeOrdered = {};
              Object.keys(beforeRequest)
                .sort()
                .forEach(function (key) {
                  beforeOrdered[key] = beforeRequest[key];
                });
              beforeHeaders.push(beforeOrdered);
            }

            let submitRequestHeaders = JSON.stringify(requestHeaders);
            let beforeRequestHeaders = JSON.stringify(beforeHeaders);
            if (submitRequestHeaders !== beforeRequestHeaders && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          } else {
            let submitRequestHeaders = JSON.stringify(this.api.request.headers);
            let beforeRequestHeaders = JSON.stringify(this.beforeRequest.headers);
            if (submitRequestHeaders !== beforeRequestHeaders && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          }
        }
        if (this.api.request.arguments && this.beforeRequest.arguments) {
          if (this.api.request.arguments.length === this.beforeRequest.arguments.length) {
            let requestArguments = [];
            let beforeArguments = [];
            for (let i = 0; i < this.api.request.arguments.length; i++) {
              if (this.api.request.arguments[i].isEdit !== undefined) {
                this.beforeRequest.arguments[i].isEdit = this.api.request.arguments[i].isEdit;
              }
              if (this.api.request.arguments[i].uuid) {
                this.beforeRequest.arguments[i].uuid = this.api.request.arguments[i].uuid;
              }
              if (this.api.request.arguments[i].time) {
                this.beforeRequest.arguments[i].time = this.api.request.arguments[i].time;
              }
              if (this.api.request.arguments[i].name === undefined) {
                this.beforeRequest.arguments[i].name = undefined;
              }
              this.beforeRequest.arguments[i].valid = this.api.request.arguments[i].valid;
              let newRequest = this.api.request.arguments[i];
              const ordered = {};
              Object.keys(newRequest)
                .sort()
                .forEach(function (key) {
                  ordered[key] = newRequest[key];
                });
              requestArguments.push(ordered);
              let beforeRequest = this.beforeRequest.arguments[i];
              const beforeOrdered = {};
              Object.keys(beforeRequest)
                .sort()
                .forEach(function (key) {
                  beforeOrdered[key] = beforeRequest[key];
                });
              beforeArguments.push(beforeOrdered);
            }
            let submitRequestQuery = JSON.stringify(requestArguments);
            let beforeRequestQuery = JSON.stringify(beforeArguments);
            if (submitRequestQuery !== beforeRequestQuery && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          } else {
            let submitRequestQuery = JSON.stringify(this.api.request.arguments);
            let beforeRequestQuery = JSON.stringify(this.beforeRequest.arguments);
            if (submitRequestQuery !== beforeRequestQuery && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          }
        }
        if (this.api.request.rest && this.beforeRequest.rest) {
          if (this.api.request.rest.length === this.beforeRequest.rest.length) {
            let requestRest = [];
            let beforeRest = [];
            for (let i = 0; i < this.api.request.rest.length; i++) {
              if (this.api.request.rest[i].isEdit !== undefined) {
                this.beforeRequest.rest[i].isEdit = this.api.request.rest[i].isEdit;
              }
              if (this.api.request.rest[i].uuid) {
                this.beforeRequest.rest[i].uuid = this.api.request.rest[i].uuid;
              }
              if (this.api.request.rest[i].time) {
                this.beforeRequest.rest[i].time = this.api.request.rest[i].time;
              }
              if (this.api.request.rest[i].name === undefined) {
                this.beforeRequest.rest[i].name = undefined;
              }
              this.beforeRequest.rest[i].valid = this.api.request.rest[i].valid;

              let newRequest = this.api.request.rest[i];
              const ordered = {};
              Object.keys(newRequest)
                .sort()
                .forEach(function (key) {
                  ordered[key] = newRequest[key];
                });
              requestRest.push(ordered);
              let beforeRequest = this.beforeRequest.rest[i];
              const beforeOrdered = {};
              Object.keys(beforeRequest)
                .sort()
                .forEach(function (key) {
                  beforeOrdered[key] = beforeRequest[key];
                });
              beforeRest.push(beforeOrdered);
            }
            let submitRequestRest = JSON.stringify(requestRest);
            let beforeRequestRest = JSON.stringify(beforeRest);
            if (submitRequestRest !== beforeRequestRest && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          } else {
            let submitRequestRest = JSON.stringify(this.api.request.rest);
            let beforeRequestRest = JSON.stringify(this.beforeRequest.rest);
            if (submitRequestRest !== beforeRequestRest && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
          }
        }
        if (this.api.request.body && this.beforeRequest.body) {
          if (this.api.request.body.valid) {
            this.beforeRequest.body.valid = this.api.request.body.valid;
          }
          if (this.api.request.body.kvs.length === this.beforeRequest.body.kvs.length) {
            let requestKvs = [];
            let beforeKvs = [];
            for (let i = 0; i < this.api.request.body.kvs.length; i++) {
              if (this.api.request.body.kvs[i].isEdit !== undefined) {
                this.beforeRequest.body.kvs[i].isEdit = this.api.request.body.kvs[i].isEdit;
              }
              if (this.api.request.body.kvs[i].files !== null && this.api.request.body.kvs[i].files.length === 0) {
                this.beforeRequest.body.kvs[i].files = this.api.request.body.kvs[i].files;
              }
              if (this.api.request.body.kvs[i].uuid) {
                this.beforeRequest.body.kvs[i].uuid = this.api.request.body.kvs[i].uuid;
              }
              if (this.api.request.body.kvs[i].time) {
                this.beforeRequest.body.kvs[i].time = this.api.request.body.kvs[i].time;
              }
              if (this.api.request.body.kvs[i].name === undefined) {
                this.beforeRequest.body.kvs[i].name = undefined;
              }
              this.beforeRequest.body.kvs[i].valid = this.api.request.body.kvs[i].valid;

              let newRequest = this.api.request.body.kvs[i];
              const ordered = {};
              Object.keys(newRequest)
                .sort()
                .forEach(function (key) {
                  ordered[key] = newRequest[key];
                });
              requestKvs.push(ordered);
              let beforeRequest = this.api.request.body.kvs[i];
              const beforeOrdered = {};
              Object.keys(beforeRequest)
                .sort()
                .forEach(function (key) {
                  beforeOrdered[key] = beforeRequest[key];
                });
              beforeKvs.push(beforeOrdered);
            }
            this.api.request.body.kvs = requestKvs;
            this.beforeRequest.body.kvs = beforeKvs;
          }
          let submitRequestBody = JSON.stringify(this.api.request.body);
          let beforeRequestBody = JSON.stringify(this.beforeRequest.body);
          if (submitRequestBody !== beforeRequestBody && !this.noShowSyncRuleRelation) {
            this.batchSyncApiVisible = true;
            this.$refs.syncCaseConfig.show();
          }
        }
        if (this.api.request.authManager && this.beforeRequest.authManager) {
          let submitRequestAuthManager = JSON.stringify(this.api.request.authManager);
          let beforeRequestAuthManager = JSON.stringify(this.beforeRequest.authManager);
          if (submitRequestAuthManager !== beforeRequestAuthManager && !this.noShowSyncRuleRelation) {
            this.batchSyncApiVisible = true;
            this.$refs.syncCaseConfig.show();
          }
        }
        if (this.api.request.hashTree && this.beforeRequest.hashTree) {
          let submitRequestHashTree = JSON.stringify(this.api.request.hashTree);
          let beforeRequestHashTree = JSON.stringify(this.beforeRequest.hashTree);
          if (submitRequestHashTree !== beforeRequestHashTree && !this.noShowSyncRuleRelation) {
            this.batchSyncApiVisible = true;
            this.$refs.syncCaseConfig.show();
          }
        }
        if (
          (this.api.request.connectTimeout !== this.beforeRequest.connectTimeout ||
            this.api.request.responseTimeout !== this.beforeRequest.responseTimeout ||
            this.api.request.followRedirects !== this.beforeRequest.followRedirects ||
            this.api.request.alias !== this.beforeRequest.alias ||
            this.apiSyncRuleRelation.showUpdateRule === true) &&
          !this.noShowSyncRuleRelation
        ) {
          this.batchSyncApiVisible = true;
          this.$refs.syncCaseConfig.show();
        }
        if (this.batchSyncApiVisible !== true) {
          updateDefinition(null, null, bodyFiles, this.api).then(() => {
            this.$success(this.$t('commons.save_success'));
            if (this.syncTabs.indexOf(this.api.id) === -1) {
              this.syncTabs.push(this.api.id);
              this.$emit('syncApi', this.api);
            }
          });
        }
      } else {
        updateDefinition(null, null, bodyFiles, this.api).then(() => {
          this.$success(this.$t('commons.save_success'));
          if (this.syncTabs.indexOf(this.api.id) === -1) {
            this.syncTabs.push(this.api.id);
            this.$emit('syncApi', this.api);
          }
        });
      }
    },
    batchSync(fromData) {
      this.beforeHttpForm = deepClone(this.api);
      this.beforeRequest = deepClone(this.api.request);
      this.beforeResponse = deepClone(this.api.response);
      this.batchSyncApiVisible = false;
      if (hasLicense() && (this.api.caseTotal > 0 || this.citedScenarioCount > 0)) {
        this.api.triggerUpdate = JSON.stringify(fromData);
        this.apiSyncRuleRelation.apiSyncCaseRequest = JSON.stringify(fromData);
        if (this.apiSyncRuleRelation.sendNotice && this.apiSyncRuleRelation.sendNotice === true) {
          this.api.sendSpecialMessage = this.apiSyncRuleRelation.sendNotice;
        } else {
          this.api.sendSpecialMessage = false;
        }

        if (this.apiSyncRuleRelation.caseCreator && this.apiSyncRuleRelation.caseCreator === true) {
          this.api.caseCreator = this.apiSyncRuleRelation.caseCreator;
        } else {
          this.api.caseCreator = false;
        }
        if (this.apiSyncRuleRelation.scenarioCreator && this.apiSyncRuleRelation.scenarioCreator === true) {
          this.api.scenarioCreator = this.apiSyncRuleRelation.scenarioCreator;
        } else {
          this.api.scenarioCreator = false;
        }
        this.apiSyncRuleRelation.resourceId = this.api.id;
        this.apiSyncRuleRelation.resourceType = 'API';
        this.saveApiSyncRuleRelation(this.apiSyncRuleRelation);
      }
    },
    saveApiSyncRuleRelation(apiSyncRuleRelation) {
      let bodyFiles = this.getBodyUploadFiles(this.api);
      updateRuleRelation(apiSyncRuleRelation.resourceId, apiSyncRuleRelation).then(() => {
        updateDefinition(null, null, bodyFiles, this.api).then(() => {
          this.$success(this.$t('commons.save_success'));
          if (this.syncTabs.indexOf(this.api.id) === -1) {
            this.syncTabs.push(this.api.id);
            this.$emit('syncApi', this.api);
          }
        });
        this.$refs.syncCaseConfig.close();
      });
    },
    getCitedScenarioCount() {
      citedApiScenarioCount(this.api.id).then((response) => {
        if (response.data) {
          this.citedScenarioCount = response.data;
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
    stop() {
      execStop(this.reportId).then(() => {
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
        versionEnableByProjectId(this.projectId).then((response) => {
          this.versionEnable = response.data;
        });
      }
    },
    init() {
      // 深度复制
      this.api = JSON.parse(JSON.stringify(this.apiData));
      this.initLocalFile();
      this.api.protocol = this.currentProtocol;
      this.currentRequest = this.api.request;
      if (!this.api.environmentId && store.useEnvironment) {
        this.api.environmentId = store.useEnvironment;
      }
      // getLastResultDetail(this.api.id, this);
      this.runLoading = false;
      this.checkVersionEnable();
      this.getCaseCount();
      if (hasLicense()) {
        this.isXpack = true;
        this.getOldVersionData();
        this.getSyncRule();
        this.getCitedScenarioCount();
      }
    },
    getSyncRule() {
      relationGet(this.api.id, 'API').then((response) => {
        if (response.data) {
          this.apiSyncRuleRelation = response.data;
          if (this.apiSyncRuleRelation.apiSyncCaseRequest) {
            this.apiSyncRuleRelation.apiSyncConfig = JSON.parse(this.apiSyncRuleRelation.apiSyncCaseRequest);
          }
          if (this.apiSyncRuleRelation.caseCreator === null || this.apiSyncRuleRelation.caseCreator === undefined) {
            this.apiSyncRuleRelation.caseCreator = true;
          }
          if (
            this.apiSyncRuleRelation.scenarioCreator === null ||
            this.apiSyncRuleRelation.scenarioCreator === undefined
          ) {
            this.apiSyncRuleRelation.scenarioCreator = true;
          }
          if (this.apiSyncRuleRelation.syncCase === null || this.apiSyncRuleRelation.syncCase === undefined) {
            this.apiSyncRuleRelation.syncCase = true;
          }
          if (this.apiSyncRuleRelation.sendNotice === null || this.apiSyncRuleRelation.sendNotice === undefined) {
            this.apiSyncRuleRelation.sendNotice = true;
          }
          this.noShowSyncRuleRelation = this.apiSyncRuleRelation.showUpdateRule;
        }
      });
    },
    getCaseCount() {
      apiTestCaseCount({ id: this.api.id }).then((response) => {
        if (response.data > 0) {
          this.api.caseTotal = response.data;
        }
      });
    },
    getOldVersionData() {
      getDefinitionVersions(this.api.id).then((response) => {
        if (response.data[0]) {
          this.beforeHttpForm = response.data[0];
          this.beforeRequest = JSON.parse(response.data[0].request);
          this.beforeResponse = JSON.parse(response.data[0].response);
        }
      });
    },
    margeFiles(targetFiles, sourceFiles) {
      targetFiles.forEach((target) => {
        sourceFiles.forEach((source) => {
          if (target.uuid === source.uuid) {
            source.file = target.file;
          }
        });
      });
    },
    initLocalFile() {
      if (this.apiData.request && this.apiData.request.body) {
        if (this.apiData.request.body.binary && this.apiData.request.body.binary.length > 0) {
          this.apiData.request.body.binary.forEach((item) => {
            this.api.request.body.binary.forEach((api) => {
              if (item.uuid && api.uuid && item.uuid === api.uuid) {
                api.files = item.files;
              } else if (item.files && api.files) {
                this.margeFiles(item.files, api.files);
              }
            });
          });
        }
        if (this.apiData.request.body.kvs && this.apiData.request.body.kvs.length > 0) {
          this.apiData.request.body.kvs.forEach((item) => {
            this.api.request.body.kvs.forEach((api) => {
              if (item.uuid && api.uuid && item.uuid === api.uuid && item.files && api.files) {
                api.files = item.files;
              } else if (item.files && api.files) {
                this.margeFiles(item.files, api.files);
              }
            });
          });
        }
      }
    },
  },

  created() {
    this.init();
  },
};
</script>

<style scoped>
.ms-htt-width {
  width: 350px;
}

:deep(.el-drawer) {
  overflow: auto;
}
</style>
