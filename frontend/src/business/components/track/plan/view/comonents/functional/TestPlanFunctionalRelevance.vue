<template>
    <functional-relevance
      :page="page"
      :get-table-data="getTestCases"
      :get-node-tree="getTreeNodes"
      :is-test-plan="true"
      :save="saveCaseRelevance"
      ref="functionalRelevance">
    </functional-relevance>
</template>

<script>

import {buildPagePath, getPageDate, getPageInfo} from "@/common/js/tableUtils";
import {TEST_PLAN_RELEVANCE_FUNC_CONFIGS} from "@/business/components/common/components/search/search-components";
import FunctionalRelevance from "@/business/components/track/plan/view/comonents/functional/FunctionalRelevance";

export default {
  name: "TestPlanFunctionalRelevance",
  components: {
    FunctionalRelevance,
  },
  data() {
    return {
      openType: 'relevance',
      result: {},
      page: getPageInfo({
        components: TEST_PLAN_RELEVANCE_FUNC_CONFIGS
      }),
    };
  },
  props: {
    planId: {
      type: String
    }
  },
  watch: {
    planId() {
      this.page.condition.planId = this.planId;
    },
  },
  methods: {
    open() {
      if (this.$refs.functionalRelevance) {
        this.$refs.functionalRelevance.open();
      }
    },
    saveCaseRelevance(param, vueObj) {
      param.planId = this.planId;
      vueObj.result = this.$post('/test/plan/relevance', param, () => {
        vueObj.isSaving = false;
        this.$success(this.$t('commons.save_success'));
        vueObj.$refs.baseRelevance.close();
        this.$emit('refresh');
      },(error) => {
        vueObj.isSaving = false;
      });
    },
    search() {
      this.getTestCases();
    },
    getTestCases() {
      let condition = this.page.condition;
      if (this.planId) {
        condition.planId = this.planId;
      }
      this.page.result = this.$post(buildPagePath('/test/case/relate', this.page), condition, response => {
        getPageDate(response, this.page);
        let data = this.page.data;
        data.forEach(item => {
          item.checked = false;
          item.tags = JSON.parse(item.tags);
        });
      });
    },
    getTreeNodes(vueObj) {
      vueObj.$refs.nodeTree.result = this.$post("/case/node/list/all/plan",
      {testPlanId: this.planId, projectId: vueObj.projectId}, response => {
        vueObj.treeNodes = response.data;
      });
      vueObj.selectNodeIds = [];
    }
  }
}
</script>

<style scoped>
</style>
