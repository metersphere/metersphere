<template>
  <el-card>
    <div class="card-content">
      <div class="ms-main-div" @click="showAll" v-if="type!=='detail'">

        <!--操作按钮-->
        <div class="ms-opt-btn">
          <el-link type="primary" style="margin-right: 20px" @click="openHis" v-if="path === '/api/automation/update'">{{ $t('operating_log.change_history') }}</el-link>

          <el-button id="inputDelay" type="primary" size="small" v-prevent-re-click @click="editScenario"
                     title="ctrl + s">
            {{ $t('commons.save') }}
          </el-button>
        </div>

        <div class="tip">{{ $t('test_track.plan_view.base_info') }}</div>
        <el-form :model="currentScenario" label-position="right" label-width="80px" size="small" :rules="rules"
                 ref="currentScenario" style="margin-right: 20px">
          <!-- 基础信息 -->
          <el-row>
            <el-col :span="7">
              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input class="ms-scenario-input" size="small" v-model="currentScenario.name"/>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="currentScenario.apiScenarioModuleId" @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-scenario-input" size="small" v-model="currentScenario.status">
                  <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7">
              <el-form-item :label="$t('api_test.definition.request.responsible')" prop="principal">
                <el-select v-model="currentScenario.principal"
                           :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                           class="ms-scenario-input">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.id + ' (' + item.name + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item :label="$t('test_track.case.priority')" prop="level">
                <el-select class="ms-scenario-input" size="small" v-model="currentScenario.level">
                  <el-option v-for="item in levels" :key="item.id" :label="item.label" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item :label="$t('api_test.automation.follow_people')" prop="followPeople">
                <el-select v-model="currentScenario.followPeople"
                           :placeholder="$t('api_test.automation.follow_people')" filterable size="small"
                           class="ms-scenario-input">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.id + ' (' + item.name + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="7">
              <el-form-item :label="$t('api_test.automation.tag')" prop="tags">
                <ms-input-tag :currentScenario="currentScenario" ref="tag"/>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item :label="$t('commons.description')" prop="description">
                <el-input class="ms-http-textarea"
                          v-model="currentScenario.description"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 10}"
                          :rows="2" size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="7" v-if="customNum">
              <el-form-item label="ID" prop="customNum">
                <el-input v-model.trim="currentScenario.customNum" size="small"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

        </el-form>
      </div>
      <!-- 场景步骤-->
      <div v-loading="loading">
        <div @click="showAll">
          <p class="tip">{{ $t('api_test.automation.scenario_step') }} </p>
        </div>
        <el-row>
          <el-col :span="21">
            <!-- 调试部分 -->
            <div class="ms-debug-div" @click="showAll" :class="{'is-top' : isTop}" ref="debugHeader">
              <el-row style="margin: 5px">
                <el-col :span="4" class="ms-col-one ms-font">
                  <el-tooltip placement="top" effect="light">
                    <template v-slot:content>
                      <div>{{
                          currentScenario.name === undefined || '' ? $t('api_test.scenario.name') : currentScenario.name
                        }}
                      </div>
                    </template>
                    <span class="scenario-name">
                        {{
                        currentScenario.name === undefined || '' ? $t('api_test.scenario.name') : currentScenario.name
                      }}
                    </span>
                  </el-tooltip>
                </el-col>
                <el-col :span="3" class="ms-col-one ms-font">
                  {{ $t('api_test.automation.step_total') }}：{{ scenarioDefinition.length }}
                </el-col>
                <el-col :span="3" class="ms-col-one ms-font">
                  <el-link class="head" @click="showScenarioParameters">{{ $t('api_test.automation.scenario_total') }}
                  </el-link>
                  ：{{ getVariableSize() }}
                </el-col>
                <el-col :span="3" class="ms-col-one ms-font">
                  <el-checkbox v-model="enableCookieShare">共享cookie</el-checkbox>
                </el-col>
                <el-col :span="3" class="ms-col-one ms-font">
                  <el-checkbox v-model="onSampleError">{{ $t('commons.failure_continues') }}</el-checkbox>
                </el-col>

                <el-col :span="8">
                  <div style="float: right;width: 300px">
                    <env-popover :disabled="scenarioDefinition.length < 1" :env-map="projectEnvMap"
                                 :project-ids="projectIds" @setProjectEnvMap="setProjectEnvMap" :result="envResult"
                                 :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                                 :isReadOnly="scenarioDefinition.length < 1" @showPopover="showPopover"
                                 :project-list="projectList" ref="envPopover" class="ms-message-right"/>
                    <el-tooltip v-if="!debugLoading" content="Ctrl + R" placement="top">
                      <el-dropdown split-button type="primary" @click="runDebug" class="ms-message-right" size="mini" @command="handleCommand">
                        {{ $t('api_test.request.debug') }}
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item>{{ $t('api_test.automation.generate_report') }}</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </el-tooltip>
                    <el-button icon="el-icon-loading" size="mini" type="primary" :disabled="debug" v-else>执行中</el-button>
                    <el-tooltip class="item" effect="dark" :content="$t('commons.refresh')" placement="top-start">
                      <el-button :disabled="scenarioDefinition.length < 1" size="mini" icon="el-icon-refresh"
                                 v-prevent-re-click @click="getApiScenario"></el-button>
                    </el-tooltip>
                    <el-tooltip class="item" effect="dark" :content="$t('commons.full_screen_editing')"
                                placement="top-start">
                      <font-awesome-icon class="alt-ico" :icon="['fa', 'expand-alt']" size="lg" @click="fullScreen"/>
                    </el-tooltip>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 场景步骤内容 -->
            <div ref="stepInfo">
              <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
                <i class="el-icon-circle-plus-outline ms-open-btn ms-open-btn-left" v-prevent-re-click @click="openExpansion"/>
              </el-tooltip>
              <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
                <i class="el-icon-remove-outline ms-open-btn" size="mini" v-prevent-re-click @click="closeExpansion"/>
              </el-tooltip>
              <el-tooltip :content="$t('api_test.scenario.disable')" placement="top" effect="light" v-if="!stepEnable">
                <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-off']" v-prevent-re-click @click="enableAll"/>
              </el-tooltip>
              <el-tooltip :content="$t('api_test.scenario.enable')" placement="top" effect="light" v-else>
                <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-on']" v-prevent-re-click @click="disableAll"/>
              </el-tooltip>
              <div class="ms-debug-result" v-if="debug">
                <span class="ms-message-right"> {{ reqTotalTime }} ms </span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_total') }} {{ reqTotal }}</span>
                <span class="ms-message-right">{{ $t('api_test.automation.request_success') }} {{ reqSuccess }}</span>
                <span class="ms-message-right"> {{ $t('api_test.automation.request_error') }} {{ reqError }}</span>
              </div>
              <el-tree node-key="resourceId" :props="props" :data="scenarioDefinition" class="ms-tree"
                       :default-expanded-keys="expandedNode"
                       :expand-on-click-node="false"
                       highlight-current
                       @node-expand="nodeExpand"
                       @node-collapse="nodeCollapse"
                       :allow-drop="allowDrop" @node-drag-end="allowDrag" @node-click="nodeClick" v-if="!loading" draggable ref="stepTree">
                    <span class="custom-tree-node father" slot-scope="{ node, data}" style="width: 96%">
                      <!-- 步骤组件-->
                       <ms-component-config :message="message" :type="data.type" :scenario="data" :response="response" :currentScenario="currentScenario" :expandedNode="expandedNode"
                                            :currentEnvironmentId="currentEnvironmentId" :node="node" :project-list="projectList" :env-map="projectEnvMap"
                                            @remove="remove" @copyRow="copyRow" @suggestClick="suggestClick" @refReload="refReload" @openScenario="openScenario"/>
                    </span>
              </el-tree>
            </div>
          </el-col>
          <!-- 按钮列表 -->
          <el-col :span="3">
            <div @click="fabClick">
              <vue-fab id="fab" mainBtnColor="#783887" size="small" :global-options="globalOptions"
                       :click-auto-close="false" v-outside-click="outsideClick">
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
          </el-col>
        </el-row>
      </div>

      <!--接口列表-->
      <scenario-api-relevance @save="pushApiOrCase" @close="setHideBtn" ref="scenarioApiRelevance" v-if="type!=='detail'"/>

      <!--自定义接口-->
      <el-drawer v-if="type!=='detail'" :visible.sync="customizeVisible" :destroy-on-close="true" direction="ltr"
                 :withHeader="false" :title="$t('api_test.automation.customize_req')" style="overflow: auto"
                 :modal="false" size="90%">
        <ms-api-customize :request="customizeRequest" @addCustomizeApi="addCustomizeApi"/>
      </el-drawer>
      <!--场景导入 -->
      <scenario-relevance v-if="type!=='detail'" @save="addScenario" @close="setHideBtn" ref="scenarioRelevance"/>

      <!-- 环境 -->
      <api-environment-config v-if="type!=='detail'" ref="environmentConfig" @close="environmentConfigClose"/>

      <!--执行组件-->
      <ms-run :debug="true" v-if="type!=='detail'" :environment="projectEnvMap" :reportId="reportId" :saved="!debug"
              :run-data="debugData"
              @runRefresh="runRefresh" ref="runTest"/>
      <!-- 调试结果 -->
      <el-drawer v-if="type!=='detail'" :visible.sync="debugVisible" :destroy-on-close="true" direction="ltr"
                 :withHeader="true" :modal="false" size="90%">
        <ms-api-report-detail :scenario="currentScenario" :report-id="reportId" :debug="true" :currentProjectId="projectId" @refresh="detailRefresh"/>
      </el-drawer>

      <!--场景公共参数-->
      <ms-variable-list v-if="type!=='detail'" @setVariables="setVariables" ref="scenarioParameters"
                        class="ms-sc-variable-header"/>
      <!--外部导入-->
      <api-import v-if="type!=='detail'" ref="apiImport" :saved="false" @refresh="apiImport"/>

      <!--步骤最大化-->
      <ms-drawer :visible="drawer" :size="100" @close="close" direction="default" :show-full-screen="false" :is-show-close="false" style="overflow: hidden">
        <template v-slot:header>
          <scenario-header :currentScenario="currentScenario" :projectEnvMap="projectEnvMap"
                           :projectIds.sync="projectIds" :projectList="projectList"
                           :scenarioDefinition="scenarioDefinition" :enableCookieShare="enableCookieShare"
                           :onSampleError="onSampleError"
                           :execDebug="stopDebug"
                           :isFullUrl.sync="isFullUrl" @closePage="close" @unFullScreen="unFullScreen"
                           @showAllBtn="showAllBtn" @runDebug="runDebug" @handleCommand="handleCommand" @setProjectEnvMap="setProjectEnvMap"
                           @showScenarioParameters="showScenarioParameters"
                           @setCookieShare="setCookieShare" @setSampleError="setSampleError"
                           ref="maximizeHeader"/>
        </template>

        <maximize-scenario :scenario-definition="scenarioDefinition" :envMap="projectEnvMap" :moduleOptions="moduleOptions"
                           :req-error="reqError" :req-success="reqSuccess" :req-total="reqTotal" :req-total-time="reqTotalTime"
                           :currentScenario="currentScenario" :type="type" :debug="debug" :reloadDebug="reloadDebug" :stepReEnable="stepEnable" ref="maximizeScenario" @openScenario="openScenario"/>
      </ms-drawer>
      <ms-change-history ref="changeHistory"/>

    </div>
  </el-card>
</template>

<script>
import {API_STATUS, PRIORITY} from "../../definition/model/JsonData";
import {buttons, setComponent} from './menu/Menu';
import {parseEnvironment} from "../../definition/model/EnvironmentModel";
import {ELEMENT_TYPE, ELEMENTS} from "./Setting";
import MsApiCustomize from "./ApiCustomize";
import {
  getUUID,
  objToStrMap,
  strMapToObj,
  handleCtrlSEvent,
  getCurrentProjectID,
  handleCtrlREvent
} from "@/common/js/utils";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";
import MsInputTag from "./MsInputTag";
import MsRun from "./DebugRun";
import MsApiReportDetail from "../report/SysnApiReportDetail";
import MsVariableList from "./variable/VariableList";
import ApiImport from "../../definition/components/import/ApiImport";
import "@/common/css/material-icons.css"
import OutsideClick from "@/common/js/outside-click";
import ScenarioApiRelevance from "./api/ApiRelevance";
import ScenarioRelevance from "./api/ScenarioRelevance";
import MsComponentConfig from "./component/ComponentConfig";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";
import MaximizeScenario from "./maximize/MaximizeScenario";
import ScenarioHeader from "./maximize/ScenarioHeader";
import MsDrawer from "../../../common/components/MsDrawer";
import MsSelectTree from "../../../common/select-tree/SelectTree";
import {saveScenario} from "@/business/components/api/automation/api-automation";
import MsChangeHistory from "../../../history/ChangeHistory";

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
    }
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
    MaximizeScenario,
    ScenarioHeader,
    MsDrawer,
    MsSelectTree,
    MsChangeHistory
  },
  data() {
    return {
      onSampleError: true,
      showConfigButtonWithOutPermission: false,
      props: {
        label: "label",
        children: "hashTree"
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
      apiListVisible: false,
      customizeVisible: false,
      isBtnHide: false,
      debugVisible: false,
      customizeRequest: {protocol: "HTTP", type: "API", hashTree: [], referenced: 'Created', active: false},
      operatingElements: [],
      currentRow: {cases: [], apis: [], referenced: true},
      selectedTreeNode: undefined,
      selectedNode: undefined,
      expandedNode: [],
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
      debugResult: new Map,
      drawer: false,
      isFullUrl: true,
      expandedStatus: false,
      stepEnable: true,
      envResult: {
        loading: false
      },
      debug: false,
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
    }
  },
  created() {
    if (!this.currentScenario.apiScenarioModuleId) {
      this.currentScenario.apiScenarioModuleId = "";
    }
    this.debug = false;
    this.debugLoading = false;
    this.operatingElements = ELEMENTS.get("ALL");
    this.getWsProjects();
    this.getMaintainerOptions();
    this.getApiScenario();
  },
  mounted() {
    this.$nextTick(() => {
      this.addListener();
    });
  },
  directives: {OutsideClick},
  computed: {
    buttons,
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    clearDebug() {
      this.reqError = 0;
      this.reqTotalTime = 0;
      this.reqTotal = 0;
      this.reqSuccess = 0;
    },
    clearResult(arr) {
      if (arr) {
        arr.forEach(item => {
          item.requestResult = undefined;
          item.result = undefined;
          item.code = undefined;
          if (item.hashTree && item.hashTree.length > 0) {
            this.clearResult(item.hashTree);
          }
        })
      }
    },
    editParent(node, status) {
      if (!status) {
        node.data.code = "error";
      }
      node.data.debug = true;
      if (node.parent && node.parent.data && node.parent.data.id) {
        this.editParent(node.parent, status);
      }
    },
    findNodeChild(arr, resourceId, status) {
      arr.forEach(item => {
        if (item.data.resourceId === resourceId) {
          this.editParent(item.parent, status);
        }
        if (item.childNodes && item.childNodes.length > 0) {
          this.findNodeChild(item.childNodes, resourceId, status);
        }
      })
    },
    findNode(resourceId, status) {
      if (this.$refs.stepTree && this.$refs.stepTree.root) {
        this.$refs.stepTree.root.childNodes.forEach(item => {
          if (item.childNodes && item.childNodes.length > 0) {
            this.findNodeChild(item.childNodes, resourceId, status);
          }
        })
      }
    },
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/api/scenario/report/get/real/" + this.reportId;
      this.websocket = new WebSocket(uri);
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {
    },
    onError(e) {
      window.console.error(e)
    },
    onMessage(e) {
      if (e.data) {
        let data = JSON.parse(e.data);
        this.formatResult(data);
        this.message = getUUID();
        if (data.end) {
          this.removeReport();
          this.debugLoading = false;
          this.stopDebug = "stop";
        }
      }
    },
    onClose(e) {
      if (e.code === 1005) {
        // 强制删除之后关闭socket，不用刷新report
        return;
      }
    },
    formatResult(res) {
      let resMap = new Map;
      let startTime = 99991611737506593;
      let endTime = 0;
      this.clearDebug();
      if (res && res.scenarios) {
        res.scenarios.forEach(item => {
          if (item && item.requestResults) {
            item.requestResults.forEach(req => {
              req.responseResult.console = res.console;
              if (req.method === 'Request') {
                req.subRequestResults.forEach(subItem => {
                  this.reqTotal++;
                  let key = subItem.resourceId;
                  if (resMap.get(key)) {
                    if (resMap.get(key).indexOf(subItem) === -1) {
                      resMap.get(key).push(subItem);
                    }
                  } else {
                    resMap.set(key, [subItem]);
                  }
                  if (subItem.success) {
                    this.reqSuccess++;
                  } else {
                    this.reqError++;
                  }
                  if (subItem.startTime && Number(subItem.startTime) < startTime) {
                    startTime = subItem.startTime;
                  }
                  if (subItem.endTime && Number(subItem.endTime) > endTime) {
                    endTime = subItem.endTime;
                  }
                })
              } else {
                this.reqTotal++;
                let key = req.resourceId;
                if (resMap.get(key)) {
                  if (resMap.get(key).indexOf(req) === -1) {
                    resMap.get(key).push(req);
                  }
                } else {
                  resMap.set(key, [req]);
                }
                if (req.success) {
                  this.reqSuccess++;
                } else {
                  this.reqError++;
                }
                if (req.startTime && Number(req.startTime) < startTime) {
                  startTime = req.startTime;
                }
                if (req.endTime && Number(req.endTime) > endTime) {
                  endTime = req.endTime;
                }
              }
            })
          }
        })
      }
      if (startTime < endTime) {
        this.reqTotalTime = endTime - startTime + 100;
      }
      this.debugResult = resMap;
      this.sort();
      // this.reload();
      this.reloadDebug = getUUID();
    },
    removeReport() {
      let url = "/api/scenario/report/remove/real/" + this.reportId;
      this.$get(url, response => {
      });
    },
    handleCommand() {
      this.debug = false;
      /*触发执行操作*/
      this.$refs['currentScenario'].validate((valid) => {
        if (valid) {
          this.debugLoading = true;
          let definition = JSON.parse(JSON.stringify(this.currentScenario));
          definition.hashTree = this.scenarioDefinition;
          this.getEnv(JSON.stringify(definition)).then(() => {
            let promise = this.$refs.envPopover.initEnv();
            promise.then(() => {
              let sign = this.$refs.envPopover.checkEnv(this.isFullUrl);
              if (!sign) {
                this.debugLoading = false;
                return;
              }
              this.editScenario().then(() => {
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
                this.debugLoading = false;
              })
            })
          })
        }
      })
    },
    openHis() {
      this.$refs.changeHistory.open(this.currentScenario.id);
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
      document.addEventListener("scroll", this.handleScroll, true);
      window.addEventListener("resize", this.handleScroll);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
      document.removeEventListener("keydown", this.createCtrlRHandle);
      document.removeEventListener("scroll", this.handleScroll, true);
      window.removeEventListener("onresize", this.handleScroll);
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
    },
    fabClick() {
      if (this.operatingElements.length < 1) {
        this.$info("引用的场景或接口无法添加配置");
      }
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
      this.selectedTreeNode = data;
      this.selectedNode = node;
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
        this.operatingElements = ELEMENTS.get("ALL");
        this.selectedTreeNode = undefined;
      }
    },
    apiListImport() {
      this.isBtnHide = true;
      this.$refs.scenarioApiRelevance.open();
    },
    sort(stepArray, scenarioProjectId) {
      if (!stepArray) {
        stepArray = this.scenarioDefinition;
      }
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (!stepArray[i].resourceId) {
          stepArray[i].resourceId = getUUID();
        }
        if (stepArray[i].type === ELEMENT_TYPE.LoopController
          && stepArray[i].loopType === "LOOP_COUNT"
          && stepArray[i].hashTree
          && stepArray[i].hashTree.length > 1) {
          stepArray[i].countController.proceed = true;
        }
        if (!stepArray[i].projectId) {
          // 如果自身没有ID并且场景有ID则赋值场景ID，否则赋值当前项目ID
          stepArray[i].projectId = scenarioProjectId ? scenarioProjectId : this.projectId;
        } else {
          const project = this.projectList.find(p => p.id === stepArray[i].projectId);
          if (!project) {
            stepArray[i].projectId = scenarioProjectId ? scenarioProjectId : this.projectId;
          }
        }
        // 添加debug结果
        let key = stepArray[i].resourceId;
        if (this.debugResult && this.debugResult.get(key)) {
          stepArray[i].requestResult = this.debugResult.get(key);
          stepArray[i].result = null;
          stepArray[i].debug = this.debug;
          this.findNode(key, stepArray[i].requestResult[0].success);
        }
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.stepSize += stepArray[i].hashTree.length;
          this.sort(stepArray[i].hashTree, stepArray[i].projectId);
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
            this.$error("不能引用或复制自身！");
            this.$refs.scenarioRelevance.changeButtonLoadingType();
            return;
          }
          if (!item.hashTree) {
            item.hashTree = [];
          }
          item.enable === undefined ? item.enable = true : item.enable;
          if (this.selectedTreeNode !== undefined) {
            this.selectedTreeNode.hashTree.push(item);
          } else {
            this.scenarioDefinition.push(item);
          }
        })
      }
      this.isBtnHide = false;
      this.sort();
      this.reload();
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
      request.requestResult = undefined;
      if (!request.url) {
        request.url = "";
      }
      if (referenced === 'REF' || !request.hashTree) {
        request.hashTree = [];
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
      this.reload();
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
            this.reload();
          }
        }
      });
    },
    copyRow(row, node) {
      const parent = node.parent
      const hashTree = parent.data.hashTree || parent.data;
      // 深度复制
      let obj = JSON.parse(JSON.stringify(row));
      obj.resourceId = getUUID();
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
      this.reload();
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      });
    },
    runDebug() {
      if (this.scenarioDefinition.length < 1) {
        return;
      }
      this.stopDebug = "";
      this.clearDebug();
      this.clearResult(this.scenarioDefinition);
      /*触发执行操作*/
      this.$refs.currentScenario.validate((valid) => {
        if (valid) {
          let definition = JSON.parse(JSON.stringify(this.currentScenario));
          definition.hashTree = this.scenarioDefinition;
          this.getEnv(JSON.stringify(definition)).then(() => {
            let promise = this.$refs.envPopover.initEnv();
            promise.then(() => {
              let sign = this.$refs.envPopover.checkEnv(this.isFullUrl);
              if (!sign) {
                this.buttonIsLoading = false;
                return;
              }
              //调试时不再保存
              this.debugData = {
                id: this.currentScenario.id,
                name: this.currentScenario.name,
                type: "scenario",
                variables: this.currentScenario.variables,
                referenced: 'Created',
                enableCookieShare: this.enableCookieShare,
                headers: this.currentScenario.headers,
                environmentMap: this.projectEnvMap,
                hashTree: this.scenarioDefinition,
                onSampleError: this.onSampleError,
              };
              this.reportId = getUUID().substring(0, 8);
              this.debug = true;
              this.debugLoading = true;
            })
          })
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
      if (dropType != "inner" && (draggingNode.data && !draggingNode.data.disabled)) {
        return true;
      } else if (dropType === "inner" && dropNode.data.referenced !== 'REF' && dropNode.data.referenced !== 'Deleted'
        && ELEMENTS.get(dropNode.data.type).indexOf(draggingNode.data.type) != -1 && !draggingNode.data.disabled) {
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
    nodeExpand(data, node) {
      if (data && data.resourceId && this.expandedNode.indexOf(data.resourceId) === -1) {
        this.expandedNode.push(data.resourceId);
      }
    },
    nodeCollapse(data, node) {
      if (data && data.resourceId) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.resourceId), 1);
      }
    },
    editScenario() {
      if (!document.getElementById("inputDelay")) {
        return;
      }
      return new Promise((resolve) => {
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
      this.stepEnable = true;
      if (this.currentScenario.tags != undefined && this.currentScenario.tags && !(this.currentScenario.tags instanceof Array)) {
        this.currentScenario.tags = JSON.parse(this.currentScenario.tags);
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
              // this.getEnv(response.data.scenarioDefinition);
              let obj = JSON.parse(response.data.scenarioDefinition);
              if (obj) {
                this.currentEnvironmentId = obj.environmentId;
                if (obj.environmentMap) {
                  this.projectEnvMap = objToStrMap(obj.environmentMap);
                } else {
                  // 兼容历史数据
                  this.projectEnvMap.set(this.projectId, obj.environmentId);
                }
                this.currentScenario.variables = [];
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
                if (obj.headers) {
                  this.currentScenario.headers = obj.headers;
                }
                this.enableCookieShare = obj.enableCookieShare;
                if (obj.onSampleError === undefined) {
                  this.onSampleError = true;
                } else {
                  this.onSampleError = obj.onSampleError;
                }
                if (obj.hashTree) {
                  obj.hashTree.forEach(item => {
                    if (!item.hashTree) {
                      item.hashTree = [];
                    }
                  });
                }
                this.scenarioDefinition = obj.hashTree;
              }
            }
            if (this.currentScenario.copy) {
              this.path = "/api/automation/create";
            }
          }
          this.loading = false;
          this.sort();
        })
      }
    },
    setParameter() {
      this.currentScenario.stepTotal = this.scenarioDefinition.length;
      this.currentScenario.projectId = this.projectId;
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
        onSampleError: this.onSampleError,
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
      if (!this.debug) {
        this.debugVisible = true;
        this.loading = false;
      } else {
        this.initWebSocket();
      }
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
    getWsProjects() {
      this.$get("/project/listAll", res => {
        this.projectList = res.data;
      })
    },
    refReload() {
      this.reload();
    },
    detailRefresh(result) {
      // 把执行结果分发给各个请求
      this.debugResult = result;
      this.sort()
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
    shrinkTreeNode() {
      //改变每个节点的状态
      for (let i in this.scenarioDefinition) {
        if (this.scenarioDefinition[i]) {
          if (this.expandedStatus && this.expandedNode.indexOf(this.scenarioDefinition[i].resourceId) === -1) {
            this.expandedNode.push(this.scenarioDefinition[i].resourceId);
          }
          if (this.stepSize < 35) {
            this.scenarioDefinition[i].active = this.expandedStatus;
          }
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
          if (this.stepSize < 35) {
            nodes[i].active = this.expandedStatus;
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
      this.shrinkTreeNode();
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
    },
    handleScroll() {
      let stepInfo = this.$refs.stepInfo;
      let debugHeader = this.$refs.debugHeader;
      if (debugHeader) {
        let originWidth = debugHeader.parentElement.clientWidth;
        if (stepInfo.getBoundingClientRect().top <= 178) {
          this.isTop = true;
          if (originWidth > 0) {
            debugHeader.style.width = originWidth + 'px';
          }
        } else {
          this.isTop = false;
        }
      }
    }
  }
}
</script>

<style scoped>
.card-content {
  height: calc(100vh - 156px);
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
  margin-top: 5px;
}

#fab {
  right: 90px;
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

.scenario-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 150px;
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

.ms-debug-result {
  float: right;
  margin-right: 30px;
  margin-top: 3px;
}

.ms-open-btn-left {
  margin-left: 35px;
}


.ms-message-right {
  margin-right: 10px;
}

.is-top {
  position: fixed;
  top: 125px;
  background: white;
  z-index: 999;
}
</style>
