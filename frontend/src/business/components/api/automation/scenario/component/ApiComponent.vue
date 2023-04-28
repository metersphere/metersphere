<template>
  <div>
    <api-base-component
      @copy="copyRow"
      @remove="remove"
      @active="active"
      :is-show-name-input="!isDeletedOrRef"
      :data="request"
      :is-deleted="request.referenced==='REF' && !isShowNum"
      :draggable="draggable"
      :color="displayColor.color"
      :background-color="displayColor.backgroundColor"
      :is-max="isMax"
      :show-btn="showBtn"
      :show-version="showVersion"
      :title="displayTitle"
      :if-from-variable-advance="ifFromVariableAdvance">

      <template v-slot:afterTitle v-if="(request.refType==='API'|| request.refType==='CASE')">
        <span v-if="isShowNum" @click="clickResource(request)">{{ "（ ID: " + request.num + "）" }}</span>
        <span v-else>
          <el-tooltip class="ms-num" effect="dark"
                      :content="request.refType==='API'?$t('api_test.automation.scenario.api_none'):$t('api_test.automation.scenario.case_none')"
                      placement="top">
            <i class="el-icon-warning"/>
          </el-tooltip>
        </span>
        <span v-xpack v-if="request.versionEnable&&showVersion">{{ $t('project.version.name') }}: {{
            request.versionName
          }}</span>
      </template>

      <template v-slot:behindHeaderLeft>
        <el-tag size="small" class="ms-tag" v-if="request.referenced==='Deleted'" type="danger">
          {{ $t('api_test.automation.reference_deleted') }}
        </el-tag>
        <el-tag size="small" class="ms-tag" v-if="request.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
        <el-tag size="small" class="ms-tag" v-if="request.referenced ==='REF'">{{
            $t('api_test.scenario.reference')
          }}
        </el-tag>
        <span class="ms-tag ms-step-name-api">{{ getProjectName(request.projectId) }}</span>
      </template>
      <template v-slot:debugStepCode>
         <span v-if="request.testing" class="ms-test-running">
           <i class="el-icon-loading" style="font-size: 16px"/>
           {{ $t('commons.testing') }}
         </span>
        <!--  场景调试步骤增加误报判断  -->
        <span class="ms-step-debug-code" :class="'ms-req-error-report'" v-if="!loading &&!request.testing && request.debug
            && request.requestResult[0] && request.requestResult[0].responseResult && request.requestResult[0].status==='errorReportResult'">
          {{ $t("error_report_library.option.name") }}
        </span>
        <span class="ms-step-debug-code"
              @click="active"
              :class="request.requestResult[0].success && reqSuccess?'ms-req-success':'ms-req-error'"
              v-else-if="!loading &&!request.testing && request.debug && request.requestResult[0] && request.requestResult[0].responseResult">
          {{ request.requestResult[0].success && reqSuccess ? 'success' : 'error' }}
        </span>
      </template>
      <template v-slot:button v-if="!ifFromVariableAdvance">
        <el-tooltip :content="$t('api_test.run')" placement="top" v-if="!loading">
          <el-button :disabled="!request.enable" @click="run" icon="el-icon-video-play" class="ms-btn" size="mini" circle/>
        </el-tooltip>
        <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
          <el-button @click.once="stop" size="mini" style="color:white;padding: 0 0.1px;width: 24px;height: 24px;"
                     class="stop-btn" circle>
            <div style="transform: scale(0.66)">
              <span style="margin-left: -4.5px;font-weight: bold;">STOP</span>
            </div>
          </el-button>
        </el-tooltip>
      </template>
      <!--请求内容-->
      <template v-slot:request>
        <legend style="width: 100%;">
          <div v-if="!ifFromVariableAdvance">
            <customize-req-info :is-customize-req="isCustomizeReq" :request="request" @setDomain="setDomain"/>
            <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
            <ms-api-request-form
              v-if="request.protocol==='HTTP' || request.type==='HTTPSamplerProxy'"
              :scenario-definition="scenarioDefinition"
              @editScenarioAdvance="editScenarioAdvance"
              :isShowEnable="true"
              :response="response"
              :referenced="true"
              :scenarioId="currentScenario.id"
              :headers="request.headers "
              :is-read-only="isCompReadOnly"
              :request="request"/>
            <esb-definition
              v-if="showXpackCompnent&&request.esbDataStruct!=null"
              v-xpack
              :request="request"
              :response="response"
              :showScript="true"
              :show-pre-script="true"
              :is-read-only="isCompReadOnly" ref="esbDefinition"/>
            <ms-tcp-format-parameters
              v-if="(request.protocol==='TCP'|| request.type==='TCPSampler')&& request.esbDataStruct==null "
              :is-read-only="isCompReadOnly"
              :response="response"
              :show-pre-script="true"
              :scenarioId="currentScenario.id"
              :show-script="true" :request="request"/>

            <ms-sql-basis-parameters
              v-if="request.protocol==='SQL'|| request.type==='JDBCSampler'"
              :request="request"
              :response="response"
              :scenarioId="currentScenario.id"
              :is-read-only="isCompReadOnly"
              :showScript="true"/>

            <ms-dubbo-basis-parameters
              v-if="request.protocol==='DUBBO' || request.protocol==='dubbo://'|| request.type==='DubboSampler'"
              :request="request"
              :scenarioId="currentScenario.id"
              :response="response"
              :is-read-only="isCompReadOnly"
              :showScript="true"/>

          </div>
        </legend>
      </template>
      <!-- 执行结果内容 -->
      <template v-slot:result>
        <div v-loading="loading">
          <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
          <div v-if="showXpackCompnent&&request.backEsbDataStruct != null">
            <esb-definition-response
              :currentProtocol="request.protocol"
              :request="request"
              :is-api-component="false"
              :show-options-button="false"
              :show-header="true"
              :result="request.requestResult"
              v-xpack
              v-if="showXpackCompnent"
            />
          </div>
          <div v-else>
            <el-tabs v-model="request.activeName" closable class="ms-tabs"
                     v-if="request.requestResult && request.requestResult.length > 1">
              <el-tab-pane v-for="(item,i) in request.requestResult" :label="'循环'+(i+1)" :key="i"
                           style="margin-bottom: 5px">
                <api-response-component
                  :currentProtocol="request.protocol"
                  :apiActive="true"
                  :result="item"
                />
              </el-tab-pane>
            </el-tabs>
            <api-response-component
              :currentProtocol="request.protocol"
              :apiActive="true"
              :result="request.requestResult[0]"
              v-else/>
          </div>
        </div>
      </template>
    </api-base-component>
    <ms-run :debug="true" :reportId="reportId" :run-data="runData" :env-map="environmentMap"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>

  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentWorkspaceId, getUUID} from "@/common/js/utils";
import {getUrl} from "@/business/components/api/automation/scenario/component/urlhelper";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};

export default {
  name: "MsApiComponent",
  props: {
    request: {},
    currentScenario: {},
    node: {},
    draggable: {
      type: Boolean,
      default: false,
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    projectList: Array,
    envMap: Map,
    message: String,
    environmentGroupId: String,
    environmentType: String,
    scenarioDefinition: Array,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    TemplateComponent: () => import("@/business/components/track/plan/view/comonents/report/TemplateComponent/TemplateComponent"),
    CustomizeReqInfo: () => import("@/business/components/api/automation/scenario/common/CustomizeReqInfo"),
    ApiBaseComponent: () => import("../common/ApiBaseComponent"),
    ApiResponseComponent: () => import("./ApiResponseComponent"),
    MsSqlBasisParameters: () => import("../../../definition/components/request/database/BasisParameters"),
    MsTcpFormatParameters: () => import("../../../definition/components/request/tcp/TcpFormatParameters"),
    MsDubboBasisParameters: () => import("../../../definition/components/request/dubbo/BasisParameters"),
    MsApiRequestForm: () => import("../../../definition/components/request/http/ApiHttpRequestForm"),
    MsRequestResultTail: () => import("../../../definition/components/response/RequestResultTail"),
    MsRun: () => import("../../../definition/components/Run"),
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default
  },
  data() {
    return {
      loading: false,
      reportId: "",
      runData: [],
      isShowInput: false,
      showXpackCompnent: false,
      environment: {},
      result: {},
      apiActive: false,
      reqSuccess: true,
      envType: this.environmentType,
      environmentMap: this.envMap,
      isShowNum: false,
      response: {},
      currentScenarioData: {},
    }
  },
  created() {
    // 历史数据兼容
    if (!this.request.requestResult) {
      this.request.requestResult = [{responseResult: {}}];
    } else if (this.request.requestResult && Object.prototype.toString.call(this.request.requestResult) !== '[object Array]') {
      let obj = JSON.parse(JSON.stringify(this.request.requestResult));
      this.request.requestResult = [obj];
    }
    // 跨项目关联，如果没有ID，则赋值本项目ID
    if (!this.request.projectId) {
      this.request.projectId = getCurrentProjectID();
    }
    this.request.customizeReq = this.isCustomizeReq;
    if (this.request.customizeReq) {
      if (this.node.parent && this.node.parent.data && this.node.parent.data.length > 1) {
        this.request.projectId = getCurrentProjectID();
      }else {
        this.request.projectId =
          this.node.parent.data instanceof Array ? this.node.parent.data[0].projectId : this.node.parent.data.projectId;
      }
    }
    if (this.currentScenario) {
      this.request.currentScenarioId = this.currentScenario.id;
    }
    // 传递场景ID
    if (this.request.hashTree) {
      this.setOwnEnvironment(this.request.hashTree);
    }
    if (this.request.id && this.request.referenced === 'REF') {
      this.request.disabled = true;
      this.request.root = this.node.parent.parent ? false : true;
    }
    this.isShowNum = this.request.num ? true : false;
    if (this.request.protocol === 'HTTP') {
      // 历史数据 auth 处理
      if (this.request.hashTree) {
        for (let index in this.request.hashTree) {
          if (this.request.hashTree[index].type == 'AuthManager') {
            this.request.authManager = this.request.hashTree[index];
            this.request.hashTree.splice(index, 1);
          }
        }
      }
    }
    if (requireComponent != null && JSON.stringify(esbDefinition) != '{}' && JSON.stringify(esbDefinitionResponse) != '{}') {
      this.showXpackCompnent = true;
    }
    if (this.request.requestResult && this.request.requestResult.length > 0) {
      this.response = this.request.requestResult[0];
    }
  },
  watch: {
    message() {
      this.forStatus();
      this.reload();
    },
    '$store.state.currentApiCase.debugLoop'() {
      this.forStatus();
      this.reload();
    },
  },
  computed: {
    displayColor() {
      if (this.isApiImport) {
        return {
          color: "#F56C6C",
          backgroundColor: "#FCF1F1"
        }
      } else if (this.isExternalImport) {
        return {
          color: "#409EFF",
          backgroundColor: "#EEF5FE"
        }
      } else if (this.isCustomizeReq) {
        return {
          color: "#008080",
          backgroundColor: "#EBF2F2"
        }
      }
      return {};
    },
    isCompReadOnly() {
      if (this.request) {
        if (this.request.disabled) {
          return this.request.disabled;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    displayTitle() {
      if (this.isApiImport) {
        return this.request.refType === 'API' ? 'API' : 'CASE';
      } else if (this.isExternalImport) {
        return this.$t('api_test.automation.external_import');
      } else if (this.isCustomizeReq) {
        return this.$t('api_test.automation.customize_req');
      }
      return "";
    },
    isApiImport() {
      let verifies = ['Deleted', 'REF', 'Copy'];
      return (this.request.referenced && verifies.indexOf(this.request.referenced) !== -1);
    },
    isExternalImport() {
      return (this.request.referenced && this.request.referenced === 'OT_IMPORT');
    },
    isCustomizeReq() {
      return (!this.request.referenced || this.request.referenced === 'Created');
    },
    isDeletedOrRef() {
      let verifies = ['Deleted', 'REF'];
      return (this.request.referenced && verifies.indexOf(this.request.referenced) !== -1);
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    setOwnEnvironment(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = ["JDBCPostProcessor", "JDBCSampler", "JDBCPreProcessor"]
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          scenarioDefinition[i].currentScenarioId = this.currentScenario.id;
        }
        if (scenarioDefinition[i].hashTree !== undefined && scenarioDefinition[i].hashTree.length > 0) {
          this.setOwnEnvironment(scenarioDefinition[i].hashTree);
        }
      }
    },
    forStatus() {
      this.reqSuccess = true;
      if (this.request.result && this.request.result.length > 0) {
        this.request.result.forEach(item => {
          item.requestResult.forEach(req => {
            if (!req.success) {
              this.reqSuccess = req.success;
            }
          })
        })
      } else if (this.request.requestResult && this.request.requestResult.length > 1) {
        this.request.requestResult.forEach(item => {
          if (!item.success) {
            this.reqSuccess = item.success;
            if (this.node && this.node.parent && this.node.parent.data) {
              this.node.parent.data.code = 'error';
            }
          }
        })
      }
      if (this.request.requestResult && this.request.requestResult.length > 0) {
        this.response = this.request.requestResult[0];
      }
    },

    remove() {
      this.$emit('remove', this.request, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.request, this.node);
    },
    setUrl(url) {
      try {
        new URL(url);
        this.request.url = url;
      } catch (e) {
        if (url && (!url.startsWith("http://") || !url.startsWith("https://"))) {
          if (!this.isCustomizeReq) {
            this.request.path = url;
            this.request.url = undefined;
          }
        }
      }
    },
    mergeHashTree(targetHashTree) {
      let sourceHashTree = this.request.hashTree;
      // 历史数据兼容
      if (sourceHashTree && targetHashTree && sourceHashTree.length < targetHashTree.length) {
        this.request.hashTree = targetHashTree;
        return;
      }
      let sourceIds = [];
      let delIds = [];
      let updateMap = new Map();
      if (!sourceHashTree || sourceHashTree.length == 0) {
        if (targetHashTree) {
          targetHashTree.forEach(item => {
            item.disabled = true;
          });
          this.request.hashTree = targetHashTree;
        }
        return;
      }
      if (targetHashTree) {
        for (let i in targetHashTree) {
          targetHashTree[i].disabled = true;
          if (targetHashTree[i].id) {
            updateMap.set(targetHashTree[i].id, targetHashTree[i]);
          }
        }
      }

      if (sourceHashTree && sourceHashTree.length > 0) {
        for (let index in sourceHashTree) {
          let source = sourceHashTree[index];
          sourceIds.push(source.id);
          // 历史数据兼容
          if (source.label !== 'SCENARIO-REF-STEP' && source.id) {
            if (updateMap.has(source.id)) {
              Object.assign(sourceHashTree[index], updateMap.get(source.id));
              sourceHashTree[index].disabled = true;
              sourceHashTree[index].label = '';
              sourceHashTree[index].enable = updateMap.get(source.id).enable;
            } else {
              delIds.push(source.id);
            }
          }
          // 历史数据兼容
          if (!source.id && source.label !== 'SCENARIO-REF-STEP' && index < targetHashTree.length) {
            Object.assign(sourceHashTree[index], targetHashTree[index]);
            sourceHashTree[index].disabled = true;
            sourceHashTree[index].label = '';
            sourceHashTree[index].enable = targetHashTree[index].enable;
          }
        }
      }
      // 删除多余的步骤
      delIds.forEach(item => {
        const removeIndex = sourceHashTree.findIndex(d => d.id && d.id === item);
        sourceHashTree.splice(removeIndex, 1);
      })

      // 补充新增的源引用步骤
      if (targetHashTree) {
        targetHashTree.forEach(item => {
          if (sourceIds.indexOf(item.id) === -1) {
            item.disabled = true;
            this.request.hashTree.push(item);
          }
        })
      }
    },
    sort() {
      for (let i in this.request.hashTree) {
        this.request.hashTree[i].index = Number(i) + 1;
        if (!this.request.hashTree[i].resourceId) {
          this.request.hashTree[i].resourceId = getUUID();
        }
      }
    },
    active() {
      this.request.active = !this.request.active;
      if (this.node) {
        this.node.expanded = this.request.active;
      }
      this.apiActive = this.request.active;
      this.reload();
    },
    run() {
      this.currentScenarioData = undefined;
      this.getParentVariables(this.node);
      this.$get('/project/getOwnerProjectIds', res => {
        const project = res.data.find(p => p === this.request.projectId);
        if (!project) {
          this.$warning(this.$t('api_test.automation.project_no_permission'));
        } else {
          let selectEnvId;
          // 自定义请求
          if (this.isApiImport || this.request.isRefEnvironment) {
            if (this.request.type && (this.request.type === "HTTPSamplerProxy" || this.request.type === "JDBCSampler" || this.request.type === "TCPSampler")) {
              if (this.$store.state.scenarioEnvMap && this.$store.state.scenarioEnvMap instanceof Map
                && this.$store.state.scenarioEnvMap.has((this.currentScenario.id + "_" + this.request.projectId))) {
                selectEnvId = this.$store.state.scenarioEnvMap.get((this.currentScenario.id + "_" + this.request.projectId));
                this.environmentMap = this.envMap;
              }
              if (!selectEnvId && !this.environmentGroupId ) {
                this.$warning(this.$t('api_test.automation.env_message'));
                return false;
              }
            }
          }
          if (!this.request.enable) {
            this.$warning(this.$t('api_test.automation.debug_message'));
            return false;
          }
          this.request.debug = true;
          this.request.active = true;
          this.loading = true;
          this.runData = [];
          if (selectEnvId) {
            this.request.useEnvironment = selectEnvId;
            this.request.environmentId = selectEnvId;
          }
          this.request.customizeReq = this.isCustomizeReq;
          // 场景变量
          let variables = [];
          if (this.currentScenario && this.currentScenario.variables) {
            variables = JSON.parse(JSON.stringify(this.currentScenario.variables));
          }
          let debugData = {
            id: this.currentScenario.id, name: this.currentScenario.name, type: "scenario",
            variables: variables, referenced: 'Created', headers: this.currentScenario.headers,
            enableCookieShare: this.enableCookieShare, environmentId: selectEnvId,
            environmentGroupId: this.environmentGroupId, hashTree: [this.request],
          };
          // 合并自身依赖场景变量
          if (this.currentScenarioData && this.currentScenarioData.variableEnable && this.currentScenarioData.variables) {
            if (!debugData.variables || debugData.variables.length === 0) {
              debugData.variables = this.currentScenarioData.variables;
            } else if (this.currentScenarioData.variables) {
              // 同名合并
              debugData.variables.forEach(data => {
                this.currentScenarioData.variables.forEach(item => {
                  if (data.type === item.type && data.name === item.name) {
                    Object.assign(data, item);
                  }
                })
              });
            }
          }
          this.runData.push(debugData);
          this.request.requestResult = [];
          this.request.result = undefined;
          /*触发执行操作*/
          this.reportId = getUUID();
        }
      })
    },
    getParentVariables(node) {
      if (!this.currentScenarioData) {
        if (node && node.data && node.data.type === "scenario") {
          this.currentScenarioData = node.data;
        } else {
          if (node.parent && node.parent.data) {
            this.getParentVariables(node.parent);
          }
        }
      }
    },
    stop() {
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.loading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    errorRefresh() {
      this.loading = false;
    },
    runRefresh(data) {
      this.request.requestResult = [data];
      this.request.result = undefined;
      this.loading = false;
      this.response = data;
      this.$emit('refReload', this.request, this.node);
    },
    setDomain() {
      this.$emit("setDomain");
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    getProjectName(id) {
      if (this.projectId !== id) {
        const project = this.projectList.find(p => p.id === id);
        return project ? project.name : "";
      }
    },

    clickResource(resource) {
      let workspaceId = getCurrentWorkspaceId();
      let isTurnSpace = true
      if (resource.projectId !== getCurrentProjectID()) {
        isTurnSpace = false;
        this.$get("/project/get/" + resource.projectId, response => {
          if (response.data) {
            workspaceId = response.data.workspaceId;
            isTurnSpace = true;
            this.checkPermission(resource, workspaceId, isTurnSpace);
          }
        });
      } else {
        this.checkPermission(resource, workspaceId, isTurnSpace);
      }
    },
    clickCase(resource) {
      let uri = getUrl(resource);
      let resourceId = resource.sourceId;
      if (resourceId && resourceId.startsWith("\"" || resourceId.startsWith("["))) {
        resourceId = JSON.parse(resource.sourceId);
      }
      if (resourceId instanceof Array) {
        resourceId = resourceId[0];
      }
      this.$get('/user/update/currentByResourceId/' + resourceId, () => {
        this.toPage(uri);
      });
    },
    toPage(uri) {
      let id = "new_a";
      let a = document.createElement("a");
      a.setAttribute("href", uri);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
    gotoTurn(resource, workspaceId, isTurnSpace) {
      if (resource.refType && resource.refType === 'API') {
        if (resource.protocol === 'dubbo://') {
          resource.protocol = 'DUBBO'
        }
        let definitionData = this.$router.resolve({
          name: 'ApiDefinition',
          params: {
            redirectID: getUUID(),
            dataType: "api",
            dataSelectRange: 'edit:' + resource.id,
            projectId: resource.projectId,
            type: resource.protocol,
            workspaceId: workspaceId,
          }
        });
        if (isTurnSpace) {
          window.open(definitionData.href, '_blank');
        }
      } else if (resource.refType && resource.refType === 'CASE') {
        this.$get("/api/testcase/findById/" + resource.id, response => {
          if (response.data) {
            response.data.sourceId = resource.resourceId;
            response.data.type = resource.type;
            response.data.refType = resource.refType;
            response.data.workspaceId = workspaceId;
            if (isTurnSpace) {
              this.clickCase(response.data)
            }
          } else {
            this.$error("接口用例场景场景已经被删除");
          }
        });
      }
    },
    checkPermission(resource, workspaceId, isTurnSpace) {
      this.$get('/project/getOwnerProjectIds', res => {
        const project = res.data.find(p => p === resource.projectId);
        if (!project) {
          this.$warning(this.$t('commons.no_permission'));
        } else {
          this.gotoTurn(resource, workspaceId, isTurnSpace)
        }

      })
    }
  }
}
</script>

<style scoped>
.ms-api-col-ot-import-button {
  background-color: #EEF5FE;
  margin-right: 20px;
  color: #409EFF;
}

.icon.is-active {
  transform: rotate(90deg);
}

.ms-tabs >>> .el-icon-close:before {
  content: "";

}

.ms-btn {
  padding: 5px;
  background-color: #409EFF;
  color: white;
}

.ms-btn-float {
  margin: 20px;
  float: right;
}

.ms-step-name-api {
  padding-left: 5px;
}

.ms-tag {
  margin-left: 0px;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 0px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 60px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-req-error-report {
  color: #F6972A;
}

.ms-test-running {
  color: #6D317C;
}

.ms-req-success {
  color: #67C23A;
}

.stop-btn {
  background-color: #E62424;
  border-color: #EE6161;
  color: white;
}

.ms-num {
  margin-left: 1rem;
  font-size: 15px;
  color: #de9d1c;
}

</style>
