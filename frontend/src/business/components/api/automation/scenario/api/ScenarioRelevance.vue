<template>
  <test-case-relevance-base
    :is-across-space="isAcrossSpace"
    :dialog-title="$t('api_test.automation.scenario_import')"
    @setProject="setProject"
    ref="baseRelevance">
    <template v-slot:aside>
      <ms-api-scenario-module
        style="margin-top: 5px;"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <ms-api-scenario-list
      v-if="versionEnable"
      :select-node-ids="selectNodeIds"
      :select-project-id="projectId"
      :referenced="true"
      :trash-enable="false"
      :is-reference-table="true"
      @selection="setData"
      :is-relate="true"
      :custom-num="customNum"
      ref="apiScenarioList">
      <template v-slot:version>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
      </template>
    </ms-api-scenario-list>

    <template v-slot:headerBtn>
      <el-button type="primary" @click="copy" :loading="buttonIsWorking" @keydown.enter.native.prevent size="mini">
        {{ $t('commons.copy') }}
      </el-button>
      <el-button type="primary" @click="reference" :loading="buttonIsWorking" @keydown.enter.native.prevent size="mini">
        {{ $t('api_test.scenario.reference') }}
      </el-button>
    </template>
  </test-case-relevance-base>
</template>

<script>
import MsContainer from "../../../../common/components/MsContainer";
import MsAsideContainer from "../../../../common/components/MsAsideContainer";
import MsMainContainer from "../../../../common/components/MsMainContainer";
import MsApiScenarioModule from "../ApiScenarioModule";
import MsApiScenarioList from "../ApiScenarioList";
import {getUUID, hasLicense} from "@/common/js/utils";
import RelevanceDialog from "../../../../track/plan/view/comonents/base/RelevanceDialog";
import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "ScenarioRelevance",
  props:{
    isAcrossSpace:{
      type:Boolean,
      default() {
        return false;
      }
    }
  },
  components: {
    'VersionSelect': VersionSelect.default,
    TestCaseRelevanceBase,
    RelevanceDialog,
    MsApiScenarioList,
    MsApiScenarioModule,
    MsMainContainer, MsAsideContainer, MsContainer
  },
  data() {
    return {
      buttonIsWorking: false,
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
      moduleOptions: {},
      isApiListEnable: true,
      currentScenario: [],
      currentScenarioIds: [],
      projectId: '',
      customNum: false,
      versionOptions: [],
      currentVersion: '',
      versionEnable: true,
    };
  },
  watch: {
    projectId(val) {
      this.$refs.nodeTree.list(this.projectId);
      if (val) {
        this.$get('/project_application/get/config/' + val + "/SCENARIO_CUSTOM_NUM", result => {
          let data = result.data;
          if (data) {
            this.customNum = data.scenarioCustomNum;
          }
        });
      }
      this.$refs.apiScenarioList.search(this.projectId);
      this.getVersionOptionList(this.projectId);
      this.checkVersionEnable(this.projectId);
    }
  },
  methods: {
    changeButtonLoadingType() {
      this.buttonIsWorking = false;
    },
    createScenarioDefinition(scenarios, data, referenced) {
      data.forEach(item => {
        let scenarioDefinition = JSON.parse(item.scenarioDefinition);
        if (scenarioDefinition && scenarioDefinition.hashTree) {
          let obj = {
            id: item.id,
            name: item.name,
            type: "scenario",
            headers: scenarioDefinition.headers,
            variables: scenarioDefinition.variables,
            environmentMap: scenarioDefinition.environmentMap,
            referenced: referenced,
            resourceId: getUUID(),
            hashTree: scenarioDefinition.hashTree,
            projectId: item.projectId,
            num: item.num,
            versionName: item.versionName,
            versionEnable: item.versionEnable
          };
          scenarios.push(obj);
        }
      });
    },
    getScenarioDefinition(referenced) {
      this.buttonIsWorking = true;
      let scenarios = [];
      let conditions = this.getConditions();
      if (conditions.selectAll) {
        let url = "/api/automation/id/all/";
        let params = {};
        params.ids = this.currentScenarioIds;
        params.condition = conditions;
        this.result = this.$post(url, params, (response) => {
          this.currentScenarioIds = response.data;
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            this.$warning('请选择场景');
            this.buttonIsWorking = false;
            return;
          }
          this.result = this.$post("/api/automation/getApiScenarios/", this.currentScenarioIds, response => {
            if (response.data) {
              this.createScenarioDefinition(scenarios, response.data, referenced)
              this.$emit('save', scenarios);
              this.$refs.baseRelevance.close();
              this.buttonIsWorking = false;
            }
          }, (error) => {
            this.buttonIsWorking = false;
          });
        }, (error) => {
          this.buttonIsWorking = false;
        });
      } else {
        if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
          this.$warning('请选择场景');
          this.buttonIsWorking = false;
          return;
        }
        this.result = this.$post("/api/automation/getApiScenarios/", this.currentScenarioIds, response => {
          if (response.data) {
            this.createScenarioDefinition(scenarios, response.data, referenced)
            this.$emit('save', scenarios);
            this.$refs.baseRelevance.close();
            this.buttonIsWorking = false;
          }
        }, (error) => {
          this.buttonIsWorking = false;
        });
      }
    },
    reference() {
      this.getScenarioDefinition("REF");
    },
    copy() {
      this.getScenarioDefinition("Copy");
    },
    close() {
      this.$emit('close');
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.$refs.baseRelevance.open();
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.search(this.projectId);
      }
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    refresh() {
      this.$refs.apiScenarioList.search();
    },
    setData(data) {
      this.currentScenario = Array.from(data).map(row => row);
      this.currentScenarioIds = Array.from(data).map(row => row.id);
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.selectNodeIds = [];
    },
    getConditions() {
      return this.$refs.apiScenarioList.getConditions();
    },
    getVersionOptionList(projectId) {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + projectId, response => {
          this.versionOptions = response.data;
        });
      }
    },
    changeVersion(currentVersion) {
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.condition.versionId = currentVersion || null;
      }
      this.refresh();
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + projectId, response => {
          this.versionEnable = false;
          this.$nextTick(() => {
            this.versionEnable = true;
          });
        });
      }
    },
  }
};
</script>

<style scoped>

</style>
