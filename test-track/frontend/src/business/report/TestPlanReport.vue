<template>
  <ms-container>
    <ms-main-container>
      <test-plan-report-list
        ref="testPlanReportList"/>
    </ms-main-container>
  </ms-container>
</template>

<script>

import TestPlanReportList from './components/TestPlanReportList';
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {TEST_PLAN_REPORT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";

export default {
  name: "TestPlanReport",
  components: {MsMainContainer, MsContainer, TestPlanReportList},
  data() {
    return {}
  },
  activated() {
    this.refreshTestPlanList();
  },
  mounted() {
    this.refreshTestPlanList();
  },
  watch: {
    '$route'(to, from) {
    }
  },
  methods: {
    refreshTestPlanList() {
      this.$refs.testPlanReportList.condition = {components: TEST_PLAN_REPORT_CONFIGS};
      this.$refs.testPlanReportList.initTableData();
    }
  }
}
</script>

<style scoped>

</style>
