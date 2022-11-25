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

export default {
  name: "TestPlanReport",
  components: {MsMainContainer, MsContainer, TestPlanReportList},
}
</script>

<style scoped>

</style>
