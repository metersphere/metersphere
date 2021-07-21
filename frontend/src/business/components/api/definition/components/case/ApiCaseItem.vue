<template>
  <el-card v-loading="result.loading" style="margin-top: 5px" @click.native="selectTestCase(apiCase,$event)">
    <div @click="active(apiCase)" v-if="type!=='detail'">
      <el-row>
        <el-col :span="3">
          <el-row>
            <el-col :span="2" style="margin-top: 5px">
              <el-checkbox class="item-select" v-model="apiCase.selected"/>
            </el-col>
            <el-col :span="2" style="margin-top: 2px">
              <show-more-btn :is-show="apiCase.selected" :buttons="buttons" :size="selectSize"/>
            </el-col>
            <el-col :span="20">
              <div class="el-step__icon is-text ms-api-col">
                <div class="el-step__icon-inner">{{ index + 1 }}</div>
              </div>
              <el-select size="mini" v-model="apiCase.priority" class="ms-api-select" @change="changePriority(apiCase)">
                <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
              </el-select>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="api.protocol==='HTTP'?6:10">
          <span @click.stop>
            <i class="icon el-icon-arrow-right" :class="{'is-active': apiCase.active}" @click="active(apiCase)"/>
            <el-input v-if="!apiCase.id || isShowInput" size="small" v-model="apiCase.name" :name="index" :key="index"
                      class="ms-api-header-select" style="width: 180px"
                      @blur="saveTestCase(apiCase,true)" :placeholder="$t('commons.input_name')" ref="nameEdit"/>
            <span v-else>
                <span>{{ apiCase.id ? apiCase.name : '' }}</span>
              <i class="el-icon-edit" style="cursor:pointer" @click="showInput(apiCase)"/>
            </span>

            <el-link type="primary" style="margin-left: 10px" @click="openHis(apiCase)" v-if="apiCase.id">{{$t('operating_log.change_history')}}</el-link>
          </span>
          <div v-if="apiCase.id" style="color: #999999;font-size: 12px">
            <!--<span>-->
            <!--{{ apiCase.createTime | timestampFormatDate }}-->
            <!--{{ apiCase.createUser }} {{ $t('api_test.definition.request.create_info') }}-->
            <!--</span>-->
            <span style="margin-left: 10px">
              {{ apiCase.updateTime | timestampFormatDate }}
              {{ apiCase.updateUser }} {{ $t('api_test.definition.request.update_info') }}
          </span>
          </div>
        </el-col>
        <el-col :span="api.protocol==='HTTP'?4:0">
          <span v-if="api.protocol==='HTTP'">
            <el-tag size="mini" :style="{'background-color': getColor(true, apiCase.request.method), border: getColor(true, apiCase.request.method)}"
                    class="api-el-tag">
                {{ apiCase.request.method }}
            </el-tag>
            <el-tooltip :content="apiCase.request.path">
              <span class="ms-col-name">{{apiCase.request.path}}</span>
            </el-tooltip>
         </span>
        </el-col>
        <el-col :span="4">
          <div class="tag-item" @click.stop>
            <ms-input-tag :currentScenario="apiCase" ref="tag" @keyup.enter.native="saveTestCase(apiCase,true)"/>
          </div>
        </el-col>

        <el-col :span="4">
          <span @click.stop>
            <ms-tip-button @click="singleRun(apiCase)" :tip="$t('api_test.run')" icon="el-icon-video-play"
                          class="run-button" size="mini" :disabled="!apiCase.id" circle/>
            <ms-tip-button @click="copyCase(apiCase)" :tip="$t('commons.copy')" icon="el-icon-document-copy"
                           size="mini" :disabled="!apiCase.id || isCaseEdit" circle/>
            <ms-tip-button @click="deleteCase(index,apiCase)" :tip="$t('commons.delete')" icon="el-icon-delete"
                           size="mini" :disabled="!apiCase.id || isCaseEdit" circle/>
            <ms-api-extend-btns :is-case-edit="isCaseEdit" :environment="environment" :row="apiCase"/>
          </span>
        </el-col>

        <el-col :span="3">
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
      </el-row>
    </div>

    <!-- 请求参数-->
    <el-collapse-transition>
      <div v-if="apiCase.active||type==='detail'">
        <el-divider></el-divider>
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <ms-api-request-form :isShowEnable="true" :showScript="true" :is-read-only="isReadOnly" :headers="apiCase.request.headers " :request="apiCase.request" v-if="api.protocol==='HTTP'"/>
        <tcp-format-parameters :showScript="true" :request="apiCase.request" v-if="api.method==='TCP' && apiCase.request.esbDataStruct == null"/>
        <esb-definition v-xpack :request="apiCase.request" :showScript="true" v-if="showXpackCompnent&&api.method==='ESB'" ref="esbDefinition"/>
        <ms-sql-basis-parameters :showScript="true" :request="apiCase.request" v-if="api.protocol==='SQL'"/>
        <ms-dubbo-basis-parameters :showScript="true" :request="apiCase.request" v-if="api.protocol==='DUBBO'"/>

        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{$t('api_test.definition.request.res_param')}}</p>
        <div v-if="showXpackCompnent&&api.method==='ESB'">
          <esb-definition-response v-xpack v-if="showXpackCompnent" :currentProtocol="apiCase.request.protocol" :request="apiCase.request" :is-api-component="false" :show-options-button="false" :show-header="true" :api-item="apiCase"/>
        </div>
        <div v-else>
          <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase" :result="runResult"/>
        </div>

        <ms-jmx-step :request="apiCase.request" :response="apiCase.responseData"/>
        <!-- 保存操作 -->
        <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(apiCase)"
                   v-if="type!=='detail'">
          {{ $t('commons.save') }}
        </el-button>
      </div>
    </el-collapse-transition>
    <ms-change-history ref="changeHistory"/>

  </el-card>

</template>

<script>
  import {_getBodyUploadFiles, getCurrentProjectID, getUUID} from "@/common/js/utils";
  import {PRIORITY, RESULT_MAP} from "../../model/JsonData";
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
  import MsJmxStep from "../step/JmxStep";
  import ApiResponseComponent from "../../../automation/scenario/component/ApiResponseComponent";
  import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";

  const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
  const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
  const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
  import {API_METHOD_COLOUR} from "../../model/JsonData";
  import MsChangeHistory from "../../../../history/ChangeHistory";

  export default {
    name: "ApiCaseItem",
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
      MsJmxStep,
      ShowMoreBtn,
      MsChangeHistory,
      "esbDefinition": esbDefinition.default,
      "esbDefinitionResponse": esbDefinitionResponse.default
    },
    data() {
      return {
        result: {},
        grades: [],
        showXpackCompnent: false,
        isReadOnly: false,
        selectedEvent: Object,
        priorities: PRIORITY,
        runData: [],
        reportId: "",
        checkedCases: new Set(),
        visible: false,
        condition: {},
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        isShowInput: false,
        buttons: [
          {name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleRunBatch},
          {name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleEditBatch}
        ],
        methodColorMap: new Map(API_METHOD_COLOUR),
      }
    },
    props: {
      runResult:{},
      selectSize: Number,
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
      type: String,
      isCaseEdit: Boolean,
    },
    created() {
      if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
        this.showXpackCompnent = true;
      }
    },
    watch: {
      'apiCase.selected'(){
        this.$emit('apiCaseSelected');
      }
    },
    methods: {
      openHis(row) {
        this.$refs.changeHistory.open(row.id);
      },
      handleRunBatch() {
        this.$emit('batchRun');
      },
      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
      handleEditBatch() {
        this.$emit('batchEditCase');
      },
      deleteCase(index, row) {
        this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + ' ' + row.name + " ？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$get('/api/testcase/delete/' + row.id, () => {
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
          this.$emit('selectTestCase', null);
        } else {
          if (this.selectedEvent.currentTarget != undefined) {
            this.selectedEvent.currentTarget.className = "el-card is-always-shadow";
          }
          this.selectedEvent.currentTarget = $event.currentTarget;
          $event.currentTarget.className = "el-card is-always-shadow is-selected";
          this.$emit('selectTestCase', item);
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
      saveCase(row, hideAlert) {
        let tmp = JSON.parse(JSON.stringify(row));
        this.isShowInput = false;
        if (this.validate(tmp)) {
          return;
        }
        tmp.request.body = row.request.body;
        let bodyFiles = this.getBodyUploadFiles(tmp);
        tmp.projectId = getCurrentProjectID();
        tmp.active = true;
        tmp.apiDefinitionId = tmp.apiDefinitionId || this.api.id;
        let url = "/api/testcase/create";
        if (tmp.id) {
          url = "/api/testcase/update";
        } else {
          tmp.id = tmp.request.id;
          tmp.request.path = this.api.path;
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
        this.result = this.$fileUpload(url, null, bodyFiles, tmp, (response) => {
          let data = response.data;
          row.id = data.id;
          row.createTime = data.createTime;
          row.updateTime = data.updateTime;
          if (!row.message) {
            this.$success(this.$t('commons.save_success'));
            if (!hideAlert) {
              this.$emit('refresh');
            }
          }
        });
      },
      saveTestCase(row, hideAlert) {
        if (this.api.saved) {
          this.addModule(row);
        } else {
          this.saveCase(row, hideAlert);
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
        if (RESULT_MAP.get(data)) {
          return RESULT_MAP.get(data);
        } else {
          return RESULT_MAP.get("default");
        }
      },
      validate(row) {
        if (!row.name) {
          this.$warning(this.$t('api_test.input_name'));
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
</style>
