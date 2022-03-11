<template>
  <div style="width: 100%;overflow: hidden">
    <div class="ms-header">
      <el-row>
        <el-col :span="1" v-show="scenarioDefinition.length > 1">
          <div class="ms-div" style="margin-left: 10px">
            <el-tooltip :content="$t('test_track.case.batch_operate')" placement="top" effect="light" v-show="!isBatchProcess">
              <font-awesome-icon class="ms-batch-btn" :icon="['fa', 'bars']" v-prevent-re-click @click="batchProcessing"/>
            </el-tooltip>
            <el-checkbox v-show="isBatchProcess" v-model="isCheckedAll" @change="checkedAll"/>
            <el-tooltip :content="$t('commons.cancel')" placement="top" effect="light" v-show="isBatchProcess">
              <font-awesome-icon class="ms-batch-btn" :icon="['fa', 'times']" v-prevent-re-click @click="cancelBatchProcessing"/>
            </el-tooltip>
          </div>
        </el-col>

        <el-col :span="9">
          <div class="ms-div" @click="showAll">
            <!-- 调试部分 -->
            <el-row>
              <el-col :span="8">
                <el-tooltip placement="top" :content="currentScenario.name">
                  <span style="margin-left: 10px">{{ currentScenario.name }}</span>
                </el-tooltip>
              </el-col>
              <el-col :span="8">
                {{ $t('api_test.automation.step_total') }}：{{ scenarioDefinition.length }}
              </el-col>
              <el-col :span="8">
                <el-link class="head" @click="showScenarioParameters">{{ $t('api_test.automation.scenario_total') }}</el-link>
                ：{{ getVariableSize() }}
              </el-col>
            </el-row>
          </div>

        </el-col>
        <el-col :span="12">
          <el-checkbox v-model="cookieShare" @change="setCookieShare">共享cookie</el-checkbox>
          <el-checkbox v-model="sampleError" @change="setOnSampleError" style="margin-right: 10px">{{ $t('commons.failure_continues') }}</el-checkbox>
          <env-popover :disabled="scenarioDefinition.length < 1" :isReadOnly="scenarioDefinition.length < 1"
                       :env-map="projectEnvMap" :project-ids="projectIds" @setProjectEnvMap="setProjectEnvMap"
                       :environment-type.sync="envType" :group-id="envGroupId"
                       @showPopover="showPopover" :project-list="projectList" ref="envPopover" class="ms-right" :has-option-group="true"
                       :result="envResult"/>

          <el-dropdown split-button type="primary" @click="debugScenario" style="margin:0px 10px 0px" size="mini" @command="handleCommand" v-show="!debugLoading">
            {{ $t('api_test.request.debug') }}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>{{ $t('api_test.automation.generate_report') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button size="mini" type="primary" style="margin:0px 10px 0px" v-show="debugLoading" @click="stop">{{ $t('report.stop_btn') }}</el-button>
        </el-col>

        <font-awesome-icon class="ms-min-alt-ico" :icon="['fa', 'compress-alt']" size="lg" @click="unFullScreen"/>
      </el-row>
    </div>

    <!-- 场景步骤-->
    <ms-container :class="{'maximize-container': !asideHidden}">
      <ms-aside-container @setAsideHidden="setAsideHidden" style="padding-top: 0px;overflow: hidden">
        <div class="ms-debug-result" v-if="reqTotal > 0">
          <span style="float: right">
             <span class="ms-message-right"> {{ reqTotalTime }} ms </span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_total') }} {{ reqTotal }}</span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_success') }} {{ reqSuccess }}</span>
                <span class="ms-message-right"> {{ $t('api_test.automation.request_error') }} {{ reqError }}</span>
          </span>
        </div>
        <!-- 场景步骤内容 -->
        <div v-loading="loading" v-if="!asideHidden">
          <el-tree node-key="resourceId"
                   v-if="showHideTree && !asideHidden"
                   :props="props"
                   :data="scenarioDefinition"
                   :default-expanded-keys="expandedNode"
                   :expand-on-click-node="false"
                   highlight-current
                   :show-checkbox="isBatchProcess"
                   @node-expand="nodeExpand"
                   @node-collapse="nodeCollapse"
                   :allow-drop="allowDrop" @node-drag-end="allowDrag" @node-click="nodeClick" class="ms-tree" ref="maxStepTree">
            <el-row class="custom-tree-node" :gutter="18" type="flex" align="middle" slot-scope="{node, data}" style="width: 98%">
              <el-col class="custom-tree-node-col" style="padding-left:0px;padding-right:0px" v-show="node && data.hashTree && data.hashTree.length > 0 && !data.isLeaf">
                <span v-show="!node.expanded" class="el-icon-circle-plus-outline custom-node_e" @click="openOrClose(node)"/>
                <span v-show="node.expanded" class="el-icon-remove-outline custom-node_e" @click="openOrClose(node)"/>
              </el-col>
              <!-- 批量操作 -->
              <el-col :class="data.checkBox? 'custom-tree-node-hide' : 'custom-tree-node-col'" style="padding-left: 0px; padding-right: 0px;"
                      v-show="(data.hashTree && data.hashTree.length === 0 )|| data.isLeaf">
                <show-more-btn :is-show="node.checked" :buttons="batchOperators" :size="selectDataCounts" v-show="data.checkBox" style="margin-right: 3px"/>
              </el-col>
              <el-col>
                <!-- 步骤组件-->
                <ms-component-config
                  :isMax="true"
                  :type="data.type"
                  :scenario="data"
                  :response="response"
                  :currentScenario="currentScenario"
                  :currentEnvironmentId="currentEnvironmentId"
                  :node="node"
                  :project-list="projectList"
                  :env-map="projectEnvMap"
                  :message="message"
                  @remove="remove" @copyRow="copyRow"
                  @runScenario="runScenario"
                  @stopScenario="stopScenario"
                  @suggestClick="suggestClick"
                  @refReload="refReload" @openScenario="openScenario"
                  v-if="stepFilter.get('ALlSamplerStep').indexOf(data.type) ===-1
                         || (!node.parent || !node.parent.data || stepFilter.get('AllSamplerProxy').indexOf(node.parent.data.type) === -1)"
                />
                <div v-else class="el-tree-node is-hidden is-focusable is-leaf" style="display: none;">
                  {{ hideNode(node) }}
                </div>
              </el-col>
            </el-row>
          </el-tree>
          <div @click="fabClick">
            <vue-fab id="fab" mainBtnColor="#783887" size="small" :global-options="globalOptions"
                     :click-auto-close="false" ref="refFab">
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

        </div>
      </ms-aside-container>

      <ms-main-container v-loading="loading">
        <div v-if="!loading">
          <!-- 第一层当前节点内容-->
          <ms-component-config
            :isMax="false"
            :showBtn="false"
            :type="selectedTreeNode.type"
            :scenario="selectedTreeNode"
            :response="response"
            :currentScenario="currentScenario"
            :currentEnvironmentId="currentEnvironmentId"
            :node="selectedNode"
            :project-list="projectList"
            :env-map="projectEnvMap"
            :scenario-definition="scenarioDefinition"
            :draggable="false"
            @editScenarioAdvance="editScenarioAdvance"
            @remove="remove" @copyRow="copyRow" @suggestClick="suggestClick" @refReload="refReload" @openScenario="openScenario"
            v-if="selectedTreeNode && selectedNode"/>
          <!-- 请求下还有的子步骤-->
          <div v-if="selectedTreeNode && selectedTreeNode.hashTree && showNode(selectedTreeNode)">
            <div v-for="item in selectedTreeNode.hashTree" :key="item.id" class="ms-col-one">
              <ms-component-config
                :showBtn="false"
                :isMax="false"
                :type="item.type"
                :scenario="item"
                :response="response"
                :currentScenario="currentScenario"
                :currentEnvironmentId="currentEnvironmentId"
                :project-list="projectList"
                :env-map="projectEnvMap"
                :draggable="false"
                @remove="remove" @copyRow="copyRow" @suggestClick="suggestClick"
                @refReload="refReload" @openScenario="openScenario"
                v-show="selectedTreeNode && selectedNode && stepFilter.get('ALlSamplerStep').indexOf(item.type) ===-1"/>
            </div>
          </div>
        </div>
      </ms-main-container>
    </ms-container>

    <!--接口列表-->
    <scenario-api-relevance @save="pushApiOrCase" ref="scenarioApiRelevance" v-if="type!=='detail'"/>

    <!--自定义接口-->
    <el-drawer v-if="type!=='detail'" :visible.sync="customizeVisible" :destroy-on-close="true" direction="ltr"
               :withHeader="false" :title="$t('api_test.automation.customize_req')" style="overflow: auto"
               :modal="false" size="90%">
      <ms-api-customize :request="customizeRequest" @addCustomizeApi="addCustomizeApi"/>
    </el-drawer>
    <!--场景导入 -->
    <scenario-relevance v-if="type!=='detail'" @save="addScenario" ref="scenarioRelevance"/>

    <!-- 环境 -->
    <api-environment-config v-if="type!=='detail'" ref="environmentConfig" @close="environmentConfigClose"/>

    <!--执行组件-->
    <ms-run :debug="true" v-if="type!=='detail'" :environment="projectEnvMap" :reportId="reportId"
            :run-data="debugData"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>
    <!-- 调试结果 -->
    <el-drawer v-if="type!=='detail'" :visible.sync="debugVisible" :destroy-on-close="true" direction="ltr"
               :withHeader="true" :modal="false" size="90%">
      <ms-api-report-detail :report-id="reportId" :debug="true" :currentProjectId="projectId" @refresh="detailRefresh"/>
    </el-drawer>

    <!--场景公共参数-->
    <ms-variable-list v-if="type!=='detail'" @setVariables="setVariables" ref="scenarioParameters"
                      class="ms-sc-variable-header"/>
    <!--外部导入-->
    <api-import v-if="type!=='detail'" ref="apiImport" :saved="false" @refresh="apiImport"/>
    <el-backtop target=".el-main .ms-main-container" :visibility-height="10" :right="50" :bottom="20"></el-backtop>

  </div>
</template>

<script>
import {API_STATUS, PRIORITY} from "../../../definition/model/JsonData";
import {parseEnvironment} from "../../../definition/model/EnvironmentModel";
import {ELEMENT_TYPE, STEP} from "../Setting";
import {exportPdf, getCurrentProjectID, getUUID, hasLicense, strMapToObj} from "@/common/js/utils";
import "@/common/css/material-icons.css"
import OutsideClick from "@/common/js/outside-click";
import {handleCtrlSEvent} from "../../../../../../common/js/utils";
import {saveScenario} from "@/business/components/api/automation/api-automation";
import {buttons, setComponent} from '../menu/Menu';
import MsContainer from "../../../../common/components/MsContainer";
import MsMainContainer from "../../../../common/components/MsMainContainer";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import html2canvas from "html2canvas";
// import MsAsideContainer from "./MsLeftContainer";

let jsonPath = require('jsonpath');
export default {
  name: "MaximizeScenario",
  props: {
    moduleOptions: Array,
    currentScenario: {},
    type: String,
    debug: Boolean,
    reloadDebug: String,
    stepReEnable: Boolean,
    scenarioDefinition: Array,
    envMap: Map,
    reqTotal: Number,
    reqSuccess: Number,
    reqError: Number,
    reqTotalTime: Number,
    message: String,
    environmentType: String,
    groupId: String,
    projectIds: Set,
    projectList: Array,
  },
  components: {
    MsAsideContainer,
    MsContainer,
    MsMainContainer,
    MsVariableList: () => import("../variable/VariableList"),
    ScenarioRelevance: () => import("../api/ScenarioRelevance"),
    ScenarioApiRelevance: () => import("../api/ApiRelevance"),
    ApiEnvironmentConfig: () => import("@/business/components/api/test/components/ApiEnvironmentConfig"),
    MsApiReportDetail: () => import("../../report/ApiReportDetail"),
    MsInputTag: () => import("../MsInputTag"),
    MsRun: () => import("../DebugRun"),
    MsApiCustomize: () => import("../ApiCustomize"),
    ApiImport: () => import("../../../definition/components/import/ApiImport"),
    MsComponentConfig: () => import("../component/ComponentConfig"),
    EnvPopover: () => import("@/business/components/api/automation/scenario/EnvPopover"),
    ShowMoreBtn: () => import( "@/business/components/track/case/components/ShowMoreBtn"),
  },
  data() {
    return {
      props: {
        label: "label",
        children: "hashTree",
        disabled: false
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
      },
      environments: [],
      projectEnvMap: Map,
      currentEnvironmentId: "",
      maintainerOptions: [],
      value: API_STATUS[0].id,
      options: API_STATUS,
      levels: PRIORITY,
      scenario: {},
      loading: false,
      showHideTree: true,
      apiListVisible: false,
      customizeVisible: false,
      scenarioVisible: false,
      debugVisible: false,
      customizeRequest: {protocol: "HTTP", type: "API", hashTree: [], referenced: 'Created', active: false},
      operatingElements: [],
      currentRow: {cases: [], apis: [], referenced: true},
      selectedTreeNode: undefined,
      selectedNode: undefined,
      expandedNode: [],
      path: "/api/automation/create",
      debugData: {},
      reportId: "",
      enableCookieShare: false,
      globalOptions: {
        spacing: 30
      },
      response: {},
      debugResult: new Map,
      expandedStatus: false,
      stepEnable: true,
      debugLoading: false,
      buttonData: [],
      stepFilter: new STEP,
      asideHidden: false,
      selectDataCounts: 0,
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
      reloadKey: "",
      envType: this.environmentType,
      envGroupId: this.groupId,
      cookieShare: false,
      sampleError: true,
      envResult: {
        loading: false
      },
      isBatchProcess: false,
      isCheckedAll: false,
    }
  },
  created() {
    if (!this.currentScenario.apiScenarioModuleId) {
      this.currentScenario.apiScenarioModuleId = "";
    }
    this.operatingElements = this.stepFilter.get("ALL");
    this.stepEnable = this.stepReEnable;
    this.initPlugins();
    this.buttonData = buttons(this);
    this.projectEnvMap = this.envMap;
  },
  mounted() {
    this.$refs.refFab.openMenu();
  },

  watch: {
    reloadDebug() {
      this.reload();
    },
    debug() {
      this.debugLoading = this.debug;
    },
  },
  directives: {OutsideClick},
  computed: {
    buttons,
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    initPlugins() {
      let url = "/plugin/list";
      this.$get(url, response => {
        let data = response.data;
        if (data) {
          data.forEach(item => {
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
            if (item.license) {
              if (hasLicense()) {
                if (this.operatingElements && this.operatingElements.includes(item.jmeterClazz)) {
                  this.buttonData.push(plugin);
                }
              }
            } else {
              if (this.operatingElements && this.operatingElements.includes(item.jmeterClazz)) {
                this.buttonData.push(plugin);
              }
            }
          });
        }
      });
    },
    // 打开引用的场景
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
    },
    createCtrlSHandle(event) {
      handleCtrlSEvent(event, this.editScenario);
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
    },
    fabClick() {
      if (this.operatingElements.length < 1) {
        this.$info("引用的场景或接口无法添加配置");
      }
    },
    showNode(node) {
      node.active = true;
      if (node && this.stepFilter.get("AllSamplerProxy").indexOf(node.type) != -1) {
        return true;
      }
      return false;
    },
    addComponent(type) {
      setComponent(type, this);
    },
    setAsideHidden(data) {
      this.asideHidden = data;
    },
    nodeClick(data, node) {
      if (data.referenced != 'REF' && data.referenced != 'Deleted' && !data.disabled) {
        this.operatingElements = this.stepFilter.get(data.type);
      } else {
        this.operatingElements = [];
      }
      if (!this.operatingElements) {
        this.operatingElements = this.stepFilter.get("ALL");
      }
      this.selectedTreeNode = data;
      this.selectedNode = node;
      this.$store.state.selectStep = data;
      this.buttonData = buttons(this);
      this.reload();
      this.initPlugins();
    },
    suggestClick(node) {
      this.response = {};
      if (node && node.parent && node.parent.data.requestResult) {
        this.response = node.parent.data.requestResult[0];
      } else if (this.selectedNode) {
        this.response = this.selectedNode.data.requestResult[0];
      }
    },
    showAll() {
      if (!this.customizeVisible) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.buttonData = buttons(this);
        this.initPlugins();
        this.selectedTreeNode = undefined;
        this.$store.state.selectStep = undefined;
      }
    },
    apiListImport() {
      this.$refs.scenarioApiRelevance.open();
    },
    recursiveSorting(arr, scenarioProjectId) {
      for (let i in arr) {
        arr[i].index = Number(i) + 1;
        if (arr[i].type === ELEMENT_TYPE.LoopController && arr[i].loopType === "LOOP_COUNT" && arr[i].hashTree && arr[i].hashTree.length > 1) {
          arr[i].countController.proceed = true;
        }
        if (!arr[i].projectId) {
          arr[i].projectId = scenarioProjectId ? scenarioProjectId : this.projectId;
        }
        if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
          this.hideTreeNode(arr[i], arr[i].hashTree);
          this.recursiveSorting(arr[i].hashTree, arr[i].projectId);
        }
        // 添加debug结果
        if (this.debugResult && this.debugResult.get(arr[i].id + arr[i].name)) {
          arr[i].requestResult = this.debugResult.get(arr[i].id + arr[i].name);
        }
      }
    },
    openOrClose(node) {
      node.expanded = !node.expanded;
      if (node.expanded) {
        this.nodeExpand(node.data);
      } else {
        this.nodeCollapse(node.data);
      }
    },
    hideNode(node) {
      node.isLeaf = true;
      node.visible = false;
    },
    hideTreeNode(node, array) {
      let isLeaf = true;
      array.forEach(item => {
        if (this.stepFilter.get('ALlSamplerStep').indexOf(item.type) === -1) {
          isLeaf = false;
        }
        if (item.hashTree && item.hashTree.length > 0) {
          this.hideTreeNode(item, item.hashTree);
        }
      })
      node.isLeaf = isLeaf;
    },
    sort() {
      for (let i in this.scenarioDefinition) {
        // 排序
        this.scenarioDefinition[i].index = Number(i) + 1;
        // 设置循环控制
        if (this.scenarioDefinition[i].type === ELEMENT_TYPE.LoopController && this.scenarioDefinition[i].hashTree
          && this.scenarioDefinition[i].hashTree.length > 1) {
          this.scenarioDefinition[i].countController.proceed = true;
        }
        // 设置项目ID
        if (!this.scenarioDefinition[i].projectId) {
          this.scenarioDefinition[i].projectId = this.projectId;
        }

        if (this.scenarioDefinition[i].hashTree != undefined && this.scenarioDefinition[i].hashTree.length > 0) {
          this.hideTreeNode(this.scenarioDefinition[i], this.scenarioDefinition[i].hashTree);
          this.recursiveSorting(this.scenarioDefinition[i].hashTree, this.scenarioDefinition[i].projectId);
        }
        // 添加debug结果
        if (this.debugResult && this.debugResult.get(this.scenarioDefinition[i].id + this.scenarioDefinition[i].name)) {
          this.scenarioDefinition[i].requestResult = this.debugResult.get(this.scenarioDefinition[i].id + this.scenarioDefinition[i].name);
        }
      }
    },
    addCustomizeApi(request) {
      this.customizeVisible = false;
      request.enable === undefined ? request.enable = true : request.enable;
      if (this.selectedTreeNode != undefined) {
        this.selectedTreeNode.hashTree.push(request);
      } else {
        this.scenarioDefinition.push(request);
      }
      this.customizeRequest = {};
      this.sort();
      this.reload();
      this.initProjectIds();
    },
    addScenario(arr) {
      if (arr && arr.length > 0) {
        arr.forEach(item => {
          if (item.id === this.currentScenario.id) {
            this.$error(this.$t('api_test.scenario.scenario_error'));
            return;
          }
          if (!item.hashTree) {
            item.hashTree = [];
          }
          item.enable === undefined ? item.enable = true : item.enable;
          this.scenarioDefinition.push(item);
        })
      }
      this.sort();
      this.reload();
      this.initProjectIds();
      this.scenarioVisible = false;
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
      if (!request.url) {
        request.url = "";
      }
      if (referenced === 'REF' || !request.hashTree) {
        request.hashTree = [];
      }
      if (this.selectedTreeNode != undefined) {
        this.selectedTreeNode.hashTree.push(request);
      } else {
        this.scenarioDefinition.push(request);
      }
    },
    pushApiOrCase(data, refType, referenced) {
      data.forEach(item => {
        this.setApiParameter(item, refType, referenced);
      });
      this.sort();
      this.reload();
      this.initProjectIds();
      this.cancelBatchProcessing();
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
            const index = hashTree.findIndex(d => d.resourceId != undefined && row.resourceId != undefined && d.resourceId === row.resourceId)
            hashTree.splice(index, 1);
            this.sort();
            this.reload();
            this.initProjectIds();
          }
        }
      });
    },
    copyRow(row, node) {
      if (!row || !node) {
        return;
      }
      const parent = node.parent
      const hashTree = parent.data.hashTree || parent.data;
      // 深度复制
      let obj = JSON.parse(JSON.stringify(row));
      if (obj.hashTree && obj.hashTree.length > 0) {
        this.resetResourceId(obj.hashTree);
      }
      obj.resourceId = getUUID();
      if (obj.name) {
        obj.name = obj.name + '_copy';
      }
      const index = hashTree.findIndex(d => d.resourceId === row.resourceId);
      if (index != -1) {
        hashTree.splice(index + 1, 0, obj);
      } else {
        hashTree.push(obj);
      }
      this.sort();
      this.reload();
    },
    resetResourceId(hashTree) {
      hashTree.forEach(item => {
        item.resourceId = getUUID();
        if (item.hashTree && item.hashTree.length > 0) {
          this.resetResourceId(item.hashTree);
        }
      })
    },
    showHide() {
      this.showHideTree = false
      this.$nextTick(() => {
        this.showHideTree = true
      });
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    debugScenario() {
      this.debugLoading = true;
      this.$emit('runDebug');
    },
    runDebug() {
      /*触发执行操作*/
      let sign = this.$refs.envPopover.checkEnv();
      if (!sign) {
        this.errorRefresh();
        return;
      }
      if (this.$refs['currentScenario']) {
        this.$refs['currentScenario'].validate((valid) => {
          if (valid) {
            Promise.all([
              this.editScenario()]).then(val => {
              if (val) {
                this.debugData = {
                  id: this.currentScenario.id,
                  name: this.currentScenario.name,
                  type: "scenario",
                  variables: this.currentScenario.variables,
                  referenced: 'Created',
                  enableCookieShare: this.enableCookieShare,
                  headers: this.currentScenario.headers,
                  environmentMap: this.projectEnvMap,
                  hashTree: this.scenarioDefinition
                };
                this.reportId = getUUID().substring(0, 8);
              }
            });
          } else {
            this.errorRefresh();
          }
        })
      }
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
      if (dropType != "inner") {
        return true;
      } else if (dropType === "inner" && dropNode.data.referenced !== 'REF' && dropNode.data.referenced !== 'Deleted'
        && this.stepFilter.get(dropNode.data.type).indexOf(draggingNode.data.type) != -1) {
        return true;
      }
      return false;
    },
    allowDrag(draggingNode, dropNode, dropType) {
      if (dropNode && draggingNode && dropType) {
        this.sort();
      }
    },
    nodeExpand(data) {
      if (data.resourceId) {
        this.expandedNode.push(data.resourceId);
      }
    },
    nodeCollapse(data) {
      if (data.resourceId) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.resourceId), 1);
      }
    },
    getPath(id) {
      if (id === null) {
        return null;
      }
      let path = this.moduleOptions.filter(function (item) {
        return item.id === id ? item.path : "";
      });
      return path[0].path;
    },
    setFiles(item, bodyUploadFiles, obj) {
      if (item.body) {
        if (item.body.kvs) {
          item.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  if (!item.id) {
                    let fileId = getUUID().substring(0, 12);
                    item.name = item.file.name;
                    item.id = fileId;
                  }
                  obj.bodyUploadIds.push(item.id);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        if (item.body.binary) {
          item.body.binary.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  if (!item.id) {
                    let fileId = getUUID().substring(0, 12);
                    item.name = item.file.name;
                    item.id = fileId;
                  }
                  obj.bodyUploadIds.push(item.id);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
      }
    },
    recursiveFile(arr, bodyUploadFiles, obj) {
      arr.forEach(item => {
        this.setFiles(item, bodyUploadFiles, obj);
        if (item.hashTree != undefined && item.hashTree.length > 0) {
          this.recursiveFile(item.hashTree, bodyUploadFiles, obj);
        }
      });
    },
    editScenario() {
      return new Promise((resolve, reject) => {
        document.getElementById("inputDelay").focus();  //  保存前在input框自动失焦，以免保存失败
        this.$refs['currentScenario'].validate((valid) => {
          if (valid) {
            this.setParameter();
            saveScenario(this.path, this.currentScenario, this.scenarioDefinition, this, (response) => {
              this.$success(this.$t('commons.save_success'));
              this.path = "/api/automation/update";
              if (response.data) {
                this.currentScenario.id = response.data.id;
              }
              if (this.currentScenario.tags instanceof String) {
                this.currentScenario.tags = JSON.parse(this.currentScenario.tags);
              }
              this.$emit('refresh', this.currentScenario);
              resolve();
            });
          }
        })
      });
    },

    setParameter() {
      this.currentScenario.stepTotal = this.scenarioDefinition.length;
      this.currentScenario.projectId = this.projectId;
      this.currentScenario.modulePath = this.getPath(this.currentScenario.apiScenarioModuleId);
      // 构建一个场景对象 方便引用处理
      let scenario = {
        id: this.currentScenario.id,
        enableCookieShare: this.enableCookieShare,
        name: this.currentScenario.name,
        type: "scenario",
        variables: this.currentScenario.variables,
        headers: this.currentScenario.headers,
        referenced: 'Created',
        environmentMap: strMapToObj(this.projectEnvMap),
        hashTree: this.scenarioDefinition,
        projectId: this.projectId,
      };
      this.currentScenario.scenarioDefinition = scenario;
      if (this.currentScenario.tags instanceof Array) {
        this.currentScenario.tags = JSON.stringify(this.currentScenario.tags);
      }
      if (this.currentModule != null) {
        this.currentScenario.modulePath = this.currentModule.method !== undefined ? this.currentModule.method : null;
        this.currentScenario.apiScenarioModuleId = this.currentModule.id;
      }
      this.currentScenario.projectId = this.projectId;
    },
    runRefresh() {
      this.debugVisible = true;
      this.loading = false;
    },
    errorRefresh() {
      this.debugVisible = false;
      this.loading = false;
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
    },
    refReload(data, node) {
      this.selectedTreeNode = data;
      this.$store.state.selectStep = data;
      this.selectedNode = node;
      this.initProjectIds();
      this.reload();
    },
    initProjectIds() {
      // 加载环境配置
      this.$nextTick(() => {
        this.projectIds.clear();
        this.scenarioDefinition.forEach(data => {
          let arr = jsonPath.query(data, "$..projectId");
          arr.forEach(a => this.projectIds.add(a));
        })
      })
    },
    detailRefresh(result) {
      // 把执行结果分发给各个请求
      this.debugResult = result;
      this.sort()
    },
    changeNodeStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (this.expandedStatus) {
            this.expandedNode.push(nodes[i].resourceId);
          }
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.changeNodeStatus(nodes[i].hashTree);
          }
        }
      }
    },
    openExpansion() {
      this.expandedNode = [];
      this.expandedStatus = true;
      let resourceIds = this.getAllResourceIds();
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
    },
    closeExpansion() {
      this.expandedStatus = false;
      this.expandedNode = [];
      let resourceIds = this.getAllResourceIds();
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.forceRerender();
    },
    stepNode() {
      //改变每个节点的状态
      for (let i in this.scenarioDefinition) {
        if (this.scenarioDefinition[i]) {
          this.scenarioDefinition[i].enable = this.stepEnable;
          if (this.scenarioDefinition[i].hashTree && this.scenarioDefinition[i].hashTree.length > 0) {
            this.stepStatus(this.scenarioDefinition[i].hashTree);
          }
        }
      }
    },
    stepStatus(resourceIds, nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (resourceIds.indexOf(nodes[i].resourceId) !== -1) {
            nodes[i].enable = this.stepEnable;
          }
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(nodes[i].hashTree);
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
      this.getAllResourceIds().forEach(item => {
        this.recursionDelete(item, this.scenarioDefinition);
      });
      this.sort();
      this.forceRerender();
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
    forceRerender() {
      this.$nextTick(() => {
        this.$store.state.forceRerenderIndex = getUUID();
      });
    },
    getAllResourceIds() {
      let selectValueArr = [];
      if (this.$refs.maxStepTree) {
        let checkNodes = this.$refs.maxStepTree.getCheckedNodes();
        for (let node of checkNodes) {
          selectValueArr.push(node.resourceId);
        }
      }
      return selectValueArr;
    },
    runScenario(scenario) {
      this.$emit('runScenario', scenario);
    },
    stopScenario() {
      this.$emit('stopScenario');
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },

    batchProcessing() {
      this.isBatchProcess = true;
      this.expandedNode = [];
      this.hideAllTreeNode(this.scenarioDefinition);
      if (this.$refs.maxStepTree && this.$refs.maxStepTree.root && this.$refs.maxStepTree.root.childNodes) {
        this.stepCheckedAll(true, this.$refs.maxStepTree.root.childNodes);
      }
      if (this.$refs.maxStepTree && this.$refs.maxStepTree.root && this.$refs.maxStepTree.root.childNodes) {
        this.stepCheckedAll(false, this.$refs.maxStepTree.root.childNodes);
      }
      this.reload();
    },
    cancelBatchProcessing() {
      this.isBatchProcess = false;
      this.isCheckedAll = false;
      if (this.$refs.maxStepTree && this.$refs.maxStepTree.root && this.$refs.maxStepTree.root.childNodes) {
        this.stepCheckedAll(true, this.$refs.maxStepTree.root.childNodes);
      }
      if (this.$refs.maxStepTree && this.$refs.maxStepTree.root && this.$refs.maxStepTree.root.childNodes) {
        this.stepCheckedAll(false, this.$refs.maxStepTree.root.childNodes);
      }
      this.selectDataCounts = 0;
      this.commandTreeNode();
      this.reload();
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
        }
      })
      if (node) {
        node.isBatchProcess = this.isBatchProcess;
        node.checkBox = false;
        node.isLeaf = isLeaf;
      }
    },
    checkedAll(v) {
      if (this.$refs.maxStepTree && this.$refs.maxStepTree.root && this.$refs.maxStepTree.root.childNodes) {
        this.stepCheckedAll(v, this.$refs.maxStepTree.root.childNodes);
      }
    },
    stepCheckedAll(v, array) {
      if (array) {
        array.forEach(item => {
          if (item.childNodes && item.childNodes.length > 0) {
            this.stepCheckedAll(v, item.childNodes);
          }
          item.checked = v;
        })
      }
    },
    reloadTreeStatus() {
      this.reloadKey = "";
      this.$nextTick(() => {
        this.reloadKey = "reload";
        let row = {resourceId: "ms-reload-max-test"};
        if (this.$refs.maxStepTree && this.$refs.maxStepTree.root.data) {
          this.$refs.maxStepTree.root.data.push(row);
          this.$refs.maxStepTree.root.data.splice(this.$refs.maxStepTree.root.data.length - 1, 1);
        }
      });
    },
    hideAllTreeNode(array) {
      array.forEach(item => {
        if (item.hashTree && item.hashTree.length > 0) {
          this.hideAllTreeNode(item.hashTree);
        }
        item.isLeaf = this.isBatchProcess;
        item.isBatchProcess = this.isBatchProcess;
        item.checkBox = this.isBatchProcess;
      })
    },
    handleExport() {
      let name = this.$t('commons.report_statistics.test_case_analysis');
      this.$nextTick(function () {
        setTimeout(() => {
          html2canvas(document.getElementById('reportAnalysis'), {
            scale: 2
          }).then(function (canvas) {
            exportPdf(name, [canvas]);
          });
        }, 1000);
      });
    },
    stop() {
      this.debugLoading = false;
      this.$emit('stop');
    },
    close() {
      this.$emit('closePage', this.currentScenario.name);
    },
    unFullScreen() {
      this.$emit('unFullScreen');
    },
    handleCommand() {
      this.debug = false;
      this.debugLoading = false;
      this.$emit('handleCommand');
    },
    setCookieShare() {
      this.$emit('setCookieShare', this.cookieShare);
    },
    setOnSampleError() {
      this.$emit('setSampleError', this.sampleError);
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
    getEnv(definition) {
      return new Promise((resolve) => {
        this.$post("/api/automation/getApiScenarioEnv", {definition: definition}, res => {
          if (res.data) {
            res.data.projectIds.push(this.projectId);
            this.$emit("update:projectIds", new Set(res.data.projectIds));
            this.$emit("update:isFullUrl", res.data.fullUrl);
          }
          resolve();
        })
      });
    },
  },
}
</script>

<style scoped>

.ms-col-one {
  margin-top: 10px;
}

#fab {
  right: 90px;
  z-index: 5;
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

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 9;
}


.ms-tree >>> .el-tree-node__expand-icon.expanded {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree >>> .el-icon-caret-right:before {
  /*content: '\e723';*/
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

.custom-tree-node {
  width: 1000px;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

.ms-debug-result {
  font-size: 12px;
  margin-right: 20px;
  margin-top: 3px;
  height: 20px;
}

.ms-message-right {
  margin-right: 10px;
}

.maximize-container .ms-aside-container {
  min-width: 500px;
}

.custom-tree-node-hide {
  width: 10px;
  padding: 0px;
  vertical-align: center;
}

.ms-aside-container {
  height: calc(100vh - 50px) !important;
}

.custom-node_e {
  color: #7C3985;
  font-size: 20px;
}

.custom-tree-node-col {
  width: 10px;
  padding: 0px;
}

.ms-header {
  border-bottom: 1px solid #E6E6E6;
  width: 100%;
  height: 36px;
  background-color: #FFF;
  font-size: 12px;
}

.ms-div {
  margin-top: 10px;
}

.ms-min-alt-ico {
  font-size: 15px;
  margin: 10px 10px 0px;
  float: right;
  color: #8c939d;
}

.ms-min-alt-ico:hover {
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
</style>
