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
    color="#606266"
    background-color="#F4F4F5"
    :title="$t('api_test.automation.scenario_import')">

    <template v-slot:behindHeaderLeft>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='Deleted'" type="danger">{{ $t('api_test.automation.reference_deleted') }}</el-tag>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
      <el-tag size="mini" class="ms-tag" v-if="scenario.referenced==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>

      <span class="ms-tag">{{ getProjectName(scenario.projectId) }}</span>
    </template>
    <template v-slot:debugStepCode>
      <span class="ms-step-debug-code" :class="node.data.code ==='error'?'ms-req-error':'ms-req-success'" v-if="!loading && node.data.debug">
        {{ getCode() }}
      </span>
    </template>

  </api-base-component>
</template>

<script>
import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
import MsTcpBasisParameters from "../../../definition/components/request/tcp/TcpBasisParameters";
import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
import MsApiRequestForm from "../../../definition/components/request/http/ApiHttpRequestForm";
import ApiBaseComponent from "../common/ApiBaseComponent";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "ApiScenarioComponent",
  props: {
    scenario: {},
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
    draggable: {
      type: Boolean,
      default: false,
    },
    currentEnvironmentId: String,
    projectList: Array
  },
  watch: {
    message() {
      this.reload();
    },
  },
  created() {
    if (!this.scenario.projectId) {
      this.scenario.projectId = getCurrentProjectID();
    }
    if (this.scenario.id && this.scenario.referenced === 'REF' && !this.scenario.loaded) {
      this.result = this.$get("/api/automation/getApiScenario/" + this.scenario.id, response => {
        if (response.data) {
          this.scenario.loaded = true;
          let obj = {};
          if (response.data.scenarioDefinition) {
            obj = JSON.parse(response.data.scenarioDefinition);
            this.scenario.hashTree = obj.hashTree;
          }
          this.scenario.projectId = response.data.projectId;
          const pro = this.projectList.find(p => p.id === response.data.projectId);
          if (!pro) {
            this.scenario.projectId = getCurrentProjectID();
          }
          if (this.scenario.hashTree) {
            this.setDisabled(this.scenario.hashTree, this.scenario.projectId);
          }
          //this.scenario.disabled = true;
          this.scenario.name = response.data.name;
          this.scenario.headers = obj.headers;
          this.scenario.variables = obj.variables;
          this.scenario.environmentMap = obj.environmentMap;
          this.$emit('refReload');
        } else {
          this.scenario.referenced = "Deleted";
        }
      })
    }
  },
  components: {ApiBaseComponent, MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
  data() {
    return {
      loading: false,
      isShowInput: false,
    }
  },
  computed: {
    isDeletedOrRef() {
      if (this.scenario.referenced != undefined && this.scenario.referenced === 'Deleted' || this.scenario.referenced === 'REF') {
        return true;
      }
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    getCode() {
      if (this.node && this.node.data.debug) {
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
        this.node.expanded = !this.node.expanded;
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
        if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
          this.recursive(arr[i].hashTree, arr[i].projectId);
        }
      }
    },
    setDisabled(scenarioDefinition, id) {
      for (let i in scenarioDefinition) {
        scenarioDefinition[i].disabled = true;
        scenarioDefinition[i].projectId = this.calcProjectId(scenarioDefinition[i].projectId, id);
        if (scenarioDefinition[i].hashTree != undefined && scenarioDefinition[i].hashTree.length > 0) {
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
      if (this.projectId !== id) {
        const project = this.projectList.find(p => p.id === id);
        return project ? project.name : "";
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

.ms-tag {
  margin-left: 0px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-req-success {
  color: #67C23A;
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
</style>
