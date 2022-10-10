<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-scenario-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <test-case-relate-scenario-list
      :select-node-ids="selectNodeIds"
      :project-id="projectId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      @selectCountChange="setSelectCounts"
      ref="apiCaseList"/>

  </test-case-relevance-base>

</template>

<script>

import TestCaseRelateApiList from "@/business/case/components/TestCaseRelateApiList";
import TestCaseRelevanceBase from "@/business/plan/view/comonents/base/TestCaseRelevanceBase";
import MsApiScenarioModule from "@/business/plan/view/comonents/api/module/ApiScenarioModule";
import TestCaseRelateScenarioList from "@/business/case/components/TestCaseRelateScenarioList";
import {saveCaseRelevanceScenario} from "@/api/testCase";

export default {
  name: "TestCaseScenarioRelate",
  components: {
    TestCaseRelateScenarioList,
    MsApiScenarioModule,
    TestCaseRelevanceBase,
    TestCaseRelateApiList,
  },
  data() {
    return {
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
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    saveCaseRelevance() {
      let ids = this.$refs.apiCaseList.getSelectIds();
      saveCaseRelevanceScenario(this.caseId, ids)
        .then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
          this.$refs.baseRelevance.close();
        });
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    }
  }
}
</script>

<style scoped>
</style>
