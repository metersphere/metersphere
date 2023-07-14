<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance"
    :is-saving="isSaving"
  >
    <template v-slot:aside>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :show-case-num="false"
        :relevance-project-id="projectId"
        :is-read-only="true"
        ref="nodeTree"
      />
    </template>

    <relevance-scenario-list
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
import RelevanceScenarioList from "./RelevanceScenarioList";
import { ENV_TYPE } from "metersphere-frontend/src/utils/constants";
import {
  getCurrentProjectID,
  hasLicense,
  strMapToObj,
} from "@/business/utils/sdk-utils";
import { getVersionFilters } from "@/business/utils/sdk-utils";
import { testPlanAutoCheck } from "@/api/remote/plan/test-plan";
import { scenarioRelevance } from "@/api/remote/plan/test-plan-scenario";
import MsApiScenarioModule from "@/business/plan/view/comonents/api/module/ApiScenarioModule";

export default {
  name: "TestCaseScenarioRelevance",
  components: {
    MsApiScenarioModule,
    RelevanceScenarioList,
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
      currentRow: {},
      projectId: "",
      versionFilters: [],
      isSaving: false,
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

      scenarioRelevance(param).then(() => {
        this.$success(this.$t("commons.save_success"));
        this.$emit("refresh");
        this.refresh();
        this.autoCheckStatus();
        this.$refs.baseRelevance.close();
      });
    },
    async saveCaseRelevance() {
      this.isSaving = true;
      const sign = await this.$refs.apiScenarioList.checkEnv();
      if (!sign) {
        this.isSaving = false;
        return false;
      }
      let selectIds = [];
      let selectRows = this.$refs.apiScenarioList.selectRows;
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let envType = this.$refs.apiScenarioList.environmentType;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      if (selectRows.size < 1) {
        this.isSaving = false;
        this.$warning(this.$t("test_track.plan_view.please_choose_test_case"));
        return;
      }
      selectRows.forEach((row) => {
        selectIds.push(row.id);
      });
      if (envType === ENV_TYPE.JSON && (!envMap || envMap.size < 1)) {
        this.isSaving = false;
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      } else if (envType === ENV_TYPE.GROUP && !envGroupId) {
        this.isSaving = false;
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      }
      let param = {};
      param.planId = this.planId;
      param.mapping = strMapToObj(map);
      param.envMap = strMapToObj(envMap);
      param.environmentType = envType;
      param.envGroupId = envGroupId;
      param.ids = selectIds;
      param.condition = this.$refs.apiScenarioList.condition;
      scenarioRelevance(param)
        .then(() => {
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
    getVersionOptions() {
      if (hasLicense()) {
        getVersionFilters(getCurrentProjectID()).then(
          (r) => (this.versionFilters = r.data)
        );
      }
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
</style>
