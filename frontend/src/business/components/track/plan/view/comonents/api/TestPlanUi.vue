<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>

      <ui-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refreshTable"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :plan-id="planId"
        :plan-status="planStatus"
        :show-case-num="false"
        ref="scenarioNodeTree">
      </ui-scenario-module>
    </template>

    <template v-slot:main>
      <!--测试用例列表-->
      <ms-test-plan-ui-scenario-list
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :version-enable="versionEnable"
        :plan-id="planId"
        :plan-status="planStatus"
        :clickType="clickType"
        @refresh="refreshTree"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="apiScenarioList"/>

    </template>

    <test-case-ui-scenario-relevance
      @refresh="refresh"
      :plan-id="planId"
      :model="model"
      :version-enable="versionEnable"
      ref="scenarioCaseRelevance"/>

  </ms-test-plan-common-component>

</template>

<script>
import NodeTree from "../../../../common/NodeTree";
import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
import TestPlanApiCaseList from "./TestPlanApiCaseList";
import TestCaseApiRelevance from "./TestCaseApiRelevance";
import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
import MsTestPlanUiScenarioList from "./TestPlanUiScenarioList";
import TestCaseUiScenarioRelevance
  from "@/business/components/track/plan/view/comonents/api/TestCaseUiScenarioRelevance";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const UiScenarioModule = requireComponent.keys().length > 0 ? requireComponent("./ui/automation/scenario/UiScenarioModule.vue") : {};

export default {
  name: "TestPlanUi",
  components: {
    TestCaseUiScenarioRelevance,
    MsTestPlanUiScenarioList,
    "UiScenarioModule": UiScenarioModule.default,
    MsApiModule,
    ApiCaseSimpleList,
    TestCaseApiRelevance,
    TestPlanApiCaseList,
    MsTestPlanCommonComponent,
    NodeTree,
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
      model: 'scenario'
    }
  },
  props: [
    'planId',
    'redirectCharType',
    'clickType',
    'versionEnable',
    'planStatus'
  ],
  mounted() {
    this.checkRedirectCharType();
  },
  watch: {
    model() {
      this.selectNodeIds = [];
      this.moduleOptions = {};
    },
    redirectCharType() {
      if (this.redirectCharType == 'scenario') {
        this.model = 'scenario';
      } else {
        this.model = 'api';
      }
    }
  },
  methods: {
    checkRedirectCharType() {
      if (this.redirectCharType == 'scenario') {
        this.model = 'scenario';
      } else {
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
