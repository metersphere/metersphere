<template>
    <functional-relevance
      :page="page"
      :multiple-project="false"
      :get-table-data="getTestCases"
      :get-node-tree="getTreeNodes"
      :save="saveCaseRelevance"
      :version-enable="versionEnable"
      ref="functionalRelevance">
    </functional-relevance>
</template>

<script>

import {buildPagePath, getPageDate, getPageInfo} from "@/common/js/tableUtils";
import {TEST_PLAN_RELEVANCE_FUNC_CONFIGS} from "@/business/components/common/components/search/search-components";
import FunctionalRelevance from "@/business/components/track/plan/view/comonents/functional/FunctionalRelevance";
import {getTestCaseNodes} from "@/network/testCase";

export default {
  name: "RelationshipFunctionalRelevance",
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
    caseId: {
      type: String
    },
    relationshipType: String,
    versionEnable: Boolean
  },
  watch: {
    caseId() {
      this.page.condition.caseId = this.caseId;
    },
  },
  methods: {
    open() {
      if (this.$refs.functionalRelevance) {
        this.$refs.functionalRelevance.open();
      }
    },
    saveCaseRelevance(param, vueObj) {
      if (this.relationshipType === 'PRE') {
        param.targetIds = param.ids;
      } else {
        param.sourceIds = param.ids;
      }
      param.id = this.caseId;
      param.type = 'TEST_CASE';

      vueObj.result = this.$post('/relationship/edge/save/batch', param, () => {
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
      if (this.caseId) {
        condition.id = this.caseId;
      }
      this.page.result = this.$post(buildPagePath('/test/case/relationship/relate', this.page), condition, response => {
        getPageDate(response, this.page);
        let data = this.page.data;
        data.forEach(item => {
          item.checked = false;
          item.tags = JSON.parse(item.tags);
        });
      });
    },
    getTreeNodes(vueObj) {
      vueObj.$refs.nodeTree.result = getTestCaseNodes(vueObj.projectId, data => {
        vueObj.treeNodes = data;
        vueObj.selectNodeIds = [];
      });
    }
  }
}
</script>

<style scoped>
</style>
