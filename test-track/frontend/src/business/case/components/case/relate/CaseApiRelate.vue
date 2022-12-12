<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ms-api-module
        :show-case-num="false"
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
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
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    saveCaseRelevance() {
      let ids = this.$refs.apiCaseList.getSelectIds();
      saveCaseRelevanceApi(this.caseId, ids).then(() => {
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
