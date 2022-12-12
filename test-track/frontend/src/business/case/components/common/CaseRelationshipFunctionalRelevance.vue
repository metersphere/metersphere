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

  import {getPageDate, getPageInfo} from "metersphere-frontend/src/utils/tableUtils";
  import {TEST_PLAN_RELEVANCE_FUNC_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
  import FunctionalRelevance from "./CaseFunctionalRelevance";
  import {addTestCaseRelationship, getTestCaseNodesByCaseFilter, testCaseRelationshipRelateList} from "@/api/testCase";

  export default {
    name: "CaseRelationshipFunctionalRelevance",
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
        param.condition = this.page.condition;
        vueObj.loading = true;
        addTestCaseRelationship(param)
          .then(() => {
            vueObj.loading = false;
            vueObj.isSaving = false;
            this.$success(this.$t('commons.save_success'), false);
            vueObj.$refs.baseRelevance.close();
            this.$emit('refresh');
          }).catch(() => {
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
        testCaseRelationshipRelateList({
            pageNum: this.page.currentPage,
            pageSize: this.page.pageSize
          },
          condition)
          .then(response => {
            getPageDate(response, this.page);
            let data = this.page.data;
            data.forEach(item => {
              item.checked = false;
              item.tags = JSON.parse(item.tags);
            });
          })
      },
      getTreeNodes(vueObj) {
        vueObj.nodeResult = getTestCaseNodesByCaseFilter(vueObj.projectId, {})
          .then(r => {
            vueObj.treeNodes = r.data;
            vueObj.selectNodeIds = [];
          });
      }
    }
  }
  </script>

  <style scoped>
  </style>
