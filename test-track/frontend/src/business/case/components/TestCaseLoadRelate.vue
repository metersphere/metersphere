<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    ref="baseRelevance">

    <test-case-relate-load-list
      :project-id="projectId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      @selectCountChange="setSelectCounts"
      ref="apiCaseList"/>

  </test-case-relevance-base>

</template>

<script>

import TestCaseRelateApiList from "@/business/case/components/TestCaseRelateApiList";
import MsApiModule from "@/business/plan/view/comonents/api/module/ApiModule";
import TestCaseRelevanceBase from "@/business/plan/view/comonents/base/TestCaseRelevanceBase";
import MsApiScenarioModule from "@/business/plan/view/comonents/api/module/ApiScenarioModule";
import TestCaseRelateScenarioList from "@/business/case/components/TestCaseRelateScenarioList";
import TestCaseRelateLoadList from "@/business/case/components/TestCaseRelateLoadList";
import {saveCaseRelevanceLoad} from "@/api/testCase";

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
      saveCaseRelevanceLoad(this.caseId, ids)
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
