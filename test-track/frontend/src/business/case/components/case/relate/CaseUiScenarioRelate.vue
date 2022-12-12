<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
        :show-case-num="false"
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
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
        this.$success(this.$t("commons.save_success"), false);
        this.$emit("refresh");
        this.$refs.baseRelevance.close();
      });
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
  },
};
</script>

<style scoped></style>
