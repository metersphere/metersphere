<template>
  <api-base-component
    v-loading="loading"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    @openScenario="openScenario"
    :data="scenario"
    :show-collapse="false"
    :is-show-name-input="!isDeletedOrRef"
    :is-disabled="true"
    :is-max="isMax"
    :show-btn="showBtn"
    :show-version="showVersion"
    color="#606266"
    background-color="#F4F4F5"
    :if-from-variable-advance="ifFromVariableAdvance"
    :environmentType="environmentType"
    :environmentGroupId="environmentGroupId"
    :envMap="envMap"
    :title="$t('commons.scenario')">

    <template v-slot:afterTitle>
      <span v-if="isShowNum" @click="clickResource(scenario)">{{ "（ ID: " + scenario.num + "）" }}</span>
      <span v-else>
        <el-tooltip class="ms-num" effect="dark" :content="$t('api_test.automation.scenario.num_none')" placement="top">
          <i class="el-icon-warning"/>
        </el-tooltip>
      </span>
      <span v-xpack v-if="scenario.versionEnable">{{ $t('project.version.name') }}: {{ scenario.versionName }}</span>
    </template>

    <template v-slot:behindHeaderLeft>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='Deleted'" type="danger">{{ $t('api_test.automation.reference_deleted') }}</el-tag>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
      <span class="ms-tag ms-step-name-api">{{ getProjectName(scenario.projectId) }}</span>
    </template>
    <template v-slot:debugStepCode>
       <span v-if="node.data.testing" class="ms-test-running">
         <i class="el-icon-loading" style="font-size: 16px"/>
         {{ $t('commons.testing') }}
       </span>
      <span class="ms-step-debug-code" :class="node.data.code ==='error'?'ms-req-error':'ms-req-success'" v-if="!loading && node.data.debug && !node.data.testing">
        {{ getCode() }}
      </span>
    </template>
    <template v-slot:button v-if="!ifFromVariableAdvance">
      <el-tooltip :content="$t('api_test.run')" placement="top" v-if="!scenario.run">
        <el-button :disabled="!scenario.enable" @click="run" icon="el-icon-video-play" style="padding: 5px" class="ms-btn" size="mini" circle/>
      </el-tooltip>
      <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
        <el-button :disabled="!scenario.enable" @click.once="stop" size="mini" style="color:white;padding: 0 0.1px;width: 24px;height: 24px;" class="stop-btn" circle>
          <div style="transform: scale(0.66)">
            <span style="margin-left: -4.5px;font-weight: bold;">STOP</span>
          </div>
        </el-button>
      </el-tooltip>
    </template>

  </api-base-component>
</template>

<script>
import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
import MsTcpBasisParameters from "../../../definition/components/request/tcp/TcpBasisParameters";
import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
import MsApiRequestForm from "../../../definition/components/request/http/ApiHttpRequestForm";
import ApiBaseComponent from "../common/ApiBaseComponent";
import {getCurrentProjectID, getUUID, strMapToObj} from "@/common/js/utils";
import {STEP} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "ApiScenarioComponent",
  props: {
    scenario: {},
    currentScenario: {},
    message: String,
    node: {},
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
    draggable: {
      type: Boolean,
      default: false,
    },
    currentEnvironmentId: String,
    projectList: Array,
    environmentType: String,
    environmentGroupId: String,
    envMap: Map,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    }
  },
  watch: {
    message() {
      if (this.message === "stop") {
        this.scenario.run = false;
      }
      this.reload();
    },
    'node.data.isBatchProcess'() {
      if (this.node.data && this.node.data.isBatchProcess && this.node.data.referenced === 'REF') {
        this.node.expanded = false;
      }
    }
  },
  created() {
    if (this.scenario.num) {
      this.isShowNum = true;
    } else {
      this.isShowNum = false;
    }
    if (this.scenario.id && this.scenario.referenced === 'REF' && !this.scenario.loaded && this.scenario.hashTree) {
      this.setDisabled(this.scenario.hashTree, this.scenario.projectId);
    }
  },
  components: {ApiBaseComponent, MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
  data() {
    return {
      loading: false,
      isShowInput: false,
      isShowNum: false,
      stepFilter: new STEP,
    }
  },
  computed: {
    isDeletedOrRef() {
      return this.scenario.referenced !== undefined && this.scenario.referenced === 'Deleted' || this.scenario.referenced === 'REF';

    },
  },
  methods: {
    run() {
      if (!this.scenario.enable) {
        this.$warning(this.$t('api_test.automation.debug_message'));
        return;
      }
      this.scenario.run = true;
      let runScenario = JSON.parse(JSON.stringify(this.scenario));
      runScenario.hashTree = [this.scenario];
      runScenario.stepScenario = true;
      this.$emit('runScenario', runScenario);
    },
    stop() {
      this.scenario.run = false;
      this.$emit('stopScenario');
      this.reload();
    },
    checkEnv(val) {
      this.$get("/api/automation/checkScenarioEnv/" + this.scenario.id, res => {
        if (this.scenario.environmentEnable && !res.data) {
          this.scenario.environmentEnable = false;
          this.$warning(this.$t('commons.scenario_warning'));
          return;
        }
        this.setDomain(val);
      });
    },
    setDomain(val) {
      let param = {
        environmentEnable: val,
        id: this.scenario.id,
        environmentType: this.environmentType,
        environmentGroupId: this.environmentGroupId,
        environmentMap: strMapToObj(this.envMap),
        definition: JSON.stringify(this.scenario)
      }
      this.$post("/api/automation/setDomain", param, res => {
        if (res.data) {
          let data = JSON.parse(res.data);
          this.scenario.hashTree = data.hashTree;
        }
      })
    },
    getCode() {
      if (this.node && this.node.data.code && this.node.data.debug) {
        if (this.node.data.code && this.node.data.code === 'error') {
          return 'error';
        } else {
          return 'success';
        }
      }
      return '';
    },
    remove() {
      this.$emit('remove', this.scenario, this.node);
    },
    active() {
      if (this.node) {
        if (this.node.data && this.node.data.isBatchProcess && this.node.data.referenced === 'REF') {
          this.node.expanded = false;
        } else {
          this.node.expanded = !this.node.expanded;
        }
      }
      this.reload();
    },
    copyRow() {
      this.$emit('copyRow', this.scenario, this.node);
    },
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    recursive(arr, id) {
      for (let i in arr) {
        arr[i].disabled = true;
        arr[i].projectId = this.calcProjectId(arr[i].projectId, id);
        if (arr[i].hashTree !== undefined && arr[i].hashTree.length > 0) {
          this.recursive(arr[i].hashTree, arr[i].projectId);
        }
      }
    },
    setDisabled(scenarioDefinition, id) {
      for (let i in scenarioDefinition) {
        scenarioDefinition[i].disabled = true;
        scenarioDefinition[i].projectId = this.calcProjectId(scenarioDefinition[i].projectId, id);
        if (scenarioDefinition[i].hashTree !== undefined && scenarioDefinition[i].hashTree.length > 0) {
          this.recursive(scenarioDefinition[i].hashTree, scenarioDefinition[i].projectId);
        }
      }
    },
    calcProjectId(projectId, parentId) {
      if (!projectId) {
        return parentId ? parentId : getCurrentProjectID();
      } else {
        const project = this.projectList.find(p => p.id === projectId);
        if (project) {
          return projectId;
        }
        return parentId ? parentId : getCurrentProjectID();
      }
    },
    getProjectName(id) {
      if (id !== getCurrentProjectID()) {
        const project = this.projectList.find(p => p.id === id);
        return project ? project.name : "";
      }

    },
    clickResource(resource) {
      let workspaceId;
      let isTurnSpace = true
      if(resource.projectId!==getCurrentProjectID()){
        isTurnSpace = false;
        this.$get("/project/get/" + resource.projectId, response => {
          if (response.data) {
            workspaceId  = response.data.workspaceId;
            isTurnSpace = true;
            this.gotoTurn(resource,workspaceId,isTurnSpace);
          }
        });
      }else {
        this.gotoTurn(resource,workspaceId,isTurnSpace);
      }

    },
    gotoTurn(resource,workspaceId,isTurnSpace){
      let automationData = this.$router.resolve({
        name: 'ApiAutomation',
        params: {redirectID: getUUID(), dataType: "scenario", dataSelectRange: 'edit:' + resource.id, projectId: resource.projectId, workspaceId: workspaceId}
      });
      if(isTurnSpace){
        window.open(automationData.href, '_blank');
      }
    }

  }
}
</script>

<style scoped>

/deep/ .el-card__body {
  padding: 6px 10px;
}

.icon.is-active {
  transform: rotate(90deg);
}

.ms-step-name-api {
  padding-left: 10px;
}

.ms-tag {
  margin-left: 0;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-test-running {
  color: #6D317C;
}

.ms-req-success {
  color: #67C23A;
}

.ms-btn {
  background-color: #409EFF;
  color: white;
}

.ms-btn-flot {
  margin: 20px;
  float: right;
}

.stop-btn {
  background-color: #E62424;
  border-color: #EE6161;
  color: white;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 60px;
}

.ms-test-running {
  color: #6D317C;
}

.ms-num {
  margin-left: 1rem;
  font-size: 15px;
  color: #de9d1c;
}
</style>
