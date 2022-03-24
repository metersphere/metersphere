<template>
  <ms-container>
    <ms-aside-container>
      <div @click="showAll">

        <div class="tip">{{ $t('test_track.plan_view.base_info') }}</div>
        <el-form :model="currentScenario" label-position="right" label-width="80px" size="small" :rules="rules"
                 ref="currentScenario">
          <!-- 基础信息 -->
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input class="ms-scenario-input" size="small" v-model="currentScenario.name"/>
          </el-form-item>

          <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
            <ms-select-tree size="small" :data="moduleOptions" :defaultKey="currentScenario.apiScenarioModuleId"
                            @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>
          </el-form-item>

          <el-form-item :label="$t('commons.status')" prop="status">
            <el-select class="ms-scenario-input" size="small" v-model="currentScenario.status">
              <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
            </el-select>
          </el-form-item>

          <el-form-item :label="$t('api_test.definition.request.responsible')" prop="principal">
            <el-select v-model="currentScenario.principal"
                       :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                       class="ms-scenario-input">
              <el-option
                v-for="item in maintainerOptions"
                :key="item.id"
                :label="item.name + ' (' + item.id + ')'"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item :label="$t('test_track.case.priority')" prop="level">
            <el-select class="ms-scenario-input" size="small" v-model="currentScenario.level">
              <el-option v-for="item in levels" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-form-item>

          <el-form-item :label="$t('api_test.automation.tag')" prop="tags">
            <ms-input-tag :currentScenario="currentScenario" ref="tag"/>
          </el-form-item>

          <el-form-item :label="$t('commons.description')" prop="description">
            <el-input class="ms-http-textarea"
                      v-model="currentScenario.description"
                      type="textarea"
                      :autosize="{ minRows: 1, maxRows: 10}"
                      :rows="1" size="small"/>
          </el-form-item>
          <el-form-item label="ID" prop="customNum" v-if="customNum">
            <el-input v-model.trim="currentScenario.customNum" size="small"></el-input>
          </el-form-item>

        </el-form>
      </div>
    </ms-aside-container>

    <!-- 右侧部分 -->
    <ms-main-container style="overflow: hidden" class="ms-scenario-main-container">
      <!-- header 调试部分 -->
      <div class="ms-debug-div" @click="showAll" ref="debugHeader">
        <el-row style="margin: 5px">
          <el-col :span="1" class="ms-col-one ms-font" v-show="scenarioDefinition.length > 1">
            <el-tooltip :content="$t('test_track.case.batch_operate')" placement="top" effect="light"
                        v-show="!isBatchProcess">
              <font-awesome-icon class="ms-batch-btn" :icon="['fa', 'bars']" v-prevent-re-click
                                 @click="batchProcessing"/>
            </el-tooltip>
            <el-checkbox v-show="isBatchProcess" v-model="isCheckedAll" @change="checkedAll"/>
            <el-tooltip :content="$t('commons.cancel')" placement="top" effect="light" v-show="isBatchProcess">
              <font-awesome-icon class="ms-batch-btn" :icon="['fa', 'times']" v-prevent-re-click
                                 @click="cancelBatchProcessing"/>
            </el-tooltip>
          </el-col>
          <el-col :span="2" class="ms-col-one ms-font">
            {{ $t('api_test.automation.step_total') }}：{{ scenarioDefinition.length }}
          </el-col>
          <el-col :span="2" class="ms-col-one ms-font">
            <el-link class="head" @click="showScenarioParameters">{{ $t('api_test.automation.scenario_total') }}
            </el-link>
            ：{{ getVariableSize() }}
          </el-col>
          <el-col :span="2" class="ms-col-one ms-font">
            <el-checkbox v-model="enableCookieShare"><span
              style="font-size: 13px;">{{ $t('api_test.scenario.share_cookie') }}</span></el-checkbox>
          </el-col>
          <el-col :span="3" class="ms-col-one ms-font">
            <el-checkbox v-model="onSampleError"><span style="font-size: 13px;">{{
                $t('commons.failure_continues')
              }}</span></el-checkbox>
          </el-col>

          <el-col :span="13">
            <env-popover :disabled="scenarioDefinition.length < 1" :env-map="projectEnvMap"
                         :project-ids="projectIds" :result="envResult"
                         :environment-type.sync="environmentType" :isReadOnly="scenarioDefinition.length < 1"
                         :group-id="envGroupId" :project-list="projectList"
                         :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                         @setProjectEnvMap="setProjectEnvMap" @setEnvGroup="setEnvGroup"
                         @showPopover="showPopover" :has-option-group="true"
                         ref="envPopover" class="ms-message-right"/>
            <el-tooltip v-if="!debugLoading" content="Ctrl + R" placement="top">
              <el-dropdown split-button type="primary" @click="runDebug" class="ms-message-right" size="mini"
                           @command="handleCommand"
                           v-permission="['PROJECT_API_SCENARIO:READ+EDIT', 'PROJECT_API_SCENARIO:READ+CREATE']">
                {{ $t('api_test.request.debug') }}
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>{{ $t('api_test.automation.generate_report') }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-tooltip>
            <el-button size="mini" type="primary" v-else @click="stop">{{ $t('report.stop_btn') }}</el-button>

            <el-button id="inputDelay" type="primary" size="mini" v-prevent-re-click @click="editScenario"
                       title="ctrl + s"
                       v-permission="['PROJECT_API_SCENARIO:READ+EDIT', 'PROJECT_API_SCENARIO:READ+CREATE', 'PROJECT_API_SCENARIO:READ+COPY']">
              {{ $t('commons.save') }}
            </el-button>

            <el-tooltip class="item" effect="dark" :content="$t('commons.refresh')" placement="top-start">
              <el-button :disabled="scenarioDefinition.length < 1" size="mini" icon="el-icon-refresh"
                         v-prevent-re-click @click="getApiScenario"></el-button>
            </el-tooltip>
            <!--操作按钮-->
            <el-link type="primary" @click.stop @click="showHistory" style="margin: 0px 5px">
              {{ $t("commons.debug_history") }}
            </el-link>

            <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-show="!showFollow">
              <i class="el-icon-star-off"
                 style="color: #783987; font-size: 22px; margin-right: 5px;cursor: pointer;position: relative; top: 3px; "
                 @click="saveFollow"/>
            </el-tooltip>
            <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-show="showFollow">
              <i class="el-icon-star-on"
                 style="color: #783987; font-size: 22px; margin-right: 5px;cursor: pointer;position: relative; top: 3px; "
                 @click="saveFollow"/>
            </el-tooltip>
            <el-link type="primary" style="margin-right: 5px" @click="openHis"
                     v-show="path === '/api/automation/update'">{{ $t('operating_log.change_history') }}
            </el-link>
            <!--  版本历史 -->
            <ms-version-history v-xpack
                                ref="versionHistory"
                                :version-data="versionData"
                                :current-id="currentScenario.id"
                                @compare="compare" @checkout="checkout" @create="create" @del="del"/>
          </el-col>
          <el-tooltip effect="dark" :content="$t('commons.full_screen_editing')"
                      placement="top-start" style="margin-top: 6px">
            <font-awesome-icon class="alt-ico" :icon="['fa', 'expand-alt']" size="lg" @click="fullScreen"/>
          </el-tooltip>
        </el-row>
      </div>

      <div class="card-content">
        <!-- 场景步骤-->
        <div v-loading="loading">
          <el-row>
            <el-col :span="21">
              <div class="ms-debug-result" v-show="debug">
                <span class="ms-message-right"> {{ reqTotalTime }} ms </span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_total') }} {{ reqTotal }}</span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_success') }} {{ reqSuccess }}</span>
                <span class="ms-message-right"> {{ $t('api_test.automation.request_error') }} {{ reqError }}</span>
              </div>
            </el-col>
            <el-col></el-col>
          </el-row>
          <el-row>
            <el-col :span="21">
              <!-- 场景步骤内容 -->
              <div ref="stepInfo">
                <el-tree node-key="resourceId" :props="props" :data="scenarioDefinition" class="ms-tree"
                         :expand-on-click-node="false"
                         :allow-drop="allowDrop"
                         :empty-text="$t('api_test.scenario.step_info')"
                         highlight-current
                         :show-checkbox="isBatchProcess"
                         @check-change="chooseHeadsUp"
                         @node-drag-end="allowDrag" @node-click="nodeClick" draggable ref="stepTree">

                  <el-row class="custom-tree-node" :gutter="10" type="flex" align="middle" slot-scope="{node, data}"
                          style="width: 100%">
                    <span class="custom-tree-node-col" style="padding-left:0px;padding-right:0px"
                          v-show="node && data.hashTree && data.hashTree.length > 0 && !data.isLeaf">
                      <span v-show="!node.expanded" class="el-icon-circle-plus-outline custom-node_e"
                            @click="openOrClose(node)"/>
                      <span v-show="node.expanded" class="el-icon-remove-outline custom-node_e"
                            @click="openOrClose(node)"/>
                    </span>
                    <!-- 批量操作 -->
                    <span :class="data.checkBox? 'custom-tree-node-hide' : 'custom-tree-node-col'"
                          style="padding-left: 0px; padding-right: 0px;"
                          v-show="(data.hashTree && data.hashTree.length === 0 )|| data.isLeaf">
                      <show-more-btn :is-show="node.checked" :buttons="batchOperators" :size="selectDataCounts"
                                     v-show="data.checkBox" style="margin-right: 10px"/>
                    </span>
                    <span style="width: calc(100% - 30px);">
                      <!-- 步骤组件-->
                      <ms-component-config
                        :scenario-definition="scenarioDefinition"
                        :message="message"
                        :type="data.type"
                        :scenario="data"
                        :response="response"
                        :currentScenario="currentScenario"
                        :currentEnvironmentId="currentEnvironmentId"
                        :node="node"
                        :project-list="projectList"
                        :env-map="projectEnvMap"
                        :env-group-id="envGroupId"
                        :environment-type="environmentType"
                        @remove="remove"
                        @copyRow="copyRow"
                        @suggestClick="suggestClick"
                        @refReload="refReload"
                        @runScenario="runDebug"
                        @stopScenario="stop"
                        @setDomain="setDomain"
                        @openScenario="openScenario"
                        @editScenarioAdvance="editScenarioAdvance"
                        v-if="stepFilter.get('ALlSamplerStep').indexOf(data.type) ===-1
                         || (!node.parent || !node.parent.data || stepFilter.get('AllSamplerProxy').indexOf(node.parent.data.type) === -1)"
                      />
                      <div v-else class="el-tree-node is-hidden is-focusable is-leaf" style="display: none;">
                        {{ hideNode(node) }}
                      </div>
                    </span>
                  </el-row>
                </el-tree>
              </div>
            </el-col>
            <!-- 按钮列表 -->
            <el-col :span="3">
              <div @click="fabClick"
                   v-permission="['PROJECT_API_SCENARIO:READ+EDIT', 'PROJECT_API_SCENARIO:READ+CREATE']">
                <vue-fab id="fab" mainBtnColor="#783887" size="small" :global-options="globalOptions"
                         :click-auto-close="false" v-outside-click="outsideClick" ref="refFab">
                  <fab-item
                    v-for="(item, index) in buttonData"
                    :key="index"
                    :idx="getIdx(index)"
                    :title="item.title"
                    :title-bg-color="item.titleBgColor"
                    :title-color="item.titleColor"
                    :color="item.titleColor"
                    :icon="item.icon"
                    @clickItem="item.click"/>
                </vue-fab>
              </div>
            </el-col>
          </el-row>
        </div>

        <!--参数设置-->
        <ms-api-variable-advance ref="scenarioVariableAdvance"
                                 :append-to-body="true"
                                 :current-item="currentItem"
                                 :variables="currentScenario.variables"
                                 :scenario-definition="scenarioDefinition"/>

        <!--接口列表-->
        <scenario-api-relevance @save="pushApiOrCase" @close="setHideBtn" ref="scenarioApiRelevance"
                                :is-across-space="true" v-if="type!=='detail'"/>

        <!--自定义接口-->
        <el-drawer v-if="type!=='detail'" :visible.sync="customizeVisible" :destroy-on-close="true" direction="ltr"
                   :withHeader="false" :title="$t('api_test.automation.customize_req')" style="overflow: auto"
                   :modal="false" size="90%">
          <ms-api-customize :request="customizeRequest" @addCustomizeApi="addCustomizeApi"/>
        </el-drawer>
        <!--场景导入 -->
        <scenario-relevance v-if="type!=='detail'" @save="addScenario" @close="setHideBtn" :is-across-space="true"
                            ref="scenarioRelevance"/>

        <!-- 环境 -->
        <api-environment-config v-if="type!=='detail'" ref="environmentConfig" @close="environmentConfigClose"/>

        <!--执行组件-->
        <ms-run :debug="true" v-if="type!=='detail'" :environment="projectEnvMap" :reportId="reportId" :saved="saved"
                :run-data="debugData" :environment-type="environmentType" :environment-group-id="envGroupId"
                :executeType="executeType"
                @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>
        <!-- 调试结果 -->
        <el-drawer v-if="type!=='detail'" :visible.sync="debugVisible" :destroy-on-close="true" direction="ltr"
                   :withHeader="true" :modal="false" size="90%">
          <ms-api-report-detail :scenario="currentScenario" :report-id="reportId" :debug="true"
                                :currentProjectId="projectId" @refresh="detailRefresh"/>
        </el-drawer>

        <!--场景公共参数-->
        <ms-variable-list v-if="type!=='detail'" @setVariables="setVariables" ref="scenarioParameters"
                          class="ms-sc-variable-header"/>
        <!--外部导入-->
        <api-import v-if="type!=='detail'" ref="apiImport" :saved="false" @refresh="apiImport"/>

        <!--步骤最大化-->
        <ms-drawer :visible="drawer" :size="100" @close="close" direction="default" :show-full-screen="false"
                   :is-show-close="false" style="overflow: hidden" v-if="drawer">
          <maximize-scenario
            :scenario-definition="scenarioDefinition"
            :projectIds.sync="projectIds"
            :projectList="projectList"
            :envMap="projectEnvMap"
            :moduleOptions="moduleOptions"
            :req-error="reqError"
            :req-success="reqSuccess"
            :req-total="reqTotal"
            :req-total-time="reqTotalTime"
            :currentScenario="currentScenario"
            :type="type"
            :debug="debugLoading"
            :reloadDebug="reloadDebug"
            :stepReEnable="stepEnable"
            :message="message"
            @setEnvType="setEnvType"
            @envGroupId="setEnvGroup"
            @closePage="close"
            @unFullScreen="unFullScreen"
            @showAllBtn="showAllBtn"
            @runDebug="runDebug"
            @handleCommand="handleCommand"
            @setProjectEnvMap="setProjectEnvMap"
            @showScenarioParameters="showScenarioParameters"
            @setCookieShare="setCookieShare"
            @setSampleError="setSampleError"
            @stop="stop"
            @openScenario="openScenario"
            @runScenario="runDebug"
            @stopScenario="stop"
            @editScenarioAdvance="editScenarioAdvance"
            ref="maximizeScenario"/>
        </ms-drawer>
        <ms-change-history ref="changeHistory"/>
        <el-backtop target=".card-content" :visibility-height="100" :right="20"></el-backtop>
      </div>
      <ms-task-center ref="taskCenter" :show-menu="false"/>

      <!--版本对比-->
      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        @close="closeDiff"
        width="100%">
        <scenario-diff
          v-if="dialogVisible"
          :custom-num="customNum"
          :currentScenarioId="currentScenario.id"
          :dffScenarioId="dffScenarioId"
          :scenarioRefId="scenarioRefId"
          :module-options="moduleOptions"
          :project-env-map="projectEnvMap"
          :old-enable-cookie-share="enableCookieShare"
          :old-on-sample-error="onSampleError"
          :project-list="projectList"
          :type="type"/>
      </el-dialog>

    </ms-main-container>
  </ms-container>
</template>

<script>
import {API_STATUS, PRIORITY} from "../../definition/model/JsonData";
import {buttons, setComponent} from './menu/Menu';
import {parseEnvironment} from "../../definition/model/EnvironmentModel";
import {ELEMENT_TYPE, STEP, TYPE_TO_C} from "./Setting";
import {KeyValue} from "@/business/components/api/definition/model/ApiTestModel";

import {
  getCurrentProjectID,
  getCurrentUser,
  getUUID,
  handleCtrlREvent,
  handleCtrlSEvent,
  hasLicense,
  objToStrMap,
  strMapToObj
} from "@/common/js/utils";
import "@/common/css/material-icons.css";
import OutsideClick from "@/common/js/outside-click";
import {
  savePreciseEnvProjectIds,
  saveScenario
} from "@/business/components/api/automation/api-automation";
import MsComponentConfig from "./component/ComponentConfig";
import {ENV_TYPE} from "@/common/js/constants";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

let jsonPath = require('jsonpath');
export default {
  name: "EditApiScenario",
  props: {
    moduleOptions: Array,
    currentScenario: {},
    type: String,
    customNum: {
      type: Boolean,
      default: false
    },
  },
  components: {
    'MsVersionHistory': versionHistory.default,
    MsComponentConfig,
    ScenarioDiff: () => import("@/business/components/api/automation/version/ScenarioDiff"),
    MsVariableList: () => import("./variable/VariableList"),
    ScenarioRelevance: () => import("./api/ScenarioRelevance"),
    ScenarioApiRelevance: () => import("./api/ApiRelevance"),
    ApiEnvironmentConfig: () => import("@/business/components/api/test/components/ApiEnvironmentConfig"),
    MsApiReportDetail: () => import("../report/SysnApiReportDetail"),
    MsInputTag: () => import("./MsInputTag"),
    MsRun: () => import("./DebugRun"),
    MsApiCustomize: () => import("./ApiCustomize"),
    ApiImport: () => import("../../definition/components/import/ApiImport"),
    EnvPopover: () => import("@/business/components/api/automation/scenario/EnvPopover"),
    MaximizeScenario: () => import("./maximize/MaximizeScenario"),
    MsDrawer: () => import("../../../common/components/MsDrawer"),
    MsSelectTree: () => import("../../../common/select-tree/SelectTree"),
    MsChangeHistory: () => import("../../../history/ChangeHistory"),
    MsTaskCenter: () => import("../../../task/TaskCenter"),
    MsApiVariableAdvance: () => import("./../../definition/components/ApiVariableAdvance"),
    MsMainContainer: () => import("@/business/components/common/components/MsMainContainer"),
    MsAsideContainer: () => import("@/business/components/common/components/MsAsideContainer"),
    MsContainer: () => import("@/business/components/common/components/MsContainer"),
    ShowMoreBtn: () => import( "@/business/components/track/case/components/ShowMoreBtn"),
  },
  data() {
    return {
      onSampleError: true,
      showConfigButtonWithOutPermission: false,
      props: {
        label: "label",
        isLeaf: "isLeaf",
        children: "hashTree",
        disabled: false
      },
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        apiScenarioModuleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        principal: [{required: true, message: this.$t('api_test.definition.request.responsible'), trigger: 'change'}],
        customNum: [
          {required: true, message: "ID必填", trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
      },
      environments: [],
      currentEnvironmentId: "",
      maintainerOptions: [],
      value: API_STATUS[0].id,
      options: API_STATUS,
      levels: PRIORITY,
      scenario: {},
      loading: false,
      renderComponent: true,
      apiListVisible: false,
      customizeVisible: false,
      isBtnHide: false,
      debugVisible: false,
      customizeRequest: {protocol: "HTTP", type: "API", hashTree: [], referenced: 'Created', active: false},
      operatingElements: [],
      selectedTreeNode: undefined,
      selectedNode: undefined,
      scenarioDefinition: [],
      path: "/api/automation/create",
      debugData: {},
      reportId: "",
      enableCookieShare: false,
      globalOptions: {
        spacing: 30
      },
      response: {},
      projectIds: new Set,
      projectEnvMap: new Map,
      projectList: [],
      drawer: false,
      isFullUrl: true,
      expandedStatus: false,
      stepEnable: true,
      envResult: {
        loading: false
      },
      debug: false,
      saved: false,
      debugLoading: false,
      reqTotal: 0,
      reqSuccess: 0,
      reqError: 0,
      reqTotalTime: 0,
      reloadDebug: "",
      stopDebug: "",
      isTop: false,
      stepSize: 0,
      message: "",
      websocket: {},
      messageWebSocket: {},
      buttonData: [],
      stepFilter: new STEP,
      plugins: [],
      clearMessage: "",
      runScenario: undefined,
      showFollow: false,
      envGroupId: "",
      environmentType: ENV_TYPE.JSON,
      executeType: "",
      versionData: [],
      dialogVisible: false,
      currentItem: {},
      pluginDelStep: false,
      isBatchProcess: false,
      isCheckedAll: false,
      selectDataCounts: 0,
      dffScenarioId: "",
      scenarioRefId: "",
      batchOperators: [
        {
          name: this.$t('api_test.automation.bulk_activation_steps'),
          handleClick: this.enableAll,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('api_test.automation.batch_disable_steps'),
          handleClick: this.disableAll,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('api_test.automation.open_expansion'),
          handleClick: this.openExpansion,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('api_test.automation.close_expansion'),
          handleClick: this.closeExpansion,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete') + "步骤",
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
      ],
    }
  },
  created() {
    if (!this.currentScenario.apiScenarioModuleId) {
      this.currentScenario.apiScenarioModuleId = "";
    }
    if (this.currentScenario.apiScenarioModuleId === 'default-module') {
      this.currentScenario.apiScenarioModuleId = this.moduleOptions[0].id;
    }
    this.debug = false;
    this.debugLoading = false;
    if (this.stepFilter) {
      this.operatingElements = this.stepFilter.get("ALL");
    }
    this.getWsProjects();
    this.getMaintainerOptions();
    this.getApiScenario();
    this.buttonData = buttons(this);
    this.getPlugins().then(() => {
      this.initPlugins();
    });

    if (hasLicense()) {
      this.getVersionHistory();
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.addListener();
    });
  },
  directives: {OutsideClick},
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    ENV_TYPE() {
      return ENV_TYPE;
    }
  },
  methods: {
    recursiveSorting(arr) {
      for (let i in arr) {
        arr[i].disabled = true;
        if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
          this.recursiveSorting(arr[i].hashTree);
        }
      }
    },
    chooseHeadsUp() {
      if (this.$refs.stepTree) {
        this.selectDataCounts = this.$refs.stepTree.getCheckedNodes().length;
      }
    },
    checkedAll(v) {
      if (this.$refs.stepTree && this.$refs.stepTree.root && this.$refs.stepTree.root.childNodes) {
        this.recursionChecked(v, this.$refs.stepTree.root.childNodes);
      }
    },
    recursionChecked(v, array) {
      if (array) {
        array.forEach(item => {
          if (item.childNodes && item.childNodes.length > 0) {
            this.recursionChecked(v, item.childNodes);
          }
          item.checked = v;
          if (item.data && item.data.type === 'scenario' && item.data.referenced === 'REF') {
            item.expanded = false;
          }
        })
      }
    },
    batchProcessing() {
      this.isBatchProcess = true;
      this.hideAllTreeNode(this.scenarioDefinition);
      if (this.$refs.stepTree && this.$refs.stepTree.root && this.$refs.stepTree.root.childNodes) {
        this.recursionExpansion([], this.$refs.stepTree.root.childNodes);
      }
      this.reloadTreeStatus();
    },
    cancelBatchProcessing() {
      this.isBatchProcess = false;
      this.isCheckedAll = false;
      if (this.$refs.stepTree && this.$refs.stepTree.root && this.$refs.stepTree.root.childNodes) {
        this.recursionChecked(false, this.$refs.stepTree.root.childNodes);
      }
      this.selectDataCounts = 0;
      this.commandTreeNode();
      this.reloadTreeStatus();
    },
    reloadTreeStatus() {
      this.$nextTick(() => {
        let row = {resourceId: "ms-reload-test"};
        if (this.$refs.stepTree && this.$refs.stepTree.root.data) {
          this.$refs.stepTree.root.data.push(row);
          this.$refs.stepTree.root.data.splice(this.$refs.stepTree.root.data.length - 1, 1);
        }
      });
    },
    openOrClose(node) {
      node.expanded = !node.expanded;
    },
    hideNode(node) {
      node.isLeaf = true;
      node.visible = false;
    },
    hideAllTreeNode(array) {
      array.forEach(item => {
        item.isLeaf = this.isBatchProcess;
        item.isBatchProcess = this.isBatchProcess;
        item.checkBox = this.isBatchProcess;
        if (item.hashTree && item.hashTree.length > 0) {
          this.hideAllTreeNode(item.hashTree);
        }
      })
    },
    commandTreeNode(node, array) {
      if (!array) {
        array = this.scenarioDefinition;
      }
      let isLeaf = true;
      array.forEach(item => {
        item.checkBox = false;
        if (isLeaf && this.stepFilter.get('ALlSamplerStep').indexOf(item.type) === -1) {
          isLeaf = false;
        }
        if (item.hashTree && item.hashTree.length > 0) {
          this.commandTreeNode(item, item.hashTree);
        } else {
          item.isLeaf = true;
        }
      })
      if (node) {
        node.isBatchProcess = this.isBatchProcess;
        node.checkBox = false;
        node.isLeaf = isLeaf;
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    setDomain() {
      if (this.projectEnvMap && this.projectEnvMap.size > 0) {
        let scenario = {
          id: this.currentScenario.id,
          enableCookieShare: this.enableCookieShare,
          name: this.currentScenario.name,
          type: "scenario",
          clazzName: TYPE_TO_C.get("scenario"),
          variables: this.currentScenario.variables,
          headers: this.currentScenario.headers,
          referenced: 'Created',
          environmentMap: strMapToObj(this.projectEnvMap),
          hashTree: this.scenarioDefinition,
          onSampleError: this.onSampleError,
          projectId: this.currentScenario.projectId ? this.currentScenario.projectId : this.projectId,
        };
        let param = {
          definition: JSON.stringify(scenario),
          environmentType: this.environmentType,
          environmentMap: strMapToObj(this.projectEnvMap),
          environmentGroupId: this.envGroupId,
          environmentEnable: false,
        };
        this.$post("/api/automation/setDomain", param, res => {
          if (res.data) {
            let data = JSON.parse(res.data);
            if (data.hashTree) {
              this.sort(data.hashTree);
              let domainMap = new Map();
              this.getEnvDomain(data.hashTree, domainMap);
              this.margeDomain(this.scenarioDefinition, domainMap);
              this.cancelBatchProcessing();
              if (this.$store.state.currentApiCase) {
                this.$store.state.currentApiCase.resetDataSource = getUUID();
              } else {
                this.$store.state.currentApiCase = {resetDataSource: getUUID()};
              }
            }
          }
        })
      }
    },
    margeDomain(array, map) {
      array.forEach(item => {
        if (item && map.has(item.resourceId)) {
          item.domain = map.get(item.resourceId);
          item.resourceId = getUUID();
        }
        if (item && item.hashTree && item.hashTree.length > 0) {
          this.margeDomain(item.hashTree, map);
        }
      })
    },
    getEnvDomain(array, map) {
      array.forEach(item => {
        if (item && item.resourceId && item.domain) {
          map.set(item.resourceId, item.domain);
        }
        if (item && item.hashTree && item.hashTree.length > 0) {
          this.getEnvDomain(item.hashTree, map);
        }
      })
    },
    initPlugins() {
      if (this.plugins) {
        this.plugins.forEach(item => {
          let plugin = {
            title: item.name,
            show: this.showButton(item.jmeterClazz),
            titleColor: "#555855",
            titleBgColor: "#F4F4FF",
            icon: "colorize",
            click: () => {
              this.addComponent(item.name, item)
            }
          }
          if (this.operatingElements && this.operatingElements.includes(item.jmeterClazz)) {
            this.buttonData.push(plugin);
          }
          this.$nextTick(() => {
            if (!this.currentScenario.name && this.$refs.refFab) {
              this.$refs.refFab.openMenu();
            }
          });
        });
      }
    },
    getPlugins() {
      return new Promise((resolve) => {
        let url = "/plugin/list";
        this.plugins = [];
        this.$get(url, response => {
          let data = response.data;
          if (data) {
            data.forEach(item => {
              if (item.license) {
                if (hasLicense()) {
                  this.plugins.push(item);
                }
              } else {
                this.plugins.push(item);
              }
            })
          }
          resolve();
        });
      });
    },
    stop() {
      if (this.reportId) {
        this.debugLoading = false;
        try {
          if (this.messageWebSocket) {
            this.messageWebSocket.close();
          }
          if (this.websocket) {
            this.websocket.close();
          }
          this.clearNodeStatus(this.$refs.stepTree.root.childNodes);
          this.clearDebug();
          this.$success(this.$t('report.test_stop_success'));
        } catch (e) {
          this.debugLoading = false;
        }
        this.runScenario = undefined;
        // 停止jmeter执行
        let url = "/api/automation/stop/" + this.reportId;
        this.$get(url, response => {
        });
      }
    },
    clearDebug() {
      this.reqError = 0;
      this.reqTotalTime = 0;
      this.reqTotal = 0;
      this.reqSuccess = 0;
      this.executeType = "";
      this.pluginDelStep = false;
    },
    clearResult(arr) {
      if (arr) {
        arr.forEach(item => {
          item.requestResult = [];
          item.result = undefined;
          item.code = undefined;
          if (item.hashTree && item.hashTree.length > 0) {
            this.clearResult(item.hashTree);
          }
        })
      }
    },
    clearNodeStatus(arr) {
      if (arr) {
        arr.forEach(item => {
          item.code = undefined;
          item.data.code = undefined;
          item.testing = undefined;
          item.data.testing = undefined;
          if (item.childNodes && item.childNodes.length > 0) {
            this.clearNodeStatus(item.childNodes);
          }
        })
      }
    },
    evaluationParent(node, status) {
      if (!node.data.code) {
        node.data.code = "success";
      }
      if (!status) {
        node.data.code = "error";
      }
      node.data.testing = false;
      node.data.debug = true;
      if (node.parent && node.parent.data && node.parent.data.id) {
        this.evaluationParent(node.parent, status);
      }
    },
    resultEvaluationChild(arr, resourceId, status) {
      arr.forEach(item => {
        if (item.data.id + "_" + item.data.parentIndex === resourceId) {
          item.data.testing = false;
          this.evaluationParent(item.parent, status);
        }
        if (item.childNodes && item.childNodes.length > 0) {
          this.resultEvaluationChild(item.childNodes, resourceId, status);
        }
      })
    },
    resultEvaluation(resourceId, status) {
      if (this.$refs.stepTree && this.$refs.stepTree.root) {
        this.$refs.stepTree.root.childNodes.forEach(item => {
          if (item.data.id + "_" + item.data.parentIndex === resourceId) {
            item.data.testing = false;
          }
          if (item.childNodes && item.childNodes.length > 0) {
            this.resultEvaluationChild(item.childNodes, resourceId, status);
          }
        })
      }
    },
    initMessageSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/ws/" + this.reportId;
      this.messageWebSocket = new WebSocket(uri);
      this.messageWebSocket.onmessage = this.onDebugMessage;
    },
    runningEditParent(node) {
      if (node.parent && node.parent.data && node.parent.data.id) {
        node.data.testing = true;
        this.runningEditParent(node.parent);
      }
    },
    runningNodeChild(arr, resultData) {
      arr.forEach(item => {
        if (resultData && resultData.startsWith("result_")) {
          let data = JSON.parse(resultData.substring(7));
          if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
            data.subRequestResults.forEach(subItem => {
              if (item.data && item.data.id + "_" + item.data.parentIndex === subItem.resourceId) {
                subItem.requestResult.console = data.responseResult.console;
                item.data.requestResult.push(subItem);
                // 更新父节点状态
                this.resultEvaluation(subItem.resourceId, subItem.success);
                item.data.testing = false;
                item.data.debug = true;
              }
            })
          } else if ((item.data && item.data.id + "_" + item.data.parentIndex === data.resourceId)
            || (item.data && item.data.resourceId + "_" + item.data.parentIndex === data.resourceId)) {
            if (item.data.requestResult) {
              item.data.requestResult.push(data);
            } else {
              item.data.requestResult = [data];
            }
            // 更新父节点状态
            this.resultEvaluation(data.resourceId, data.success);
            item.data.testing = false;
            item.data.debug = true;
          }
        } else if (item.data && item.data.id + "_" + item.data.parentIndex === resultData) {
          item.data.testing = true;
          this.runningEditParent(item.parent);
        }
        if (item.childNodes && item.childNodes.length > 0) {
          this.runningNodeChild(item.childNodes, resultData);
        }
      })
    },
    runningEvaluation(resultData) {
      if (this.$refs.stepTree && this.$refs.stepTree.root) {
        this.$refs.stepTree.root.childNodes.forEach(item => {
          if (item.data && item.data.id + "_" + item.data.parentIndex === resultData) {
            item.data.testing = true;
          } else if (resultData && resultData.startsWith("result_")) {
            let data = JSON.parse(resultData.substring(7));
            if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
              data.subRequestResults.forEach(subItem => {
                if (item.data && item.data.id + "_" + item.data.parentIndex === subItem.resourceId) {
                  item.data.requestResult.push(subItem);
                  // 更新父节点状态
                  this.resultEvaluation(subItem.resourceId, subItem.success);
                  item.data.testing = false;
                  item.data.debug = true;
                }
              })
            } else if (item.data && item.data.id + "_" + item.data.parentIndex === data.resourceId
              || (item.data && item.data.resourceId + "_" + item.data.parentIndex === data.resourceId)) {
              item.data.requestResult.push(data);
              // 更新父节点状态
              this.resultEvaluation(data.resourceId, data.success);
              item.data.testing = false;
              item.data.debug = true;
            }
          }
          if (item.childNodes && item.childNodes.length > 0) {
            this.runningNodeChild(item.childNodes, resultData);
          }
        })
      }
    },
    onDebugMessage(e) {
      if (e.data && e.data.startsWith("result_")) {
        let data = JSON.parse(e.data.substring(7));
        this.reqTotal += 1;
        let time = data.endTime - data.startTime;
        this.reqTotalTime += time > 0 ? time : 0;
        if (data.error === 0) {
          this.reqSuccess += 1;
        } else {
          this.reqError += 1;
        }
      }
      this.runningEvaluation(e.data);
      this.message = getUUID();
      if (e.data && e.data.indexOf("MS_TEST_END") !== -1) {
        this.runScenario = undefined;
        this.debugLoading = false;
        this.message = "stop";
        this.stopDebug = "stop";
        this.reload();
      }
    },
    handleCommand() {
      this.debug = false;
      this.saved = true;
      this.executeType = "Saved";
      this.validatePluginData(this.scenarioDefinition);
      if (this.pluginDelStep) {
        this.$error("场景包含插件步骤，对应场景已经删除不能执行！");
        return;
      }
      /*触发执行操作*/
      this.$refs['currentScenario'].validate(async (valid) => {
          if (valid) {
            this.debugLoading = true;
            let definition = JSON.parse(JSON.stringify(this.currentScenario));
            definition.hashTree = this.scenarioDefinition;
            await this.getEnv(JSON.stringify(definition));
            await this.$refs.envPopover.initEnv();
            const sign = await this.$refs.envPopover.checkEnv(this.isFullUrl);
            if (!sign) {
              this.debugLoading = false;
              return;
            }
            this.initParameter();
            this.debugData = {
              id: this.currentScenario.id,
              name: this.currentScenario.name,
              type: "scenario",
              variables: this.currentScenario.variables,
              referenced: 'Created',
              onSampleError: this.onSampleError,
              enableCookieShare: this.enableCookieShare,
              headers: this.currentScenario.headers,
              environmentMap: this.projectEnvMap,
              hashTree: this.scenarioDefinition
            };
            this.reportId = getUUID().substring(0, 8);
            this.debugLoading = false;
            this.pluginDelStep = false;
            this.$emit('refresh');
          }
        }
      )
    },
    validatePluginData(steps) {
      steps.forEach(step => {
        if (step.plugin_del) {
          this.pluginDelStep = true;
        }
        if (step.hashTree && step.hashTree.length > 0) {
          this.validatePluginData(step.hashTree);
        }
      });
    },
    openHis() {
      this.$refs.changeHistory.open(this.currentScenario.id, ["接口自动化", "Api automation", "接口自動化", "API_AUTOMATION"]);
    },
    setModule(id, data) {
      this.currentScenario.apiScenarioModuleId = id;
      this.currentScenario.modulePath = data.path;
    },
    setHideBtn() {
      this.$refs.scenarioRelevance.changeButtonLoadingType();
      this.$refs.scenarioApiRelevance.changeButtonLoadingType();
      this.isBtnHide = false;
    },
    // 打开引用的场景
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    setCookieShare(cookie) {
      this.enableCookieShare = cookie;
    },
    setSampleError(sampleError) {
      this.onSampleError = sampleError;
    },
    showAllBtn() {
      this.$refs.maximizeScenario.showAll();
    },
    addListener() {
      document.addEventListener("keydown", this.createCtrlSHandle);
      document.addEventListener("keydown", this.createCtrlRHandle);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
      document.removeEventListener("keydown", this.createCtrlRHandle);
    },
    createCtrlSHandle(event) {
      handleCtrlSEvent(event, this.editScenario);
    },
    createCtrlRHandle(event) {
      handleCtrlREvent(event, this.runDebug);
    },
    getIdx(index) {
      return index - 0.33
    },
    setVariables(v, headers) {
      this.currentScenario.variables = v;
      this.currentScenario.headers = headers;
      if (this.path.endsWith("/update")) {
        // 直接更新场景防止编辑内容丢失
        this.editScenario();
      }
      if (this.$refs.maximizeHeader) {
        this.$refs.maximizeHeader.getVariableSize();
      }
      this.reload();
    },
    showButton(...names) {
      for (const name of names) {
        if (name && this.operatingElements && this.operatingElements.includes(name)) {
          return true;
        }
      }
      return false;
    },
    outsideClick(e) {
      e.stopPropagation();
      this.showAll();
      this.buttonData = buttons(this);
      this.initPlugins();
    },
    fabClick() {
      if (this.operatingElements && this.operatingElements.length < 1) {
        if (this.selectedTreeNode && this.selectedTreeNode.referenced === 'REF' || this.selectedTreeNode.disabled) {
          this.$warning(this.$t('api_test.scenario.scenario_warning'));
        } else {
          this.$warning(this.$t('api_test.scenario.scenario_step_warning'));
        }
      }
    },
    addComponent(type, plugin) {
      setComponent(type, this, plugin);
    },
    nodeClick(data, node) {
      if ((data.referenced != 'REF' && data.referenced != 'Deleted' && !data.disabled && this.stepFilter) || data.refType === 'CASE') {
        this.operatingElements = this.stepFilter.get(data.type);
      } else {
        this.operatingElements = [];
      }
      if (!this.operatingElements && this.stepFilter) {
        this.operatingElements = this.stepFilter.get("ALL");
      }
      this.selectedTreeNode = data;
      this.selectedNode = node;
      this.$store.state.selectStep = data;
      this.buttonData = buttons(this);
      this.initPlugins();
    },
    suggestClick(node) {
      this.response = {};
      if (node.parent && node.parent.data.requestResult) {
        this.response = node.parent.data.requestResult[0];
      }
    },
    showAll() {
      // 控制当有弹出页面操作时禁止刷新按钮列表
      if (!this.customizeVisible && !this.isBtnHide) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.selectedTreeNode = undefined;
        this.$store.state.selectStep = undefined;
      }
    },
    apiListImport() {
      this.isBtnHide = true;
      this.$refs.scenarioApiRelevance.open();
    },
    sort(stepArray, scenarioProjectId, fullPath) {
      if (!stepArray) {
        stepArray = this.scenarioDefinition;
      }
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (!stepArray[i].resourceId) {
          stepArray[i].resourceId = getUUID();
        }
        // 历史数据处理
        if (stepArray[i].type === "HTTPSamplerProxy" && !stepArray[i].headers) {
          stepArray[i].headers = [new KeyValue()];
        }
        if (stepArray[i].type === ELEMENT_TYPE.LoopController
          && stepArray[i].loopType === "LOOP_COUNT"
          && stepArray[i].hashTree
          && stepArray[i].hashTree.length > 1) {
          stepArray[i].countController.proceed = true;
        }
        if (!stepArray[i].clazzName) {
          stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
        }
        if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
          stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
        }
        if (!stepArray[i].projectId) {
          // 如果自身没有ID并且场景有ID则赋值场景ID，否则赋值当前项目ID
          stepArray[i].projectId = scenarioProjectId ? scenarioProjectId : this.projectId;
        }
        // 添加debug结果
        stepArray[i].parentIndex = fullPath ? fullPath + "_" + stepArray[i].index : stepArray[i].index;
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.stepSize += stepArray[i].hashTree.length;
          this.sort(stepArray[i].hashTree, stepArray[i].projectId, stepArray[i].parentIndex);
        }
      }
    },
    addCustomizeApi(request) {
      this.customizeVisible = false;
      request.enable === undefined ? request.enable = true : request.enable;
      if (this.selectedTreeNode !== undefined) {
        this.selectedTreeNode.hashTree.push(request);
      } else {
        this.scenarioDefinition.push(request);
      }
      this.customizeRequest = {};
      this.sort();
      this.reload();
    },
    addScenario(arr) {
      if (arr && arr.length > 0) {
        arr.forEach(item => {
          if (item.id === this.currentScenario.id) {
            this.$error(this.$t("api_test.scenario.scenario_error"));
            this.$refs.scenarioRelevance.changeButtonLoadingType();
            return;
          }
          if (!item.hashTree) {
            item.hashTree = [];
          }
          this.resetResourceId(item.hashTree);
          item.enable === undefined ? item.enable = true : item.enable;
          item.variableEnable = item.variableEnable === undefined ? true : item.variableEnable;
          if (this.selectedTreeNode !== undefined) {
            this.selectedTreeNode.hashTree.push(item);
          } else {
            this.scenarioDefinition.push(item);
          }
        })
      }
      this.isBtnHide = false;
      this.sort();
      this.cancelBatchProcessing();
    },
    setApiParameter(item, refType, referenced) {
      let request = {};
      if (Object.prototype.toString.call(item.request).indexOf("String") > 0) {
        request = JSON.parse(item.request);
      } else {
        request = item.request;
      }
      if (item.protocol) {
        request.protocol = item.protocol;
      }
      if (request.protocol === "DUBBO") {
        request.protocol = "dubbo://";
      }
      request.id = item.id;
      request.name = item.name;
      request.refType = refType;
      request.referenced = referenced;
      request.enable === undefined ? request.enable = true : request.enable;
      request.active = false;
      request.resourceId = getUUID();
      request.projectId = item.projectId;
      request.num = item.num;
      request.versionEnable = item.versionEnable;
      request.versionId = item.versionId;
      request.versionName = item.versionName;
      request.requestResult = [];
      if (!request.url) {
        request.url = "";
      }
      if (!request.hashTree) {
        request.hashTree = [];
      }
      if (referenced === 'REF' && request.hashTree) {
        this.recursiveSorting(request.hashTree);
      }
      if (this.selectedTreeNode !== undefined) {
        this.selectedTreeNode.hashTree.push(request);
      } else {
        this.scenarioDefinition.push(request);
      }
    },
    pushApiOrCase(data, refType, referenced) {
      data.forEach(item => {
        this.setApiParameter(item, refType, referenced);
      });
      this.isBtnHide = false;
      this.$refs.scenarioApiRelevance.changeButtonLoadingType();
      this.sort();
      this.cancelBatchProcessing();
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    openTagConfig() {
      if (!this.projectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.tag.open();
    },
    remove(row, node) {
      let name = row === undefined || row.name === undefined ? "" : row.name;
      this.$alert(this.$t('api_test.definition.request.delete_confirm_step') + ' ' + name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            const parent = node.parent
            const hashTree = parent.data.hashTree || parent.data;
            const index = hashTree.findIndex(d => d.resourceId !== undefined && row.resourceId !== undefined && d.resourceId === row.resourceId)
            hashTree.splice(index, 1);
            this.sort();
            this.forceRerender();
          }
        }
      });
    },
    resetResourceId(hashTree) {
      hashTree.forEach(item => {
        item.resourceId = getUUID();
        if (item.hashTree && item.hashTree.length > 0) {
          this.resetResourceId(item.hashTree);
        }
      })
    },
    copyRow(row, node) {
      const parent = node.parent
      const hashTree = parent.data.hashTree || parent.data;
      // 深度复制
      let obj = JSON.parse(JSON.stringify(row));
      obj.resourceId = getUUID();
      if (obj.hashTree && obj.hashTree.length > 0) {
        this.resetResourceId(obj.hashTree);
      }
      if (obj.name) {
        obj.name = obj.name + '_copy';
      }
      const index = hashTree.findIndex(d => d.resourceId === row.resourceId);
      if (index !== -1) {
        hashTree.splice(index + 1, 0, obj);
      } else {
        hashTree.push(obj);
      }
      this.sort();
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      });
    },
    forceRerender() {
      this.$nextTick(() => {
        this.$store.state.forceRerenderIndex = getUUID();
      });
    },
    runDebug(runScenario) {
      if (this.scenarioDefinition.length < 1) {
        return;
      }
      this.stopDebug = "";
      this.clearDebug();
      this.validatePluginData(this.scenarioDefinition);
      if (this.pluginDelStep) {
        this.$error("场景包含插件步骤，对应场景已经删除不能调试！");
        return;
      }
      this.clearResult(this.scenarioDefinition);
      this.clearNodeStatus(this.$refs.stepTree.root.childNodes);
      this.saved = runScenario && runScenario.stepScenario ? false : true;
      /*触发执行操作*/
      this.$refs.currentScenario.validate(async (valid) => {
        if (valid) {
          let definition = JSON.parse(JSON.stringify(this.currentScenario));
          definition.hashTree = this.scenarioDefinition;
          await this.getEnv(JSON.stringify(definition));
          await this.$refs.envPopover.initEnv();
          const sign = await this.$refs.envPopover.checkEnv(this.isFullUrl);
          if (!sign) {
            this.buttonIsLoading = false;
            this.clearMessage = getUUID().substring(0, 8);
            return;
          }
          let scenario = undefined;
          if (runScenario && runScenario.type === 'scenario') {
            scenario = runScenario;
            this.runScenario = runScenario;
          }
          //调试时不再保存
          this.debugData = {
            id: scenario ? scenario.id : this.currentScenario.id,
            name: scenario ? scenario.name : this.currentScenario.name,
            type: "scenario",
            variables: scenario ? scenario.variables : this.currentScenario.variables,
            referenced: 'Created',
            enableCookieShare: this.enableCookieShare,
            headers: scenario ? scenario.headers : this.currentScenario.headers,
            environmentMap: scenario && scenario.environmentEnable ? scenario.environmentMap : this.projectEnvMap,
            hashTree: scenario ? scenario.hashTree : this.scenarioDefinition,
            onSampleError: this.onSampleError,
          };
          if (scenario && scenario.environmentEnable) {
            this.debugData.environmentEnable = scenario.environmentEnable;
            this.debugLoading = false;
          } else {
            this.debugLoading = true;
          }
          this.reportId = getUUID().substring(0, 8);
          this.debug = true;
          this.pluginDelStep = false;
        } else {
          this.clearMessage = getUUID().substring(0, 8);
        }
      })
    },
    getEnvironments() {
      if (this.projectId) {
        this.$get('/api/environment/list/' + this.projectId, response => {
          this.environments = response.data;
          this.environments.forEach(environment => {
            parseEnvironment(environment);
          });
          let hasEnvironment = false;
          for (let i in this.environments) {
            if (this.environments[i].id === this.currentEnvironmentId) {
              hasEnvironment = true;
              break;
            }
          }
          if (!hasEnvironment) {
            this.currentEnvironmentId = '';
          }
          //检查场景是否需要先进行保存
          this.checkDataIsCopy();
        });
      }
    },
    checkDataIsCopy() {
      //  如果是复制按钮创建的场景，直接进行保存
      if (this.currentScenario.copy) {
        this.editScenario(false);
      }
    },

    openEnvironmentConfig() {
      if (!this.projectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.environmentConfig.open(this.projectId);
    },
    environmentConfigClose() {
      this.getEnvironments();
    },
    allowDrop(draggingNode, dropNode, dropType) {
      // 增加插件权限控制
      if (dropType != "inner") {
        if (draggingNode.data.disabled && draggingNode.parent && draggingNode.parent.data && draggingNode.parent.data.disabled) {
          return false;
        }
        if (draggingNode && dropNode && draggingNode.level >= dropNode.level) {
          return true;
        }
        return false;
      } else if (dropType === "inner" && dropNode.data.referenced !== 'REF' && dropNode.data.referenced !== 'Deleted'
        && (this.stepFilter.get(dropNode.data.type) && this.stepFilter.get(dropNode.data.type).indexOf(draggingNode.data.type) != -1)) {
        return true;
      }
      return false;
    },
    allowDrag(draggingNode, dropNode, dropType) {
      if (dropNode && draggingNode && dropType) {
        this.sort();
        this.forceRerender();
        this.cancelBatchProcessing();
      }
    },
    editScenario() {
      if (!document.getElementById("inputDelay")) {
        return;
      }
      this.validatePluginData(this.scenarioDefinition);
      if (this.pluginDelStep) {
        this.$error("场景包含插件步骤，对应场景已经删除不能编辑！");
        return;
      }
      return new Promise((resolve) => {
        document.getElementById("inputDelay").focus();  //  保存前在input框自动失焦，以免保存失败
        if (this.$refs['currentScenario']) {
          this.$refs['currentScenario'].validate(async (valid) => {
            if (valid) {
              await this.setParameter();
              if (!this.currentScenario.versionId) {
                if (this.$refs.versionHistory && this.$refs.versionHistory.currentVersion) {
                  this.currentScenario.versionId = this.$refs.versionHistory.currentVersion.id;
                }
              }
              saveScenario(this.path, this.currentScenario, this.scenarioDefinition, this, (response) => {
                this.$success(this.$t('commons.save_success'));
                this.path = "/api/automation/update";
                this.$store.state.pluginFiles = [];
                if (response.data) {
                  this.currentScenario.id = response.data.id;
                }
                // 保存成功后刷新历史版本
                this.getVersionHistory();
                if (this.currentScenario.tags instanceof String) {
                  this.currentScenario.tags = JSON.parse(this.currentScenario.tags);
                }
                this.pluginDelStep = false;
                this.$emit('refresh', this.currentScenario);
                resolve();
              });
            }
          })
        }
      });
    },
    getEnv(definition) {
      return new Promise((resolve) => {
        this.$post("/api/automation/getApiScenarioEnv", {definition: definition}, res => {
          if (res.data) {
            this.projectIds = new Set(res.data.projectIds);
            this.projectIds.add(this.projectId);
            this.isFullUrl = res.data.fullUrl;
          }
          resolve();
        })
      });
    },
    getApiScenario() {
      this.loading = true;
      this.isBatchProcess = false;
      this.stepEnable = true;
      this.isCheckedAll = false;
      this.selectDataCounts = 0;
      if (this.currentScenario.tags !== undefined && this.currentScenario.tags) {
        if (!(this.currentScenario.tags instanceof Array)) {
          this.currentScenario.tags = JSON.parse(this.currentScenario.tags);
        }
      } else {
        this.$set(this.currentScenario, 'tags', [])
      }
      if (!this.currentScenario.variables) {
        this.currentScenario.variables = [];
      }
      if (!this.currentScenario.headers) {
        this.currentScenario.headers = [];
      }
      if (this.currentScenario.id) {
        this.result = this.$get("/api/automation/getApiScenario/" + this.currentScenario.id, response => {
          if (response.data) {
            this.path = "/api/automation/update";
            if (response.data.scenarioDefinition != null) {
              let obj = JSON.parse(response.data.scenarioDefinition);
              if (obj) {
                this.currentEnvironmentId = obj.environmentId;
                if (response.data.environmentJson) {
                  this.projectEnvMap = objToStrMap(JSON.parse(response.data.environmentJson));
                } else {
                  // 兼容历史数据
                  this.projectEnvMap.set(this.projectId, obj.environmentId);
                }
                this.$store.state.scenarioEnvMap = this.projectEnvMap;
                this.envGroupId = response.data.environmentGroupId;
                if (response.data.environmentType) {
                  this.environmentType = response.data.environmentType;
                }
                this.currentScenario.variables = [];
                if (obj.headers) {
                  this.currentScenario.headers = obj.headers;
                }
                let index = 1;
                if (obj.variables) {
                  obj.variables.forEach(item => {
                    // 兼容历史数据
                    if (item.name) {
                      if (!item.type) {
                        item.type = "CONSTANT";
                        item.id = getUUID();
                      }
                      item.num = index;
                      this.currentScenario.variables.push(item);
                      index++;
                    }
                  })
                }
                this.enableCookieShare = obj.enableCookieShare;
                if (obj.onSampleError === undefined) {
                  this.onSampleError = true;
                } else {
                  this.onSampleError = obj.onSampleError;
                }
                this.dataProcessing(obj.hashTree);
                this.scenarioDefinition = obj.hashTree;
              }
            }
            if (this.currentScenario.copy) {
              this.path = "/api/automation/create";
            }
            this.$get('/api/automation/follow/' + this.currentScenario.id, response => {
              this.currentScenario.follows = response.data;
              for (let i = 0; i < response.data.length; i++) {
                if (response.data[i] === this.currentUser().id) {
                  this.showFollow = true;
                  break;
                }
              }
            });
          }
          this.loading = false;
          this.sort();
          this.$nextTick(() => {
            this.cancelBatchProcessing();
          });
          // 记录初始化数据
          let v1 = {
            apiScenarioModuleId: this.currentScenario.apiScenarioModuleId,
            name: this.currentScenario.name,
            status: this.currentScenario.status,
            principal: this.currentScenario.principal,
            level: this.currentScenario.level,
            tags: this.currentScenario.tags,
            description: this.currentScenario.description,
            scenarioDefinition: JSON.parse(JSON.stringify(this.scenarioDefinition))
          };

          this.currentScenario.scenarioDefinitionOrg = v1;
          this.currentScenario.scenarioDefinition = this.scenarioDefinition;
        })
      }
    },
    dataProcessing(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].hashTree) {
            stepArray[i].hashTree = [];
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i].hashTree.length > 0) {
            this.dataProcessing(stepArray[i].hashTree);
          }
        }
      }
    },

    formatData(hashTree) {
      for (let i in hashTree) {
        if (hashTree[i] && TYPE_TO_C.get(hashTree[i].type) && !hashTree[i].clazzName) {
          hashTree[i].clazzName = TYPE_TO_C.get(hashTree[i].type);
        }
        if (hashTree[i] && hashTree[i].authManager && !hashTree[i].authManager.clazzName) {
          hashTree[i].authManager.clazzName = TYPE_TO_C.get(hashTree[i].authManager.type);
        }
        if (hashTree[i].hashTree && hashTree[i].hashTree.length > 0) {
          this.formatData(hashTree[i].hashTree);
        }
      }
    },
    initParameter() {
      this.currentScenario.stepTotal = this.scenarioDefinition.length;
      if (!this.currentScenario.projectId) {
        this.currentScenario.projectId = this.projectId;
      }
      // 构建一个场景对象 方便引用处理
      let scenario = {
        id: this.currentScenario.id,
        enableCookieShare: this.enableCookieShare,
        name: this.currentScenario.name,
        type: "scenario",
        clazzName: TYPE_TO_C.get("scenario"),
        variables: this.currentScenario.variables,
        headers: this.currentScenario.headers,
        referenced: 'Created',
        environmentMap: strMapToObj(this.projectEnvMap),
        hashTree: this.scenarioDefinition,
        onSampleError: this.onSampleError,
        projectId: this.currentScenario.projectId ? this.currentScenario.projectId : this.projectId,
      };
      // 历史数据处理
      if (scenario.hashTree) {
        this.formatData(scenario.hashTree);
      }
      this.currentScenario.environmentType = this.environmentType;
      this.currentScenario.environmentJson = JSON.stringify(strMapToObj(this.projectEnvMap));
      this.currentScenario.environmentGroupId = this.envGroupId;
      this.currentScenario.scenarioDefinition = scenario;
      if (this.currentScenario.tags instanceof Array) {
        this.currentScenario.tags = JSON.stringify(this.currentScenario.tags);
      }
      if (this.currentModule != null) {
        this.currentScenario.modulePath = this.currentModule.method !== undefined ? this.currentModule.method : null;
        this.currentScenario.apiScenarioModuleId = this.currentModule.id;
      }
    },
    async setParameter() {
      this.initParameter();
      let definition = JSON.parse(JSON.stringify(this.currentScenario));
      definition.hashTree = this.scenarioDefinition;
      await this.getEnv(JSON.stringify(definition));
      // 保存时同步所需要的项目环境
      savePreciseEnvProjectIds(this.projectIds, this.projectEnvMap);
    },
    runRefresh() {
      if (!this.debug) {
        this.debugVisible = true;
        this.loading = false;
      } else {
        this.initMessageSocket();
      }
    },
    errorRefresh(error) {
      this.debug = false;
      this.isTop = false;
      this.debugLoading = false;
      this.debugVisible = false;
      this.loading = false;
      this.runScenario = undefined;
      this.message = "stop";
      this.clearMessage = getUUID().substring(0, 8);
      this.debugData = {};
    },
    showScenarioParameters() {
      this.$refs.scenarioParameters.open(this.currentScenario.variables, this.currentScenario.headers);
    },
    apiImport(importData) {
      if (importData && importData.data) {
        importData.data.forEach(item => {
          this.setApiParameter(item, "API", "OT_IMPORT");
        })
        this.sort();
        this.reload();
      }
    },
    getVariableSize() {
      let size = 0;
      if (this.currentScenario.variables) {
        size += this.currentScenario.variables.length;
      }
      if (this.currentScenario.headers && this.currentScenario.headers.length > 1) {
        size += this.currentScenario.headers.length - 1;
      }
      return size;
    },
    handleEnv() {
      this.$refs.apiScenarioEnv.open();
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
      this.$store.state.scenarioEnvMap = projectEnvMap;
      this.setDomain(true);
    },
    setEnvGroup(id) {
      this.envGroupId = id;
      this.setDomain(true);
    },
    setEnvType(val) {
      this.environmentType = val;
    },
    getWsProjects() {
      this.$get("/project/getOwnerProjects", res => {
        this.projectList = res.data;
      })
    },
    refReload() {
      this.reload();
    },
    detailRefresh(result) {
      // 把执行结果分发给各个请求
      this.debugData = {};
    },
    fullScreen() {
      this.drawer = true;
    },
    unFullScreen() {
      this.drawer = false;
    },
    close(name) {
      this.drawer = false;
      this.$emit('closePage', name);
    },
    showPopover() {
      let definition = JSON.parse(JSON.stringify(this.currentScenario));
      definition.hashTree = this.scenarioDefinition;
      this.envResult.loading = true;
      this.getEnv(JSON.stringify(definition)).then(() => {
        this.$refs.envPopover.openEnvSelect();
        this.envResult.loading = false;
      })
    },
    changeNodeStatus(resourceIds, nodes) {
      for (let i in nodes) {
        if (nodes[i] && !(nodes[i].type === 'scenario' && nodes[i].referenced === 'REF')) {
          if (resourceIds.indexOf(nodes[i].resourceId) !== -1) {
            nodes[i].active = this.expandedStatus;
          }
          if (nodes[i].hashTree && nodes[i].hashTree.length > 0 && resourceIds.length < 60) {
            this.changeNodeStatus(resourceIds, nodes[i].hashTree);
          }
        }
      }
    },
    getAllResourceIds() {
      let selectValueArr = [];
      if (this.$refs.stepTree) {
        let checkNodes = this.$refs.stepTree.getCheckedNodes();
        for (let node of checkNodes) {
          selectValueArr.push(node.resourceId);
        }
      }
      return selectValueArr;
    },
    recursionExpansion(resourceIds, array) {
      if (array) {
        array.forEach(item => {
          if (item.data && item.data.type === 'scenario' && item.data.referenced === 'REF') {
            item.expanded = false;
          } else {
            if (resourceIds.indexOf(item.data.resourceId) !== -1) {
              item.expanded = this.expandedStatus;
            }
          }
          if (item.childNodes && item.childNodes.length > 0) {
            this.recursionExpansion(resourceIds, item.childNodes);
          }
        })
      }
    },
    openExpansion() {
      this.expandedStatus = true;
      let resourceIds = this.getAllResourceIds();
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.recursionExpansion(resourceIds, this.$refs.stepTree.root.childNodes);
    },
    closeExpansion() {
      this.expandedStatus = false;
      let resourceIds = this.getAllResourceIds();
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.recursionExpansion(resourceIds, this.$refs.stepTree.root.childNodes);
    },
    stepStatus(resourceIds, nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (resourceIds.indexOf(nodes[i].resourceId) !== -1) {
            nodes[i].enable = this.stepEnable;
          }
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(resourceIds, nodes[i].hashTree);
          }
        }
      }
    },
    enableAll() {
      this.stepEnable = true;
      let resourceIds = this.getAllResourceIds();
      this.stepStatus(resourceIds, this.scenarioDefinition);
    },
    disableAll() {
      this.stepEnable = false;
      let resourceIds = this.getAllResourceIds();
      this.stepStatus(resourceIds, this.scenarioDefinition);
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.module.delete_batch_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.getAllResourceIds().forEach(item => {
              this.recursionDelete(item, this.scenarioDefinition);
            });
            this.sort();
            this.forceRerender();
          }
        }
      });
    },
    recursionDelete(resourceId, nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (resourceId === nodes[i].resourceId) {
            nodes.splice(i, 1);
          } else {
            if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
              this.recursionDelete(resourceId, nodes[i].hashTree);
            }
          }
        }
      }
    },
    showHistory() {
      this.$refs.taskCenter.openScenarioHistory(this.currentScenario.id);
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.currentScenario.follows.length; i++) {
          if (this.currentScenario.follows[i] === this.currentUser().id) {
            this.currentScenario.follows.splice(i, 1)
            break;
          }
        }
        if (this.currentScenario.id) {
          this.$post("/api/automation/update/follows/" + this.currentScenario.id, this.currentScenario.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.currentScenario.follows) {
          this.currentScenario.follows = [];
        }
        this.currentScenario.follows.push(this.currentUser().id)
        if (this.currentScenario.id) {
          this.$post("/api/automation/update/follows/" + this.currentScenario.id, this.currentScenario.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    editScenarioAdvance(data) {
      // 打开编辑参数设置对话框，并且传递修改的蓝笔引用值参数
      this.currentItem = data;
      this.$refs.scenarioVariableAdvance.open();
    },
    getVersionHistory() {
      this.$get('/api/automation/versions/' + this.currentScenario.id, response => {
        if (this.currentScenario.copy) {
          this.versionData = response.data.filter(v => v.versionId === this.currentScenario.versionId);
        } else {
          this.versionData = response.data;
        }
      });
    },
    compare(row) {
      this.scenarioRefId = this.currentScenario.refId;
      this.dffScenarioId = row.id;
      this.dialogVisible = true;
    },
    closeDiff() {
      this.oldScenarioDefinition = []
    },

    checkout(row) {
      let api = this.versionData.filter(v => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      Object.assign(this.currentScenario, api);
      this.getApiScenario();
      this.getVersionHistory();
    },
    create(row) {
      // 创建新版本
      this.currentScenario.versionId = row.id;
      this.currentScenario.versionName = row.name;
      this.loading = true;
      this.editScenario()
        .then(() => {
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/automation/delete/' + row.id + '/' + this.currentScenario.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.card-content {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.ms-scenario-input {
  width: 100%;
}

.ms-debug-div {
  border: 1px #DCDFE6 solid;
  border-radius: 4px;
  margin-right: 0px;
}

.ms-font {
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
}

.ms-col-one {
  margin-top: 5px;
}

#fab {
  right: 60px;
  bottom: 120px;
  z-index: 5;
}

/deep/ .el-tree-node__content {
  height: 100%;
  margin-top: 3px;
  vertical-align: center;
}

/deep/ .el-card__body {
  padding: 6px 10px;
}

/deep/ .el-drawer__body {
  overflow: auto;
}

/deep/ .el-step__icon.is-text {
  border: 1px solid;
}

/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

/deep/ .el-link {
  font-weight: normal;
}

/deep/ .el-checkbox {
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
  font-weight: normal;
}

/deep/ .el-checkbox__label {
  padding-left: 5px;
}

.head {
  border-bottom: 1px solid #303133;
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree >>> .el-tree-node__content > .el-tree-node__expand-icon {
  padding: 3px;
}

.ms-tree >>> .el-icon-caret-right:before {
  padding: 0;
  content: "";
}

.ms-tree >>> .el-tree-node__expand-icon.is-leaf {
  color: transparent;
}

.ms-tree >>> .el-tree-node__expand-icon {
  color: #7C3985;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded.el-icon-caret-right:before {
  color: #7C3985;
  /* content: "\e722";*/
  padding: 0;
  content: "";
}

.ms-sc-variable-header >>> .el-dialog__body {
  padding: 0px 20px;
}

.alt-ico {
  font-size: 15px;
  margin: 5px 10px 0px;
  float: right;
  color: #8c939d;
}

.alt-ico:hover {
  color: black;
  cursor: pointer;
}

.ms-batch-btn {
  margin-left: 5px;
}

.ms-batch-btn:hover {
  cursor: pointer;
  color: #6D317C;
}

.ms-debug-result {
  margin-top: 3px;
  height: 20px;
  float: right;
}

.ms-message-right {
  margin-right: 10px;
}

.custom-node_e {
  color: #7C3985;
  font-size: 20px;
}

.custom-tree-node-col {
  width: 25px;
  padding: 0px;
  margin-left: 5px;
  vertical-align: center;
}

.custom-tree-node-hide {
  width: 15px;
  padding: 0px;
  vertical-align: center;
}

/deep/ .show-more-btn {
  width: 0px;
  height: 17px;
  line-height: 10px;
}

/deep/ .ms-main-container {
  padding: 5px 5px 5px 10px;
}
</style>
