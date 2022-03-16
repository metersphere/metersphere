<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance">

    <test-case-relate-load-list
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
import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
import TestCaseRelateScenarioList from "@/business/components/track/case/components/TestCaseRelateScenarioList";
import TestCaseRelateLoadList from "@/business/components/track/case/components/TestCaseRelateLoadList";

export default {
  name: "TestCaseLoadRelate",
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
      this.result = this.$post("/test/case/relate/test/performance/" + this.caseId, ids, (response) => {
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
