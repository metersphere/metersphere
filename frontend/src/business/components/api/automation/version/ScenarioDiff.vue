<template>
  <div>
    <el-row>
      <el-col :span="12">
        <el-tag>当前{{ oldData.versionName }}</el-tag>
        <span style="margin-left: 10px">{{ oldData.userName }}</span><span style="margin-left: 10px">{{ oldData.createTime | timestampFormatDate }}</span>
      </el-col>
      <el-col :span="12">
        <el-tag>{{ newData.versionName }}</el-tag>
        <span style="margin-left: 10px">{{ newData.userName }}</span><span style="margin-left: 10px">{{ newData.createTime | timestampFormatDate }}</span>
      </el-col>
    </el-row>
    <div class="compare-class" v-loading="isReloadData">
      <el-card ref="old" style="width: 50%">
        <el-card>
          <div class="card-content">
            <div class="ms-main-div" @click="showAll">

              <div class="tip">{{ $t('test_track.plan_view.base_info') }}</div>
              <el-form :model="oldData" label-position="right" label-width="80px" size="small" :rules="rules" :disabled="true"
                       ref="currentScenario" style="margin-right: 20px">
                <!-- 基础信息 -->
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.name')" prop="name">
                      <el-input class="ms-scenario-input" size="small" v-model="oldData.name"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
                      <ms-select-tree size="small" :data="moduleOptions" :defaultKey="oldData.apiScenarioModuleId" :obj="moduleObj" clearable checkStrictly/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.status')" prop="status">
                      <el-select class="ms-scenario-input" size="small" v-model="oldData.status">
                        <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('api_test.definition.request.responsible')" prop="principal">
                      <el-select v-model="oldData.principal"
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
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('test_track.case.priority')" prop="level">
                      <el-select class="ms-scenario-input" size="small" v-model="oldData.level">
                        <el-option v-for="item in levels" :key="item.id" :label="item.label" :value="item.id"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('api_test.automation.tag')" prop="tags">
                      <ms-input-tag :currentScenario="oldData" ref="tag"/>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.description')" prop="description">
                      <el-input class="ms-http-textarea"
                                v-model="oldData.description"
                                type="textarea"
                                :autosize="{ minRows: 1, maxRows: 10}"
                                :rows="1" size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7" v-if="customNum">
                    <el-form-item label="ID" prop="customNum">
                      <el-input v-model.trim="oldData.customNum" size="small"></el-input>
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
                                oldData.name === undefined || '' ? $t('api_test.scenario.name') : oldData.name
                              }}
                            </div>
                          </template>
                          <span class="scenario-name">
                        {{
                              oldData.name === undefined || '' ? $t('api_test.scenario.name') : oldData.name
                            }}
                    </span>
                        </el-tooltip>
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        {{ $t('api_test.automation.step_total') }}：{{ oldScenarioDefinition.length }}
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-link class="head">{{ $t('api_test.automation.scenario_total') }}
                        </el-link>
                        ：{{ getOldVariableSize() }}
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-checkbox v-model="oldEnableCookieShare"><span style="font-size: 13px;">{{ $t('api_test.scenario.share_cookie') }}</span></el-checkbox>
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-checkbox v-model="oldOnSampleError"><span style="font-size: 13px;">{{ $t('commons.failure_continues') }}</span></el-checkbox>
                      </el-col>

                    </el-row>
                  </div>

                  <!-- 场景步骤内容 -->
                  <div ref="stepInfo" id="stepInfo">
                    <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
                      <i class="el-icon-circle-plus-outline ms-open-btn ms-open-btn-left" @click="openExpansion('old')"/>
                    </el-tooltip>
                    <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
                      <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion('old')"/>
                    </el-tooltip>
                    <el-tree node-key="resourceId" :props="props" :data="oldScenarioDefinition" class="ms-tree"
                             :default-expanded-keys="oldExpandedNode"
                             :expand-on-click-node="false"
                             highlight-current
                             @node-expand="nodeExpand(oldScenarioDefinition,null,'old')"
                             @node-collapse="nodeCollapse(oldScenarioDefinition,null,'old')"
                             @node-click="oldNodeClick"
                             draggable ref="stepTree">
                    <span class="custom-tree-node father" slot-scope="{ node, data}" style="width: 96%">
                      <!-- 步骤组件-->
                       <ms-component-config
                         :type="data.type"
                         :scenario="data"
                         :node="node"
                         :env-map="projectEnvMap"
                         :project-list="projectList"
                         :show-version="false"
                       />
                    </span>
                    </el-tree>
                  </div>
                </el-col>
              </el-row>
            </div>
            <el-backtop target=".card-content" :visibility-height="100" :right="50"></el-backtop>
          </div>
        </el-card>
      </el-card>
      <el-card ref="new" style="width: 50%">
        <el-card>
          <div class="card-content">
            <div class="ms-main-div" @click="showAll" v-if="type!=='detail'">

              <div class="tip">{{ $t('test_track.plan_view.base_info') }}</div>
              <el-form :model="newData" label-position="right" label-width="80px" size="small" :rules="rules" :disabled="true"
                       ref="currentScenario" style="margin-right: 20px">
                <!-- 基础信息 -->
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.name')" prop="name">
                      <el-input class="ms-scenario-input" size="small" v-model="newData.name"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
                      <ms-select-tree size="small" :data="moduleOptions" :defaultKey="newData.apiScenarioModuleId" :obj="moduleObj" clearable checkStrictly/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.status')" prop="status">
                      <el-select class="ms-scenario-input" size="small" v-model="newData.status">
                        <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('api_test.definition.request.responsible')" prop="principal">
                      <el-select v-model="newData.principal"
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
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('test_track.case.priority')" prop="level">
                      <el-select class="ms-scenario-input" size="small" v-model="newData.level">
                        <el-option v-for="item in levels" :key="item.id" :label="item.label" :value="item.id"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item :label="$t('api_test.automation.tag')" prop="tags">
                      <ms-input-tag :currentScenario="newData" ref="tag"/>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="7">
                    <el-form-item :label="$t('commons.description')" prop="description">
                      <el-input class="ms-http-textarea"
                                v-model="newData.description"
                                type="textarea"
                                :autosize="{ minRows: 1, maxRows: 10}"
                                :rows="1" size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="7" v-if="customNum">
                    <el-form-item label="ID" prop="customNum">
                      <el-input v-model.trim="newData.customNum" size="small"></el-input>
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
                                newData.name === undefined || '' ? $t('api_test.scenario.name') : newData.name
                              }}
                            </div>
                          </template>
                          <span class="scenario-name">
                        {{
                              newData.name === undefined || '' ? $t('api_test.scenario.name') : newData.name
                            }}
                    </span>
                        </el-tooltip>
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        {{ $t('api_test.automation.step_total') }}：{{ newScenarioDefinition.length }}
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-link class="head">{{ $t('api_test.automation.scenario_total') }}
                        </el-link>
                        ：{{ getNewVariableSize() }}
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-checkbox v-model="newEnableCookieShare"><span style="font-size: 13px;">{{ $t('api_test.scenario.share_cookie') }}</span></el-checkbox>
                      </el-col>
                      <el-col :span="3" class="ms-col-one ms-font">
                        <el-checkbox v-model="newOnSampleError"><span style="font-size: 13px;">{{ $t('commons.failure_continues') }}</span></el-checkbox>
                      </el-col>

                    </el-row>
                  </div>

                  <!-- 场景步骤内容 -->
                  <div ref="newStepInfo">
                    <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
                      <i class="el-icon-circle-plus-outline ms-open-btn ms-open-btn-left" @click="openExpansion('new')"/>
                    </el-tooltip>
                    <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
                      <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion('new')"/>
                    </el-tooltip>
                    <el-tree node-key="resourceId" :props="props" :data="newScenarioDefinition" class="ms-tree"
                             :expand-on-click-node="false"
                             :default-expanded-keys="newExpandedNode"
                             highlight-current
                             @node-expand="nodeExpand(newScenarioDefinition,null,'new')"
                             @node-collapse="nodeCollapse(newScenarioDefinition,null,'new')"
                             @node-click="nodeClick"
                             draggable ref="newStepTree">
                    <span class="custom-tree-node father" slot-scope="{ node, data}" style="width: 96%">
                      <!-- 步骤组件-->
                       <ms-component-config
                         :type="data.type"
                         :scenario="data"
                         :node="node"
                         :env-map="newProjectEnvMap"
                         :project-list="projectList"
                         :show-version="false"
                       />
                    </span>
                    </el-tree>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-backtop target=".card-content" :visibility-height="100" :right="50"></el-backtop>
          </div>
        </el-card>
      </el-card>
      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        append-to-body
        width="100%"
      >
        <scenario-child-diff
          v-if="dialogVisible"
          :old-data="leftChildData"
          :new-data="rightChildData"
          :old-node="leftChildNode"
          :new-node="rightChildNode"
          :old-project-env-map="projectEnvMap"
          :new-project-env-map="newProjectEnvMap"
          :old-v-node="leftChildVnode"
          :new-v-node="rightChildVnode"
          :old-color="oldColor"
          :new-color="newColor"
        ></scenario-child-diff>
      </el-dialog>
    </div>
  </div>
</template>
<script>

import {API_STATUS, PRIORITY} from "@/business/components/api/definition/model/JsonData";
import {ENV_TYPE} from "@/common/js/constants";
import {ELEMENT_TYPE, STEP, TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import MsComponentConfig from "@/business/components/api/automation/scenario/component/ComponentConfig";
import ScenarioChildDiff from "@/business/components/api/automation/version/ScenarioChildDiff";
import {objToStrMap} from "@/common/js/utils";

const {diff} = require("@/business/components/performance/v_node_diff");
const {KeyValue} = require("@/business/components/api/definition/model/ApiTestModel");
const {getUUID} = require("@/common/js/utils");
export default {
  name: "ScenarioDiff",
  props: {
    showFollow: {
      type: Boolean
    },
    newShowFollow: {
      type: Boolean
    },
    customNum: {
      type: Boolean,
      default: false
    },
    isTop: {
      type: Boolean,
      default: false
    },
    rules: {
      type: Object
    },
    projectIds: {},
    oldVariableSize: {},
    newVariableSize: {},
    moduleOptions: {},
    maintainerOptions: {},
    oldEnableCookieShare: {},
    oldOnSampleError: {},
    projectEnvMap: {},
    type: {},
    projectList: {
      type: Array,
    },
    currentScenarioId: String,
    dffScenarioId: String,
    scenarioRefId: String,
  },
  components: {
    ScenarioChildDiff,
    MsComponentConfig,
    MsSelectTree: () => import("@/business/components/common/select-tree/SelectTree"),
    MsInputTag: () => import("@/business/components/api/automation/scenario/MsInputTag"),
    EnvPopover: () => import("@/business/components/api/automation/scenario/EnvPopover"),
  },
  watch: {
    'dialogVisible'() {
      if (this.dialogVisible === false) {
        this.leftChildData = {};
        this.leftChildNode = {};
        this.leftChildVnode = {};
        this.rightChildData = {};
        this.rightChildNode = {};
        this.rightChildVnode = {};
        this.currentRightChild = undefined;
        this.currentLeftChild = undefined;
      }
    }
  },
  data() {
    return {
      options: API_STATUS,
      levels: PRIORITY,
      loading: false,
      isReloadData: true,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      oldEnvResult: {
        loading: false
      },
      newEnvResult: {
        loading: false
      },
      showHideTree: true,
      environmentType: ENV_TYPE.JSON,
      props: {
        label: "label",
        children: "hashTree"
      },
      oldExpandedNode: [],
      newExpandedNode: [],
      stepEnable: true,
      currentLeftChild: undefined,
      currentRightChild: undefined,
      leftChildData: {},
      rightChildData: {},
      leftChildNode: {},
      rightChildNode: {},
      leftChildVnode: {},
      rightChildVnode: {},
      dialogVisible: false,
      oldColor: "",
      newColor: "",
      newScenarioDefinition: [],
      oldScenarioDefinition: [],
      oldData: {},
      newData: {},
      newEnableCookieShare: {},
      newOnSampleError: {},
      newProjectEnvMap: new Map,
    }
  },
  methods: {
    getCurrentScenario() {
      if (this.currentScenarioId) {
        this.result = this.$get("/api/automation/getApiScenario/" + this.currentScenarioId, response => {
          if (response.data) {
            if (response.data.scenarioDefinition != null) {
              let obj = JSON.parse(response.data.scenarioDefinition);
              if (obj) {
                this.oldScenarioDefinition = obj.hashTree;
              }
            }
            this.oldData = response.data;
            this.$get('/api/automation/follow/' + this.currentScenarioId, response => {
              this.oldData.follows = response.data;
              for (let i = 0; i < response.data.length; i++) {
                if (response.data[i] === this.currentUser().id) {
                  this.showFollow = true;
                  break;
                }
              }
            });
          }
        })
      }
    },
    getDffScenario() {
      this.$get('/api/automation/get/' + this.dffScenarioId + "/" + this.scenarioRefId, response => {
        this.$get("/api/automation/getApiScenario/" + response.data.id, res => {
          if (res.data) {
            if (res.data.scenarioDefinition != null) {
              let obj = JSON.parse(res.data.scenarioDefinition);
              if (obj) {
                if (obj.hashTree) {
                  for (let i = 0; i < obj.hashTree.length; i++) {
                    if (!obj.hashTree[i].index) {
                      obj.hashTree[i].index = i + 1;
                    }
                    obj.hashTree[i].disabled = true;
                    if (!obj.hashTree[i].requestResult) {
                      obj.hashTree[i].requestResult = [{responseResult: {}}];
                    }
                  }
                  this.newEnableCookieShare = obj.enableCookieShare;
                  if (obj.onSampleError === undefined) {
                    this.newOnSampleError = true;
                  } else {
                    this.newOnSampleError = obj.onSampleError;
                  }
                }
                this.newScenarioDefinition = obj.hashTree;
                for (let i = 0; i < this.oldScenarioDefinition.length; i++) {
                  this.oldScenarioDefinition[i].disabled = true;
                }
                if (response.data.environmentJson) {
                  this.newProjectEnvMap = objToStrMap(JSON.parse(response.data.environmentJson));
                } else {
                  // 兼容历史数据
                  this.newProjectEnvMap.set(this.projectId, obj.environmentId);
                }
              }
            }
            res.data.userName = response.data.userName
            this.dealWithTag(res.data);
            this.newData = res.data;
            this.closeExpansion()
          }
        });
      })
    },
    dealWithTag(newScenario) {
      if (newScenario.tags) {
        if (Object.prototype.toString.call(newScenario.tags) === "[object String]") {
          newScenario.tags = JSON.parse(newScenario.tags);
        }
      }
    },
    getDiff() {
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      //oldVnode.style.backgroundColor = "rgb(241,200,196)";
      if (this.oldData.createTime > this.newData.createTime) {
        this.oldColor = "rgb(121, 225, 153,0.3)";
        this.newColor = "rgb(241,200,196,0.45)"
      } else {
        this.oldColor = "rgb(241,200,196,0.45)"
        this.newColor = "rgb(121, 225, 153,0.3)";
      }
      diff(oldVnode, vnode, this.oldColor, this.newColor);
      this.isReloadData = false
    },
    showAll() {
      // 控制当有弹出页面操作时禁止刷新按钮列表
      if (!this.customizeVisible && !this.isBtnHide) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.selectedTreeNode = undefined;
        this.$store.state.selectStep = undefined;
      }
    },
    oldShowPopover() {
      let definition = JSON.parse(JSON.stringify(this.oldData));
      definition.hashTree = this.oldScenarioDefinition;
      this.oldEnvResult.loading = true;
      this.getEnv(JSON.stringify(definition)).then(() => {
        this.$refs.envPopover.openEnvSelect();
        this.oldEnvResult.loading = false;
      })
    },
    newShowPopover() {
      let definition = JSON.parse(JSON.stringify(this.newData));
      definition.hashTree = this.newScenarioDefinition;
      this.newEnvResult.loading = true;
      this.getEnv(JSON.stringify(definition)).then(() => {
        this.$refs.envPopover.openEnvSelect();
        this.newEnvResult.loading = false;
      })
    },
    changeNodeStatus(nodes, source) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (this.expandedStatus) {
            if (source === "new") {
              this.newExpandedNode.push(nodes[i].resourceId);
            } else {
              this.oldExpandedNode.push(nodes[i].resourceId);
            }
          }
          nodes[i].active = this.expandedStatus;
          if (this.stepSize > 35 && this.expandedStatus) {
            nodes[i].active = false;
          }
          if (nodes[i].hashTree !== undefined && nodes[i].hashTree.length > 0) {
            this.changeNodeStatus(nodes[i].hashTree, source);
          }
        }
      }
    },
    openExpansion(source) {
      this.newExpandedNode = [];
      this.oldExpandedNode = [];
      this.expandedStatus = true;
      if (source === "new") {
        this.changeNodeStatus(this.newScenarioDefinition, source);
      } else {
        this.changeNodeStatus(this.oldScenarioDefinition, source);
      }

    },
    closeExpansion(source) {
      this.expandedStatus = false;
      this.newExpandedNode = [];
      this.oldExpandedNode = [];
      if (source === "new") {
        this.changeNodeStatus(this.newScenarioDefinition, source);
      } else {
        this.changeNodeStatus(this.oldScenarioDefinition, source);
      }
      this.showHide();
    },
    showHide() {
      this.showHideTree = false
      this.$nextTick(() => {
        this.showHideTree = true
      });
    },
    stepStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          nodes[i].enable = this.stepEnable;
          if (nodes[i].hashTree !== undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(nodes[i].hashTree);
          }
        }
      }
    },
    enableAll(source) {
      this.stepEnable = true;
      let scenarioDefinition;
      if (source === "new") {
        scenarioDefinition = this.newScenarioDefinition;
      } else {
        scenarioDefinition = this.oldScenarioDefinition;
      }
      this.stepStatus(scenarioDefinition);
    },
    disableAll(source) {
      this.stepEnable = false;
      let scenarioDefinition;
      if (source === "new") {
        scenarioDefinition = this.newScenarioDefinition;
      } else {
        scenarioDefinition = this.oldScenarioDefinition;
      }
      this.stepStatus(scenarioDefinition);
    },
    nodeExpand(data, node, source) {
      if (source === "new") {
        if (data && data.resourceId && this.newExpandedNode.indexOf(data.resourceId) === -1) {
          this.newExpandedNode.push(data.resourceId);
        }
      } else {
        if (data && data.resourceId && this.oldExpandedNode.indexOf(data.resourceId) === -1) {
          this.oldExpandedNode.push(data.resourceId);
        }
      }

    },
    nodeCollapse(data, node, source) {
      if (data && data.resourceId) {
        if (source === "new") {
          this.newExpandedNode.splice(this.newExpandedNode.indexOf(data.resourceId), 1);
        } else {
          this.oldExpandedNode.splice(this.oldExpandedNode.indexOf(data.resourceId), 1);
        }
      }
    },
    nodeClick(data, node, source) {
      this.$store.state.selectStep = data;
      this.rightChildData = data;
      this.rightChildNode = node
      this.rightChildVnode = source
      this.currentRightChild = source;
      if (this.currentLeftChild) {
        this.dialogVisible = true;
      }
    },
    oldNodeClick(data, node, source) {
      this.$store.state.selectStep = data;
      this.leftChildData = data;
      this.leftChildNode = node
      this.leftChildVnode = source
      this.currentLeftChild = source;
      if (this.currentRightChild) {
        this.dialogVisible = true;
      }
    },
    getOldVariableSize() {
      let size = 0;
      if (this.oldData.variables) {
        size += this.oldData.variables.length;
      }
      if (this.oldData.headers && this.oldData.headers.length > 1) {
        size += this.oldData.headers.length - 1;
      }
      return size;
    },
    getNewVariableSize() {
      let size = 0;
      if (this.newData.variables) {
        size += this.newData.variables.length;
      }
      if (this.newData.headers && this.newData.headers.length > 1) {
        size += this.newData.headers.length - 1;
      }
      return size;
    }
  },
  created() {
    this.getCurrentScenario();
    this.getDffScenario();
  },
  mounted() {
    this.$nextTick(function () {
      setTimeout(this.getDiff, (this.$refs.old.$children.length + 1) * 5000);
    })
  }
}

</script>
<style scoped>
.compare-class {
  display: flex;
  justify-content: space-between;
}
</style>
