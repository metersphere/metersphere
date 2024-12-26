<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    :dialog-title="$t('api_test.home_page.failed_case_list.table_value.case_type.load')"
    ref="baseRelevance"
  >
    <test-case-relate-load-list
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
import MsApiModule from "@/business/plan/view/comonents/api/module/ApiModule";
import TestCaseRelevanceBase from "@/business/case/components/common/CaseRelevanceSideDialog";
import MsApiScenarioModule from "@/business/plan/view/comonents/api/module/ApiScenarioModule";
import TestCaseRelateScenarioList from "@/business/case/components/TestCaseRelateScenarioList";
import TestCaseRelateLoadList from "./CaseRelateLoadList";
import { saveCaseRelevanceLoad } from "@/api/testCase";

export default {
  name: "CaseLoadRelate",
  components: {
    TestCaseRelateLoadList,
    TestCaseRelateScenarioList,
    MsApiScenarioModule,
    TestCaseRelevanceBase,
    MsApiModule,
    TestCaseRelateApiList,
  },
  data() {
    return {
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
      if (this.$refs.baseRelevance) {
        this.$refs.baseRelevance.clear();
      }
    },
    open() {
      this.condition.notInIds = this.notInIds; // 查询排除哪些用例
      this.init();
      this.$refs.baseRelevance.open();
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
    saveCaseRelevance() {
      let ids = this.$refs.apiCaseList.getSelectIds();
      saveCaseRelevanceLoad(this.caseId, ids).then(() => {
        this.$success(this.$t("commons.relate_success"), false);
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
