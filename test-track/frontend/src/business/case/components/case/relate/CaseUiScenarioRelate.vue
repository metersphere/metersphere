<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    @clearCondition="clearCondition"
    :dialog-title="$t('api_test.home_page.failed_case_list.table_value.case_type.ui')"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        :case-condition="condition"
        ref="nodeTree"
      />
    </template>

    <test-case-relate-ui-scenario-list
      :select-node-ids="selectNodeIds"
      :project-id="projectId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      :test-case-id="caseId"
      @selectCountChange="setSelectCounts"
      @setCondition="setCondition"
      ref="apiCaseList"
    />
  </test-case-relevance-base>
</template>

<script>
import TestCaseRelateApiList from "@/business/case/components/TestCaseRelateApiList";
import TestCaseRelevanceBase from "@/business/case/components/common/CaseRelevanceSideDialog";
import UiScenarioModule from "./UiScenarioModule";
import TestCaseRelateUiScenarioList from "./CaseRelateUiScenarioList";
import { saveUiCaseRelevanceScenario } from "@/api/testCase";

export default {
  name: "CaseUiScenarioRelate",
  components: {
    TestCaseRelateUiScenarioList,
    UiScenarioModule,
    TestCaseRelevanceBase,
    TestCaseRelateApiList,
  },
  data() {
    return {
      selectNodeIds: [],
      moduleOptions: {},
      condition: {},
      projectId: "",
    };
  },
  props: {
    caseId: {
      type: String,
    },
    versionEnable: {
      type: Boolean,
      default: false,
    },
    notInIds: {
      type: Array,
      default: null,
    },
  },
  methods: {
    clearSelect() {
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.clear();
      }
    },
    open() {
      this.condition.notInIds = this.notInIds; // 查询排除哪些用例
      this.init();
      this.$refs.baseRelevance.open();
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.clear();
      }
    },
    init() {
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.initTable();
      }
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.list();
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
    },

    refresh(data) {
      this.$refs.apiCaseList.initTable(data);
    },

    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    saveCaseRelevance() {
      let ids = this.$refs.apiCaseList.getSelectIds();
      saveUiCaseRelevanceScenario(this.caseId, ids).then(() => {
        this.$success(this.$t("commons.relate_success"), false);
        this.$emit("refresh");
        this.$refs.baseRelevance.close();
      });
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
    setCondition(data) {
      this.condition = data;
      this.$refs.nodeTree.list(this.projectId);
    },
    clearCondition() {
      this.selectNodeIds = [];
    },
  },
};
</script>

<style scoped></style>
