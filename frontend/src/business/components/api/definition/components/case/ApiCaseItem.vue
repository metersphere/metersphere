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

            <el-link type="primary" style="margin-left: 10px" @click="openHis(apiCase)" v-if="apiCase.id">{{ $t('operating_log.change_history') }}</el-link>
          </span>
          <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
            <span style="margin-left: 10px">
              {{ apiCase.updateTime | timestampFormatDate }}
              {{ apiCase.updateUser }} {{ $t('api_test.definition.request.update_info') }}
          </span>
          </div>
        </el-col>
        <el-col :span="2">
          <el-select size="mini" v-model="apiCase.priority" class="ms-api-select" @change="changePriority(apiCase)" :disabled="loaded">
            <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
          </el-select>
        </el-col>
        <el-col :span="api.protocol==='HTTP'?4:0">
          <span v-if="api.protocol==='HTTP'">
            <el-tag size="mini" :style="{'background-color': getColor(true, apiCase.request.method), border: getColor(true, apiCase.request.method)}"
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
              <el-select size="small" v-model="apiCase.caseStatus" style="margin-right: 5px" @change="saveTestCase(apiCase,true)" :disabled="loaded">
                <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
              </el-select>
            </el-col>
            <el-col :span="16">
              <div class="tag-item" @click.stop>
                <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
                  <i class="el-icon-star-off" style="color: #783987; font-size: 25px; margin-top: 2px; margin-right: 15px;cursor: pointer " @click="saveFollow"/>
                </el-tooltip>
                <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
                  <i class="el-icon-star-on" style="color: #783987; font-size: 28px; margin-top: 2px; margin-right: 15px;cursor: pointer " @click="saveFollow" v-if="showFollow"/>
                </el-tooltip>
              </div>
            </el-col>
          </el-row>
          <el-row style="margin-top: 5px">
            <div class="tag-item" @click.stop>
              <ms-input-tag :currentScenario="apiCase" ref="tag" @keyup.enter.native="saveTestCase(apiCase,true)" :disabled="loaded"/>
            </div>
          </el-row>
        </el-col>

        <el-col :span="3">
          <span @click.stop v-if="!loaded">
            <ms-tip-button @click="singleRun(apiCase)" :tip="$t('api_test.run')" icon="el-icon-video-play" v-permission="['PROJECT_API_DEFINITION:READ+RUN']"
                           class="run-button" size="mini" :disabled="!apiCase.id || loaded" circle v-if="!loading"/>
            <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
              <el-button :disabled="!apiCase.id" @click.once="stop(apiCase)" size="mini" style="color:white;padding: 0;width: 28px;height: 28px;" class="stop-btn" circle>
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
          <el-link @click.stop type="danger" v-if="apiCase.execResult && apiCase.execResult==='error'" @click="showExecResult(apiCase)">
            {{ getResult(apiCase.execResult) }}
          </el-link>
          <el-link @click.stop v-else-if="apiCase.execResult && apiCase.execResult==='success'" @click="showExecResult(apiCase)">
            {{ getResult(apiCase.execResult) }}
          </el-link>
          <div v-else> {{ getResult(apiCase.execResult) }}</div>
          <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
            <span> {{ apiCase.execTime | timestampFormatDate }}</span>
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
      <div v-if="apiCase.active||type==='detail'" v-loading="loading">
        <el-divider></el-divider>
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <ms-api-request-form :isShowEnable="true" :showScript="true" :headers="apiCase.request.headers "
                             :response="apiCase.responseData" :request="apiCase.request" v-if="api.protocol==='HTTP'"/>
        <tcp-format-parameters :showScript="true" :show-pre-script="true" :request="apiCase.request"
                               :response="apiCase.responseData"
                               v-if="api.method==='TCP'"/>
        <esb-definition v-xpack :request="apiCase.request" :show-pre-script="true" :showScript="true"
                        :response="apiCase.responseData"
                        v-if="isXpack&&api.method==='ESB'" ref="esbDefinition"/>
        <ms-sql-basis-parameters :showScript="true" :request="apiCase.request" :response="apiCase.responseData"
                                 v-if="api.protocol==='SQL'"/>
        <ms-dubbo-basis-parameters :showScript="true" :request="apiCase.request" :response="apiCase.responseData"
                                   v-if="api.protocol==='DUBBO'"/>
        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
        <div v-if="isXpack&&api.method==='ESB'">
          <esb-definition-response v-xpack v-if="isXpack" :currentProtocol="apiCase.request.protocol" :request="apiCase.request" :is-api-component="false" :show-options-button="false" :show-header="true" :api-item="apiCase"/>
        </div>
        <div v-else>
          <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase" :result="runResult"/>
        </div>
      </div>
    </el-collapse-transition>
    <ms-change-history ref="changeHistory"/>
  </el-card>


</template>

<script>
import {_getBodyUploadFiles, getCurrentProjectID, getCurrentUser, getUUID} from "@/common/js/utils";
import {API_STATUS, PRIORITY} from "../../model/JsonData";
import MsTag from "../../../../common/components/MsTag";
import MsTipButton from "../../../../common/components/MsTipButton";
import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import ApiEnvironmentConfig from "../environment/ApiEnvironmentConfig";
import MsApiAssertions from "../assertion/ApiAssertions";
import MsSqlBasisParameters from "../request/database/BasisParameters";
import TcpFormatParameters from "@/business/components/api/definition/components/request/tcp/TcpFormatParameters";
import MsDubboBasisParameters from "../request/dubbo/BasisParameters";
import MsApiExtendBtns from "../reference/ApiExtendBtns";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsRequestResultTail from "../response/RequestResultTail";
import ApiResponseComponent from "../../../automation/scenario/component/ApiResponseComponent";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
import {API_METHOD_COLOUR} from "../../model/JsonData";
import MsChangeHistory from "../../../../history/ChangeHistory";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import {hasPermission} from '@/common/js/utils';
import ApiCaseHeader from "./ApiCaseHeader";

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
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default ,
    ApiCaseHeader
  },
  data() {
    return {
      options: API_STATUS,
      result: {},
      grades: [],
      resultMap: new Map([
        ['success', this.$t('test_track.plan_view.execute_result') + '：' + this.$t('test_track.plan_view.pass')],
        ['error', this.$t('test_track.plan_view.execute_result') + '：' + this.$t('api_test.home_page.detail_card.execution_failed')],
        ['errorReportResult', this.$t('test_track.plan_view.execute_result') + '：' + this.$t('error_report_library.option.name')],
        ['default', this.$t('test_track.plan_view.execute_result') + '：' + this.$t('api_test.home_page.detail_card.unexecute')]
      ]),
      isXpack: false,
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
      compare: [],
      isSave: false
    }
  },
  props: {
    runResult: {},
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
    maintainerOptions: Array,
  },
  created() {
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
      this.isXpack = true;
    }
    if (this.apiCase && this.apiCase.id) {
      this.showFollow = false;
      this.$get('/api/testcase/follow/' + this.apiCase.id, response => {
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
  },
  methods: {
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
            this.$get('/api/testcase/deleteToGc/' + row.id, () => {
              this.$success(this.$t('commons.delete_success'));
              this.$emit('refresh');
            });
          }
        }
      });
    },
    singleRun(data) {
      if (this.api.protocol !== "SQL" && this.api.protocol != "DUBBO" && this.api.protocol != "dubbo://" && !this.environment) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      data.message = true;
      data.request.useEnvironment = this.environment;
      this.saveTestCase(data);
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
        let obj = {name: "copy_" + data.name, priority: data.priority, active: true, tags: data.tags, request: request, uuid: uuid};
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
      this.$fileUpload("/api/definition/create", null, bodyFiles, data, () => {
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
        this.saveLoading = false
      });
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
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
      this.result = this.$fileUpload(url, null, bodyFiles, tmp, (response) => {
        let data = response.data;
        row.id = data.id;
        row.createTime = data.createTime;
        row.updateTime = data.updateTime;
        this.compare = [];
        if (!row.message) {
          this.$success(this.$t('commons.save_success'));
          this.reload();
          this.isSave = false;
          // 刷新编辑后用例列表
          if (this.api.source === "editCase") {
            this.$emit('reLoadCase');
          }
          if (!hideAlert) {
            this.$emit('refresh');
          }
        }
      }, (error) => {
        this.isSave = false;
      });
    },
    saveTestCase(row, hideAlert) {
      if (this.validate(row)) {
        return;
      }
      this.compare = [];
      if (this.compare.indexOf(row.id) === -1) {
        this.compare.push(row.id);
        if (this.api.saved) {
          this.addModule(row);
        } else {
          this.api.source = "editCase";
          if (!this.isSave){
            this.saveCase(row, hideAlert);
          }
        }
      }
    },
    showInput(row) {
      // row.type = "create";
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
    getResult(data) {
      if (this.resultMap.get(data)) {
        return this.resultMap.get(data);
      } else {
        return this.resultMap.get("default");
      }
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
          this.$post("/api/testcase/update/follows/" + this.apiCase.id, this.apiCase.follows, () => {
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
          this.$post("/api/testcase/update/follows/" + this.apiCase.id, this.apiCase.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
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

.ms-api-label {
  color: #CCCCCC;
}

.ms-api-col {
  background-color: #7C3985;
  border-color: #7C3985;
  margin-right: 10px;
  color: white;
}

.is-selected {
  background: #EFF7FF;
}

.icon.is-active {
  transform: rotate(90deg);
}

.item-select {
  margin-right: 10px;
}

.ms-opt-btn {
  position: fixed;
  left: 60px;
  z-index: 1;
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
</style>
