<template>
  <test-case-relevance-base
      @setProject="setProject"
      @save="saveCaseRelevance"
      @close="close"
      :plan-id="planId"
      :is-saving="isSaving"
      ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
          class="node-tree"
          @nodeSelectEvent="nodeChange"
          @refreshTable="refresh"
          @setModuleOptions="setModuleOptions"
          :relevance-project-id="projectId"
          :is-read-only="true"
          :show-case-num="false"
          ref="nodeTree"
      />
    </template>

    <relevance-ui-scenario-list
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :version-enable="versionEnable"
        :plan-id="planId"
        :project-id="projectId"
        @selectCountChange="setSelectCounts"
        ref="apiScenarioList"
    />
  </test-case-relevance-base>
</template>

<script>
import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
import {strMapToObj} from "metersphere-frontend/src/utils";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import RelevanceUiScenarioList from "@/business/plan/view/comonents/ui/RelevanceUiScenarioList";
import {testPlanAutoCheck} from "@/api/remote/plan/test-plan";
import UiScenarioModule from "@/business/plan/view/comonents/ui/UiScenarioModule";
import {uiAutomationRelevance} from "@/api/remote/ui/api-scenario";

export default {
  name: "TestCaseUiScenarioRelevance",
  components: {
    UiScenarioModule,
    RelevanceUiScenarioList,
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
      isSaving: false,
      projectId: "",
    };
  },
  props: {
    planId: {
      type: String,
    },
    versionEnable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    planId() {
      this.condition.planId = this.planId;
    },
  },
  methods: {
    close() {
      this.projectId = "";
    },
    open() {
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.search();
      }
      this.$refs.baseRelevance.open();
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.clear();
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.$refs.apiScenarioList.closeEnv();
      this.$refs.apiScenarioList.initProjectIds();
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

    postRelevance() {
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let envType = this.$refs.apiScenarioList.environmentType;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      if (envType === ENV_TYPE.JSON && (!envMap || envMap.size < 1)) {
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      } else if (envType === ENV_TYPE.GROUP && !envGroupId) {
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      }
      let param = {};
      param.planId = this.planId;
      param.mapping = map;
      param.envMap = strMapToObj(envMap);
      param.environmentType = envType;
      param.envGroupId = envGroupId;
      this.loading = true;
      uiAutomationRelevance(param).then(() => {
        this.loading = false;
        this.$success(this.$t("commons.save_success"));
        this.$emit("refresh");
        this.refresh();
        this.autoCheckStatus();
        this.$refs.baseRelevance.close();
      });
    },

    saveCaseRelevance() {
      this.isSaving = true;
      let selectIds = [];
      let selectRows = this.$refs.apiScenarioList.selectRows;
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      if (!this.$refs.apiScenarioList.selectAll) {
        selectRows.forEach((row) => {
          selectIds.push(row.id);
        });
      } else {
        selectIds = this.$refs.apiScenarioList.selectAllIds;
      }

      if (selectIds.length < 1) {
        this.isSaving = false;
        this.$warning(this.$t("test_track.plan_view.please_choose_test_case"));
        return;
      }

      let param = {};
      param.planId = this.planId;
      param.mapping = map;
      param.envMap = strMapToObj(envMap);
      param.envGroupId = envGroupId;
      param.selectIds = selectIds;
      this.loading = true;
      uiAutomationRelevance(param)
          .then(() => {
            this.loading = false;
            this.isSaving = false;
            this.$success(this.$t("plan.relevance_case_success"));
            this.$emit("refresh");
            this.refresh();
            this.autoCheckStatus();
          })
          .catch(() => {
            this.isSaving = false;
          });
    },
    autoCheckStatus() {
      //  检查执行结果，自动更新计划状态
      if (!this.planId) {
        return;
      }
      testPlanAutoCheck(this.planId);
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
  },
};
</script>

<style scoped>
:deep(.select-menu) {
  margin-bottom: 15px;
}

:deep(.environment-select) {
  float: right;
  margin-right: 10px;
}

:deep(.module-input) {
  width: 243px;
}

:deep(.table-select-icon) {
  position: absolute;
  display: inline-block;
  top: -13px !important;
  left: -30px;
  width: 30px;
}

.node-tree {
  margin-top: 15px;
  margin-bottom: 15px;
  max-height: calc(75vh - 120px);
  overflow-y: auto;
}
</style>
