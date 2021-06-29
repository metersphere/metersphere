<template>
  <ms-test-plan-common-component>
    <template v-slot:aside>
      <ms-api-module
        v-if="model === 'api'"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refreshTable"
        @setModuleOptions="setModuleOptions"
        :review-id="reviewId"
        :is-read-only="true"
        :redirectCharType="redirectCharType"
        ref="apiNodeTree"
      >
      <template v-slot:header>
        <div class="model-change-radio">
          <el-radio v-model="model" label="api">接口用例</el-radio>
          <el-radio v-model="model" label="scenario">场景用例</el-radio>
        </div>
      </template>
      </ms-api-module>

      <ms-api-scenario-module
        v-if="model === 'scenario'"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refreshTable"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :review-id="reviewId"
        ref="scenarioNodeTree">
        <template v-slot:header>
          <div class="model-change-radio">
            <el-radio v-model="model" label="api">接口用例</el-radio>
            <el-radio v-model="model" label="scenario">场景用例</el-radio>
          </div>
        </template>
      </ms-api-scenario-module>
    </template>
    <template v-slot:main>
     <test-plan-api-case-list
       v-if="model === 'api'"
       :current-protocol="currentProtocol"
       :currentRow="currentRow"
       :select-node-ids="selectNodeIds"
       :trash-enable="trashEnable"
       :is-case-relevance="true"
       :model="'plan'"
       :review-id="reviewId"
       :clickType="clickType"
       @refresh="refreshTree"
       @relevanceCase="openTestCaseRelevanceDialog"
       ref="apiCaseList"/>

      <ms-test-plan-api-scenario-list
        v-if="model === 'scenario'"
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :review-id="reviewId"
        :clickType="clickType"
        @refresh="refreshTree"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="apiScenarioList"/>
    </template>
    <test-review-relevance-api
      @refresh="refresh"
      :review-id="reviewId"
      :model="model"
      ref="apiCaseRelevance"
    />
    <test-review-relevance-scenario
      @refresh="refresh"
      :review-id="reviewId"
      :model="model"
      ref="scenarioCaseRelevance"
    />

  </ms-test-plan-common-component>
</template>

<script>
import MsTestPlanCommonComponent from "@/business/components/track/plan/view/comonents/base/TestPlanCommonComponent";
import MsTestPlanApiScenarioList from "@/business/components/track/plan/view/comonents/api/TestPlanApiScenarioList";
import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
import ApiCaseSimpleList from "@/business/components/api/definition/components/list/ApiCaseSimpleList";
import TestPlanApiCaseList from "@/business/components/track/plan/view/comonents/api/TestPlanApiCaseList";
import TestCaseRelevance from "@/business/components/track/plan/view/comonents/functional/TestCaseFunctionalRelevance";
import NodeTree from "@/business/components/track/common/NodeTree";
import MsApiModule from "../../../../api/definition/components/module/ApiModule"
import TestReviewRelevanceApi from "@/business/components/track/review/view/components/TestReviewRelevanceApi";
import TestReviewRelevanceScenario
  from "@/business/components/track/review/view/components/TestReviewRelevanceScenario";

export default {
  name: "TestReviewApi",
  components: {
    TestReviewRelevanceScenario,
    TestReviewRelevanceApi,
    MsTestPlanApiScenarioList,
    MsApiScenarioModule,
    ApiCaseSimpleList,
    TestPlanApiCaseList,
    MsTestPlanCommonComponent,
    TestCaseRelevance,
    NodeTree,
    MsApiModule,
  },
  data() {
    return {
      result: {},
      treeNodes: [],
      currentRow: "",
      trashEnable: false,
      currentProtocol: null,
      currentModule: null,
      selectNodeIds: [],
      moduleOptions: {},
      model: 'api'
    }
  },
  props: [
    'reviewId',
    'redirectCharType',
    'clickType'
  ],
  mounted() {
    this.checkRedirectCharType();
  },
  watch: {
    model() {
      this.selectNodeIds = [];
      this.moduleOptions = {};
    },
    redirectCharType(){
      if(this.redirectCharType=='scenario'){
        this.model = 'scenario';
      }else{
        this.model = 'api';
      }
    }
  },
  methods: {
    checkRedirectCharType(){
      if(this.redirectCharType=='scenario'){
        this.model = 'scenario';
      }else{
        this.model = 'api';
      }
    },
    refresh() {
      this.refreshTree();
      this.refreshTable();
    },
    refreshTable() {
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.initTable();
      }
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.search();
      }
    },
    refreshTree() {
      if (this.$refs.apiNodeTree) {
        this.$refs.apiNodeTree.list();
      }
      if (this.$refs.scenarioNodeTree) {
        this.$refs.scenarioNodeTree.list();
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

    openTestCaseRelevanceDialog(model) {
      if (model === 'scenario') {
        this.$refs.scenarioCaseRelevance.open();
      } else {
        this.$refs.apiCaseRelevance.open();
      }
    },
  }
}
</script>

<style scoped>
.model-change-radio {
  height: 25px;
  line-height: 25px;
  margin: 5px 10px;
}

</style>
