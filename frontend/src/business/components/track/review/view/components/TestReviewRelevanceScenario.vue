<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance">
    <template v-slot:aside>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :relevance-project-id="projectId"
        :is-read-only="true"
        ref="nodeTree"
      />
    </template>
    <review-relevance-scenario-list
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :review-id="reviewId"
      :version-enable="versionEnable"
      :project-id="projectId"
      ref="apiScenarioList"/>
  </test-case-relevance-base>
</template>
<script>
import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
import ReviewRelevanceScenarioList
  from "@/business/components/track/review/view/components/ReviewRelevanceScenarioList";
import {strMapToObj} from "@/common/js/utils";

export default {
  name: "TestReviewRelevanceScenario",
  components: {ReviewRelevanceScenarioList, MsApiScenarioModule, TestCaseRelevanceBase},
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
    reviewId: {
      type: String
    },
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    reviewId() {
      this.condition.reviewId = this.reviewId;
    },
  },
  methods: {
    open() {
      this.$refs.baseRelevance.open();
      if (this.$refs.apiScenarioList) {
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

    saveCaseRelevance() {
      const sign = this.$refs.apiScenarioList.checkEnv();
      if (!sign) {
        return false;
      }
      let param = {};
      let url = '/api/automation/relevance/review';
      let rows = this.$refs.apiScenarioList.selectRows;
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let map = new Map();
      rows.forEach(row => {
        map.set(row.id, row.projectIds);
      })
      param.reviewId = this.reviewId;
      param.mapping = strMapToObj(map);
      param.envMap = strMapToObj(envMap);

      this.result = this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
        this.refresh();
        this.$refs.baseRelevance.close();
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
