<template>
  <test-case-relevance-base
    :is-across-space="isAcrossSpace"
    :dialog-title="$t('api_test.automation.scenario_import')"
    @setProject="setProject"
    @refreshNode="refresh"
    ref="baseRelevance">
    <template v-slot:aside>
      <ms-api-scenario-module
        style="margin-top: 5px"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        :select-project-id="projectId"
        ref="nodeTree" />
    </template>

    <relevance-scenario-list
      :version-enable="versionEnable"
      :select-node-ids="selectNodeIds"
      :project-id="projectId"
      :referenced="true"
      :trash-enable="false"
      @selectCountChange="setSelectCounts"
      ref="apiScenarioList">
    </relevance-scenario-list>

    <template v-slot:headerBtn>
      <table-select-count-bar :count="selectCounts" style="float: left; margin: 5px" />

      <el-button size="mini" icon="el-icon-refresh" @click="refresh" />
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
import { getProjectVersions, versionEnableByProjectId } from '@/api/xpack';
import { apiScenarioAll, getApiScenarios, getApiScenarioStep } from '@/api/scenario';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsAsideContainer from 'metersphere-frontend/src/components/MsAsideContainer';
import MsMainContainer from 'metersphere-frontend/src/components/MsMainContainer';
import MsApiScenarioModule from '../ApiScenarioModule';
import { getUUID, operationConfirm } from 'metersphere-frontend/src/utils';
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import RelevanceDialog from '@/business/commons/RelevanceDialog';
import RelevanceScenarioList from './RelevanceScenarioList';
import TestCaseRelevanceBase from '../../../commons/TestCaseRelevanceBase';
import TableSelectCountBar from '@/business/automation/scenario/api/TableSelectCountBar';
import { getProjectConfig } from '@/api/project';
export default {
  name: 'ScenarioRelevance',
  props: {
    isAcrossSpace: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  components: {
    TableSelectCountBar,
    TestCaseRelevanceBase,
    RelevanceScenarioList,
    RelevanceDialog,
    MsApiScenarioModule,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
  },
  data() {
    return {
      buttonIsWorking: false,
      result: false,
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
      selectCounts: null,
    };
  },
  watch: {
    projectId(val) {
      this.$refs.nodeTree.list(this.projectId);
      if (val) {
        getProjectConfig(val, '/SCENARIO_CUSTOM_NUM').then((result) => {
          let data = result.data;
          if (data) {
            this.customNum = data.scenarioCustomNum;
          }
        });
      }
      this.$refs.apiScenarioList.search(this.projectId);
      this.getVersionOptionList(this.projectId);
      this.checkVersionEnable(this.projectId);
    },
  },
  methods: {
    changeButtonLoadingType() {
      this.buttonIsWorking = false;
    },
    createScenarioDefinition(scenarios, data, referenced) {
      let emptyStepScenarios = '';
      data.forEach((item) => {
        if (!item.stepTotal || item.stepTotal == 0) {
          emptyStepScenarios += item.name + ',';
        } else {
          let scenarioDefinition = JSON.parse(item.scenarioDefinition);
          if (scenarioDefinition && scenarioDefinition.hashTree) {
            let obj = {
              id: item.id,
              name: item.name,
              type: 'scenario',
              headers: scenarioDefinition.headers,
              variables: scenarioDefinition.variables,
              environmentMap: scenarioDefinition.environmentMap,
              referenced: referenced,
              refType: 'scenario',
              resourceId: getUUID(),
              hashTree: scenarioDefinition.hashTree,
              projectId: item.projectId,
              num: item.num,
              customNum: item.customNum,
              showCustomNum: scenarioDefinition.showCustomNum,
              versionName: item.versionName,
              versionEnable: item.versionEnable,
            };
            scenarios.push(obj);
          }
        }
      });
      if (emptyStepScenarios !== '') {
        if (emptyStepScenarios.endsWith(',')) {
          emptyStepScenarios = emptyStepScenarios.substring(0, emptyStepScenarios.length - 1);
          this.$error(this.$t('api_test.scenario.scenario_step_is_empty', [emptyStepScenarios]));
        }
      }
    },
    getScenarioDefinition(referenced) {
      this.buttonIsWorking = true;
      let scenarios = [];
      let conditions = this.getConditions();
      this.currentScenarioIds.sort((a, b) => {
        return conditions.tableDataIds.indexOf(a) - conditions.tableDataIds.indexOf(b);
      });
      if (conditions.selectAll) {
        let params = {};
        params.ids = this.currentScenarioIds;
        params.condition = conditions;
        apiScenarioAll(params).then((response) => {
          this.currentScenarioIds = response.data;
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            this.$warning(this.$t('automation.scenario_message'));
            this.buttonIsWorking = false;
            return;
          }
          getApiScenarioStep(this.currentScenarioIds).then((response) => {
            if (response.data > 500) {
              operationConfirm(
                this,
                this.$t('automation.scenario_step_ref_message', [response.data]) + '？',
                () => {
                  this.pushApiScenario(scenarios, referenced);
                },
                () => {
                  this.buttonIsWorking = false;
                }
              );
            } else {
              this.pushApiScenario(scenarios, referenced);
            }
          });
        });
      } else {
        if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
          this.$warning(this.$t('automation.scenario_message'));
          this.buttonIsWorking = false;
          return;
        }
        getApiScenarioStep(this.currentScenarioIds).then((response) => {
          if (response.data > 500) {
            operationConfirm(
              this,
              this.$t('automation.scenario_step_ref_message', [response.data]) + '？',
              () => {
                this.pushApiScenario(scenarios, referenced);
              },
              () => {
                this.buttonIsWorking = false;
              }
            );
          } else {
            this.pushApiScenario(scenarios, referenced);
          }
        });
      }
    },
    pushApiScenario(scenarios, referenced) {
      this.result = getApiScenarios(this.currentScenarioIds).then(
        (response) => {
          if (response.data) {
            this.currentScenarioIds = [];
            this.createScenarioDefinition(scenarios, response.data, referenced);
            this.$emit('save', scenarios);
            this.$refs.baseRelevance.close();
            this.buttonIsWorking = false;
          }
        },
        () => {
          this.buttonIsWorking = false;
        }
      );
    },
    reference() {
      this.getScenarioDefinition('REF');
    },
    copy() {
      this.getScenarioDefinition('Copy');
    },
    close() {
      this.$emit('close');
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.selectCounts = 0;
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
      this.$refs.apiScenarioList.search(this.projectId);
    },
    setData(data) {
      this.currentScenario = Array.from(data).map((row) => row);
      this.currentScenarioIds = Array.from(data).map((row) => row.id);
    },
    setProject(projectId) {
      this.projectId = projectId;
    },
    getConditions() {
      return this.$refs.apiScenarioList.getConditions();
    },
    getVersionOptionList(projectId) {
      if (hasLicense()) {
        getProjectVersions(projectId).then((response) => {
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
        versionEnableByProjectId(projectId).then((response) => {
          this.versionEnable = false;
          this.$nextTick(() => {
            this.versionEnable = true;
          });
        });
      }
    },
    setSelectCounts(data) {
      this.selectCounts = data;
      this.setData(this.$refs.apiScenarioList.selectRows);
    },
  },
};
</script>

<style scoped></style>
