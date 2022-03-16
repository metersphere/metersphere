<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <test-case-relate-api-list
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :project-id="projectId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      ref="apiCaseList"/>

  </test-case-relevance-base>

</template>

<script>

import TestCaseRelateApiList from "@/business/components/track/case/components/TestCaseRelateApiList";
import MsApiModule from "@/business/components/api/definition/components/module/ApiModule";
import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";

export default {
  name: "TestCaseApiRelate",
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
      projectId: ""
    };
  },
  props: {
    caseId: {
      type: String
    },
    versionEnable: {
      type: Boolean,
      default: false
    },
    notInIds: {
      type: Array,
      default: null
    }
  },
  methods: {
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
      this.result = this.$post("/test/case/relate/test/testcase/" + this.caseId, ids, (response) => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
        this.$refs.baseRelevance.close();
      });
    },
  }
}
</script>

<style scoped>
</style>
