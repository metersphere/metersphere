<template>
  <div>
    <!-- 场景步骤-->
    <ms-container>
      <ms-aside-container style="padding-top: 0px">
        <!-- 场景步骤内容 -->
        <div v-loading="loading">
          <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
            <i class="el-icon-circle-plus-outline  ms-open-btn ms-open-btn-left" v-prevent-re-click @click="openExpansion"/>
          </el-tooltip>
          <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
            <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion"/>
          </el-tooltip>
          <el-tooltip :content="$t('api_test.scenario.disable')" placement="top" effect="light" v-if="!stepEnable">
            <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-off']" @click="enableAll"/>
          </el-tooltip>
          <el-tooltip :content="$t('api_test.scenario.enable')" placement="top" effect="light" v-else>
            <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-on']" @click="disableAll"/>
          </el-tooltip>
          <span class="ms-debug-result" v-if="debug">
              <div class="ms-debug-result" v-if="debug">
                <span class="ms-message-right"> {{ reqTotalTime }} ms </span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_total') }} {{ reqTotal }}</span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_success') }} {{ reqSuccess }}</span>
                <span class="ms-message-right"> {{ $t('api_test.automation.request_error') }} {{ reqError }}</span>
              </div>
          </span>
          <el-tree node-key="resourceId"
                   :props="props"
                   :data="scenarioDefinition"
                   :default-expanded-keys="expandedNode"
                   :expand-on-click-node="false"
                   highlight-current
                   @node-expand="nodeExpand"
                   @node-collapse="nodeCollapse"
                   :allow-drop="allowDrop" @node-drag-end="allowDrag" @node-click="nodeClick" v-if="!loading" draggable>
              <span class="custom-tree-node father" slot-scope="{ node, data}">
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
                   @remove="remove" @copyRow="copyRow"
                   @suggestClick="suggestClick"
                   @refReload="refReload" @openScenario="openScenario"/>
              </span>
          </el-tree>
          <div @click="fabClick">
            <vue-fab id="fab" mainBtnColor="#783887" size="small" :global-options="globalOptions"
                     :click-auto-close="false">
              <fab-item
                v-for="(item, index) in buttons"
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
            :draggable="false"
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
                v-if="selectedTreeNode && selectedNode"/>
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
            @runRefresh="runRefresh" ref="runTest"/>
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
  </div>
</template>

<script>
import {API_STATUS, PRIORITY} from "../../../definition/model/JsonData";
import {parseEnvironment} from "../../../definition/model/EnvironmentModel";
import {ELEMENT_TYPE, ELEMENTS} from "../Setting";
import MsApiCustomize from "../ApiCustomize";
import {getCurrentProjectID, getUUID, strMapToObj} from "@/common/js/utils";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";
import MsInputTag from "../MsInputTag";
import MsRun from "../DebugRun";
import MsApiReportDetail from "../../report/ApiReportDetail";
import MsVariableList from "../variable/VariableList";
import ApiImport from "../../../definition/components/import/ApiImport";
import "@/common/css/material-icons.css"
import OutsideClick from "@/common/js/outside-click";
import ScenarioApiRelevance from "../api/ApiRelevance";
import ScenarioRelevance from "../api/ScenarioRelevance";
import MsComponentConfig from "../component/ComponentConfig";
import {handleCtrlSEvent} from "../../../../../../common/js/utils";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";
import MsContainer from "../../../../common/components/MsContainer";
import MsMainContainer from "../../../../common/components/MsMainContainer";
import MsAsideContainer from "./MsLeftContainer";
import {saveScenario} from "@/business/components/api/automation/api-automation";
import {buttons, setComponent} from '../menu/Menu';

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
  },
  components: {
    MsVariableList,
    ScenarioRelevance,
    ScenarioApiRelevance,
    ApiEnvironmentConfig,
    MsApiReportDetail,
    MsInputTag, MsRun,
    MsApiCustomize,
    ApiImport,
    MsComponentConfig,
    EnvPopover,
    MsContainer,
    MsMainContainer,
    MsAsideContainer
  },
  data() {
    return {
      props: {
        label: "label",
        children: "hashTree"
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
      currentEnvironmentId: "",
      maintainerOptions: [],
      value: API_STATUS[0].id,
      options: API_STATUS,
      levels: PRIORITY,
      scenario: {},
      loading: false,
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
      projectIds: new Set,
      projectEnvMap: new Map,
      projectList: [],
      debugResult: new Map,
      expandedStatus: false,
      stepEnable: true,
      debugLoading: false,
    }
  },
  created() {
    if (!this.currentScenario.apiScenarioModuleId) {
      this.currentScenario.apiScenarioModuleId = "";
    }
    this.operatingElements = ELEMENTS.get("ALL");
    this.projectEnvMap = this.envMap;
    this.stepEnable = this.stepReEnable;
  },
  watch: {
    envMap() {
      this.projectEnvMap = this.envMap;
    },
    reloadDebug() {
      this.reload();
    }
  },
  directives: {OutsideClick},
  computed: {
    buttons,
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
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
      if (node && ELEMENTS.get("AllSamplerProxy").indexOf(node.type) != -1) {
        return true;
      }
      return false;
    },
    addComponent(type) {
      setComponent(type, this);
    },
    nodeClick(data, node) {
      if (data.referenced != 'REF' && data.referenced != 'Deleted' && !data.disabled) {
        this.operatingElements = ELEMENTS.get(data.type);
      } else {
        this.operatingElements = [];
      }
      if (data) {
        data.active = true;
        if (data.hashTree) {
          data.hashTree.forEach(item => {
            if (item) {
              item.active = true;
            }
          })
        }
      } else {
        data.active = false;
      }
      this.selectedTreeNode = data;
      this.selectedNode = node;
      this.reload();
    },
    suggestClick(node) {
      this.response = {};
      if (node && node.parent && node.parent.data.requestResult) {
        this.response = node.parent.data.requestResult;
      } else if (this.selectedNode) {
        this.response = this.selectedNode.data.requestResult;
      }
    },
    showAll() {
      if (!this.customizeVisible) {
        this.operatingElements = ELEMENTS.get("ALL");
        this.selectedTreeNode = undefined;
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
          this.recursiveSorting(arr[i].hashTree, arr[i].projectId);
        }
        // 添加debug结果
        if (this.debugResult && this.debugResult.get(arr[i].id + arr[i].name)) {
          arr[i].requestResult = this.debugResult.get(arr[i].id + arr[i].name);
        }
      }
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
            this.$error("不能引用或复制自身！");
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
    }
    ,
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    runDebug() {
      /*触发执行操作*/
      let sign = this.$refs.envPopover.checkEnv();
      if (!sign) {
        return;
      }
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
      if (dropType != "inner") {
        return true;
      } else if (dropType === "inner" && dropNode.data.referenced !== 'REF' && dropNode.data.referenced !== 'Deleted'
        && ELEMENTS.get(dropNode.data.type).indexOf(draggingNode.data.type) != -1) {
        return true;
      }
      return false;
    },
    allowDrag(draggingNode, dropNode, dropType) {
      if (dropNode && draggingNode && dropType) {
        this.sort();
        this.reload();
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
            saveScenario(this.path, this.currentScenario, this.scenarioDefinition, (response) => {
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
    shrinkTreeNode() {
      //改变每个节点的状态
      for (let i in this.scenarioDefinition) {
        if (i > 30 && this.expandedStatus) {
          continue;
        }
        if (this.scenarioDefinition[i]) {
          if (this.expandedStatus) {
            this.expandedNode.push(this.scenarioDefinition[i].resourceId);
          }
          this.scenarioDefinition[i].active = this.expandedStatus;
          if (this.scenarioDefinition[i].hashTree && this.scenarioDefinition[i].hashTree.length > 0) {
            this.changeNodeStatus(this.scenarioDefinition[i].hashTree);
          }
        }
      }
    },
    changeNodeStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (this.expandedStatus) {
            this.expandedNode.push(nodes[i].resourceId);
          }
          nodes[i].active = this.expandedStatus;
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.changeNodeStatus(nodes[i].hashTree);
          }
        }
      }
    },
    openExpansion() {
      if (this.scenarioDefinition && this.scenarioDefinition.length > 30) {
        this.$alert(this.$t('api_test.definition.request.step_message'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.expandedNode = [];
              this.expandedStatus = true;
              this.shrinkTreeNode();
            }
          }
        });
      } else {
        this.expandedNode = [];
        this.expandedStatus = true;
        this.shrinkTreeNode();
      }
    },
    closeExpansion() {
      this.expandedStatus = false;
      this.expandedNode = [];
      this.shrinkTreeNode();
      this.reload();
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
    stepStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          nodes[i].enable = this.stepEnable;
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(nodes[i].hashTree);
          }
        }
      }
    },
    enableAll() {
      this.stepEnable = true;
      this.stepNode();
    },
    disableAll() {
      this.stepEnable = false;
      this.stepNode();
    }
  }
}
</script>

<style scoped>
.card-content {
  height: calc(100vh - 196px);
  overflow-y: auto;
}

.ms-scenario-input {
  width: 100%;
}

.ms-main-div {
  background-color: white;
}

.ms-debug-div {
  border: 1px #DCDFE6 solid;
  border-radius: 4px;
  margin-right: 20px;
}

.ms-scenario-button {
  margin-left: 20px;
  padding: 7px;
}

.ms-api-col {
  background-color: #7C3985;
  border-color: #7C3985;
  margin-right: 10px;
  color: white;
}

.ms-font {
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
}

.ms-col-one {
  margin-top: 10px;
}

#fab {
  left: 100px;
  z-index: 5;
}

/deep/ .el-tree-node__content {
  height: 100%;
  margin-top: 8px;
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

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree >>> .el-icon-caret-right:before {
  content: '\e723';
  font-size: 20px;
}

.ms-tree >>> .el-tree-node__expand-icon.is-leaf {
  color: transparent;
}

.ms-tree >>> .el-tree-node__expand-icon {
  color: #7C3985;
}

.ms-tree >>> .el-tree-node__expand-icon.expanded.el-icon-caret-right:before {
  color: #7C3985;
  content: "\e722";
  font-size: 20px;
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

.scenario-aside {
  min-width: 400px;
  position: relative;
  border-radius: 4px;
  border: 1px solid #EBEEF5;
  box-sizing: border-box;
}

.scenario-main {
  position: relative;
  margin-left: 20px;
  border: 1px solid #EBEEF5;
}

.scenario-list {
  overflow-y: auto;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 28px;
}

.father:hover .child {
  display: block;
}

.ms-open-btn {
  margin: 5px 5px 0px;
  color: #6D317C;
  font-size: 20px;
}

.ms-open-btn:hover {
  background-color: #F2F9EE;
  cursor: pointer;
  color: #67C23A;
}

.ms-open-btn-left {
  margin-left: 30px;
}

.ms-debug-result {
  float: right;
  margin-right: 30px;
  margin-top: 3px;
}

.ms-message-right {
  margin-right: 10px;
}

</style>
