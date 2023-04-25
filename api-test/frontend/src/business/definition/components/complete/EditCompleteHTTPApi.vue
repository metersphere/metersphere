<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">
      <el-form :model="httpForm" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->
        <div class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i
              class="el-icon-star-off"
              style="
                color: var(--primary_color);
                font-size: 25px;
                margin-right: 5px;
                position: relative;
                top: 5px;
                cursor: pointer;
              "
              @click="saveFollow" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i
              class="el-icon-star-on"
              style="
                color: var(--primary_color);
                font-size: 28px;
                margin-right: 5px;
                position: relative;
                top: 5px;
                cursor: pointer;
              "
              @click="saveFollow" />
          </el-tooltip>
          <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="httpForm.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <mx-version-history
            v-xpack
            ref="versionHistory"
            :version-data="versionData"
            :current-id="httpForm.id"
            :has-latest="hasLatest"
            @compare="compare"
            @checkout="checkout"
            @create="create"
            @setLatest="setLatest"
            @del="del" />
          <el-button
            v-if="!isXpack || !apiSyncRuleRelation.showUpdateRule"
            type="primary"
            size="small"
            @click="saveApi"
            v-prevent-re-click
            title="ctrl + s"
            v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
            >{{ $t('commons.save') }}
          </el-button>
          <el-dropdown
            v-else
            v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
            split-button
            type="primary"
            size="small"
            @click="saveApi"
            @command="handleCommand">
            {{ $t('commons.save') }}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="openSyncRule"
                >{{ $t('commons.save') + '&' + $t('workstation.sync') + $t('commons.setting') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <br />

        <!-- 基础信息 -->
        <div class="base-info">
          <el-row>
            <el-col :span="16">
              <el-form-item :label="$t('api_report.request')" prop="path">
                <el-input
                  :placeholder="$t('api_test.definition.request.path_info')"
                  v-model="httpForm.path"
                  class="ms-http-input"
                  size="small"
                  style="margin-top: 5px"
                  @change="urlChange">
                  <el-select v-model="httpForm.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id" />
                  </el-select>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 请求参数 -->
        <div>
          <ms-form-divider :title="$t('api_test.definition.request.req_param')" />
          <ms-api-request-form
            :showScript="false"
            :request="request"
            :headers="request.headers"
            :isShowEnable="isShowEnable" />
        </div>
      </el-form>

      <!-- 响应内容-->
      <ms-form-divider :title="$t('api_test.definition.request.res_param')" />
      <ms-response-text :response="response" />

      <api-other-info :api="httpForm" ref="apiOtherInfo" />

      <ms-change-history ref="changeHistory" />

      <el-dialog :fullscreen="true" :visible.sync="dialogVisible" :destroy-on-close="true" width="100%">
        <http-api-version-diff
          v-if="dialogVisible"
          :old-data="httpForm"
          :show-follow="showFollow"
          :new-data="newData"
          :new-show-follow="newShowFollow"
          :old-mock-url="getUrlPrefix"
          :new-mock-url="newMockUrl"
          :rule="rule"
          :maintainer-options="maintainerOptions"
          :module-options="moduleOptions"
          :request="newRequest"
          :old-request="request"
          :response="newResponse"
          :old-response="response"></http-api-version-diff>
      </el-dialog>
    </el-card>

    <el-dialog
      :title="$t('commons.sync_other_info')"
      :visible.sync="createNewVersionVisible"
      :show-close="false"
      width="30%">
      <div>
        <el-checkbox v-model="httpForm.newVersionRemark">{{ $t('commons.remark') }}</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionDeps">{{ $t('commons.relationship.name') }}</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionCase">CASE</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionMock">MOCK</el-checkbox>
      </div>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="cancelCreateNewVersion" :title="$t('commons.edit_info')" @confirm="saveApi">
        </ms-dialog-footer>
      </template>
    </el-dialog>
    <api-sync-case-config
      :is-xpack="isXpack"
      :api-sync-rule-relation="apiSyncRuleRelation"
      :batch-sync-api-visible="batchSyncApiVisible"
      :show-api-sync-config="true"
      @batchSync="batchSync"
      ref="syncCaseConfig">
    </api-sync-case-config>
  </div>
</template>

<script>
import { createMockConfig } from '@/api/api-mock';
import {
  citedApiScenarioCount,
  definitionFollow,
  delDefinitionByRefId,
  getDefinitionById,
  getDefinitionByIdAndRefId,
  getDefinitionVersions,
  getMockEnvironment,
  updateDefinitionFollows,
} from '@/api/definition';
import { apiTestCaseCount } from '@/api/api-test-case';
import { relationGet, updateRuleRelation } from '@/api/xpack';
import MsApiRequestForm from '../request/http/ApiHttpRequestForm';
import MsResponseText from '../response/ResponseText';
import { API_STATUS, REQ_METHOD } from '../../model/JsonData';
import { KeyValue } from '../../model/ApiTestModel';
import MsInputTag from 'metersphere-frontend/src/components/MsInputTag';
import MsJsr233Processor from '../../../automation/scenario/component/Jsr233Processor';
import MsSelectTree from 'metersphere-frontend/src/components/select-tree/SelectTree';
import MsChangeHistory from '@/business/history/ApiHistory';
import { getCurrentUser } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import MsFormDivider from 'metersphere-frontend/src/components/MsFormDivider';
import ApiOtherInfo from '@/business/definition/components/complete/ApiOtherInfo';
import HttpApiVersionDiff from './version/HttpApiVersionDiff';
import { createComponent } from '.././jmeter/components';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import { getProjectMemberOption } from '@/api/project';
import { getDefaultVersion, setLatestVersionById } from 'metersphere-frontend/src/api/version';

import { useApiStore } from '@/store';
import ApiSyncCaseConfig from '@/business/definition/components/sync/ApiSyncCaseConfig';

const { Body } = require('@/business/definition/model/ApiTestModel');
const Sampler = require('@/business/definition/components/jmeter/components/sampler/sampler');

const store = useApiStore();

export default {
  name: 'MsAddCompleteHttpApi',
  components: {
    MxVersionHistory: () => import('metersphere-frontend/src/components/version/MxVersionHistory'),
    MsDialogFooter,
    ApiOtherInfo,
    MsFormDivider,
    MsJsr233Processor,
    MsResponseText,
    MsApiRequestForm,
    MsInputTag,
    MsSelectTree,
    MsChangeHistory,
    HttpApiVersionDiff,
    ApiSyncCaseConfig,
  },
  data() {
    let validateURL = (rule, value, callback) => {
      if (!this.httpForm.path || !this.httpForm.path.startsWith('/')) {
        callback(this.$t('api_test.definition.request.path_valid_info'));
      }
      callback();
    };
    let validateModuleId = (rule, value, callback) => {
      if (this.httpForm.moduleId.length === 0 || !this.httpForm.moduleId) {
        callback(this.$t('test_track.case.input_module'));
      } else {
        callback();
      }
    };
    return {
      rule: {
        name: [
          {
            required: true,
            message: this.$t('test_track.case.input_name'),
            trigger: 'blur',
          },
          {
            max: 100,
            message: this.$t('test_track.length_less_than') + '100',
            trigger: 'blur',
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
        userId: [
          {
            required: true,
            message: this.$t('test_track.case.input_maintainer'),
            trigger: 'change',
          },
        ],
        moduleId: [{ required: true, validator: validateModuleId, trigger: 'change' }],
        status: [
          {
            required: true,
            message: this.$t('commons.please_select'),
            trigger: 'change',
          },
        ],
      },
      httpForm: { environmentId: '', path: '', tags: [] },
      beforeHttpForm: { environmentId: '', path: '', tags: [] },
      beforeRequest: { arguments: [] },
      beforeResponse: {},
      newData: { environmentId: '', path: '', tags: [] },
      dialogVisible: false,
      isShowEnable: true,
      showFollow: false,
      newShowFollow: false,
      maintainerOptions: [],
      currentModule: {},
      reqOptions: REQ_METHOD,
      options: API_STATUS,
      mockEnvironment: {},
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      mockBaseUrl: '',
      newMockBaseUrl: '',
      versionData: [],
      newRequest: Sampler,
      newResponse: {},
      createNewVersionVisible: false,
      batchSyncApiVisible: false,
      isXpack: false,
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
      citedScenarioCount: 0,
      latestVersionId: '',
      hasLatest: false,
    };
  },
  props: {
    moduleOptions: {},
    request: {},
    response: {},
    basisData: {},
    syncTabs: Array,
    projectId: String,
  },
  watch: {
    'httpForm.path': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      },
    },
    syncTabs() {
      if (this.basisData && this.syncTabs && this.syncTabs.includes(this.basisData.id)) {
        // 标示接口在其他地方更新过，当前页面需要同步
        getDefinitionById(this.basisData.id).then((response) => {
          if (response.data) {
            let request = JSON.parse(response.data.request);
            let index = this.syncTabs.findIndex((item) => {
              if (item === this.basisData.id) {
                return true;
              }
            });
            this.syncTabs.splice(index, 1);
            this.httpForm.path = response.data.path;
            this.httpForm.method = response.data.method;
            Object.assign(this.request, request);
          }
        });
      }
    },
    batchSyncApiVisible() {
      if (!this.batchSyncApiVisible && this.apiSyncRuleRelation.showUpdateRule) {
        this.noShowSyncRuleRelation = true;
      }
    },
  },
  computed: {
    getUrlPrefix() {
      if (this.httpForm.path == null) {
        return this.mockBaseUrl;
      } else {
        let path = this.httpForm.path;
        let protocol = this.httpForm.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.httpForm.request != null && this.httpForm.request.rest != null) {
            let pathUrlArr = path.split('/');
            let newPath = '';
            pathUrlArr.forEach((item) => {
              if (item !== '') {
                let pathItem = item;
                if (item.indexOf('{') === 0 && item.indexOf('}') === item.length - 1) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.httpForm.request.rest.length; i++) {
                    let param = this.httpForm.request.rest[i];
                    if (param.name === paramItem) {
                      pathItem = param.value;
                    }
                  }
                }
                newPath += '/' + pathItem;
              }
            });
            if (newPath !== '') {
              path = newPath;
            }
          }
        }
        return this.mockBaseUrl + path;
      }
    },
    newMockUrl() {
      if (this.newData.path == null) {
        return this.newMockBaseUrl;
      } else {
        let path = this.newData.path;
        let protocol = this.newData.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.newData.request != null && this.newData.request.rest != null) {
            let pathUrlArr = path.split('/');
            let newPath = '';
            pathUrlArr.forEach((item) => {
              if (item !== '') {
                let pathItem = item;
                if (item.indexOf('{') === 0 && item.indexOf('}') === item.length - 1) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.newData.request.rest.length; i++) {
                    let param = this.newData.request.rest[i];
                    if (param.name === paramItem) {
                      pathItem = param.value;
                    }
                  }
                }
                newPath += '/' + pathItem;
              }
            });
            if (newPath !== '') {
              path = newPath;
            }
          }
        }
        return this.newMockBaseUrl + path;
      }
    },
  },
  methods: {
    apiMapStatus() {
      store.apiStatus.set('fromChange', true);
      if (this.httpForm.id) {
        store.apiMap.set(this.httpForm.id, store.apiStatus);
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    openHis() {
      this.$refs.changeHistory.open(this.httpForm.id, ['接口定义', '接口定義', 'Api definition', 'API_DEFINITION']);
    },
    mockSetting() {
      if (this.basisData.id) {
        store.currentApiCase = { mock: getUUID() };
        this.$emit('changeTab', 'mock');
      } else {
        this.$alert(this.$t('api_test.mock.create_error'));
      }
    },
    runTest() {
      this.$refs['httpForm'].validate((valid) => {
        if (valid) {
          this.setParameter();
          this.$emit('runTest', this.httpForm);
        } else {
          return false;
        }
      });
    },
    getMaintainerOptions() {
      getProjectMemberOption().then((data) => {
        this.maintainerOptions = data.data;
      });
    },
    setParameter() {
      this.request.path = this.httpForm.path;
      this.request.method = this.httpForm.method;
      if (this.httpForm && this.httpForm.request) {
        this.httpForm.request.useEnvironment = undefined;
      }
      if (this.httpForm.tags instanceof Array) {
        this.httpForm.tags = JSON.stringify(this.httpForm.tags);
      }
      if (this.beforeHttpForm) {
        if (this.beforeHttpForm.tags instanceof Array) {
          this.beforeHttpForm.tags = JSON.stringify(this.beforeHttpForm.tags);
        }
      }
    },
    saveApi() {
      this.$refs['httpForm'].validate((valid) => {
        if (valid) {
          this.setParameter();
          if (!this.httpForm.versionId) {
            if (this.$refs.versionHistory && this.$refs.versionHistory.currentVersion) {
              this.httpForm.versionId = this.$refs.versionHistory.currentVersion.id;
            }
          }
          if (hasLicense() && (this.httpForm.caseTotal > 0 || this.citedScenarioCount > 0) && !this.httpForm.isCopy) {
            if (this.httpForm.method !== this.beforeHttpForm.method && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
            if (this.httpForm.path !== this.beforeHttpForm.path && !this.noShowSyncRuleRelation) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
            if (this.request.headers && this.beforeRequest.headers) {
              if (this.request.headers.length === this.beforeRequest.headers.length) {
                let requestHeaders = [];
                let beforeHeaders = [];
                for (let i = 0; i < this.request.headers.length; i++) {
                  this.beforeRequest.headers[i].valid = this.request.headers[i].valid;
                  if (this.request.headers[i].isEdit !== undefined) {
                    this.beforeRequest.headers[i].isEdit = this.request.headers[i].isEdit;
                  }
                  if (this.request.headers[i].uuid) {
                    this.beforeRequest.headers[i].uuid = this.request.headers[i].uuid;
                  }
                  if (this.request.headers[i].time) {
                    this.beforeRequest.headers[i].time = this.request.headers[i].time;
                  }
                  if (this.request.headers[i].name === undefined) {
                    this.beforeRequest.headers[i].name = undefined;
                  }
                  let newRequest = this.request.headers[i];
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
                let submitRequestHeaders = JSON.stringify(this.request.headers);
                let beforeRequestHeaders = JSON.stringify(this.beforeRequest.headers);
                if (submitRequestHeaders !== beforeRequestHeaders && !this.noShowSyncRuleRelation) {
                  this.batchSyncApiVisible = true;
                  this.$refs.syncCaseConfig.show();
                }
              }
            }
            if (this.request.arguments && this.beforeRequest.arguments) {
              if (this.request.arguments.length === this.beforeRequest.arguments.length) {
                let requestArguments = [];
                let beforeArguments = [];
                for (let i = 0; i < this.request.arguments.length; i++) {
                  if (this.request.arguments[i].isEdit !== undefined) {
                    this.beforeRequest.arguments[i].isEdit = this.request.arguments[i].isEdit;
                  }
                  if (this.request.arguments[i].uuid) {
                    this.beforeRequest.arguments[i].uuid = this.request.arguments[i].uuid;
                  }
                  if (this.request.arguments[i].time) {
                    this.beforeRequest.arguments[i].time = this.request.arguments[i].time;
                  }
                  if (this.request.arguments[i].name === undefined) {
                    this.beforeRequest.arguments[i].name = undefined;
                  }
                  this.beforeRequest.arguments[i].valid = this.request.arguments[i].valid;
                  let newRequest = this.request.arguments[i];
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
                let submitRequestQuery = JSON.stringify(this.request.arguments);
                let beforeRequestQuery = JSON.stringify(this.beforeRequest.arguments);
                if (submitRequestQuery !== beforeRequestQuery && !this.noShowSyncRuleRelation) {
                  this.batchSyncApiVisible = true;
                  this.$refs.syncCaseConfig.show();
                }
              }
            }
            if (this.request.rest && this.beforeRequest.rest) {
              if (this.request.rest.length === this.beforeRequest.rest.length) {
                let requestRest = [];
                let beforeRest = [];
                for (let i = 0; i < this.request.rest.length; i++) {
                  if (this.request.rest[i].isEdit !== undefined) {
                    this.beforeRequest.rest[i].isEdit = this.request.rest[i].isEdit;
                  }
                  if (this.request.rest[i].uuid) {
                    this.beforeRequest.rest[i].uuid = this.request.rest[i].uuid;
                  }
                  if (this.request.rest[i].time) {
                    this.beforeRequest.rest[i].time = this.request.rest[i].time;
                  }
                  if (this.request.rest[i].name === undefined) {
                    this.beforeRequest.rest[i].name = undefined;
                  }
                  this.beforeRequest.rest[i].valid = this.request.rest[i].valid;

                  let newRequest = this.request.rest[i];
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
                let submitRequestRest = JSON.stringify(this.request.rest);
                let beforeRequestRest = JSON.stringify(this.beforeRequest.rest);
                if (submitRequestRest !== beforeRequestRest && !this.noShowSyncRuleRelation) {
                  this.batchSyncApiVisible = true;
                  this.$refs.syncCaseConfig.show();
                }
              }
            }
            if (this.request.body && this.beforeRequest.body) {
              if (this.request.body.valid) {
                this.beforeRequest.body.valid = this.request.body.valid;
              }
              if (this.request.body.kvs.length === this.beforeRequest.body.kvs.length) {
                let requestKvs = [];
                let beforeKvs = [];
                for (let i = 0; i < this.request.body.kvs.length; i++) {
                  if (this.request.body.kvs[i].isEdit !== undefined) {
                    this.beforeRequest.body.kvs[i].isEdit = this.request.body.kvs[i].isEdit;
                  }
                  if (this.request.body.kvs[i].files !== null && this.request.body.kvs[i].files.length === 0) {
                    this.beforeRequest.body.kvs[i].files = this.request.body.kvs[i].files;
                  }
                  if (this.request.body.kvs[i].uuid) {
                    this.beforeRequest.body.kvs[i].uuid = this.request.body.kvs[i].uuid;
                  }
                  if (this.request.body.kvs[i].time) {
                    this.beforeRequest.body.kvs[i].time = this.request.body.kvs[i].time;
                  }
                  if (this.request.body.kvs[i].name === undefined) {
                    this.beforeRequest.body.kvs[i].name = undefined;
                  }
                  this.beforeRequest.body.kvs[i].valid = this.request.body.kvs[i].valid;

                  let newRequest = this.request.body.kvs[i];
                  const ordered = {};
                  Object.keys(newRequest)
                    .sort()
                    .forEach(function (key) {
                      ordered[key] = newRequest[key];
                    });
                  requestKvs.push(ordered);
                  let beforeRequest = this.beforeRequest.body.kvs[i];
                  const beforeOrdered = {};
                  Object.keys(beforeRequest)
                    .sort()
                    .forEach(function (key) {
                      beforeOrdered[key] = beforeRequest[key];
                    });
                  beforeKvs.push(beforeOrdered);
                }
                this.request.body.kvs = requestKvs;
                this.beforeRequest.body.kvs = beforeKvs;
              }
              if (Object.keys(this.request.body).indexOf('type')) {
                this.beforeRequest.body.type = this.request.body.type;
              }
              let submitRequestBody = JSON.stringify(this.request.body);
              let beforeRequestBody = JSON.stringify(this.beforeRequest.body);
              if (submitRequestBody !== beforeRequestBody && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
                this.$refs.syncCaseConfig.show();
              }
            }
            if (this.request.authManager && this.beforeRequest.authManager) {
              let submitRequestAuthManager = JSON.stringify(this.request.authManager);
              let beforeRequestAuthManager = JSON.stringify(this.beforeRequest.authManager);
              if (submitRequestAuthManager !== beforeRequestAuthManager && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
                this.$refs.syncCaseConfig.show();
              }
            }
            if (this.request.hashTree && this.beforeRequest.hashTree) {
              let submitRequestHashTree = JSON.stringify(this.request.hashTree);
              let beforeRequestHashTree = JSON.stringify(this.beforeRequest.hashTree);
              if (submitRequestHashTree !== beforeRequestHashTree && !this.noShowSyncRuleRelation) {
                this.batchSyncApiVisible = true;
                this.$refs.syncCaseConfig.show();
              }
            }
            if (
              (this.request.connectTimeout !== this.beforeRequest.connectTimeout ||
                this.request.responseTimeout !== this.beforeRequest.responseTimeout ||
                this.request.followRedirects !== this.beforeRequest.followRedirects ||
                this.request.alias !== this.beforeRequest.alias ||
                this.apiSyncRuleRelation.showUpdateRule === true) &&
              !this.noShowSyncRuleRelation
            ) {
              this.batchSyncApiVisible = true;
              this.$refs.syncCaseConfig.show();
            }
            if (this.batchSyncApiVisible !== true) {
              this.$emit('saveApi', this.httpForm);
            }
          } else {
            this.$emit('saveApi', this.httpForm);
          }
        } else {
          if (this.$refs.versionHistory) {
            this.$refs.versionHistory.loading = false;
          }
          return false;
        }
      });
    },
    batchSync(fromData) {
      if (hasLicense() && (this.httpForm.caseTotal > 0 || this.citedScenarioCount > 0) && !this.httpForm.isCopy) {
        this.httpForm.triggerUpdate = JSON.stringify(fromData);
        this.apiSyncRuleRelation.apiSyncCaseRequest = JSON.stringify(fromData);
        if (this.apiSyncRuleRelation.sendNotice && this.apiSyncRuleRelation.sendNotice === true) {
          this.httpForm.sendSpecialMessage = this.apiSyncRuleRelation.sendNotice;
        } else {
          this.httpForm.sendSpecialMessage = false;
        }

        if (this.apiSyncRuleRelation.caseCreator && this.apiSyncRuleRelation.caseCreator === true) {
          this.httpForm.caseCreator = this.apiSyncRuleRelation.caseCreator;
        } else {
          this.httpForm.caseCreator = false;
        }
        if (this.apiSyncRuleRelation.scenarioCreator && this.apiSyncRuleRelation.scenarioCreator === true) {
          this.httpForm.scenarioCreator = this.apiSyncRuleRelation.scenarioCreator;
        } else {
          this.httpForm.scenarioCreator = false;
        }
        this.apiSyncRuleRelation.resourceId = this.httpForm.id;
        this.apiSyncRuleRelation.resourceType = 'API';
        this.saveApiSyncRuleRelation(this.apiSyncRuleRelation);
      }
    },
    saveApiSyncRuleRelation(apiSyncRuleRelation) {
      updateRuleRelation(apiSyncRuleRelation.resourceId, apiSyncRuleRelation).then(() => {
        this.$emit('saveApi', this.httpForm);
        this.$refs.syncCaseConfig.close();
      });
    },
    createModules() {
      this.$emit('createRootModelInTree');
    },
    urlChange() {
      if (!this.httpForm.path || this.httpForm.path.indexOf('?') === -1) return;
      let url = this.getURL(this.addProtocol(this.httpForm.path));
      if (url) {
        this.httpForm.path = decodeURIComponent(this.httpForm.path.substr(0, this.httpForm.path.indexOf('?')));
      }
    },
    addProtocol(url) {
      if (url) {
        if (!url.toLowerCase().startsWith('https') && !url.toLowerCase().startsWith('http')) {
          return 'https://' + url;
        }
      }
      return url;
    },
    getURL(urlStr) {
      try {
        let url = new URL(urlStr);
        if (url.search && url.search.length > 1) {
          let params = url.search.substr(1).split('&');
          params.forEach((param) => {
            if (param) {
              let keyValues = param.split('=');
              if (keyValues) {
                this.request.arguments.splice(
                  0,
                  0,
                  new KeyValue({
                    name: keyValues[0],
                    required: false,
                    value: keyValues[1],
                    isEdit: false,
                  })
                );
              }
            }
          });
        }
        return url;
      } catch (e) {
        this.$error(this.$t('api_test.request.url_invalid'), 2000);
      }
    },
    setModule(id, data) {
      if (id && id !== '') {
        this.httpForm.moduleId = id;
      }
      if (data && data.path) {
        this.httpForm.modulePath = data.path;
      }
    },
    initMockEnvironment() {
      let protocol = document.location.protocol;
      protocol = protocol.substring(0, protocol.indexOf(':'));
      getMockEnvironment(this.projectId).then((response) => {
        this.mockEnvironment = response.data;
        let httpConfig = JSON.parse(this.mockEnvironment.config);
        if (httpConfig != null) {
          httpConfig = httpConfig.httpConfig;
          let httpType = httpConfig.defaultCondition;
          let conditions = httpConfig.conditions;
          conditions.forEach((condition) => {
            if (condition.type === httpType) {
              this.mockBaseUrl = condition.protocol + '://' + condition.socket;
              this.newMockBaseUrl = this.mockBaseUrl;
            }
          });
        }
      });
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.httpForm.follows.length; i++) {
          if (this.httpForm.follows[i] === this.currentUser().id) {
            this.httpForm.follows.splice(i, 1);
            break;
          }
        }
        if (this.basisData.id) {
          updateDefinitionFollows(this.basisData.id, this.httpForm.follows).then(() => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.httpForm.follows) {
          this.httpForm.follows = [];
        }
        this.httpForm.follows.push(this.currentUser().id);
        if (this.basisData.id) {
          updateDefinitionFollows(this.basisData.id, this.httpForm.follows).then(() => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getDefaultVersion() {
      getDefaultVersion(this.projectId).then((response) => {
        this.latestVersionId = response.data;
        this.getVersionHistory();
      });
    },
    getVersionHistory() {
      getDefinitionVersions(this.httpForm.id).then((response) => {
        if (this.httpForm.isCopy) {
          this.versionData = response.data.filter((v) => v.versionId === this.httpForm.versionId);
        } else {
          this.versionData = response.data;
        }
        if (this.versionData[0]) {
          this.beforeHttpForm = this.versionData[0];
          this.beforeRequest = JSON.parse(this.versionData[0].request);
          this.beforeResponse = JSON.parse(this.versionData[0].response);
        }
        let latestVersionData = response.data.filter((v) => v.versionId === this.latestVersionId);
        if (latestVersionData.length > 0) {
          this.hasLatest = false;
        } else {
          this.hasLatest = true;
        }
      });
    },
    compare(row) {
      this.httpForm.createTime = this.$refs.versionHistory.versionOptions.filter(
        (v) => v.id === this.httpForm.versionId
      )[0].createTime;
      getDefinitionByIdAndRefId(row.id, this.httpForm.refId).then((response) => {
        getDefinitionById(response.data.id).then((res) => {
          if (res.data) {
            this.newData = res.data;
            this.newData.createTime = row.createTime;
            this.dealWithTag(res.data);
            this.setRequest(res.data);
            if (!this.setRequest(res.data)) {
              this.newRequest = createComponent('HTTPSamplerProxy');
              this.dialogVisible = true;
            }
            this.formatApi(res.data);
          }
        });
      });
    },
    setRequest(api) {
      if (api.request) {
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          this.newRequest = api.request;
        } else {
          this.newRequest = JSON.parse(api.request);
        }
        if (!this.newRequest.headers) {
          this.newRequest.headers = [];
        }
        this.dialogVisible = true;
        return true;
      }
      return false;
    },
    dealWithTag(api) {
      if (api.tags) {
        if (Object.prototype.toString.call(api.tags) === '[object String]') {
          api.tags = JSON.parse(api.tags);
        }
      }
      if (this.httpForm.tags) {
        if (Object.prototype.toString.call(this.httpForm.tags) === '[object String]') {
          this.httpForm.tags = JSON.parse(this.httpForm.tags);
        }
      }
    },
    formatApi(api) {
      if (api.response != null && api.response !== 'null' && api.response) {
        if (
          Object.prototype.toString
            .call(api.response)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === 'object'
        ) {
          this.newResponse = api.response;
        } else {
          this.newResponse = JSON.parse(api.response);
        }
      } else {
        this.newResponse = {
          headers: [],
          body: new Body(),
          statusCode: [],
          type: 'HTTP',
        };
      }
      if (!this.newRequest.hashTree) {
        this.newRequest.hashTree = [];
      }
      if (this.newRequest.body && !this.newRequest.body.binary) {
        this.newRequest.body.binary = [];
      }
      // 处理导入数据缺失问题
      if (this.newResponse.body) {
        let body = new Body();
        Object.assign(body, this.newResponse.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.newResponse.body = body;
      }
      this.newRequest.clazzName = TYPE_TO_C.get(this.newRequest.type);

      this.sort(this.newRequest.hashTree);
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === 'Assertions' && !stepArray[i].document) {
            stepArray[i].document = {
              type: 'JSON',
              data: {
                xmlFollowAPI: false,
                jsonFollowAPI: false,
                json: [],
                xml: [],
              },
            };
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    cancelCreateNewVersion() {
      this.createNewVersionVisible = false;
      this.getVersionHistory();
    },
    checkout(row) {
      let api = this.versionData.filter((v) => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      this.$emit('checkout', api);
    },
    create(row) {
      // 创建新版本
      this.httpForm.versionId = row.id;
      this.httpForm.versionName = row.name;
      apiTestCaseCount({ id: this.httpForm.id }).then((response) => {
        if (response.data > 0) {
          this.httpForm.caseTotal = response.data;
        }
        this.$set(this.httpForm, 'newVersionRemark', !!this.httpForm.remark);
        this.$set(this.httpForm, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
        this.$set(this.httpForm, 'newVersionCase', this.httpForm.caseTotal > 0);
        createMockConfig({
          projectId: this.projectId,
          apiId: this.httpForm.id,
        }).then(
          (response) => {
            this.$set(this.httpForm, 'newVersionMock', response.data.mockExpectConfigList.length > 0);

            if (
              this.$refs.apiOtherInfo.relationshipCount > 0 ||
              this.httpForm.remark ||
              this.httpForm.newVersionCase ||
              this.httpForm.newVersionMock
            ) {
              this.createNewVersionVisible = true;
            } else {
              this.saveApi();
              if (this.$refs.versionHistory) {
                this.$refs.versionHistory.loading = false;
              }
            }
          },
          (error) => {
            if (this.$refs.versionHistory) {
              this.$refs.versionHistory.loading = false;
            }
          }
        );
      });
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            delDefinitionByRefId(row.id, this.httpForm.refId).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        },
      });
    },
    setLatest(row) {
      let param = {
        projectId: this.projectId,
        type: 'API',
        versionId: row.id,
        resourceId: this.basisData.id,
      };
      setLatestVersionById(param).then(() => {
        this.$success(this.$t('commons.modify_success'));
        this.checkout(row);
      });
    },
    getSyncRule() {
      relationGet(this.httpForm.id, 'API').then((response) => {
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
    handleCommand(command) {
      if (command === 'openSyncRule') {
        this.noShowSyncRuleRelation = false;
        this.saveApi();
      }
    },
    getCitedScenarioCount() {
      citedApiScenarioCount(this.httpForm.id).then((response) => {
        if (response.data) {
          this.citedScenarioCount = response.data;
        }
      });
    },
    getCaseCount() {
      apiTestCaseCount({ id: this.httpForm.id }).then((response) => {
        if (response.data > 0) {
          this.httpForm.caseTotal = response.data;
        }
      });
    },
  },

  created() {
    this.getMaintainerOptions();
    this.isXpack = !!hasLicense();
    if (!this.basisData.environmentId) {
      this.basisData.environmentId = '';
    }
    if (this.basisData.moduleId &&
      this.basisData.moduleId === 'default-module' &&
      this.moduleOptions) {
      this.basisData.moduleId = this.moduleOptions[0].id;
    }
    if (this.basisData.isCopy) {
      this.basisData.caseTotal = 0;
    }
    this.httpForm = JSON.parse(JSON.stringify(this.basisData));
    if (this.basisData.request && this.basisData.id) {
      this.httpForm.path = this.basisData.request.path;
      this.httpForm.method = this.basisData.request.method;
    }
    definitionFollow(this.basisData.id).then((response) => {
      this.httpForm.follows = response.data;
      this.beforeHttpForm.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === this.currentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    });
    this.initMockEnvironment();

    if (hasLicense()) {
      this.getDefaultVersion();
      this.getSyncRule();
      this.getCitedScenarioCount();
      this.getCaseCount();
    }
  },
};
</script>

<style scoped>
.timeClass {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 5px;
}

.base-info .el-form-item {
  width: 100%;
}

.mock-info {
  margin: 20px 45px;
}

.ms-opt-btn {
  position: fixed;
  right: 10px !important;
  z-index: 1;
  top: 85px;
}

/*.base-info .el-form-item :deep(.el-form-item__content) {*/
/*  width: 80%;*/
/*}*/

.base-info .ms-http-select {
  width: 100%;
}

.ms-http-textarea {
  width: 100%;
}

.ms-left-cell {
  margin-top: 100px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}
</style>
