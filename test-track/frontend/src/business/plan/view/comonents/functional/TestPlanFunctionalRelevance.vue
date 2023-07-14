<template>
  <functional-relevance
    :page="page"
    :get-table-data="getTestCases"
    :get-node-tree="getTreeNodes"
    :is-test-plan="true"
    :save="saveCaseRelevance"
    :version-enable="versionEnable"
    ref="functionalRelevance">
  </functional-relevance>
</template>

<script>

import {getPageDate, getPageInfo} from "metersphere-frontend/src/utils/tableUtils";
import {TEST_PLAN_RELEVANCE_FUNC_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import FunctionalRelevance from "@/business/plan/view/comonents/functional/FunctionalRelevance";
import {testPlanRelevance} from "@/api/remote/plan/test-plan";
import {testCaseRelateList} from "@/api/testCase";
import {testCaseNodeListPlanRelate} from "@/api/test-case-node";
import {parseTag} from "@/business/utils/sdk-utils";

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
    },
    versionEnable: {
      type: Boolean,
      default: false
    },
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
      vueObj.page.loading = true;
      if (param.ids.length > 0) {
        param.planId = this.planId;
        testPlanRelevance(param)
          .then(() => {
            vueObj.page.loading = false;
            vueObj.isSaving = false;
            this.$success(this.$t("plan.relevance_case_success"));
            vueObj.setSelectCounts(0);
            this.$emit('refresh');
          }).catch((error) => {
          vueObj.isSaving = false;
        });
      } else {
        vueObj.page.loading = false;
        vueObj.isSaving = false;
        this.$warning(this.$t('test_track.plan_view.please_choose_test_case'));
      }
    },
    search() {
      this.getTestCases();
    },
    getTestCases() {
      let condition = this.page.condition;
      if (this.planId) {
        condition.planId = this.planId;
      }
      this.page.loading = true;
      testCaseRelateList({pageNum: this.page.currentPage, pageSize: this.page.pageSize}, condition)
        .then(response => {
          this.page.loading = false;
          getPageDate(response, this.page);
          let data = this.page.data;
          parseTag(this.page.data);
          data.forEach(item => {
            item.checked = false;
          });
        });
    },
    getTreeNodes(vueObj, condition) {
      vueObj.loading = true;
      testCaseNodeListPlanRelate({planId: this.planId, projectId: vueObj.projectId, ...condition})
        .then(response => {
          vueObj.loading = false;
          vueObj.treeNodes = response.data;
        })
      vueObj.selectNodeIds = [];
    }
  }
}
</script>

<style scoped>
</style>
