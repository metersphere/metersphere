<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    @clearCondition="clearCondition"
    :dialog-title="$t('api_test.home_page.failed_case_list.table_value.case_type.api')"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ms-api-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :case-condition="condition"
        ref="nodeTree"
      />
    </template>

    <test-case-relate-api-list
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :project-id="projectId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      @selectCountChange="setSelectCounts"
      @setCondition="setCondition"
      ref="apiCaseList"
    />
  </test-case-relevance-base>
</template>

<script>
import TestCaseRelateApiList from "./CaseRelateApiList";
import MsApiModule from "./ApiModule";
import TestCaseRelevanceBase from "@/business/case/components/common/CaseRelevanceSideDialog";
import { saveCaseRelevanceApi } from "@/api/testCase";

export default {
  name: "CaseApiRelate",
  components: {
    TestCaseRelevanceBase,
    MsApiModule,
    TestCaseRelateApiList,
  },
  data() {
    return {
      currentProtocol: null,
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
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    saveCaseRelevance() {
      let ids = this.$refs.apiCaseList.getSelectIds();
      saveCaseRelevanceApi(this.caseId, ids).then(() => {
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
