<template>
  <el-card style="margin-top: 5px" @click.native="selectTestCase(apiCase,$event)" v-loading="saveLoading">
    <div @click="active(apiCase)" v-if="type!=='detail'">
      <el-row>
        <el-col :span="api.protocol==='HTTP'?4:8" v-loading="loading && !(apiCase.active||type==='detail')">
          <span @click.stop>
            <i class="icon el-icon-arrow-right" :class="{'is-active': apiCase.active}" @click="active(apiCase)"/>
            <el-input v-if="!apiCase.id || isShowInput" size="small" v-model="apiCase.name" :name="index" :key="index"
                      class="ms-api-header-select" style="width: 180px"
                      :readonly="!hasPermission('PROJECT_API_DEFINITION:READ+EDIT_CASE')"
                      :placeholder="$t('commons.input_name')" ref="nameEdit"/>
            <span v-else>
              <el-tooltip :content="apiCase.id ? apiCase.name : ''" placement="top">
                <span>{{ apiCase.id ? apiCase.name : '' | ellipsis }}</span>
              </el-tooltip>

              <i class="el-icon-edit" style="cursor:pointer" @click="showInput(apiCase)"/>
            </span>

            <el-link type="primary" style="margin-left: 10px" @click="openHis(apiCase)"
                     v-if="apiCase.id">{{ $t('operating_log.change_history') }}</el-link>
          </span>
          <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
            <span style="margin-left: 10px">
              {{ apiCase.updateTime | datetimeFormat }}
              {{ apiCase.updateUser }} {{ $t('api_test.definition.request.update_info') }}
          </span>
          </div>
        </el-col>
        <el-col :span="2">
          <el-select size="mini" v-model="apiCase.priority" class="ms-api-select" @change="changePriority(apiCase)"
                     :disabled="readonly">
            <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
          </el-select>
        </el-col>
        <el-col :span="api.protocol==='HTTP'?4:0">
          <span v-if="api.protocol==='HTTP'">
            <el-tag size="mini"
                    :style="{'background-color': getColor(true, apiCase.request.method), border: getColor(true, apiCase.request.method)}"
                    class="api-el-tag">
                {{ apiCase.request.method }}
            </el-tag>
            <el-tooltip :content="apiCase.request.path">
              <span class="ms-col-name">{{ apiCase.request.path }}</span>
            </el-tooltip>
         </span>
        </el-col>
        <el-col :span="5">
          <el-row>
            <el-col :span="8">
              <el-select size="small" v-model="apiCase.caseStatus" style="margin-right: 5px"
                         @change="saveTestCase(apiCase,true)" :disabled="readonly">
                <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
              </el-select>
            </el-col>
            <el-col :span="16">
              <div class="tag-item" @click.stop>
                <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow"
                            :disabled="true">
                  <i class="el-icon-star-off"
                     style="color: var(--primary_color); font-size: 25px; margin-top: 2px; margin-right: 15px;cursor: pointer "
                     @click="saveFollow"/>
                </el-tooltip>
                <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow"
                            :disabled="true">
                  <i class="el-icon-star-on"
                     style="color: #783987; font-size: 28px; margin-top: 2px; margin-right: 15px;cursor: pointer "
                     @click="saveFollow" v-if="showFollow"/>
                </el-tooltip>
              </div>
            </el-col>
          </el-row>
          <el-row style="margin-top: 5px">
            <div class="tag-item" @click.stop>
              <ms-input-tag :currentScenario="apiCase" ref="tag" @keyup.enter.native="saveTestCase(apiCase,true)"
                            :read-only="readonly"/>
            </div>
          </el-row>
        </el-col>

        <el-col :span="3">
          <span @click.stop v-if="!loaded">
            <ms-tip-button @click="singleRun(apiCase)" :tip="$t('api_test.run')" icon="el-icon-video-play"
                           v-permission="['PROJECT_API_DEFINITION:READ+RUN']"
                           class="run-button" size="mini" :disabled="!apiCase.id || loaded" circle v-if="!loading"/>
            <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
              <el-button :disabled="!apiCase.id" @click.once="stop(apiCase)" size="mini"
                         style="color:white;padding: 0;width: 28px;height: 28px;" class="stop-btn" circle>
                <div style="transform: scale(0.72)">
                  <span style="margin-left: -3.5px;font-weight: bold">STOP</span>
                </div>
              </el-button>
            </el-tooltip>
          </span>
          <span @click.stop>
            <ms-api-extend-btns :is-case-edit="isCaseEdit" :environment="environment" :row="apiCase"/>
          </span>
        </el-col>

        <el-col :span="4">
          <ms-api-report-status :status="apiCase.execResult"/>
          <div v-if="apiCase.id" style="color: #999999;font-size: 12px;padding: 5px;">
            <span> {{ apiCase.execTime | datetimeFormat }}</span>
            {{ apiCase.updateUser }}
          </div>
        </el-col>
        <el-col :span="2">
          <el-link style="float:right;" type="primary" @click.stop @click="showHistory(apiCase.id)">
            {{ $t("commons.execute_history") }}
          </el-link>
        </el-col>
      </el-row>
    </div>

    <!-- 请求参数-->
    <el-collapse-transition>
      <div v-if="apiCase.active||type ==='detail'" v-loading="loading">
        <el-divider></el-divider>
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <ms-api-request-form
          :isShowEnable="true"
          :showScript="true"
          :headers="apiCase.request.headers "
          :response="apiCase.responseData"
          :request="apiCase.request" :case-id="apiCase.apiDefinitionId" v-if="api.protocol==='HTTP'"/>
        <tcp-format-parameters
          :showScript="true"
          :show-pre-script="true"
          :request="apiCase.request"
          :response="apiCase.responseData"
          v-if="api.method==='TCP'"/>
        <mx-esb-definition
          class="esb-div"
          v-xpack
          :request="apiCase.request"
          :show-pre-script="true"
          :showScript="true"
          :response="apiCase.responseData"
          v-if="api.method==='ESB'" ref="esbDefinition"/>
        <ms-sql-basis-parameters
          :showScript="true"
          :request="apiCase.request"
          :response="apiCase.responseData"
          v-if="api.method==='SQL'"/>
        <ms-dubbo-basis-parameters
          :showScript="true"
          :request="apiCase.request"
          :response="apiCase.responseData"
          v-if="api.protocol==='DUBBO'"/>
        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
        <div v-if="api.method==='ESB'">
          <mx-esb-definition-response
            v-xpack
            :currentProtocol="apiCase.request.protocol"
            :request="apiCase.request"
            :is-api-component="false"
            :show-options-button="false"
            :show-header="true"
            :api-item="apiCase"/>
        </div>
        <div v-else>
          <api-response-component
            :currentProtocol="apiCase.request.protocol"
            :api-item="apiCase"
            :result="apiCase.responseData"/>
        </div>
      </div>
    </el-collapse-transition>
    <ms-change-history ref="changeHistory"/>
    <el-dialog :visible.sync="syncCaseVisible" :append-to-body="true"
               :title="$t('commons.save')+'&'+$t('workstation.sync_setting')">

      <el-row style="margin-bottom: 10px;box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)">
        <span style="padding-left: 10px;">
            {{ $t('project_application.workstation.update_case_tip') }}
          <el-tooltip class="ms-num" effect="dark"
                      :content="$t('project_application.workstation.case_receiver_tip')"
                      placement="top">
                <i class="el-icon-warning"/>
          </el-tooltip>
        </span>
        <p
          style="font-size: 12px;color: var(--primary_color);margin-bottom: 20px;text-decoration:underline;cursor: pointer; padding-left: 10px;"
          @click="gotoApiMessage">
          {{ $t('project_application.workstation.go_to_case_message') }}
        </p>
        <el-row style="margin-bottom: 5px;margin-top: 5px">
          <el-col :span="4"><span
            style="font-weight: bold;padding-left: 10px;">{{ $t('api_test.definition.recipient') + ":" }}</span>
          </el-col>
          <el-col :span="20" style="color: var(--primary_color)">
            <el-checkbox v-model="caseSyncRuleRelation.scenarioCreator">
              {{ $t('commons.scenario') + $t('api_test.creator') }}
            </el-checkbox>
          </el-col>
        </el-row>
      </el-row>
      <el-row>
        <el-checkbox v-model="caseSyncRuleRelation.showUpdateRule" style="padding-left: 10px;">{{
            $t('project_application.workstation.no_show_setting')
          }}
        </el-checkbox>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="syncCaseVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="saveCaseAndNotice()">{{ $t('commons.confirm') }}</el-button>
      </span>
    </el-dialog>
  </el-card>


</template>

<script>
import {citedScenarioCount, deleteToGc, editApiCase, editFollowsByParam, getApiCaseFollow} from "@/api/api-test-case";
import {createDefinition} from "@/api/definition";
import {relationGet, updateRuleRelation} from "@/api/xpack";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentUser} from "metersphere-frontend/src/utils/token";
import {hasLicense, hasPermission} from "metersphere-frontend/src/utils/permission";
import {_getBodyUploadFiles, mergeRequestDocumentData} from "@/business/definition/api-definition";
import {API_METHOD_COLOUR, API_STATUS, PRIORITY} from "../../model/JsonData";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import ApiEnvironmentConfig from "metersphere-frontend/src/components/environment/ApiEnvironmentConfig";
import MsApiAssertions from "../assertion/ApiAssertions";
import MsSqlBasisParameters from "../request/database/BasisParameters";
import TcpFormatParameters from "@/business/definition/components/request/tcp/TcpFormatParameters";
import MsDubboBasisParameters from "../request/dubbo/BasisParameters";
import MsApiExtendBtns from "../reference/ApiExtendBtns";
import MsInputTag from "@/business/automation/scenario/MsInputTag";
import MsRequestResultTail from "../response/RequestResultTail";
import ApiResponseComponent from "../../../automation/scenario/component/ApiResponseComponent";
import ShowMoreBtn from "@/business/commons/ShowMoreBtn";
import MsChangeHistory from "@/business/history/ApiHistory";
import {TYPE_TO_C} from "@/business/automation/scenario/Setting";
import ApiCaseHeader from "./ApiCaseHeader";
import {deepClone} from "metersphere-frontend/src/utils/tableUtils";
import {useApiStore} from "@/store";

const store = useApiStore();
export default {
  name: "ApiCaseItem",
  filters: {
    ellipsis(value) {
      if (!value) {
        return '';
      }
      if (value.length > 20) {
        return value.slice(0, 20) + '...'
      }
      return value
    }
  },
  components: {
    ApiResponseComponent,
    MsInputTag,
    MsTag,
    MsTipButton,
    MsApiRequestForm,
    ApiEnvironmentConfig,
    MsApiAssertions,
    MsSqlBasisParameters,
    TcpFormatParameters,
    MsDubboBasisParameters,
    MsApiExtendBtns,
    MsRequestResultTail,
    ShowMoreBtn,
    MsChangeHistory,
    ApiCaseHeader,
    MsApiReportStatus: () => import("../../../automation/report/ApiReportStatus"),
    MxEsbDefinition: () => import("@/business/definition/components/esb/MxEsbDefinition"),
    MxEsbDefinitionResponse: () => import("@/business/definition/components/esb/MxEsbDefinitionResponse")
  },
  data() {
    return {
      options: API_STATUS,
      result: false,
      grades: [],
      selectedEvent: Object,
      priorities: PRIORITY,
      runData: [],
      reportId: "",
      checkedCases: new Set(),
      visible: false,
      condition: {},
      responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
      isShowInput: false,
      methodColorMap: new Map(API_METHOD_COLOUR),
      saveLoading: false,
      showFollow: false,
      beforeRequest: {},
      beforeUpdateRequest: {},
      compare: [],
      isSave: false,
      tagCount: 0,
      requestCount: 0,
      readonly: false,
      noShowSyncRuleRelation: false,
      syncCaseVisible: false,
      readyToSaveCase: {},
      readyToHideAlert: false,
      caseSyncRuleRelation: {
        scenarioCreator: true,
        showUpdateRule: false,
      },
      citedScenarioCount: 0,
    }
  },
  props: {
    loaded: {
      type: Boolean,
      default: false,
    },
    apiCase: {
      type: Object,
      default() {
        return {}
      },
    },
    environment: String,
    index: {
      type: Number,
      default() {
        return 0
      }
    },
    api: {
      type: Object,
      default() {
        return {}
      }
    },
    currentApi: {},
    type: String,
    isCaseEdit: Boolean,
    loading: {
      type: Boolean,
      default() {
        return false;
      }
    },
    maintainerOptions: Array
  },
  beforeDestroy() {
    this.$EventBus.$off('showXpackCaseSet');
  },
  created() {
    store.scenarioEnvMap = undefined;
    if (this.apiCase.request && this.apiCase.request.hashTree && this.apiCase.request.hashTree.length > 0) {
      let index = this.apiCase.request.hashTree.findIndex(item => item.type === 'Assertions');
      if (index !== -1) {
        this.apiCase.request.hashTree[index].document.nodeType = 'Case';
        this.apiCase.request.hashTree[index].document.apiDefinitionId = this.apiCase.apiDefinitionId;
      }
    }
    this.readonly = !hasPermission('PROJECT_API_DEFINITION:READ+EDIT_CASE');
    if (this.apiCase && this.apiCase.id) {
      this.showFollow = false;
      getApiCaseFollow(this.apiCase.id).then(response => {
        this.apiCase.follows = response.data;
        for (let i = 0; i < response.data.length; i++) {
          if (response.data[i] === this.currentUser().id) {
            this.showFollow = true;
            break;
          }
        }
      });
    }
    if (this.currentApi && this.currentApi.request) {
      this.beforeRequest = JSON.parse(JSON.stringify(this.currentApi.request));
    }
    if (hasLicense()) {
      this.beforeUpdateRequest = deepClone(this.apiCase.request);
      this.getSyncRule();
      this.getCitedScenarioCount();
      this.$EventBus.$on('showXpackCaseSet', noShowSyncRuleRelation => {
        this.handleXpackCaseSetChange(noShowSyncRuleRelation);
      });
    }
    this.reload();
  },
  watch: {
    'apiCase.name': {
      handler(v) {
        this.saveStatus();
      }
    },
    'apiCase.priority': {
      handler(v) {
        this.saveStatus();
      }
    },
    'apiCase.caseStatus': {
      handler(v) {
        this.saveStatus();
      }
    },
    'apiCase.tags': {
      handler(v) {
        this.tagCount++;
        if (this.tagCount > 2) {
          this.saveStatus();
        }
      }
    },
    'apiCase.request': {
      handler(v) {
        this.requestCount++;
        if (this.requestCount > 1) {
          this.saveStatus();
        }
      }
    },
    'caseSyncRuleRelation.showUpdateRule': {
      handler(v) {
        this.noShowSyncRuleRelation = v;
        this.$EventBus.$emit('showXpackCaseBtn', v);
      }
    }
  },
  mounted() {
    if (!(store.apiCaseMap instanceof Map)) {
      store.apiCaseMap = new Map();
    }
    if (this.apiCase.id) {
      store.apiCaseMap.set(this.apiCase.id, 0);
    }
    // 记录原始数据源ID
    this.apiCase.request.originalDataSourceId = this.apiCase.request.dataSourceId;
    this.apiCase.request.originalEnvironmentId = this.apiCase.request.environmentId;

    if (this.apiCase && this.apiCase.request && this.apiCase.request.hashTree) {
      this.setOriginal(this.apiCase.request.hashTree);
    }
  },
  methods: {
    setOriginal(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = ["JDBCPostProcessor", "JDBCSampler", "JDBCPreProcessor"]
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          scenarioDefinition[i].originalDataSourceId = scenarioDefinition[i].dataSourceId;
          scenarioDefinition[i].originalEnvironmentId = scenarioDefinition[i].environmentId;
        }
        if (scenarioDefinition[i].hashTree && scenarioDefinition[i].hashTree.length > 0) {
          this.setOriginal(scenarioDefinition[i].hashTree);
        }
      }
    },
    saveStatus() {
      if (store.apiCaseMap && this.apiCase.id) {
        let change = store.apiCaseMap.get(this.apiCase.id);
        change = change + 1;
        store.apiCaseMap.set(this.apiCase.id, change);
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    hasPermission,
    openHis(row) {
      this.$refs.changeHistory.open(row.id, ["接口定义用例", "接口定義用例", "Api definition case", "API_DEFINITION_CASE"]);
    },
    getColor(enable, method) {
      if (enable) {
        return this.methodColorMap.get(method);
      }
    },
    deleteCase(index, row) {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            deleteToGc(row.id).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.$emit('refresh');
            });
          }
        }
      });
    },
    singleRun(data) {
      let methods = ["SQL", "DUBBO", "dubbo://", "TCP"];
      if (data.apiMethod && methods.indexOf(data.apiMethod) === -1 && (!this.environment || this.environment === undefined)) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      mergeRequestDocumentData(data.request);
      if (data.apiMethod !== "SQL" && data.apiMethod !== "DUBBO" && data.apiMethod !== "dubbo://" && data.apiMethod !== "TCP") {
        data.request.useEnvironment = this.environment;
      } else {
        data.request.useEnvironment = data.request.environmentId;
      }
      this.$emit('singleRun', data);
    },
    stop(data) {
      this.$emit('stop', data.id);
    },
    copyCase(data) {
      if (data && data.request) {
        let uuid = getUUID();
        let request = JSON.parse(JSON.stringify(data.request));
        request.id = uuid;
        let obj = {
          name: "copy_" + data.name,
          priority: data.priority,
          active: true,
          tags: data.tags,
          request: request,
          uuid: uuid
        };
        this.$emit('copyCase', obj);
      }
    },
    selectTestCase(item, $event) {
      if (!item.id || !this.loaded) {
        return;
      }
      if ($event.currentTarget.className.indexOf('is-selected') > 0) {
        $event.currentTarget.className = "el-card is-always-shadow";
        this.currentApi.request = this.beforeRequest;
      } else {
        if (this.selectedEvent.currentTarget != undefined) {
          this.selectedEvent.currentTarget.className = "el-card is-always-shadow";
        }
        this.selectedEvent.currentTarget = $event.currentTarget;
        $event.currentTarget.className = "el-card is-always-shadow is-selected";
        this.currentApi.request = item.request;
        this.currentApi.request.changeId = getUUID();
      }
      this.$emit("setSelectedCaseId", item.id);
    },
    changePriority(row) {
      if (row.id) {
        this.saveTestCase(row);
      }
    },
    setParameters(data) {
      data.projectId = getCurrentProjectID();
      data.request.name = data.name;
      if (data.protocol === "DUBBO" || data.protocol === "dubbo://") {
        data.request.protocol = "dubbo://";
      } else {
        data.request.protocol = data.protocol;
      }
      data.id = data.request.id;
      if (!data.method) {
        data.method = data.protocol;
      }
    },
    addModule(row) {
      this.saveApi(row, "default-module");
    },
    saveApi(row, module) {
      let data = this.api;
      data.name = this.apiCase.name;
      data.moduleId = module;
      data.modulePath = "/" + this.$t('commons.module_title');
      this.setParameters(data);
      let bodyFiles = this.getBodyUploadFiles(data);
      createDefinition(null, bodyFiles, data).then(() => {
        if (row) {
          this.api.saved = false;
          row.apiDefinitionId = data.id;
          this.saveCase(row);
        }
      });
    },
    reload() {
      this.saveLoading = true
      this.$nextTick(() => {
        this.saveLoading = false;
        if (this.apiCase.id) {
          store.apiCaseMap.set(this.apiCase.id, 0);
        }
      });
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    saveCase(row, hideAlert) {
      this.isSave = true;
      let tmp = JSON.parse(JSON.stringify(row));
      this.isShowInput = false;
      tmp.request.body = row.request.body;
      let bodyFiles = this.getBodyUploadFiles(tmp);
      tmp.projectId = getCurrentProjectID();
      tmp.active = true;
      tmp.request.useEnvironment = this.environment;
      tmp.apiDefinitionId = tmp.apiDefinitionId || this.api.id;
      let url = "/api/testcase/create";
      if (tmp.id) {
        url = "/api/testcase/update";
      } else {
        tmp.request.id = getUUID();
        tmp.id = tmp.request.id;
        row.request.id = tmp.request.id;
        tmp.request.path = this.api.path;
        tmp.versionId = this.api.versionId;
        if (tmp.request.protocol != "dubbo://" && tmp.request.protocol != "DUBBO") {
          tmp.request.method = this.api.method;
        }
      }

      if (tmp.request.esbDataStruct != null) {
        tmp.esbDataStruct = JSON.stringify(tmp.request.esbDataStruct);
      }
      if (tmp.request.backEsbDataStruct != null) {
        tmp.backEsbDataStruct = JSON.stringify(tmp.request.backEsbDataStruct);
      }

      if (tmp.tags instanceof Array) {
        tmp.tags = JSON.stringify(tmp.tags);
      }
      if (tmp.request) {
        tmp.request.clazzName = TYPE_TO_C.get(tmp.request.type);
        if (tmp.request.authManager) {
          tmp.request.authManager.clazzName = TYPE_TO_C.get(tmp.request.authManager.type);
        }
        this.sort(tmp.request.hashTree);
      }
      this.result = editApiCase(url, null, bodyFiles, tmp).then((response) => {
        let data = response.data.data;
        row.id = data.id;
        row.createTime = data.createTime;
        row.updateTime = data.updateTime;
        this.compare = [];
        row.type = null;
        this.$success(this.$t('commons.save_success'));
        this.tagCount = 0;
        this.requestCount = 0;
        this.reload();
        this.isSave = false;
        // 刷新编辑后用例列表
        if (this.api.source === "editCase") {
          this.$emit('reLoadCase');
        }
        if (!hideAlert) {
          this.$emit('refresh');
        }
      }, (error) => {
        this.isSave = false;
      });
    },
    saveTestCase(row, hideAlert) {
      if (this.validate(row)) {
        return;
      }
      mergeRequestDocumentData(this.apiCase.request);
      if (hasLicense()) {
        this.readyToSaveCase = row;
        this.readyToHideAlert = hideAlert;
        this.syncCaseVisible = this.validCaseRestChange();
      }
      if (!this.syncCaseVisible) {
        this.compare = [];
        if (this.compare.indexOf(row.id) === -1) {
          this.compare.push(row.id);
          if (this.api.saved) {
            this.addModule(row);
          } else {
            this.api.source = "editCase";
            if (!this.isSave) {
              this.saveCase(row, hideAlert);
            }
          }
        }
      }
    },
    validCaseRestChange() {
      let syncCaseVisible = false;
      if (this.citedScenarioCount === 0) {
        return false;
      }
      if (this.apiCase.request.headers && this.beforeUpdateRequest.headers) {
        let submitRequestHeaders = JSON.stringify(this.apiCase.request.headers);
        let beforeRequestHeaders = JSON.stringify(this.beforeUpdateRequest.headers);
        if ((submitRequestHeaders !== beforeRequestHeaders) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (this.apiCase.request.arguments && this.beforeUpdateRequest.arguments) {
        let submitRequestQuery = JSON.stringify(this.apiCase.request.arguments);
        let beforeRequestQuery = JSON.stringify(this.beforeUpdateRequest.arguments);
        if ((submitRequestQuery !== beforeRequestQuery) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (this.apiCase.request.rest && this.beforeUpdateRequest.rest) {
        let submitRequestRest = JSON.stringify(this.apiCase.request.rest);
        let beforeRequestRest = JSON.stringify(this.beforeUpdateRequest.rest);
        if ((submitRequestRest !== beforeRequestRest) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (this.apiCase.request.body && this.beforeUpdateRequest.body) {
        let submitRequestBody = JSON.stringify(this.apiCase.request.body);
        let beforeRequestBody = JSON.stringify(this.beforeUpdateRequest.body);
        if ((submitRequestBody !== beforeRequestBody) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (this.apiCase.request.authManager && this.beforeUpdateRequest.authManager) {
        let submitRequestAuthManager = JSON.stringify(this.apiCase.request.authManager);
        let beforeRequestAuthManager = JSON.stringify(this.beforeUpdateRequest.authManager);
        if ((submitRequestAuthManager !== beforeRequestAuthManager) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (this.apiCase.request.hashTree && this.beforeUpdateRequest.hashTree) {
        let submitRequestHashTree = JSON.stringify(this.apiCase.request.hashTree);
        let beforeRequestHashTree = JSON.stringify(this.beforeUpdateRequest.hashTree);
        if ((submitRequestHashTree !== beforeRequestHashTree) && !this.noShowSyncRuleRelation) {
          syncCaseVisible = true;
        }
      }
      if (((this.apiCase.request.connectTimeout !== this.beforeUpdateRequest.connectTimeout) || (this.apiCase.request.responseTimeout !== this.beforeUpdateRequest.responseTimeout)
        || (this.apiCase.request.followRedirects !== this.beforeUpdateRequest.followRedirects) || (this.apiCase.request.alias !== this.beforeUpdateRequest.alias)
        || this.caseSyncRuleRelation.showUpdateRule === true) && !this.noShowSyncRuleRelation) {
        syncCaseVisible = true;
      }
      return syncCaseVisible;
    },
    showInput(row) {
      this.isShowInput = true;
      row.active = true;
      this.active(row);
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    },
    active(item) {
      item.active = !item.active;
    },
    validate(row) {
      if (!row.name) {
        this.$warning(this.$t('api_test.input_name'));
        return true;
      }
      if (row.name.length > 100) {
        this.$warning(this.$t('api_test.input_name_length'));
        return true;
      }
    },
    showExecResult(item) {
      item.active = true;
      item.isActive = true;
    },
    getBodyUploadFiles(row) {
      let bodyUploadFiles = [];
      row.bodyUploadIds = [];
      _getBodyUploadFiles(row.request, bodyUploadFiles, row);
      return bodyUploadFiles;
    },
    showHistory(id) {
      this.$emit("showHistory", id);
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.apiCase.follows.length; i++) {
          if (this.apiCase.follows[i] === this.currentUser().id) {
            this.apiCase.follows.splice(i, 1)
            break;
          }
        }
        if (this.apiCase.id) {
          editFollowsByParam(this.apiCase.id, this.apiCase.follows).then(() => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.apiCase.follows) {
          this.apiCase.follows = [];
        }
        this.apiCase.follows.push(this.currentUser().id)
        if (this.apiCase.id) {
          editFollowsByParam(this.apiCase.id, this.apiCase.follows).then(() => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    gotoApiMessage() {
      let apiResolve = this.$router.resolve({
        name: 'MessageSettings'
      });
      window.open(apiResolve.href, '_blank');
    },
    saveCaseAndNotice() {
      if (hasLicense()) {
        this.caseSyncRuleRelation.resourceId = this.apiCase.id;
        this.caseSyncRuleRelation.resourceType = "CASE";
        this.saveCaseSyncRuleRelation(this.caseSyncRuleRelation);
      }
    },
    saveCaseSyncRuleRelation(caseSyncRuleRelation) {
      this.saveLoading = true;
      updateRuleRelation(caseSyncRuleRelation.resourceId, caseSyncRuleRelation).then(() => {
        this.compare = [];
        if (this.compare.indexOf(this.readyToSaveCase.id) === -1) {
          this.compare.push(this.readyToSaveCase.id);
          if (this.api.saved) {
            this.addModule(this.readyToSaveCase);
          } else {
            this.api.source = "editCase";
            if (!this.isSave) {
              this.saveCase(this.readyToSaveCase, this.readyToHideAlert);
            }
          }
        }
        this.syncCaseVisible = false;
      });
    },
    getSyncRule() {
      relationGet(this.apiCase.id, 'CASE').then(response => {
        if (response.data) {
          this.caseSyncRuleRelation = response.data;
          if (this.caseSyncRuleRelation.scenarioCreator !== false) {
            this.caseSyncRuleRelation.scenarioCreator = true;
          }
          this.noShowSyncRuleRelation = this.caseSyncRuleRelation.showUpdateRule
          this.$EventBus.$emit('showXpackCaseBtn', this.caseSyncRuleRelation.showUpdateRule);
        }
      });
    },
    handleXpackCaseSetChange(noShowSyncRuleRelation) {
      this.noShowSyncRuleRelation = noShowSyncRuleRelation
    },
    getCitedScenarioCount() {
      citedScenarioCount(this.apiCase.id).then(response => {
        if (response.data) {
          this.citedScenarioCount = response.data;
        }
      });
    }
  }
}
</script>

<style scoped>
.ms-api-select {
  margin-left: 10px;
  width: 65px;
}

.ms-api-header-select {
  margin-left: 20px;
  min-width: 100px;
}

.is-selected {
  background: #EFF7FF;
}

.icon.is-active {
  transform: rotate(90deg);
}

.api-el-tag {
  color: white;
}

.tag-item {
  margin-right: 20px;
}

.ms-col-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 150px;
}

.stop-btn {
  background-color: #E62424;
  border-color: #dd3636;
  color: white;
}

:deep(.el-card__body) {
  padding: 5px 10px;
}

.esb-div :deep(.el-table) {
  overflow: auto;
}
</style>
