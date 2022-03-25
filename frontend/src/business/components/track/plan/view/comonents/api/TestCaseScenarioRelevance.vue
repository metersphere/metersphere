<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :relevance-project-id="projectId"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <relevance-scenario-list
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :version-enable="versionEnable"
      :plan-id="planId"
      :project-id="projectId"
      ref="apiScenarioList"/>

  </test-case-relevance-base>

</template>

<script>

  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import {getCurrentProjectID, strMapToObj} from "../../../../../../../common/js/utils";
  import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
  import MsApiScenarioList from "../../../../../api/automation/scenario/ApiScenarioList";
  import MsApiScenarioModule from "../../../../../api/automation/scenario/ApiScenarioModule";
  import RelevanceScenarioList from "./RelevanceScenarioList";
  import {ENV_TYPE} from "@/common/js/constants";

  export default {
    name: "TestCaseScenarioRelevance",
    components: {
      RelevanceScenarioList,
      MsApiScenarioModule,
      MsApiScenarioList,
      ApiCaseSimpleList,
      MsApiModule,
      TestCaseRelevanceBase,
    },
    data() {
      return {
        showCasePage: true,
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        moduleOptions: {},
        trashEnable: false,
        condition: {},
        currentRow: {},
        projectId: ""
      };
    },
    props: {
      planId: {
        type: String
      },
      versionEnable: {
        type: Boolean,
        default: false
      }
    },
    watch: {
      planId() {
        this.condition.planId = this.planId;
      },
    },
    methods: {
      open() {
        this.$refs.baseRelevance.open();
        if (this.$refs.apiScenarioList) {
          this.$refs.apiScenarioList.clear();
          this.$refs.apiScenarioList.search();
        }
      },
      setProject(projectId) {
        this.projectId = projectId;
      },

      refresh(data) {
          this.$refs.apiScenarioList.search(data);
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

      async saveCaseRelevance() {
        const sign = await this.$refs.apiScenarioList.checkEnv();
        if (!sign) {
          return false;
        }
        let param = {};
        let url = '/api/automation/relevance';
        const envMap = this.$refs.apiScenarioList.projectEnvMap;
        let map = this.$refs.apiScenarioList.map;
        let envType = this.$refs.apiScenarioList.environmentType;
        let envGroupId = this.$refs.apiScenarioList.envGroupId;
        if (!map || map.size < 1) {
          this.$warning(this.$t("api_test.please_select_case"));
          return false;
        }
        if (envType === ENV_TYPE.JSON && (!envMap || envMap.size < 1)) {
          this.$warning(this.$t("api_test.environment.select_environment"));
          return false;
        } else if (envType === ENV_TYPE.GROUP && !envGroupId) {
          this.$warning(this.$t("api_test.environment.select_environment"));
          return false;
        }
        param.planId = this.planId;
        param.mapping = strMapToObj(map);
        param.envMap = strMapToObj(envMap);
        param.environmentType = envType;
        param.envGroupId = envGroupId;

        this.result = this.$post(url, param, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
          this.refresh();
          this.autoCheckStatus();
          this.$refs.baseRelevance.close();
        });
      },
      autoCheckStatus() { //  检查执行结果，自动更新计划状态
        if (!this.planId) {
          return;
        }
        this.$post('/test/plan/autoCheck/' + this.planId, (response) => {
        });
      },
    }
  }
</script>

<style scoped>

  /deep/ .select-menu {
    margin-bottom: 15px;
  }

  /deep/ .environment-select {
    float: right;
    margin-right: 10px;
  }

  /deep/ .module-input {
    width: 243px;
  }

</style>
