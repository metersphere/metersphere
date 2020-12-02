<template>
  <el-card class="card-content">
    <div style="background-color: white;">
      <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
      <el-form :model="basicForm" label-position="right" label-width="80px" size="small" :rules="rules" ref="basicForm" style="margin-right: 20px">
        <!-- 基础信息 -->
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('commons.name')" prop="name">
              <el-input class="ms-scenario-input" size="small" v-model="basicForm.name"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
              <el-select class="ms-scenario-input" size="small" v-model="basicForm.moduleId">
                <el-option v-for="item in moduleOptions" :key="item.id" :label="item.path" :value="item.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('commons.status')" prop="status">
              <el-select class="ms-scenario-input" size="small" v-model="basicForm.status">
                <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('api_test.definition.request.responsible')" prop="principal">
              <el-select v-model="basicForm.principal"
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
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('test_track.case.priority')" prop="level">
              <el-select class="ms-scenario-input" size="small" v-model="basicForm.level">
                <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('api_test.automation.follow_people')" prop="followPeople">
              <el-select v-model="basicForm.followPeople"
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
          <el-col :span="12">
            <el-form-item label="Tag" prop="tagId">
              <el-select v-model="basicForm.tagId" size="small" class="ms-scenario-input" placeholder="Tag"
                         @change="tagChange" :multiple="true">
                <el-option
                  v-for="item in maintainerOptions"
                  :key="item.id"
                  :label="item.id + ' (' + item.name + ')'"
                  :value="item.id"/>
                <el-button size="mini" type="primary" @click="openTagConfig" class="ms-scenario-button">
                  {{ $t('api_test.automation.create_tag') }}
                </el-button>
                <template v-slot:empty>
                  <div>
                    <el-button size="mini" type="primary" @click="closeTagConfig" class="ms-scenario-button">
                      {{ $t('api_test.automation.create_tag') }}
                    </el-button>
                  </div>
                </template>
              </el-select>

            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('commons.description')" prop="description">
              <el-input class="ms-http-textarea"
                        v-model="basicForm.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 10}"
                        :rows="2" size="small"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 场景步骤-->
      <div v-loading="isReloadData">
        <p class="tip">{{$t('api_test.automation.scenario_step')}} </p>
        <el-row>
          <el-col :span="21">
            <div style="margin-left: 20px;border:1px #DCDFE6 solid;border-radius: 4px;margin-right: 10px">
              <el-row style="margin: 5px">
                <el-col :span="6" class="ms-col-one">
                  {{basicForm.name ===undefined || ''? $t('api_test.scenario.name') : basicForm.name}}
                </el-col>
                <el-col :span="5" class="ms-col-one">
                  {{$t('api_test.automation.step_total')}}:
                </el-col>
                <el-col :span="5" class="ms-col-one">
                  {{$t('api_test.automation.scenario_total')}}:
                </el-col>
                <el-col :span="6">
                  {{$t('api_test.definition.request.run_env')}}:
                  <el-select v-model="basicForm.environmentId" size="small" class="ms-htt-width"
                             :placeholder="$t('api_test.definition.request.run_env')"
                             @change="environmentChange" clearable>
                    <el-option v-for="(environment, index) in environments" :key="index"
                               :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                               :value="environment.id"/>
                    <el-button class="ms-scenario-button" size="mini" type="primary" @click="openEnvironmentConfig">
                      {{ $t('api_test.environment.environment_config') }}
                    </el-button>
                    <template v-slot:empty>
                      <div class="empty-environment">
                        <el-button class="ms-scenario-button" size="mini" type="primary" @click="openEnvironmentConfig">
                          {{ $t('api_test.environment.environment_config') }}
                        </el-button>
                      </div>
                    </template>
                  </el-select>
                </el-col>
                <el-col :span="2">
                  <el-button size="small" type="primary" @click="runDebug">{{$t('api_test.request.debug')}}</el-button>
                </el-col>
              </el-row>
            </div>
            <!-- 场景步骤内容 -->
            <div style="margin-top: 10px" v-loading="isReloadData">
              <el-tree node-key="resourceId" :props="props" :data="scenarioDefinition"
                       :default-expanded-keys="expandedNode"
                       :expand-on-click-node="false"
                       @node-expand="nodeExpand"
                       @node-collapse="nodeCollapse"
                       :allow-drop="allowDrop" @node-drag-end="allowDrag" @node-click="nodeClick" v-if="!isReloadData" draggable>
                 <span class="custom-tree-node father" slot-scope="{ node, data}" style="width: 96%">
                    <template>
                      <!-- 场景 -->
                      <el-card v-if="data.type==='scenario'">
                        <el-row>
                            <div class="el-step__icon is-text ms-api-col" style="float: left">
                              <div class="el-step__icon-inner">{{data.$treeNodeId}}</div>
                            </div>
                            <div style="margin-left: 20px;float: left"> {{data.name}}</div>
                        </el-row>
                      </el-card>
                      <!--条件控制器-->
                      <ms-if-controller :controller="data" :node="node" v-if="data.type==='IfController'" @remove="remove"/>
                      <!--等待控制器-->
                      <ms-constant-timer :timer="data" :node="node" v-if="data.type==='ConstantTimer'" @remove="remove"/>
                      <!--自定义脚本-->
                      <ms-jsr233-processor v-if="data.type==='JSR223Processor'" @remove="remove" :title="$t('api_test.automation.customize_script')"
                                           style-type="color: #7B4D12;background-color: #F1EEE9" :jsr223-processor="data" :node="node"/>
                      <!--前置脚本-->
                      <ms-jsr233-processor v-if="data.type==='JSR223PreProcessor'" @remove="remove" :title="$t('api_test.definition.request.pre_script')"
                                           style-type="color: #B8741A;background-color: #F9F1EA" :jsr223-processor="data" :node="node"/>
                      <!--后置脚本-->
                      <ms-jsr233-processor v-if="data.type==='JSR223PostProcessor'" @remove="remove" :title="$t('api_test.definition.request.post_script')"
                                           style-type="color: #783887;background-color: #F2ECF3" :jsr223-processor="data" :node="node"/>
                      <!--断言规则-->
                      <ms-api-assertions v-if="data.type==='Assertions'" @remove="remove" customizeStyle="margin-top: 0px" :assertions="data" :node="node"/>
                      <!--提取规则-->
                      <ms-api-extract @remove="remove" v-if="data.type==='Extract'" customizeStyle="margin-top: 0px" :extract="data" :node="node"/>
                      <!--API 导入 -->
                      <ms-api-component :request="data" @remove="remove" current-project="currentProject" v-if="data.type==='HTTPSamplerProxy'||'DubboSampler'||'JDBCSampler'||'TCPSampler'" :node="node"/>
                    </template>
                   </span>
              </el-tree>
            </div>
          </el-col>
          <!-- 按钮列表 -->
          <div>
            <el-col :span="3" class="ms-left-cell">
              <el-button type="primary" icon="el-icon-refresh" size="small" @click="showAll">{{$t('commons.show_all')}}</el-button>
              <br/>
              <div v-if="operatingElements.indexOf('HTTPSamplerProxy')>0 || operatingElements.indexOf('DubboSampler')>0 || operatingElements.indexOf('JDBCSampler')>0 || operatingElements.indexOf('TCPSampler')>0 ">
                <el-button class="ms-right-buttion" size="small" style="color: #F56C6C;background-color: #FCF1F1" @click="apiListImport">+{{$t('api_test.automation.api_list_import')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('OT_IMPORT')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #409EFF;background-color: #EEF5FE" @click="addComponent('OT_IMPORT')">+{{$t('api_test.automation.external_import')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('ConstantTimer')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #67C23A;background-color: #F2F9EE" @click="addComponent('ConstantTimer')">+{{$t('api_test.automation.wait_controller')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('IfController')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #E6A23C;background-color: #FCF6EE" @click="addComponent('IfController')">+{{$t('api_test.automation.if_controller')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('scenario')===0">
                <el-button class="ms-right-buttion" size="small" style="color: #606266;background-color: #F4F4F5" @click="addComponent('scenario')">+{{$t('api_test.automation.scenario_import')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('JSR223Processor')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #7B4D12;background-color: #F1EEE9" @click="addComponent('JSR223Processor')">+{{$t('api_test.automation.customize_script')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('CustomizeReq')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #008080;background-color: #EBF2F2" @click="addComponent('CustomizeReq')">+{{$t('api_test.automation.customize_req')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('JSR223PreProcessor')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #B8741A;background-color: #F9F1EA" @click="addComponent('JSR223PreProcessor')">+{{$t('api_test.definition.request.pre_script')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('JSR223PostProcessor')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #783887;background-color: #F2ECF3" @click="addComponent('JSR223PostProcessor')">+{{$t('api_test.definition.request.post_script')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('Assertions')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #A30014;background-color: #F7E6E9" @click="addComponent('Assertions')">+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
              </div>
              <div v-if="operatingElements.indexOf('Extract')>0">
                <el-button class="ms-right-buttion" size="small" style="color: #015478;background-color: #E6EEF2" @click="addComponent('Extract')">+{{$t('api_test.definition.request.extract_param')}}</el-button>
              </div>
            </el-col>
          </div>
        </el-row>
      </div>

      <!--接口列表-->
      <el-drawer :visible.sync="apiListVisible" :destroy-on-close="true" direction="ltr" :title="$t('api_test.automation.api_list_import')" :modal="false" size="90%">
        <ms-api-definition :visible="true" :currentRow="currentRow"/>
        <el-button style="float: right;margin: 20px" @click="addReferenceApi">{{$t('api_test.scenario.reference')}}</el-button>
      </el-drawer>

      <!--自定义接口-->
      <el-drawer :visible.sync="customizeVisible" :destroy-on-close="true" direction="ltr" :title="$t('api_test.automation.customize_req')" style="overflow: auto" :modal="false" size="90%">
        <ms-api-customize :request="customizeRequest" @addCustomizeApi="addCustomizeApi" :current-project="currentProject"/>
        <!--<el-button style="float: right;margin: 20px" @click="addCustomizeApi">{{$t('commons.save')}}</el-button>-->
      </el-drawer>
    </div>
  </el-card>
</template>

<script>
  import {API_STATUS} from "../../definition/model/JsonData";
  import {WORKSPACE_ID} from '@/common/js/constants';
  import {createComponent} from "../../definition/components/jmeter/components";
  import {Assertions, Extract, IfController, JSR223Processor, ConstantTimer} from "../../definition/model/ApiTestModel";
  import MsJsr233Processor from "./Jsr233Processor";
  import {parseEnvironment} from "../../definition/model/EnvironmentModel";
  import MsConstantTimer from "./ConstantTimer";
  import MsIfController from "./IfController";
  import MsApiAssertions from "../../definition/components/assertion/ApiAssertions";
  import MsApiExtract from "../../definition/components/extract/ApiExtract";
  import MsApiDefinition from "../../definition/ApiDefinition";
  import MsApiComponent from "./ApiComponent";
  import {ELEMENTS, ELEMENT_TYPE} from "./Setting";
  import MsApiCustomize from "./ApiCustomize";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "EditApiScenario",
    props: {
      moduleOptions: Array,
      currentProject: {}
    },
    components: {MsJsr233Processor, MsConstantTimer, MsIfController, MsApiAssertions, MsApiExtract, MsApiDefinition, MsApiComponent, MsApiCustomize},
    data() {
      return {
        props: {
          label: "label",
          children: "hashTree"
        },
        rules: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        },
        basicForm: {},
        environments: [],
        maintainerOptions: [],
        value: API_STATUS[0].id,
        options: API_STATUS,
        scenario: {},
        isReloadData: false,
        apiListVisible: false,
        customizeVisible: false,
        customizeRequest: {protocol: "HTTP", type: "API", hashTree: [], referenced: false, active: false},
        operatingElements: [],
        currentRow: {cases: [], apis: []},
        selectedTreeNode: undefined,
        expandedNode: [],
        scenarioDefinition: []
      }
    },
    created() {
      this.operatingElements = ELEMENTS.get("ALL");
      this.getMaintainerOptions();
    },
    watch: {},
    methods: {
      nodeClick(e) {
        console.log(e)
        this.operatingElements = ELEMENTS.get(e.type);
        this.selectedTreeNode = e;
      },
      showAll() {
        this.operatingElements = ELEMENTS.get("ALL");
        this.selectedTreeNode = undefined;
        this.reload();
      },
      apiListImport() {
        this.apiListVisible = true;
      },
      addComponent(type) {
        switch (type) {
          case ELEMENT_TYPE.IfController:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new IfController()) :
              this.scenarioDefinition.push(new IfController());
            break;
          case ELEMENT_TYPE.ConstantTimer:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new ConstantTimer()) :
              this.scenarioDefinition.push(new ConstantTimer());
            break;
          case ELEMENT_TYPE.JSR223Processor:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new JSR223Processor()) :
              this.scenarioDefinition.push(new JSR223Processor());
            break;
          case ELEMENT_TYPE.JSR223PreProcessor:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PreProcessor"})) :
              this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PreProcessor"}));
            break;
          case ELEMENT_TYPE.JSR223PostProcessor:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PostProcessor"})) :
              this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PostProcessor"}));
            break;
          case ELEMENT_TYPE.Assertions:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new Assertions()) :
              this.scenarioDefinition.push(new Assertions());
            break;
          case ELEMENT_TYPE.Extract:
            this.selectedTreeNode != undefined ? this.selectedTreeNode.hashTree.push(new Extract()) :
              this.scenarioDefinition.push(new Extract());
            break;
          case ELEMENT_TYPE.CustomizeReq:
            this.customizeRequest = {protocol: "HTTP", type: "API", hashTree: [], referenced: false, active: false};
            this.customizeVisible = true;
            break;
          default:
            break;
        }
        this.reload();
      },
      addCustomizeApi(request) {
        this.customizeVisible = false;
        if (this.selectedTreeNode != undefined) {
          this.selectedTreeNode.hashTree.push(request);
        } else {
          this.scenarioDefinition.push(request);
        }
        this.customizeRequest = {};
        this.reload();
      },
      addReferenceApi() {
        if (this.currentRow.cases.length === 0 && this.currentRow.apis.length === 0) {
          this.$warning(this.$t('api_test.automation.reference_info'));
          return;
        }
        this.currentRow.cases.forEach(item => {
          let request = {};
          if (Object.prototype.toString.call(item.request).indexOf("String") > 0) {
            request = JSON.parse(item.request);
          } else {
            request = item.request;
          }
          request.referenced = true;
          request.active = false;
          request.resourceId = getUUID();

          if (this.selectedTreeNode != undefined) {
            this.selectedTreeNode.hashTree.push(request);
          } else {
            this.scenarioDefinition.push(request);
          }
        })
        this.currentRow.apis.forEach(item => {
          let request = {};
          if (Object.prototype.toString.call(item.request).indexOf("String") > 0) {
            request = JSON.parse(item.request);
          } else {
            request = item.request;
          }
          request.referenced = true;
          request.active = false;
          request.resourceId = getUUID();

          if (this.selectedTreeNode != undefined) {
            this.selectedTreeNode.hashTree.push(request);
          } else {
            this.scenarioDefinition.push(request);
          }
        })
        this.apiListVisible = false;
        this.reload();
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      tagChange() {

      },
      openTagConfig() {
        if (!this.currentProject) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProject.id);
      },
      closeTagConfig() {

      },
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let assertions = new Assertions();
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row, node) {
        const parent = node.parent
        const hashTree = parent.data.hashTree || parent.data;
        const index = hashTree.findIndex(d => d.id != undefined && row.id != undefined && d.id === row.id)
        hashTree.splice(index, 1);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      runDebug() {

      },
      getEnvironments() {
        if (this.currentProject) {
          this.$get('/api/environment/list/' + this.currentProject.id, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            let hasEnvironment = false;
            for (let i in this.environments) {
              if (this.environments[i].id === this.api.environmentId) {
                this.api.environment = this.environments[i];
                hasEnvironment = true;
                break;
              }
            }
            if (!hasEnvironment) {
              this.api.environmentId = '';
              this.api.environment = undefined;
            }
          });
        } else {
          this.api.environmentId = '';
          this.api.environment = undefined;
        }
      },
      openEnvironmentConfig() {
        if (!this.currentProject) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProject.id);
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.api.request.useEnvironment = this.environments[i].id;
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      },
      allowDrop(draggingNode, dropNode, type) {
        if (ELEMENTS.get(dropNode.data.type).indexOf(draggingNode.data.type) != -1) {
          return true;
        }
        return false;
      },
      allowDrag() {

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
    }
  }
</script>

<style scoped>
  .ms-scenario-input {
    width: 100%;
  }

  .ms-scenario-button {
    margin-left: 45%;
    padding: 7px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
  }

  .ms-api-col {
    background-color: #7C3985;
    border-color: #7C3985;
    margin-right: 10px;
    color: white;
  }

  .ms-col-one {
    margin-top: 6px;
  }

  .ms-right-buttion {
    margin-top: 10px;
  }

  /deep/ .el-tree-node__content {
    height: 100%;
    margin-top: 8px;
    vertical-align: center;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }

  /deep/ .el-drawer__body {
    overflow: auto;
  }

</style>
