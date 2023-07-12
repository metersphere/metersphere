<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    :is-saving="isSaving"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
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
import { strMapToObj } from "metersphere-frontend/src/utils";
import { ENV_TYPE } from "metersphere-frontend/src/utils/constants";
import RelevanceUiScenarioList from "@/business/plan/view/comonents/ui/RelevanceUiScenarioList";
import { testPlanAutoCheck } from "@/api/remote/plan/test-plan";
import { testPlanUiScenarioRelevanceListIds } from "@/api/remote/ui/test-plan-ui-scenario-case";
import UiScenarioModule from "@/business/plan/view/comonents/ui/UiScenarioModule";
import { uiAutomationRelevance } from "@/api/remote/ui/api-scenario";

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
      param.mapping = strMapToObj(map);
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
    getAllId(param) {
      return new Promise((resolve) => {
        testPlanUiScenarioRelevanceListIds(param).then((r) => {
          resolve(r.data);
        });
      });
    },
    async saveCaseRelevance() {
      this.isSaving = true;
      let selectIds = [];
      let selectRows = this.$refs.apiScenarioList.selectRows;
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      selectRows.forEach((row) => {
        selectIds.push(row.id);
      });
      if (selectIds.length < 1) {
        this.isSaving = false;
        this.$warning(this.$t("test_track.plan_view.please_choose_test_case"));
        return;
      }

      if (!envMap || envMap.size == 0) {
        this.isSaving = false;
        this.$warning(this.$t("api_test.environment.select_environment"));
        return;
      }

      let param = {};
      param.planId = this.planId;
      param.mapping = strMapToObj(map);
      param.envMap = strMapToObj(envMap);
      param.envGroupId = envGroupId;
      param.selectIds = selectIds;

      //查找所有数据
      let params = this.$refs.apiScenarioList.condition;
      if (params.selectAll) {
        let result = await this.getAllId(params);
        param.selectIds = result;
      }
      this.loading = true;
      uiAutomationRelevance(param)
        .then(() => {
          this.loading = false;
          this.isSaving = false;
          this.$success(this.$t("commons.save_success"));
          this.$emit("refresh");
          this.refresh();
          this.autoCheckStatus();
          this.$refs.baseRelevance.close();
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
</style>
